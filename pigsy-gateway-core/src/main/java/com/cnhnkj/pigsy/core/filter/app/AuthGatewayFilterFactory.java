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

import static org.springframework.http.server.PathContainer.parsePath;

import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Component
@Slf4j
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

  private static final String AUTH_PATTERN = "/{serviceId}/api/auth/**";

  private final PathPatternParser pathPatternParser = new PathPatternParser();

  private static final boolean MUST_LOGIN = true;

  @Resource
  private ObjectMapper objectMapper;

  @Resource
  private AuthService authService;


  public AuthGatewayFilterFactory() {
    super(Config.class);
    log.info("Loaded AuthGatewayFilterFactory");
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Collections.singletonList("enabled");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      if (!config.getEnabled()) {
        return chain.filter(exchange);
      }

      PathContainer path = parsePath(exchange.getRequest().getURI().getRawPath());

      PathPattern pathPattern = this.pathPatternParser.parse(AUTH_PATTERN);

      if (!pathPattern.matches(path)) {
        return chain.filter(exchange);
      }

      String ticket = exchange.getRequest().getHeaders().getFirst(HeaderConstants.X_CLIENT_TOKEN);
      ServerHttpResponse response = exchange.getResponse();

      return appAuth(ticket, response, exchange, chain);
    };
  }

  private Mono<Void> appAuth(String ticket, ServerHttpResponse response, ServerWebExchange exchange,
      GatewayFilterChain chain) {
    if (StringUtils.isEmpty(ticket)) {
      response.setStatusCode(HttpStatus.OK);
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.USER_NOT_LOGIN);
    } else {
      return authService.appAuth(ticket, response, exchange, chain, MUST_LOGIN);
    }
  }

  @Data
  public static class Config {

    // 控制是否开启认证
    private Boolean enabled;
  }
}
