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

package com.cnhnkj.pigsy.auth.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cnhnkj.pigsy.auth.config.JwtSecretConfig;
import com.cnhnkj.pigsy.auth.domain.JwtInfo;
import com.cnhnkj.pigsy.auth.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
@Service
@EnableConfigurationProperties({JwtSecretConfig.class})
public class JwtService {

  @Resource
  private JwtSecretConfig jwtSecretConfig;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  private static final long JWT_EXPIRE_SECOND = 24 * 60 * 60;


  @SneakyThrows
  public JwtInfo checkJwt(String jwt) {
    DecodedJWT decodedJWT = JwtUtil.getDecodedJWT(jwt, jwtSecretConfig.getPublicKey());
    String payload = new String(Base64.getDecoder().decode(decodedJWT.getPayload()));
    ObjectMapper objectMapper = new ObjectMapper();
    String dingId = objectMapper.readTree(payload).get("dingId").asText();

    String value = stringRedisTemplate.opsForValue().get(dingId);
    if(!Strings.isNullOrEmpty(value)) {
      return objectMapper.readValue(value, JwtInfo.class);
    } else {
      throw new RuntimeException("用户不存在");
    }
  }

  @SneakyThrows
  public String createJwtSign(String dingId) {
    ObjectMapper objectMapper = new ObjectMapper();
    String value = stringRedisTemplate.opsForValue().get(dingId);
    if (Strings.isNullOrEmpty(value)) {
      JwtInfo jwtInfo = createJwtInfo(dingId);
      value = objectMapper.writeValueAsString(jwtInfo);
      stringRedisTemplate.opsForValue().set(dingId, value);
    }

    //生成sign
    String sign = JwtUtil.createJwtSign(dingId, jwtSecretConfig.getPrivateKey(), JWT_EXPIRE_SECOND);

    //更新缓存时间
    stringRedisTemplate.expire(dingId, JWT_EXPIRE_SECOND, TimeUnit.SECONDS);
    return sign;
  }

  private JwtInfo createJwtInfo(String dingId) {
    JwtInfo jwtInfo = new JwtInfo();
    jwtInfo.setDingId(dingId);
    String name = RandomStringUtils.random(6);
    jwtInfo.setName(name);
    String phone = RandomStringUtils.randomNumeric(13);
    jwtInfo.setPhone(phone);

    jwtInfo.setEmail(name + "@cnhnkj.com");
    jwtInfo.setRole("admin");
    jwtInfo.setDepartment("tech");
    return jwtInfo;
  }
}
