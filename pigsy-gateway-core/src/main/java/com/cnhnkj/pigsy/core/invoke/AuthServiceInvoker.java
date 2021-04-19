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

package com.cnhnkj.pigsy.core.invoke;

import com.cnhnkj.pigsy.common.BaseResult;
import javax.annotation.Resource;
import lombok.Data;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Component
public class AuthServiceInvoker {

  @Resource
  @LoadBalanced
  private WebClient.Builder builder;

  private static final String AUTH_GET_INFO_BY_TICKET_URL = "http://pigsy-auth/internal/auth/userInfo/by/token";

  public Mono<BaseResult<UserInfo>> getUserInfoByToken(String token) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("token", token);
    return builder.build().post().uri(AUTH_GET_INFO_BY_TICKET_URL).contentType(
        MediaType.APPLICATION_FORM_URLENCODED).body(BodyInserters.fromFormData(formData)).retrieve()
        .bodyToMono(new ParameterizedTypeReference<BaseResult<UserInfo>>() {
        }).onErrorReturn(BaseResult.fail(-1, "服务器异常,请稍后再试"));
  }

  @Data
  public static class UserInfo {

    private Long userId;
    private String username;
  }
}
