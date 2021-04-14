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

package com.cnhnkj.pigsy.core.utils;

import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */
@Slf4j
public class ResponseUtil {

  public static Mono<Void> sendErrorMsg(ObjectMapper objectMapper, ServerHttpResponse response,
      ErrorEnum errorEnum) {
    response.getHeaders().add(Constants.Response.CODE, String.valueOf(errorEnum.getCode()));
    return response.writeWith(
        Flux.just(ResponseUtil.serviceExceptionResponse(objectMapper, errorEnum))
            .map(bx -> {
              response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
              return response.bufferFactory().wrap(bx.getBytes(StandardCharsets.UTF_8));
            })).then();
  }


  public static String serviceExceptionResponse(ObjectMapper objectMapper, ErrorEnum errorEnum) {
    try {
      return objectMapper.writeValueAsString(errorEnum.setTraceId(MDC.get("traceId")));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return "{\"msg\":\"" + ErrorEnum.PIGSY_INNER_ERROR.getMsg() + "\", \"code\":"+ ErrorEnum.PIGSY_INNER_ERROR.getCode() + "}";
    }
  }

}
