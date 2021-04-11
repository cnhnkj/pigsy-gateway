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

package com.cnhnkj.pigsy.auth.controller;

import com.cnhnkj.pigsy.auth.service.JwtService;
import com.cnhnkj.pigsy.common.BaseResult;
import com.cnhnkj.pigsy.common.BaseResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
@Tag(name = "内部用户jwt校验控制器")
@RestController
@RequestMapping("/internal/auth")
public class JwtController {

  @Resource
  private JwtService jwtService;

  @RequestMapping(value = "/jwt/check", method = RequestMethod.POST)
  @Operation(description = "jwt的验证接口")
  public BaseResult checkJwt(
      @RequestParam @Parameter(description = "用于校验的jwt") String jwt) {
    try {
      return BaseResult.success();
    } catch (Exception e) {
      log.warn("校验jwt信息失败，jwt: {}", jwt, e);
      return BaseResult.fail(BaseResultCode.SERVER_INNER_ERROR.getCode(), BaseResultCode.SERVER_INNER_ERROR.getMsg());
    }
  }

}
