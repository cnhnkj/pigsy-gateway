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

import com.cnhnkj.pigsy.core.config.CommonConfig;
import java.util.Optional;
import javax.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Configuration
@EnableConfigurationProperties(CommonConfig.class)
public class CorsFilter {

  @Resource
  private CommonConfig commonConfig;

  @Bean
  public WebFilter corsFilter() {
    return (ServerWebExchange ctx, WebFilterChain chain) -> {
      ServerHttpRequest request = ctx.getRequest();
      if (!CorsUtils.isCorsRequest(request)) {
        return chain.filter(ctx);
      }
      HttpHeaders requestHeaders = request.getHeaders();
      ServerHttpResponse response = ctx.getResponse();
      HttpHeaders headers = response.getHeaders();
      headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Optional.ofNullable(requestHeaders.getOrigin()).orElse("*"));
      headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders.getAccessControlRequestHeaders());
      headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, HEAD");
      headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
      headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
      headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(commonConfig.getCorsMaxAge()));

      if (request.getMethod() == HttpMethod.OPTIONS) {
        response.setStatusCode(HttpStatus.OK);
        return Mono.empty();
      }
      return chain.filter(ctx);
    };
  }
}

