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

package com.cnhnkj.pigsy.auth.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.cnhnkj.pigsy.auth.AuthApplication;
import com.cnhnkj.pigsy.auth.domain.JwtInfo;
import com.cnhnkj.pigsy.auth.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthApplication.class)
@Slf4j
public class JwtControllerTest {

  @Autowired
  private JwtService jwtService;

  @Test
  public void jwtServiceTest() {
    String sign = jwtService.createJwtSign("12345");
    JwtInfo jwtInfo = jwtService.checkJwt(sign);
    log.info(jwtInfo.toString());
    assertThat(jwtInfo.getDingId()).isEqualTo("12345");
  }
}
