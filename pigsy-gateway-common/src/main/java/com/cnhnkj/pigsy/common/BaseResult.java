package com.cnhnkj.pigsy.common;

import lombok.Data;

@Data
public class BaseResult<T> {

  private int code;
  private String msg;
  private String traceId;
  private T data;


  public BaseResult(T data) {
    this.code = BaseResultCode.SUCCESS.getCode();
    this.msg = BaseResultCode.SUCCESS.getMsg();
    this.data = data;
  }

  public BaseResult(T data, Object pageAuth) {
    this.code = BaseResultCode.SUCCESS.getCode();
    this.msg = BaseResultCode.SUCCESS.getMsg();
    this.data = data;
  }

  public BaseResult() {
    this.code = BaseResultCode.SUCCESS.getCode();
    this.msg = BaseResultCode.SUCCESS.getMsg();
  }

  public BaseResult(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public static BaseResult fail() {
    return fail(BaseResultCode.SERVER_INNER_ERROR.getCode(), BaseResultCode.SERVER_INNER_ERROR.getMsg());
  }

  public static <T> BaseResult<T> fail(String msg) {
    return fail(BaseResultCode.SERVER_INNER_ERROR.getCode(), msg);
  }

  public static <T> BaseResult<T> fail(int code, String msg) {
    BaseResult<T> r = new BaseResult<>();
    r.setCode(code);
    r.setMsg(msg);
    return r;
  }

  public static <T> BaseResult<T> success() {
    return new BaseResult<>();
  }

  public static <T> BaseResult<T> success(T t) {
    BaseResult<T> r = new BaseResult<>();
    r.setData(t);
    return r;
  }

}
