package com.huawei.lts.common.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "log.lts")
public class LtsClientConfig {
    private boolean enabled;
    private String accessKey;
    private String secretKey;
    private String projectId;
    private String region;
    private String groupId;
    private String streamId;
    private String clusterId;
    private Integer batchSize;
    private int queueMaxSize;
}
