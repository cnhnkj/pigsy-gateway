package com.cnhnkj.pigsy.auth;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class AuthApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(AuthApplication.class).web(WebApplicationType.SERVLET)
        .run(args);
  }
}
