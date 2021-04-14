package com.cnhnkj.pigsy.core.constants;

import org.springframework.core.Ordered;

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
