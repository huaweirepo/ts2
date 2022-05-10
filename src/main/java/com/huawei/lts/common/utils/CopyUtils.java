package com.huawei.lts.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能描述 属性复制工具类
 *
 * @author jWX1116205
 * @since 2022-01-19
 */
@Slf4j
public class CopyUtils {
    public static <T> T copyProperties(Object source, T target) throws BeansException {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");
        Class<?> actualEditable = target.getClass();
        PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);

        AccessController.doPrivileged((PrivilegedAction<? extends Object>) () -> {
            for (PropertyDescriptor targetPd : targetPds) {
                Method writeMethod = targetPd.getWriteMethod();
                PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (writeMethod == null || sourcePd == null) {
                    continue;
                }
                Method readMethod = sourcePd.getReadMethod();
                if (readMethod != null && isAssignable(writeMethod.getParameterTypes()[0],
                        readMethod.getReturnType())) {
                    try {

                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                            readMethod.setAccessible(true);
                        }
                        Object value = readMethod.invoke(source);
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        writeMethod.invoke(target, value);
                    } catch (Throwable ex) {
                        throw new FatalBeanException(
                                "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                    }
                }
            }
            return null;
        });

        return target;
    }

    private static boolean isAssignable(Class<?> parameterType, Class<?> returnType) {
        return ClassUtils.isAssignable(parameterType, returnType) || (returnType.equals(Timestamp.class)
                && parameterType.equals(String.class));
    }

    /**
     * 将一个对象的属性拷贝到另一个类
     *
     * @param source   源对象
     * @param target   目标对象
     * @param consumer 额外的处理
     * @param <O>      输出类型
     * @param <I>      输入类型
     * @return 拷贝后的对象
     * @throws BeansException
     */
    public static <O, I> O copyProperties(I source, O target, BiConsumer<I, O> consumer) throws Exception {
        O output = copyProperties(source, target);
        if (null != consumer) {
            consumer.accept(source, output);
        }
        return output;
    }

    /**
     * 将请求的属性拷贝到Map中
     *
     * @param request 请求对象
     * @return 转换后的参数Map
     * @throws BeansException
     */
    public static Map<String, Object> copyToMap(Object request) throws BeansException {
        Assert.notNull(request, "request must not be null");
        Map<String, Object> criteria = new HashMap<>(16);

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(request.getClass());
        AccessController.doPrivileged((PrivilegedAction<? extends Object>) () -> {
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod.getName().equals("getClass")) {
                    continue;
                }

                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                    readMethod.setAccessible(true);
                }

                try {
                    Object value = readMethod.invoke(request);
                    String name = propertyDescriptor.getName();
                    if (null != value) {
                        criteria.put(name, value);
                    }
                } catch (Throwable e) {
                    log.error("", e);
                }
            }
            return null;
        });
        
        return criteria;
    }

    /**
     * 将I类型的列表转换为O类型的列表
     *
     * @param input       输入
     * @param outputClazz 输出类型
     * @param <O>         输入类型
     * @param <I>         输出类型
     * @return O类型的列表
     */
    public static <O, I> List<O> copyList(List<I> input, Class<O> outputClazz) {
        return Optional.ofNullable(input).orElseGet(ArrayList::new).stream().map(source -> {
            try {
                return copyProperties(source, outputClazz.newInstance());
            } catch (IllegalAccessException | InstantiationException e) {
                throw new IllegalStateException("new instance exception", e);
            }
        }).collect(Collectors.toList());
    }

    @FunctionalInterface
    public interface BiConsumer<T, U> {
        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         * @throws Exception if something wrong
         */
        void accept(T t, U u) throws Exception;
    }
}