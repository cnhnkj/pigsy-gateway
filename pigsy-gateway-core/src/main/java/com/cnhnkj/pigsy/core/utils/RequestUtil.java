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

import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

public class RequestUtil {

  public static String getService(ServerWebExchange exchange) {
    RequestPath requestPath = exchange.getRequest().getPath();
    return getServiceName(requestPath);
  }

  public static String getService(ServerHttpRequest request) {
    RequestPath requestPath = request.getPath();
    return getServiceName(requestPath);
  }

  private static String getServiceName(RequestPath requestPath) {
    String service;
    if (requestPath.toString().startsWith("/actuator") || requestPath.toString().startsWith("/gateway") || requestPath.toString()
        .contains("favicon")) {
      service = "pigsy";
    } else {
      try {
        service = requestPath.pathWithinApplication().subPath(1, 2).toString();
      } catch (Exception ignore) {
        service = "pigsy";
      }
    }
    return service;
  }
}
