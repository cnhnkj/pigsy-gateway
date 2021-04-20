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

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.core.Ordered;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

public class Constants {

  public static class AppId {

    //ANDROID客户端
    public final static String ANDROID_APP_ID = "1";
    //IOS客户端
    public final static String IOS_APP_ID = "2";
    //客户端内嵌页面
    public final static String WEB_IN_APP_ID = "3";

    //微信小程序
    public final static String WECHAT_MINI_PROGRAM_ID = "11";

    //支付宝小程序
    public final static String ALIPAY_MINI_PROGRAM_APP_ID = "12";

    //百度小程序
    public final static String BAIDU_MINI_PROGRAM_ID = "13";

    //pc站
    public final static String PC_ID = "31";

    //m站
    public final static String MOBILE_H5_ID = "41";
  }

  public static class Secret {

    public final static int APP_TYPE = 1;
    public final static int MINI_PROGRAM_TYPE = 2;
    public final static int PC_TYPE = 3;
    public final static int MOBILE_H5_TYPE = 4;
  }

  public final static List<String> IN_APP_ID_LIST = Lists
      .newArrayList(AppId.ANDROID_APP_ID, AppId.IOS_APP_ID, AppId.WEB_IN_APP_ID);



  public final static List<String> OUT_APP_ID_LIST = Lists
      .newArrayList(AppId.WECHAT_MINI_PROGRAM_ID, AppId.ALIPAY_MINI_PROGRAM_APP_ID, AppId.BAIDU_MINI_PROGRAM_ID, AppId.PC_ID,
          AppId.MOBILE_H5_ID);


  //http请求相关的常量
  public static class Request {

    public final static String REAL_IP = "realIp";
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
