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

package com.cnhnkj.pigsy.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.filter.global.GuardFilter;
import com.cnhnkj.pigsy.core.utils.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */
@RunWith(MockitoJUnitRunner.class)
public class GuardFilterTest {

  @InjectMocks
  private GuardFilter filter;

  @Mock
  private GatewayFilterChain chain;


  @Test
  public void testErrorEnumString() {
    String errorEnumString =
        "{\"msg\":\"" + ErrorEnum.PIGSY_INNER_ERROR.getMsg() + "\", \"code\":" + ErrorEnum.PIGSY_INNER_ERROR.getCode() + "}";
    assertThat(errorEnumString).isEqualTo("{\"msg\":\"网关未知错误\", \"code\":700500}");
  }

  @Test
  public void testRequestRealIpFromRealIpHeader() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("endpoint")
        .header(HeaderConstants.REAL_IP, "127.0.0.1", "127.0.0.2")
        .header(HeaderConstants.X_FORWARDED_FOR, "127.0.0.6, 127.0.0.5, 127.0.0.4")
        .header(HeaderConstants.REMOTE_IP, "127.0.0.10").build());
    String realIp = IpUtil.getRealIp(exchange);
    assertThat(realIp).isEqualTo("127.0.0.1");
  }

  @Test
  public void testRequestRealIpFromXffHeader() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("endpoint")
        .header(HeaderConstants.X_FORWARDED_FOR, "127.0.0.3, 127.0.0.2, 127.0.0.1")
        .header(HeaderConstants.REMOTE_IP, "127.0.0.5").build());
    String realIp = IpUtil.getRealIp(exchange);
    assertThat(realIp).isEqualTo("127.0.0.3");
  }

  @Test
  public void testRequestRealIpFromRemoteIpHeader() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("endpoint")
        .header(HeaderConstants.REMOTE_IP, "127.0.0.5").build());
    String realIp = IpUtil.getRealIp(exchange);
    assertThat(realIp).isEqualTo("127.0.0.5");
  }

  @Test
  public void testRequestRealIpFromNullHeader() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("endpoint").build());
    String realIp = IpUtil.getRealIp(exchange);
    assertThat(realIp).isNull();
  }

  @Test
  public void testGuardFilterNoServiceId() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("endpoint").build());
    ReflectionTestUtils.setField(filter, "objectMapper", new ObjectMapper());

    filter.filter(exchange, chain);

    assertThat(exchange.getResponse().getHeaders().getFirst(Constants.RESPONSE_CODE))
        .isEqualTo(String.valueOf(ErrorEnum.REQUEST_URI_PATH_ERROR.getCode()));

    verifyNoMoreInteractions(chain);
  }

  @Test
  public void testGuardFilterNoRealIp() {
    ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/pigsy-test/hello").build());
    ReflectionTestUtils.setField(filter, "objectMapper", new ObjectMapper());

    filter.filter(exchange, chain);

    assertThat(exchange.getResponse().getHeaders().getFirst(Constants.RESPONSE_CODE))
        .isEqualTo(String.valueOf(ErrorEnum.REAL_IP_INVALID.getCode()));

    verifyNoMoreInteractions(chain);
  }
}
