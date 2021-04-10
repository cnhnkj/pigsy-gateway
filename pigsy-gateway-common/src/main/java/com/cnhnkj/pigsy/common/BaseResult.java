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

package com.cnhnkj.pigsy.common;

import lombok.Data;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

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
