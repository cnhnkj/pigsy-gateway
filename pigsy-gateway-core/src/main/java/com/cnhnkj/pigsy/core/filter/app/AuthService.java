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

import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.invoke.AuthServiceInvoker;
import com.cnhnkj.pigsy.core.invoke.AuthServiceInvoker.JwtInfo;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
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
public class AuthService {

  @Resource
  private AuthServiceInvoker authServiceInvoker;

  @Resource
  private ObjectMapper objectMapper;

  public Mono<Void> appAuth(String token, ServerHttpResponse response, ServerWebExchange exchange, GatewayFilterChain chain,
      boolean mustLogin) {
    return authServiceInvoker.getUserInfoByToken(token)
        .flatMap(userInfoBaseResult -> {
          if (userInfoBaseResult.getCode() != 0 || StringUtils
              .isEmpty(userInfoBaseResult.getData()) || StringUtils.isEmpty(userInfoBaseResult.getData().getUserId())) {
            if (mustLogin) {
              log.warn(String.format("token鉴权失败，token是%s, 错误信息是%s", token, userInfoBaseResult.getMsg()));
              return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.USER_NOT_LOGIN);
            } else {
              return chain.filter(exchange);
            }
          } else {
            String userId = userInfoBaseResult.getData().getUserId().toString();
            ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate()
                .header(HeaderConstants.X_USER_ID, userId).build();
            exchange.getAttributes().put(HeaderConstants.X_USER_ID, userId);
            exchange.mutate().request(serverHttpRequest).build();
            return chain.filter(exchange);
          }
        });
  }

  public Mono<Void> backendAuth(String jwt, ServerHttpResponse response, ServerWebExchange exchange, GatewayFilterChain chain) {
    return authServiceInvoker.getUserInfoByJwt(jwt)
        .flatMap(jwtInfoBaseResult -> {
          if (jwtInfoBaseResult.getCode() != 0 || StringUtils
              .isEmpty(jwtInfoBaseResult.getData()) || StringUtils.isEmpty(jwtInfoBaseResult.getData().getDingId())) {
            log.warn(String.format("jwt鉴权失败，jwt是%s, 错误信息是%s", jwt, jwtInfoBaseResult.getMsg()));
            return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.JWT_HEADER_IS_ERROR);
          } else {
            JwtInfo jwtInfo = jwtInfoBaseResult.getData();
            try {
              String jwtInfoBase64 = new String(Base64.getEncoder()
                  .encode(objectMapper.writeValueAsString(jwtInfo).getBytes(StandardCharsets.UTF_8)));
              ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate()
                  .header(HeaderConstants.X_JWT_INFO, jwtInfoBase64).build();
              exchange.getAttributes().put(HeaderConstants.X_JWT_INFO, jwtInfoBase64);
              exchange.mutate().request(serverHttpRequest).build();
              return chain.filter(exchange);
            } catch (Exception e) {
              return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.JWT_HEADER_IS_ERROR);
            }
          }
        });
  }
}
