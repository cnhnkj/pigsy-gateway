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

import static com.cnhnkj.pigsy.core.errors.ErrorEnum.PIGSY_INNER_ERROR;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

public class PigsyGatewayException extends RuntimeException {

  private final String message;

  private final int code;

  public PigsyGatewayException(String message) {
    super(message);
    this.message = message;
    this.code = PIGSY_INNER_ERROR.getCode();
  }

  public PigsyGatewayException(int code, String message) {
    super(message);
    this.message = message;
    this.code = code;
  }

}
