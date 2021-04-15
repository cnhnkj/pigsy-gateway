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

package com.cnhnkj.pigsy.core.constants;

import org.springframework.core.Ordered;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

public class Constants {

  //http请求相关的常量
  public static class Request {
    public final static String REAL_IP = "real_ip";
    public final static String PATH = "path";
    public final static String DURATION = "duration";
    public final static String HEADER = "header";
    public final static String PARAMS = "params";
    public final static String COOKIES = "cookies";
    public final static String HTTP_METHOD = "httpMethod";
  }

  //http返回相关的常量
  public static class Response {
    public final static String HTTP_STATUS = "http_status";
    public final static String CODE = "code";
    public final static String OK_CODE = "0";
  }

  public final static String PROFILE = "profile";


  public static class GlobalOrder {
    public final static int GUARD_FILTER_ORDER = 0;
    public final static int ACCESS_LOG_ORDER = Ordered.HIGHEST_PRECEDENCE;
  }


}
