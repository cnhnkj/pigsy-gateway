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

package com.cnhnkj.pigsy.core.filter.global;


import com.cnhnkj.pigsy.core.config.CommonConfig;
import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.errors.PigsyGatewayException;
import com.cnhnkj.pigsy.core.utils.IpUtil;
import com.cnhnkj.pigsy.core.utils.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
@Component
@EnableConfigurationProperties(CommonConfig.class)
public class GuardFilter implements GlobalFilter, Ordered {

  @Resource
  private CommonConfig commonConfig;

  @Resource
  private ObjectMapper objectMapper;

  private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getRawPath();
    int serviceIdIndex = path.indexOf("/", 1);

    ServerHttpResponse response = exchange.getResponse();

    if (serviceIdIndex == -1) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.REQUEST_URI_PATH_ERROR);
    }

    String realIp = IpUtil.getRealIp(exchange);
    if (Strings.isEmpty(realIp)) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.REAL_IP_INVALID);
    }

    //检查ip的正确性
    Set<String> forbidIps = commonConfig.getForbidInfo().getIpSet();
    if (null != forbidIps && !forbidIps.isEmpty() && IpUtil.isInIpList(realIp, forbidIps)) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.IP_IS_FORBID);
    }

    //检查服务是否正常
    try {
      checkMaintenanceServices(realIp, path, serviceIdIndex);
    } catch (PigsyGatewayException e) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.SERVICE_IS_MAINTENANCE.setMsg(e.getMessage()));
    }

    //检查api是否正常
    Set<String> forbidApis = commonConfig.getForbidInfo().getApiSet();
    if (StringUtils.isNotBlank(path) && null != forbidApis && forbidApis.contains(path)) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.API_IS_FORBID);
    }

    //检查header是否正常
    try {
      checkForbidHeaders(exchange);
    } catch (PigsyGatewayException e) {
      return ResponseUtil.sendErrorMsg(objectMapper, response, ErrorEnum.HEADER_IS_FORBID);
    }

    return chain.filter(exchange);
  }

  private void checkForbidHeaders(ServerWebExchange exchange) {
    Set<String> forbidHeaderSet = commonConfig.getForbidInfo().getHeaderSet();
    if (!CollectionUtils.isEmpty(forbidHeaderSet)) {
      forbidHeaderSet.removeIf(header -> header.contains("|") || header.length() < 8);
    }

    if (!CollectionUtils.isEmpty(forbidHeaderSet)) {
      StringBuilder sb = new StringBuilder("|");
      exchange.getRequest().getHeaders().values()
          .forEach(header -> header.forEach(h -> sb.append(h.replaceAll("\\|", "")).append("|")));
      String headerValue = sb.toString();
      boolean hasForbidHeader = forbidHeaderSet.stream().anyMatch(headerValue::contains);
      if (hasForbidHeader) {
        throw new PigsyGatewayException(ErrorEnum.HEADER_IS_FORBID.getCode(), ErrorEnum.HEADER_IS_FORBID.getMsg());
      }
    }
  }

  private void checkMaintenanceServices(String realIp, String path, int serviceIdIndex) {
    String serviceId = path.substring(1, serviceIdIndex);
    Set<String> maintenanceServices = commonConfig.getMaintenanceInfo().getServices();
    if (StringUtils.isNotBlank(serviceId) && null != maintenanceServices && maintenanceServices.contains(serviceId)) {
      LocalDateTime startTime = null, endTime = null;
      String maintenanceStartTime = commonConfig.getMaintenanceInfo().getStartTime();
      String maintenanceEndTime = commonConfig.getMaintenanceInfo().getEndTime();
      if (Strings.isNotEmpty(maintenanceStartTime) && Strings.isNotEmpty(maintenanceEndTime)) {
        startTime = LocalDateTime.parse(maintenanceStartTime, dateTimeFormatter);
        endTime = LocalDateTime.parse(maintenanceEndTime, dateTimeFormatter);
      }

      log.info("服务正在维护状态，请求真实ip是{}, 服务id是{}, 维护开始时间是{}, 维护结束时间是{}", realIp, serviceId,
          startTime, endTime);

      Set<String> whiteIpSet = commonConfig.getMaintenanceInfo().getWhiteIpSet();
      if (realIp != null && !whiteIpSet.contains(realIp)) {
        String msg = ErrorEnum.SERVICE_IS_MAINTENANCE.getMsg() + getTimeMsg(startTime, endTime);
        throw new PigsyGatewayException(ErrorEnum.SERVICE_IS_MAINTENANCE.getCode(), msg);
      }
    }
  }

  private static String getTimeMsg(LocalDateTime startTime, LocalDateTime endTime) {
    if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
      String startDay = String.format("%d年%02d月%02d日", startTime.getYear(), startTime.getMonthValue(), startTime.getDayOfMonth());
      String endDay = String.format("%d年%02d月%02d日", endTime.getYear(), endTime.getMonthValue(), endTime.getDayOfMonth());
      String time1 = String.format("%02d:%02d", startTime.getHour(), startTime.getMinute());
      String time2 = String.format("%02d:%02d", endTime.getHour(), endTime.getMinute());
      if (startDay.equals(endDay)) {
        return "维护时间: " + startDay + "\n" + time1 + " ~ " + time2;
      } else {
        return "维护时间: " + startDay + " " + time1 + " ~ " + endDay + " " + time2;
      }
    } else {
      return "敬请期待";
    }
  }

  @Override
  public int getOrder() {
    return Constants.GLOBAL_GUARD_FILTER_ORDER;
  }
}
