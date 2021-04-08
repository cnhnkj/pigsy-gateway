package com.cnhnkj.pigsy.core;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableApolloConfig
public class PigsyApplication {

  public static void main(String[] args) {
    SpringApplication.run(PigsyApplication.class, args);
  }


}
