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

import com.cnhnkj.pigsy.auth.domain.UserInfo;
import com.cnhnkj.pigsy.auth.service.TokenService;
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
@Tag(name = "用户token校验控制器")
@RestController
@RequestMapping("/internal/auth")
public class AuthController {

  @Resource
  private TokenService tokenService;

  @RequestMapping(value = "/token/create", method = RequestMethod.POST)
  @Operation(description = "登录成功之后生产token的方法")
  public BaseResult<String> createToken(
      @RequestParam @Parameter(description = "要生成token的用户id") Long userId,
      @RequestParam @Parameter(description = "要生成token的用户名称") String username) {
    try {
      log.info("path is /internal/auth/token/create, userId is {}, username is {}", userId, username);
      String token = tokenService.createToken(userId, username);
      return BaseResult.success(token);
    } catch (Exception e) {
      log.warn("产生ticket失败，userId: {}, username: {}", userId, username, e);
      return BaseResult.fail(BaseResultCode.SERVER_INNER_ERROR.getCode(), BaseResultCode.SERVER_INNER_ERROR.getMsg());
    }
  }

  @RequestMapping(value = "/userInfo/by/token", method = RequestMethod.POST)
  @Operation(description = "根据token获取用户的基本信息")
  public BaseResult<UserInfo> getUserInfoByToken(
      @RequestParam @Parameter(description = "要用于获取userId所使用的token") String token) {
    try {
      log.info("path is /internal/auth/userInfo/by/token, token is {}", token);
      UserInfo userInfo = tokenService.getUserInfoByToken(token);
      return BaseResult.success(userInfo);
    } catch (Exception e) {
      log.warn("通过ticket获取用户信息失败，token: {}", token, e);
      return BaseResult.fail(BaseResultCode.SERVER_INNER_ERROR.getCode(), BaseResultCode.SERVER_INNER_ERROR.getMsg());
    }
  }
}
