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
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
              String msg = String.format("token鉴权失败，token是%s, 错误信息是%s", token, userInfoBaseResult.getMsg());
              log.warn(msg);
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
}
