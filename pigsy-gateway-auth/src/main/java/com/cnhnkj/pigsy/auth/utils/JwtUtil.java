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

package com.cnhnkj.pigsy.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import lombok.SneakyThrows;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */
public class JwtUtil {

  @SneakyThrows
  public static DecodedJWT getDecodedJWT(String jwt, String publicKey) {
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
    Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);
    JWTVerifier verifier = JWT.require(algorithm).build();
    return verifier.verify(jwt);
  }

  @SneakyThrows
  public static String createJwtSign(String dingId, String privateKey, long expireTime) {
    java.security.Security.addProvider(
        new org.bouncycastle.jce.provider.BouncyCastleProvider()
    );


    byte[] keyBytes = Base64.getDecoder().decode(privateKey);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    RSAPrivateKey rsaPrivateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);

    Algorithm algorithm = Algorithm.RSA256(null, rsaPrivateKey);

    LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(expireTime);

    Map<String, Object> header = Map.of("typ", "JWT", "alg", "RSA256");

    return JWT.create()
        .withHeader(header)
        .withExpiresAt(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))
        .withClaim("dingId", dingId)
        .withIssuer("pigsy-auth")
        .withIssuedAt(new Date())
        .sign(algorithm);
  }
}
