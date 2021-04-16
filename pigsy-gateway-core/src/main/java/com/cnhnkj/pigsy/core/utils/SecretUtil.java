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

package com.cnhnkj.pigsy.core.utils;

import com.cnhnkj.pigsy.core.constants.Constants;
import com.cnhnkj.pigsy.core.errors.ErrorEnum;
import com.cnhnkj.pigsy.core.errors.PigsyGatewayException;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */

@Slf4j
public class SecretUtil {

  private static void checkSecret(String nonce, String timestamp, String deviceId,
      String secret) {
    if (nonce.length() != 32) {
      log.warn("随机数长度错误");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }

    if (timestamp.length() != 13) {
      log.warn("时间戳长度错误");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }

    if (deviceId.length() != 32) {
      log.warn("设备号长度错误");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }

    if (secret.length() != 32) {
      log.warn("密钥长度错误");
      throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }


  public static String generateSecret(String nonce, String timestamp, String deviceId,
      String secret, int secretType) {
    checkSecret(nonce, timestamp, deviceId, secret);

    String obscureNonce = obscureNonce(nonce, secretType);
    log.debug("obscureNonce is {}, type is {}", obscureNonce, secretType);

    String obscureTimestamp = obscureTimestamp(timestamp, secretType);
    log.debug("obscureTimestamp is {}, type is {}", obscureTimestamp, secretType);

    String obscureDeviceId = obscureDeviceId(deviceId, secretType);
    log.debug("obscureDeviceId is {}, type is {}", obscureDeviceId, secretType);

    String obscureSecret = obscureSecret(secret, secretType);
    log.debug("obscureSecret is {}, type is {}", obscureSecret, secretType);

    String obscure = obscureAll(obscureNonce, obscureTimestamp, obscureDeviceId, obscureSecret, secretType);
    log.debug("obscure is {} type is {}", obscure, secretType);

    String sign = DigestUtils.sha384Hex(obscure);
    log.debug("sign is {}, type is {}", sign, secretType);
    return sign;
  }

  private static String obscureNonce(String nonce, int secretType) {
    switch (secretType) {
      case Constants.Secret.APP_TYPE:
      case Constants.Secret.MOBILE_H5_TYPE:
      case Constants.Secret.PC_TYPE:
      case Constants.Secret.MINI_PROGRAM_TYPE:
        return DigestUtils.md5Hex(nonce);
      default:
        log.warn("签名类型错误,类型为" + secretType);
        throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }

  private static String obscureTimestamp(String timestamp, int secretType) {
    switch (secretType) {
      case Constants.Secret.APP_TYPE:
      case Constants.Secret.MOBILE_H5_TYPE:
      case Constants.Secret.PC_TYPE:
      case Constants.Secret.MINI_PROGRAM_TYPE:
        return DigestUtils.sha256Hex(timestamp);
      default:
        log.warn("签名类型错误,类型为" + secretType);
        throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }

  private static String obscureDeviceId(String deviceId, int secretType) {
    switch (secretType) {
      case Constants.Secret.APP_TYPE:
      case Constants.Secret.MOBILE_H5_TYPE:
      case Constants.Secret.PC_TYPE:
      case Constants.Secret.MINI_PROGRAM_TYPE:
        return DigestUtils.sha1Hex(deviceId);
      default:
        log.warn("签名类型错误,类型为" + secretType);
        throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }

  private static String obscureSecret(String secret, int secretType) {
    String hex;
    switch (secretType) {
      case Constants.Secret.APP_TYPE:
      case Constants.Secret.MOBILE_H5_TYPE:
      case Constants.Secret.PC_TYPE:
      case Constants.Secret.MINI_PROGRAM_TYPE:
        hex = DigestUtils.sha384Hex(secret);
        break;
      default:
        log.warn("签名类型错误,类型为" + secretType);
        throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }

    return Long.toUnsignedString(Long
        .parseUnsignedLong(hex.substring(hex.length() - 16, hex.length() - 1), 16));
  }

  private static String obscureAll(String obscureNonce, String obscureTimestamp,
      String obscureDeviceId, String obscureSecret, int secretType) {
    switch (secretType) {
      case Constants.Secret.APP_TYPE:
      case Constants.Secret.MOBILE_H5_TYPE:
      case Constants.Secret.PC_TYPE:
      case Constants.Secret.MINI_PROGRAM_TYPE:
        return String.join(":", Lists
            .newArrayList(obscureTimestamp, obscureDeviceId, obscureSecret, obscureNonce));
      default:
        log.warn("签名类型错误,类型为" + secretType);
        throw new PigsyGatewayException(ErrorEnum.REQUEST_PARAM_ERROR.getCode(), ErrorEnum.REQUEST_PARAM_ERROR.getMsg());
    }
  }

}
