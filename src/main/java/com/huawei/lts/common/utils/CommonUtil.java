/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.lts.common.utils;

import java.util.*;

/**
 * 通过方法不好分类的都放在这里
 *
 * @since 2021-12-03
 */
public class CommonUtil {
    /**
     * 获取 UUID 除去 "-"
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 初始化headerMap
     *
     * @param tenantDomain
     * @return headerMap
     */
    public static Map<String, Object> buildHeaderMap(String tenantDomain) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("tenantDomain", tenantDomain);
        return headerMap;
    }

    /**
     * 将一个list均分成n个list来批量插入
     *
     * @param source
     * @return
     */
    public static <T> List<List<T>> averageAssign(List<T> source, int n) {
        List<List<T>> result = new ArrayList<>();
        int size = source.size();
        int number = size % n == 0 ? size / n : (size / n) + 1;
        List<T> value;
        for (int i = 0; i < number; i++) {
            if (i == number - 1) {
                value = source.subList(i * n, size);
            } else {
                value = source.subList(i * n, (i + 1) * n);
            }
            result.add(value);
        }

        return result;
    }

    public static <T> List<List<T>> splistList(List<T> list, int subNum) {
        List<List<T>> tNewList = new ArrayList<>();
        int priIndex = 0;
        int lastPriIndex = 0;
        int insertTimes = list.size() / subNum;
        List<T> subList = new ArrayList<>();
        for (int i = 0; i <= insertTimes; i++) {
            priIndex = subNum * i;
            lastPriIndex = priIndex + subNum;
            if (i == insertTimes) {
                subList = list.subList(priIndex, list.size());
            } else {
                subList = list.subList(priIndex, lastPriIndex);
            }
            if (subList.size() > 0) {
                tNewList.add(subList);
            }
        }
        return tNewList;
    }
}