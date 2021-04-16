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

import com.cnhnkj.pigsy.core.errors.PigsyGatewayException;

/**
 * @author longzhe[longzhe@cnhnkj.com]
 */
public class VersionUtil {

  public static int compareVersion(String version1, String version2) {
    // 注意此处为正则匹配，不能用.
    String[] versionArray1 = version1.split("\\.");
    String[] versionArray2 = version2.split("\\.");
    int idx = 0;

    if (versionArray1.length != 3 || versionArray2.length != 3) {
      throw new PigsyGatewayException("版本号格式不正确");
    }
    // 取数组最小长度值
    int minLength = versionArray1.length;
    int diff = 0;
    // 先比较长度，再比较字符
    while (idx < minLength
        && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0
        && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
      ++idx;
    }
    return diff;
  }

}
