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

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Data
@ConfigurationProperties(prefix = "pigsy.rate.limiter")
@RefreshScope
public class RateLimiterConfig {

  private Map<String, Bucket> services;

  @Data
  public static class Bucket {
    //令牌桶填充的速率
    private int replenishRate = 300;
    //令牌桶的容量
    private int burstCapacity = 300;
  }
}
