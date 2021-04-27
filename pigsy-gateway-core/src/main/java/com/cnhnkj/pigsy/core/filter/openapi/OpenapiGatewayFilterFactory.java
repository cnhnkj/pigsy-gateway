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

package com.cnhnkj.pigsy.core.filter.openapi;

import static org.springframework.http.server.PathContainer.parsePath;

import com.cnhnkj.pigsy.core.config.OpenapiConfig;
import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.utils.IpUtil;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Component
@Slf4j
@EnableConfigurationProperties(OpenapiConfig.class)
public class OpenapiGatewayFilterFactory extends
    AbstractGatewayFilterFactory<OpenapiGatewayFilterFactory.Config> {

  private static final String OPENAPI_PATTERN = "/{serviceId}/openapi/**";

  private final PathPatternParser pathPatternParser = new PathPatternParser();

  @Resource
  private ObjectMapper objectMapper;

  @Resource
  private OpenapiConfig openapiConfig;

  @Value("${spring.profiles.active}")
  private String profile;

  public OpenapiGatewayFilterFactory() {
    super(Config.class);
    log.info("Loaded OpenapiGatewayFilterFactory");
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Collections.singletonList("enabled");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {

      exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.add(HeaderConstants.X_PIGSY_PROFILE, profile));

      ServerHttpResponse response = exchange.getResponse();
      PathContainer path = parsePath(exchange.getRequest().getURI().getRawPath());

      PathPattern pathPattern = this.pathPatternParser.parse(OPENAPI_PATTERN);

      if (!pathPattern.matches(path)) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.SERVICE_NOT_FOUND);
      }

      String realPath = path.value();
      Map<String, Set<String>> ipWhiteSet = openapiConfig.getIpWhiteSet();
      if (CollectionUtils.isEmpty(ipWhiteSet)) {
        return chain.filter(exchange);
      }

      List<String> containPatternPath = ipWhiteSet.keySet()
          .stream().filter(k -> k.contains("/*")).collect(Collectors.toList());

      if (ipWhiteSet.containsKey(realPath)) {
        return handlerInAllowIp(realPath, exchange, chain, response);
      } else if (!CollectionUtils.isEmpty(containPatternPath)) {
        Optional<String> matchPath = containPatternPath.stream()
            .filter(p -> this.pathPatternParser.parse(p).matches(path)).findFirst();
        if (matchPath.isPresent()) {
          return handlerInAllowIp(matchPath.get(), exchange, chain, response);
        }
      }
      return chain.filter(exchange);
    };
  }

  private Mono<Void> handlerInAllowIp(String realPath, ServerWebExchange exchange,
      GatewayFilterChain chain, ServerHttpResponse response) {
    Set<String> allowIpSet = openapiConfig.getIpWhiteSet().get(realPath);
    //任何环境如果配置了0.0.0.0表示都可以访问（为了兼容一些公司有大量的不固定的ip）
    if (!CollectionUtils.isEmpty(allowIpSet) && allowIpSet.contains("0.0.0.0")) {
      return chain.filter(exchange);
    }

    String realIp = IpUtil.getRealIp(exchange);
    if (realIp == null) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.REAL_IP_INVALID);
    }

    if (!IpUtil.isInIpList(realIp, allowIpSet)) {
      log.error("realIp {} is not in allowIpSet {}", realIp, String.join(",", allowIpSet));
      response.setStatusCode(HttpStatus.FORBIDDEN);
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.IP_IS_FORBID);
    }
    return chain.filter(exchange);
  }

  @Data
  public static class Config {

    // 控制是否开启认证
    private Boolean enabled;
  }
}
