package com.cnhnkj.pigsy.auth.service;

import com.cnhnkj.pigsy.auth.domain.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenService {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  // token有效期为7天
  private static final long TOKEN_EXPIRE_TIME_DAY = 7;

  @SneakyThrows
  //目前只是简单测试，没有考虑同一个用户会产生多个token的情况
  public String createToken(Long userId, String username) {

    ObjectMapper objectMapper = new ObjectMapper();
    String token = UUID.randomUUID().toString();
    UserInfo usi = new UserInfo();
    usi.setUserId(userId);
    usi.setUsername(username);

    stringRedisTemplate.opsForValue()
        .set(token, objectMapper.writeValueAsString(usi), TOKEN_EXPIRE_TIME_DAY, TimeUnit.DAYS);

    //保证每天进行续签一次
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String key = date + ":" + token;
    stringRedisTemplate.opsForValue().set(key, "1", 24, TimeUnit.DAYS);

    return token;
  }

  @SneakyThrows
  public UserInfo getUserInfoByToken(String token) {
    ObjectMapper objectMapper = new ObjectMapper();

    UserInfo userInfo = new UserInfo();
    String ssoStr = stringRedisTemplate.opsForValue().get(token);
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String key = date + ":" + token;
    if (StringUtils.isNotBlank(ssoStr)) {
      userInfo = objectMapper.readValue(ssoStr, UserInfo.class);
      String index = stringRedisTemplate.opsForValue().get(key);
      if (StringUtils.isBlank(index)) {
        //续签token时间
        stringRedisTemplate.expire(token, TOKEN_EXPIRE_TIME_DAY, TimeUnit.DAYS);
        //每天计数一次，只续签一次token
        stringRedisTemplate.opsForValue().set(key, "1", 24, TimeUnit.HOURS);
      }
    }

    return userInfo;
  }
}
