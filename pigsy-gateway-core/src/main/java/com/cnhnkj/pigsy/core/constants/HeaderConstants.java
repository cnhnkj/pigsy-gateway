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

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

public class HeaderConstants {


  public final static String CONTENT_TYPE = "content-type";
  //xff的header
  public final static String X_FORWARDED_FOR = "x-forwarded-for";


  //客户端访问的真实ip(必须要有)
  public final static String X_REAL_IP = "x-real-ip";
  //客户端的类型（必须要有）
  public final static String X_CLIENT_APPID = "x-client-appid";
  //客户端的页面(必须要有)
  public final static String X_CLIENT_PAGE = "x-client-page";
  //客户端的设备id(必须要有)
  public final static String X_CLIENT_ID = "x-client-id";
  //64位随机数,用于防重放攻击
  public final static String X_CLIENT_NONCE = "x-client-nonce";
  //客户端时间(必须要有)
  public final static String X_CLIENT_TIME = "x-client-time";
  //用户的登录凭证(可能没有)
  public final static String X_CLIENT_TOKEN = "x-client-token";
  //客户端的gps地址(可能没有)
  public final static String X_CLIENT_LOCATION = "x-client-location";
  //客户端请求签名
  public final static String X_CLIENT_SIGN = "x-client-sign";
  //客户端的ua(必须要有)
  public final static String X_CLIENT_UA = "x-client-ua";
  //平台(必须要有)
  public final static String X_CLIENT_OS_TYPE = "x-client-os-type";
  //系统版本(必须要有)
  public final static String X_CLIENT_SYSTEM_VERSION = "x-client-system-version";
  //渠道(必须要有)
  public final static String X_CLIENT_CHANNEL = "x-client-channel";
  //app版本(必须要有)
  public final static String X_CLIENT_APP_VERSION = "x-client-app-version";
  //机型(不一定要有)
  public final static String X_CLIENT_MODEL = "x-client-model";
  //网络类型(不一定有)
  public final static String X_CLIENT_NET = "x-client-net";

  //对端ip
  public final static String REMOTE_IP = "remoteip";
  //网关的环境
  public final static String X_PIGSY_PROFILE = "x-pigsy-profile";

  //通过token转换得到的userId
  public final static String X_USER_ID = "x-user-id";
  //通过jwt的头获取用户信息
  public final static String X_JWT_INFO = "x-jwt-info";
}
