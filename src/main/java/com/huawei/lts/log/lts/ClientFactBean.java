package com.huawei.lts.log.lts;

import com.huawei.Client;
import com.huawei.exception.ClientException;
import com.huawei.lts.common.config.LtsClientConfig;
import com.huawei.model.LogResult;
import com.huawei.model.client.ClientConfig;
import com.huawei.model.pushLog.LogItems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ClientFactBean {
    private static volatile Client client;

    @Autowired
    private LtsClientConfig ltsClientConfig;

    public ClientFactBean() {
    }

    public Client getLtsLogClient() throws ClientException {
        // 创建链接
        // 初始化链接配置
        if (client == null) {
            synchronized (ClientFactBean.class) {
                if (client == null) {
                    ClientConfig clientConfigInstance = ClientConfig.custom()
                            .setAccessKey(ltsClientConfig.getAccessKey())   // 当前租户的AK（Access Key）
                            .setSecretKey(ltsClientConfig.getSecretKey())  // 当前租户的SK（SecretKey Key）
                            .setProjectId(ltsClientConfig.getProjectId())  // 当前租户的项目ID（Project Id）
                            .setRegion(ltsClientConfig.getRegion())  // 当前租户的区域信息
                            .enableCompression(true)  // 发送日志是否需要压缩（当日志内容过大，请开启true。不设置此参数，默认为false不开启压缩）
                            .builder();
                    client = new Client(clientConfigInstance);
                }
            }
        }
        return client;
    }

    public LogResult sentLog2Lts(LogItems logItems) throws ClientException {
        return getLtsLogClient().pushLog(ltsClientConfig.getGroupId(), ltsClientConfig.getStreamId(), logItems);
    }

    public LogResult sentLog2Lts(String groupId, String streamId, LogItems logItems) throws ClientException {
        return getLtsLogClient().pushLog(groupId, streamId, logItems);
    }
}
