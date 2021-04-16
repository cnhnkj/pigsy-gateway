/*
 *   Copyright 2014-2021 Hunan Huinong Technology Co.,Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.cnhnkj.pigsy.core.filter.app;

import com.cnhnkj.pigsy.core.config.CommonConfig;
import com.cnhnkj.pigsy.core.config.SecretConfig;
import com.cnhnkj.pigsy.core.config.SecretConfig.MasterSlaveConfig;
import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.errors.PigsyGatewayException;
import com.cnhnkj.pigsy.core.utils.IpUtil;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.cnhnkj.pigsy.core.utils.SecretUtil;
import com.cnhnkj.pigsy.core.utils.VersionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
@Component
@EnableConfigurationProperties({SecretConfig.class, CommonConfig.class})
public class ParamCheckGatewayFilterFactory extends AbstractGatewayFilterFactory<ParamCheckGatewayFilterFactory.Config> {

  @Resource
  private ObjectMapper objectMapper;

  @Resource
  private RedisTemplate<String, Object> redisTemplate;

  @Resource
  private SecretConfig secretConfig;

  @Resource
  private CommonConfig commonConfig;

  @Value("${spring.profiles.active}")
  private String profile;

  public ParamCheckGatewayFilterFactory() {
    super(Config.class);
    log.info("Loaded ParamCheckGatewayFilterFactory");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {

      if (!config.getEnabled()) {
        return chain.filter(exchange);
      }

      if (profile.contains("local")) {
        //如果是本地启动，那么不做任何检测，用于本地调试
        getUaField(exchange, HeaderConstants.X_REAL_IP, IpUtil.getRealIp(exchange));
        return modifyHeaderAndFilter(exchange, chain);
      }
      ServerHttpResponse response = exchange.getResponse();
      ServerHttpRequest serverHttpRequest = exchange.getRequest();
      HttpHeaders headers = serverHttpRequest.getHeaders();

      //必须需要的参数检查
      try {
        String appId = getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_APPID);

        //app应用或者app应用内的h5页面
        if (Constants.IN_APP_ID_LIST.contains(appId)) {
          String clientUa = getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_UA);
          parseClientUa(exchange, clientUa);
          //非app内的m站、pc站、各种小程序
        } else if (Constants.OUT_APP_ID_LIST.contains(appId)) {
          String clientUa = getMaybeHeader(exchange, headers, HeaderConstants.X_CLIENT_UA);
          if (!StringUtils.isEmpty(clientUa)) {
            parseClientUa(exchange, clientUa);
          }
        } else {
          log.warn("请求参数的appId错误，appId为" + appId);
          throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
        }
        getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_ID);
        getUaField(exchange, HeaderConstants.X_REAL_IP, IpUtil.getRealIp(exchange));

        //通过时间来防止重放攻击
        String clientTime = getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_TIME);
        String clientNonce = getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_NONCE);
        checkClientTimeNormal(clientTime, clientNonce);

        getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_PAGE);
        getMustHeader(exchange, headers, HeaderConstants.CONTENT_TYPE);
        getMustHeader(exchange, headers, HeaderConstants.X_FORWARDED_FOR);

        String clientSign = getMustHeader(exchange, headers, HeaderConstants.X_CLIENT_SIGN);
        if (clientSign.length() != 96) {
          log.warn("签名长度错误,长度为:" + clientSign.length());
          throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
        }
        checkClientSign(exchange, appId, clientSign);

      } catch (PigsyGatewayException e) {
        return ResponseUtil
            .sendErrorMsg(objectMapper, response, ErrorEnum.PIGSY_EXCEPTION_ERROR.setCode(e.getCode()).setMsg(e.getMessage()));
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
        return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.PIGSY_INNER_ERROR);
      }

      //可能需要的参数检查
      getMaybeHeader(exchange, headers, HeaderConstants.X_CLIENT_TOKEN);
      getMaybeHeader(exchange, headers, HeaderConstants.X_CLIENT_LOCATION);
      getMaybeHeader(exchange, headers, HeaderConstants.X_MINI_PROGRAM_ACCESS_TOKEN);
      getMaybeHeader(exchange, headers, HeaderConstants.X_MINI_PROGRAM_ID);

      return modifyHeaderAndFilter(exchange, chain);
    };
  }

  private Mono<Void> modifyHeaderAndFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest().mutate()
        .headers(httpHeaders -> {
          httpHeaders.remove(HeaderConstants.X_CLIENT_NONCE);
          httpHeaders.remove(HeaderConstants.X_CLIENT_TIME);
          httpHeaders.remove(HeaderConstants.X_CLIENT_TOKEN);
          httpHeaders.remove(HeaderConstants.X_CLIENT_SIGN);
          httpHeaders.remove(HeaderConstants.X_CLIENT_UA);
          httpHeaders.remove(HeaderConstants.X_REAL_IP);

          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_REAL_IP);
          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_CLIENT_OS_TYPE);
          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_CLIENT_SYSTEM_VERSION);
          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_CLIENT_CHANNEL);
          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_CLIENT_APP_VERSION);
          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_CLIENT_MODEL);
          addHttpHeaders(httpHeaders, exchange, HeaderConstants.X_CLIENT_NET);

        }).build();

    return chain.filter(exchange.mutate().request(request).build());
  }

  private void addHttpHeaders(HttpHeaders httpHeaders, ServerWebExchange exchange, String name) {
    Object attribute = exchange.getAttributes().get(name);
    if (attribute != null) {
      httpHeaders.add(name, String.valueOf(attribute));
    }
  }


  //通过校验签名验证请求的合法性
  private void checkClientSign(ServerWebExchange serverWebExchange, String appId,
      String clientSign) {
    // 客户端签名验证
    String clientTime = serverWebExchange.getAttribute(HeaderConstants.X_CLIENT_TIME);
    String clientNonce = serverWebExchange.getAttribute(HeaderConstants.X_CLIENT_NONCE);
    String clientId = serverWebExchange.getAttribute(HeaderConstants.X_CLIENT_ID);

    switch (appId) {
      case Constants.AppId.ANDROID_APP_ID:
      case Constants.AppId.IOS_APP_ID:
      case Constants.AppId.WEB_IN_APP_ID: {
        if (!verifySign(clientSign, clientNonce, clientTime, clientId, secretConfig.getApp(), Constants.Secret.APP_TYPE)) {
          log.warn("app端签名错误, 设备号是{}, 请求路径是{}", clientId,
              serverWebExchange.getRequest().getURI().getRawPath());
          throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
        }
        break;
      }
      case Constants.AppId.WECHAT_MINI_PROGRAM_ID:
      case Constants.AppId.ALIPAY_MINI_PROGRAM_APP_ID:
      case Constants.AppId.BAIDU_MINI_PROGRAM_ID: {
        if (!verifySign(clientSign, clientNonce, clientTime, clientId, secretConfig.getMiniProgram(),
            Constants.Secret.MINI_PROGRAM_TYPE)) {
          log.warn("小程序端签名错误, 设备号是{}, 请求路径是{}", clientId,
              serverWebExchange.getRequest().getURI().getRawPath());
          throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
        }
        break;
      }
      case Constants.AppId.PC_ID: {
        if (!verifySign(clientSign, clientNonce, clientTime, clientId, secretConfig.getPc(), Constants.Secret.PC_TYPE)) {
          log.warn("pc端签名错误, 设备号是{}, 请求路径是{}", clientId,
              serverWebExchange.getRequest().getURI().getRawPath());
          throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
        }
        break;
      }
      case Constants.AppId.MOBILE_H5_ID: {
        if (!verifySign(clientSign, clientNonce, clientTime, clientId, secretConfig.getMobileH5(),
            Constants.Secret.MOBILE_H5_TYPE)) {
          log.warn("移动端h5签名错误, 设备号是{}, 请求路径是{}", clientId,
              serverWebExchange.getRequest().getURI().getRawPath());
          throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
        }
        break;
      }
    }
  }


  private boolean verifySign(String clientSign, String clientNonce, String clientTime, String clientId,
      MasterSlaveConfig masterSlaveConfig, int secretType) {

    String masterSecret = masterSlaveConfig.getMaster();
    String masterSign = null;

    if (!StringUtils.isEmpty(masterSecret)) {
      masterSign = SecretUtil.generateSecret(clientNonce, clientTime, clientId, masterSecret, secretType);
    }

    String slaveSecret = masterSlaveConfig.getSlave();
    String slaveSign = null;

    if (!StringUtils.isEmpty(slaveSecret)) {
      slaveSign = SecretUtil.generateSecret(clientNonce, clientTime, clientId, masterSign, secretType);
    }

    return (masterSign != null && masterSign.equals(clientSign)) || (slaveSign != null && slaveSign.equals(clientSign));
  }


  private void checkClientTimeNormal(String clientTime, String clientNonce) {
    Integer requestAvailDuration = commonConfig.getRequestAvailDuration();
    Instant instant = Instant.ofEpochMilli(Long.parseLong(clientTime));
    ZoneId zone = ZoneId.systemDefault();
    LocalDateTime clientDateTime = LocalDateTime.ofInstant(instant, zone);
    LocalDateTime now = LocalDateTime.now();
    if (Duration.between(clientDateTime, now).abs().getSeconds() > requestAvailDuration) {
      log.warn("请求时间错误, 请求时间是{}, 当前时间是{}", clientDateTime, now);
      throw new PigsyGatewayException(ErrorEnum.CLIENT_LOCAL_TIME_ERROR.getCode(), ErrorEnum.CLIENT_LOCAL_TIME_ERROR.getMsg());
    }

    Boolean existNonce = Optional.ofNullable(redisTemplate.opsForValue()
        .setIfAbsent(clientNonce, "1", requestAvailDuration * 2 + 5, TimeUnit.SECONDS))
        .orElse(false);
    if (!existNonce) {
      log.warn("随机数{}已经存在", clientNonce);
      throw new PigsyGatewayException("客户端请求错误");
    }
  }


  private String getMaybeHeader(ServerWebExchange exchange, HttpHeaders headers, String key) {
    String value = headers.getFirst(key);
    if (!StringUtils.isEmpty(value)) {
      exchange.getAttributes().put(key, value);
    }
    return value;
  }

  private String getMustHeader(ServerWebExchange exchange, HttpHeaders headers, String key) {
    String value = headers.getFirst(key);
    if (StringUtils.isEmpty(value)) {
      log.warn("getMustHeader方法:" + key + "参数未传");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }

    exchange.getAttributes().put(key, value);
    return value;
  }

  private void getUaField(ServerWebExchange exchange, String key, String value) {
    if (!StringUtils.isEmpty(value)) {
      exchange.getAttributes().put(key, value);
    } else {
      log.warn("getUaField方法:" + key + "参数未传");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }

  private void parseClientUa(ServerWebExchange exchange, String clientUa) {
    if (!StringUtils.isEmpty(clientUa)) {
      String[] fields = clientUa.split("[|]");
      if (fields.length != 6 && fields.length != 5) {
        log.warn("clientUa长度错误,长度为" + fields.length);
        throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
      }
      getUaField(exchange, HeaderConstants.X_CLIENT_OS_TYPE, fields[0]);
      getUaField(exchange, HeaderConstants.X_CLIENT_SYSTEM_VERSION, fields[1]);
      getUaField(exchange, HeaderConstants.X_CLIENT_CHANNEL, fields[2]);
      String appVersion = fields[3];
      getUaField(exchange, HeaderConstants.X_CLIENT_APP_VERSION, appVersion);
      String minimumSupportedVersion = commonConfig.getMinimumSupportedVersion();

      if (Strings.isNotEmpty(minimumSupportedVersion) && VersionUtil.compareVersion(appVersion, minimumSupportedVersion) < 0) {
        log.warn("当前app版本过低,当前版本为" + appVersion);
        throw new PigsyGatewayException(ErrorEnum.CLIENT_VERSION_NOT_SUPPORT.getCode(),
            ErrorEnum.CLIENT_VERSION_NOT_SUPPORT.getMsg());
      }

      getUaField(exchange, HeaderConstants.X_CLIENT_MODEL, fields[4]);
      if (fields.length == 6) {
        getUaField(exchange, HeaderConstants.X_CLIENT_NET, fields[5]);
      }
    } else {
      log.warn("请求头clientUa参数未传");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }


  @Override
  public List<String> shortcutFieldOrder() {
    return Collections.singletonList("enabled");
  }


  @Data
  public static class Config {

    // 控制是否开启认证
    private Boolean enabled;
  }

}
