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

import com.cnhnkj.pigsy.core.constants.HeaderConstants;
import java.net.InetSocketAddress;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
public class IpUtil {

  public static String getRealIp(ServerWebExchange exchange) {
    String realIp = exchange.getRequest().getHeaders().getFirst(HeaderConstants.REAL_IP);
    if (StringUtils.isNotBlank(realIp)) {
      return realIp;
    }

    String xff = exchange.getRequest().getHeaders().getFirst(HeaderConstants.X_FORWARDED_FOR);
    if (StringUtils.isNotBlank(xff)) {
      realIp = xff.split(",")[0].strip();
      return realIp;
    }

    String remoteIp = exchange.getRequest().getHeaders().getFirst(HeaderConstants.REMOTE_IP);
    if (StringUtils.isNotBlank(remoteIp)) {
      return remoteIp;
    }

    InetSocketAddress inetSocketAddress = exchange.getRequest().getRemoteAddress();
    if (inetSocketAddress != null) {
      return inetSocketAddress.getAddress().toString().replace("/", "");
    }

    return null;
  }


  public static boolean isInIpList(String address, Set<String> ipList) {
    try {
      return ipList.stream().anyMatch(ip -> {
        //ip是范围表示
        if (ip.contains("-")) {
          String[] ipRange = ip.split("-");
          String startIp = ipRange[0].trim();
          String endIp = ipRange[1].trim();
          return ((compareIpV4s(address, startIp)) >= 0) && (compareIpV4s(address, endIp) <= 0);
          //ip是掩码表示
        } else if (ip.contains("/")) {
          SubnetUtils subnetUtils = new SubnetUtils(ip);
          return subnetUtils.getInfo().isInRange(address);
          //具体ip值
        } else {
          return address.equals(ip);
        }
      });
    } catch (Exception e) {
      return false;
    }
  }

  private static int compareIpV4s(String ip1, String ip2) {
    int result;
    int ipValue1 = getIpV4Value(ip1);     // 获取ip1的32bit值
    int ipValue2 = getIpV4Value(ip2); // 获取ip2的32bit值
    result = Integer.compare(ipValue1, ipValue2);
    return result;
  }

  private static int getIpV4Value(String ipOrMask) {
    byte[] addr = getIpV4Bytes(ipOrMask);
    int address1 = addr[3] & 0xFF;
    address1 |= ((addr[2] << 8) & 0xFF00);
    address1 |= ((addr[1] << 16) & 0xFF0000);
    address1 |= ((addr[0] << 24) & 0xFF000000);
    return address1;
  }

  private static byte[] getIpV4Bytes(String ipOrMask) {
    try {
      String[] addrs = ipOrMask.split("\\.");
      int length = addrs.length;
      byte[] addr = new byte[length];
      for (int index = 0; index < length; index++) {
        addr[index] = (byte) (Integer.parseInt(addrs[index]) & 0xff);
      }
      return addr;
    } catch (Exception ignored) {
    }
    return new byte[4];
  }

}
