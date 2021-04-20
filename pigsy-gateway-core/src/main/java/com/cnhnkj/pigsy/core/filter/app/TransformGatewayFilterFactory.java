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
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Component
@Slf4j
public class TransformGatewayFilterFactory extends AbstractGatewayFilterFactory<TransformGatewayFilterFactory.Config> {

  private static final String TRANSFORM_PATTERN = "/{serviceId}/api/transform/**";

  private final PathPatternParser pathPatternParser = new PathPatternParser();

  private static final boolean MUST_LOGIN = false;

  @Resource
  private AuthService authService;

  public TransformGatewayFilterFactory() {
    super(Config.class);
    log.info("Loaded TransformGatewayFilterFactory");
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

      PathPattern pathPattern = this.pathPatternParser.parse(TRANSFORM_PATTERN);

      if (!pathPattern.matches(path)) {
        return chain.filter(exchange);
      }

      String token = exchange.getRequest().getHeaders().getFirst(HeaderConstants.X_CLIENT_TOKEN);
      ServerHttpResponse response = exchange.getResponse();

      if (StringUtils.isEmpty(token)) {
        return chain.filter(exchange);
      }

      return authService.appAuth(token, response, exchange, chain, MUST_LOGIN);
    };
  }


  @Data
  public static class Config {

    // 控制是否开启认证
    private Boolean enabled;
  }
}
