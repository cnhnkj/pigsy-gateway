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

package com.cnhnkj.pigsy.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Data
public class JwtInfo {

  @Schema(description = "钉钉的id")
  private String dingId;
  @Schema(description = "姓名")
  private String name;
  @Schema(description = "邮箱")
  private String email;
  @Schema(description = "手机")
  private String phone;
  @Schema(description = "角色(用于权限)")
  private String role;
  @Schema(description = "部门")
  private String department;

}
