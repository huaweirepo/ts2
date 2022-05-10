package com.huawei.lts.log.aop;

import java.lang.annotation.*;

/**
 * 自定义方法日志记录注解-https://blog.csdn.net/qq_44209274/article/details/116267856
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogMethod {
    /**
     * 记录标题
     */
    public String title() default "";

    /**
     * 操作类别
     */
    public String operatorType() default "";

    /**
     * 日志内容
     */
    public String content() default "";
}
