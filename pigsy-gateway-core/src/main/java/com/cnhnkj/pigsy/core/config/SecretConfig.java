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

package com.cnhnkj.pigsy.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Data
@ConfigurationProperties(prefix = "pigsy.secret")
@RefreshScope
public class SecretConfig {

  //给app使用的密钥
  private MasterSlaveConfig app;
  //给移动端h5使用的密钥
  private MasterSlaveConfig mobileH5;
  //给pc站使用的密钥
  private MasterSlaveConfig pc;
  //给小程序使用的密钥
  private MasterSlaveConfig miniProgram;
  //给内部系统使用的密钥
  private MasterSlaveConfig jwt;

  @Data
  public static class MasterSlaveConfig {

    //主密钥
    private String master;
    //备用密钥（用于密钥替换）
    private String slave;
  }
}
