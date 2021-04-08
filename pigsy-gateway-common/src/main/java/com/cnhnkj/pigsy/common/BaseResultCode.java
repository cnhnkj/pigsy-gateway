package com.cnhnkj.pigsy.common;

public enum BaseResultCode {
  SUCCESS(0, "success"),
  SERVER_INNER_ERROR(50000, "抱歉，服务器开了点小差，请稍后再试。"),

  ;

  private int code;
  private String msg;

  BaseResultCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getMsg() {
    return msg;
  }

  public int getCode() {
    return code;
  }

}
