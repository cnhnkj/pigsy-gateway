package com.cnhnkj.pigsy.core.filter.app;

import static org.springframework.http.server.PathContainer.parsePath;

import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Component
@Slf4j
public class ApiGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiGatewayFilterFactory.Config> {

  private static final String API_PATTERN = "/{serviceId}/api/**";

  @Resource
  private ObjectMapper objectMapper;

  @Value("${spring.profiles.active}")
  private String profile;

  private final PathPatternParser pathPatternParser = new PathPatternParser();

  public ApiGatewayFilterFactory() {
    super(Config.class);
    log.info("Loaded ApiGatewayFilterFactory");
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Collections.singletonList("enabled");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      if (!config.getEnabled()) {
        return chain.filter(exchange);
      }

      exchange.getRequest().mutate().headers(httpHeaders -> httpHeaders.add(HeaderConstants.X_PIGSY_PROFILE, profile));
      PathContainer path = parsePath(exchange.getRequest().getURI().getRawPath());

      PathPattern pathPattern = this.pathPatternParser.parse(API_PATTERN);

      if (!pathPattern.matches(path)) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.SERVICE_NOT_FOUND);
      }
      return chain.filter(exchange);
    };
  }

  @Data
  public static class Config {

    // 控制是否开启认证
    private Boolean enabled;
  }
}
