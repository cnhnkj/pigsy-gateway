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
  //给h5使用的密钥
  private MasterSlaveConfig h5;
  //给pc站使用的密钥
  private MasterSlaveConfig pc;
  //给微信小程序使用的密钥
  private MasterSlaveConfig wxApp;
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
