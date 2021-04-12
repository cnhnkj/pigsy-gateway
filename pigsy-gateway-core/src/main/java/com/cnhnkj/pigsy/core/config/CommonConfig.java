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

import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Data
@ConfigurationProperties(prefix = "pigsy.common")
@RefreshScope
public class CommonConfig {

  //客户端时间和服务器时间前后可以相差多少秒
  private Integer requestAvailDuration = 1800;

  //服务端最小支持版本（版本规则是 x.y.z）
  private String minimumSupportedVersion = "";

  //慢请求定义的时间，单位毫秒
  private Long slowLogTime = 2000L;

  //禁止访问配置
  private ForbidInfo forbidInfo;

  //停机维护配置
  private MaintenanceInfo maintenanceInfo;

  @Data
  public static class ForbidInfo {

    //禁止访问的api
    private Set<String> apis;
    //禁止访问的header
    private Set<String> headers;
    //禁止访问的ip
    private Set<String> ips;

  }

  @Data
  public static class MaintenanceInfo {

    //维护中的服务列表
    private Set<String> services;
    //停机维护的开始时间 时间格式yyyy-MM-dd HH
    private Set<String> startTime;
    //停机维护的结束时间 时间格式yyyy-MM-dd HH
    private Set<String> endTime;

  }

}
