/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.huawei.lts.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Jackson json序列化和反序列化工具类
 *
 * @author y00464350
 * @since 2022-02-16
 */
public class JsonUtil {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 将对象转换成json字符串。
     *
     * @param data 对象
     * @return json字符串
     * @throws JsonProcessingException 转换异常
     */
    public static String objectToJson(Object data) throws JsonProcessingException {
        return MAPPER.writeValueAsString(data);
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     * @param <T>      泛型
     * @return 对象
     * @throws JsonProcessingException 转换异常
     */
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) throws JsonProcessingException {
        return MAPPER.readValue(jsonData, beanType);
    }

    /**
     * 将json数据转换成pojo对象list
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     * @param <T>      泛型
     * @return 对象
     * @throws JsonProcessingException 转换异常
     */
    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) throws JsonProcessingException {
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
        return MAPPER.readValue(jsonData, javaType);
    }

}
