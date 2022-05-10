package com.huawei.lts.common.config;

import com.huawei.lts.log.LogLtsSchedule;
import com.huawei.lts.log.aop.LogMethodAspect;
import com.huawei.lts.log.lts.ClientFactBean;
import com.huawei.lts.log.lts.LogItemsUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(value = LtsClientConfig.class)
@ConditionalOnProperty(prefix = "log.lts", name = "enabled", havingValue = "true")
@Import(value = {ScheduleConfig.class})
public class LtsLogAutoConfiguration {
    @Bean
    public ClientFactBean clientFactBean() {
        return new ClientFactBean();
    }

    @Bean
    public LogLtsSchedule logLtsSchedule() {
        return new LogLtsSchedule();
    }

    @Bean
    public LogItemsUtil logItemsUtil() {
        return new LogItemsUtil();
    }

    @Bean
    public LogMethodAspect logMethodAspect() {
        return new LogMethodAspect();
    }
}
