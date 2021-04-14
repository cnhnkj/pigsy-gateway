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

package com.cnhnkj.pigsy.core.filter.global;


import static com.google.common.base.Strings.emptyToNull;

import com.cnhnkj.pigsy.core.config.CommonConfig;
import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.utils.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
@Component
@EnableConfigurationProperties({CommonConfig.class})
public class AccessLogFilter implements GlobalFilter, Ordered {

  @Resource
  private CommonConfig commonConfig;

  @Resource
  private ObjectMapper objectMapper;

  @Value("${spring.profiles.active}")
  private String profile;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    long requestTime = System.currentTimeMillis();
    String rawPath = exchange.getRequest().getURI().getRawPath();

    return chain.filter(exchange)
        .then(Mono.fromRunnable(realAccessLog(exchange, rawPath, requestTime)));
  }

  private Runnable realAccessLog(ServerWebExchange exchange, String rawPath, long requestTime) {
    return () -> {
      ServerHttpResponse httpResponse = exchange.getResponse();
      HttpStatus statusCode = httpResponse.getStatusCode();
      Map<String, String> accessLog = new HashMap<>();
      String path = rawPath == null ? "root" : rawPath;
      accessLog.put(Constants.Request.REAL_IP, IpUtil.getRealIp(exchange));
      accessLog.put(Constants.Request.PATH, path);
      accessLog.put(Constants.Response.HTTP_STATUS, String.valueOf(statusCode));
      accessLog.put(Constants.PROFILE, profile);

      long requestDuration = System.currentTimeMillis() - requestTime;
      accessLog.put(Constants.Request.DURATION, String.valueOf(requestDuration));
      //配合框架进行了修改，为了方便获取code
      String code = exchange.getResponse().getHeaders().getFirst(Constants.Response.CODE);
      if (Strings.isNullOrEmpty(code)) {
        code = String.valueOf(Optional.ofNullable(exchange.getResponse().getRawStatusCode()).orElse(-1));
        if (code.equals(String.valueOf(HttpStatus.OK.value()))) {
          code = Constants.Response.OK_CODE;
        }
      }
      accessLog.put(Constants.Response.CODE, code);

      //正常的和重定向的
      if ((!code.equals(Constants.Response.OK_CODE) && !code.equals(String.valueOf(HttpStatus.FOUND.value())))
          || requestDuration >= commonConfig.getSlowLogTime()) {
        String headers = exchange.getRequest().getHeaders().toString();
        String queryParams = Joiner.on(",").withKeyValueSeparator("=")
            .join(exchange.getRequest().getQueryParams().toSingleValueMap());
        String queryCookies = Joiner.on(",").withKeyValueSeparator("=")
            .join(exchange.getRequest().getCookies().toSingleValueMap());
        String httpMethod = exchange.getRequest().getMethodValue();

        accessLog.put(Constants.Request.HEADER, headers);
        accessLog.put(Constants.Request.PARAMS, emptyToNull(queryParams));
        accessLog.put(Constants.Request.COOKIES, emptyToNull(queryCookies));
        accessLog.put(Constants.Request.HTTP_METHOD, httpMethod);
      }

      try {
        log.info("{}", objectMapper.writeValueAsString(accessLog));
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    };
  }

  @Override
  public int getOrder() {
    return Constants.GlobalOrder.ACCESS_LOG_ORDER;
  }
}
