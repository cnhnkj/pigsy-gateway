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

package com.cnhnkj.pigsy.core.errors;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 网关返回的错误码 700XXX（700开头，与统一的错误码500进行区别）
 * @author longzhe[longzhe@cnhnkj.com]
 */

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum ErrorEnum {

  PIGSY_INNER_ERROR(700500, "网关未知错误"),
  SERVICE_IS_MAINTENANCE(700501, "访问的服务正在维护中"),
  API_IS_FORBID(700502, "对应的功能已经被禁止"),
  HEADER_IS_FORBID(700503, "对应的请求头已经被禁止"),
  IP_IS_FORBID(700504, "对应的请求ip已经被禁止"),


  REQUEST_URI_PATH_ERROR(700401, "访问的接口路径错误"),
  REAL_IP_INVALID(700402, "真实ip地址获取失败"),
  SERVICE_NOT_FOUND(700404, "没有找到对应的服务"),
  ;


  private int code;
  private String msg;
  private String traceId;

  ErrorEnum(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public ErrorEnum setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public ErrorEnum setMsg(String msg) {
    this.msg = msg;
    return this;
  }

  public ErrorEnum setCode(int code) {
    this.code = code;
    return this;
  }

}
