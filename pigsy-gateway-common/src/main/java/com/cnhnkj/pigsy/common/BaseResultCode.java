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

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

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
