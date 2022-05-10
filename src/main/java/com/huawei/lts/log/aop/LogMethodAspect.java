package com.huawei.lts.log.aop;

import com.alibaba.fastjson.JSON;
import com.huawei.lts.common.config.LtsClientConfig;
import com.huawei.lts.common.constant.NumberConstant;
import com.huawei.lts.common.utils.CopyUtils;
import com.huawei.lts.common.utils.ServletUtils;
import com.huawei.lts.log.lts.LogItemsUtil;
import com.huawei.model.pushLog.LogContent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 方法操作日志记录处理
 */
@Slf4j
@Aspect
public class LogMethodAspect {
    @Autowired(required = false)
    private LogItemsUtil logItemsUtil;
    @Autowired
    private LtsClientConfig ltsClientConfig;

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(com.huawei.lts.log.aop.LogMethod)")
    public void logMethodPointCut() {
    }

    /**
     * 处理完请求后执行
     */
    @AfterReturning(pointcut = "logMethodPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, result);
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(value = "logMethodPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object result) {
        try {
            String reqIp = ServletUtils.getIp();
            String requestUrl = ServletUtils.getRequest().getRequestURI();
            // 获得注解
            LogMethod logMethod = getAnnotationLog(joinPoint);
            if (logMethod == null) {
                return;
            }
            // 将入参转换成json
            String params = argsArrayToString(joinPoint.getArgs());
            String logTemplate = "title:{0},operatorType:{1},content:{2},return:{3}";
            String logTemplate2 = "reqIp:{0},requestUrl:{1},params={2}";
            if (e != null) {
                logTemplate = "reqIp:{0},requestUrl:{1},params={2},errorMsg={3}";
                String errorMsg = stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace());
                logTemplate2 = MessageFormat.format(logTemplate2, reqIp, requestUrl, params, errorMsg);
            } else {
                logTemplate2 = MessageFormat.format(logTemplate2, reqIp, requestUrl, params);
            }
            String methodResp = "void";
            if (result != null) {
                Map<String, Object> resultMap = CopyUtils.copyToMap(result);
                resultMap.remove("result");
                methodResp = JSON.toJSONString(resultMap);
            }
            logTemplate = MessageFormat.format(logTemplate, logMethod.title(), logMethod.operatorType(), logMethod.content(), methodResp);
            logTemplate = logTemplate.concat(";请求参数：").concat(logTemplate2);
            if (ltsClientConfig.isEnabled()) {
                LogContent logContent = new LogContent();
                logContent.setLogTimeNs(System.currentTimeMillis() * NumberConstant.ONE_MILLION);
                logContent.setLog(logTemplate);
                logItemsUtil.pushLogContext(logContent, null, null);
            } else {
                if (e != null) {
                    log.error(logTemplate);
                } else {
                    log.info(logTemplate);
                }
            }
        } catch (Exception exp) {
            log.error("异常信息:{}", exp.getMessage());
        }
    }

    /**
     * 获取存在的注解
     */
    private LogMethod getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(LogMethod.class);
        }
        return null;
    }

    /**
     * 转换异常信息为字符串
     */
    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuffer strbuff = new StringBuffer();
        for (StackTraceElement stet : elements) {
            strbuff.append(stet + "\n");
        }
        String message = exceptionName + ":" + exceptionMessage + "\n\t" + strbuff.toString();
        message = substring(message, 0, 2000);
        return message;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (Object o : paramsArray) {
                if (o != null) {
                    try {
                        Object jsonObj = JSON.toJSON(o);
                        params += jsonObj.toString() + " ";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params.trim();
    }

    //字符串截取
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        } else {
            if (end < 0) {
                end += str.length();
            }

            if (start < 0) {
                start += str.length();
            }

            if (end > str.length()) {
                end = str.length();
            }

            if (start > end) {
                return "";
            } else {
                if (start < 0) {
                    start = 0;
                }

                if (end < 0) {
                    end = 0;
                }
                return str.substring(start, end);
            }
        }
    }

    /**
     * 转换request 请求参数
     *
     * @param paramMap request获取的参数数组
     */
    public Map<String, String> converMap(Map<String, String[]> paramMap) {
        Map<String, String> returnMap = new HashMap<>();
        for (String key : paramMap.keySet()) {
            returnMap.put(key, paramMap.get(key)[0]);
        }
        return returnMap;
    }
}
