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

package com.cnhnkj.pigsy.core.metrics;

import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.constants.Constants.Response;
import com.cnhnkj.pigsy.core.utils.RequestUtil;
import com.google.common.base.Strings;
import io.micrometer.core.instrument.Tags;
import java.util.Optional;
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Configuration
public class MetricsConfiguration {

  @Bean
  public WebFluxTagsContributor metricsWebFluxConfigurer() {
    return (exchange, ex) -> Tags.of("service", RequestUtil.getService(exchange), "responseStatus", getResponseStatus(exchange));
  }

  //暂时的code只区分正常，
  private String getResponseStatus(ServerWebExchange exchange) {
    String responseCode = exchange.getResponse().getHeaders().getFirst(Response.CODE);
    if (Strings.isNullOrEmpty(responseCode)) {
      responseCode = String.valueOf(Optional.ofNullable(exchange.getResponse().getRawStatusCode()).orElse(-1));
      if (responseCode.equals("200")) {
        responseCode = "0";
      }
    }

    int code = Integer.parseInt(responseCode);

    if (code == 0 || code == 302) {
      return Constants.Response.SUCCESS;
    } else if (code >= 400 && code < 500) {
      return Constants.Response.CLIENT_REQUEST_ERROR;
    } else if (code >= 500 && code < 600) {
      return Constants.Response.SERVER_REQUEST_ERROR;
    } else {
      return Constants.Response.BUSINESS_ERROR;
    }
  }
}