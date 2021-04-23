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

package com.cnhnkj.pigsy.core.filter.backend;

import static org.springframework.http.server.PathContainer.parsePath;

import com.cnhnkj.pigsy.core.config.SecretConfig;
import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.filter.app.AuthService;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
@Component
@EnableConfigurationProperties(SecretConfig.class)
public class BackendGatewayFilterFactory extends AbstractGatewayFilterFactory<BackendGatewayFilterFactory.Config> {

  private static final String AUTH_PATTERN = "/{serviceId}/backend/**";

  private final PathPatternParser pathPatternParser = new PathPatternParser();

  @Resource
  private ObjectMapper objectMapper;

  @Resource
  private AuthService authService;

  @Value("${spring.profiles.active}")
  private String profile;

  public BackendGatewayFilterFactory() {
    super(Config.class);
    log.info("Loaded BackendGatewayFilterFactory");
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Collections.singletonList("enabled");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {

      ServerHttpResponse response = exchange.getResponse();
      ServerHttpRequest request = exchange.getRequest();

      exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.add(HeaderConstants.X_PIGSY_PROFILE, profile));
      PathContainer path = parsePath(exchange.getRequest().getURI().getRawPath());

      PathPattern pathPattern = this.pathPatternParser.parse(AUTH_PATTERN);

      if (!pathPattern.matches(path)) {
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.SERVICE_NOT_FOUND);
      }
      String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (StringUtils.isNotBlank(authorization)&& authorization.split(" ").length > 1) {
        String jwt = authorization.split(" ")[1];
        return authService.backendAuth(jwt, response, exchange, chain);
      } else {
        response.setStatusCode(HttpStatus.OK);
        return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.JWT_HEADER_IS_NULL);
      }
    };
  }

  @Data
  public static class Config {

    // 控制是否开启认证
    private Boolean enabled;
  }

}
