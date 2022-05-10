package com.huawei.lts.log.lts;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huawei.exception.ClientException;
import com.huawei.lts.common.config.LtsClientConfig;
import com.huawei.lts.common.constant.NumberConstant;
import com.huawei.lts.common.utils.CommonUtil;
import com.huawei.model.LogResult;
import com.huawei.model.pushLog.LogContent;
import com.huawei.model.pushLog.LogItem;
import com.huawei.model.pushLog.LogItems;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LogItemsUtil {
    @Autowired
    private ClientFactBean clientFactBean;
    @Autowired
    private LtsClientConfig ltsClientConfig;

    protected static int BATCHSIZE_DEFAULT = 20;
    protected LogTemplateQueue<LogContent> LOG_CONTENT_QUEUE = null;

    @PostConstruct
    public void init() throws InterruptedException {
        LOG_CONTENT_QUEUE = LogTemplateQueue.getInstance(ltsClientConfig.getQueueMaxSize());
    }

    public void sentLog2Lts(String groupId, String streamId) {
        List<LogItems> logItemsList =
                buildLogItemsWrapper(ltsClientConfig.getProjectId(), ltsClientConfig.getClusterId(), ltsClientConfig.getBatchSize());
        if (CollectionUtils.isEmpty(logItemsList)) {
            log.warn("---->未发现日志要上报!");
            return;
        }
        log.info("--->批量写入日志大小：{},写入批次号：{}", ltsClientConfig.getBatchSize(), logItemsList.size());
        try {
            for (LogItems logItems : logItemsList) {
                LogResult logResult = null;
                if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(streamId)) {
                    logResult = clientFactBean.sentLog2Lts(groupId, streamId, logItems);
                    log.info("--->未使用配置日志组和日志流ID,手动指定-->{}-{}", groupId, streamId);
                } else {
                    logResult = clientFactBean.sentLog2Lts(logItems);
                }
                if (!logResult.isSuccess()) {
                    log.error("*******>写入日志failed {}-{}", logResult.getErrorCode(), logResult.getErrorMessage());
                }
            }
            log.info("---->日志写入成功!");
        } catch (ClientException ex) {
            log.error("*******>定时任务写入日志 error", ex);
        }

    }

    private List<LogItems> buildLogItemsWrapper(String projectId, String clusterId, int batchSize) {
        List<LogItems> logItemsList = generateLogItemList(projectId, clusterId, batchSize);
        if (CollectionUtils.isEmpty(logItemsList)) {
            return null;
        }
        return logItemsList;
    }

    private List<LogItems> generateLogItemList(String projectId, String clusterId, int batchSize) {
        List<LogItems> logItemsList = Lists.newArrayList();
        List<LogContent> contents = generateLogContentList(batchSize);
        if (CollectionUtils.isEmpty(contents)) {
            return logItemsList;
        }
        List<List<LogContent>> logContentList = CommonUtil.averageAssign(contents, batchSize);
        for (List<LogContent> logContents : logContentList) {
            LogItems logItems = new LogItems();
            logItems.setLogItems(Lists.newArrayList());

            LogItem logItem = new LogItem();
            logItem.setTenantProjectId(projectId);
            Map<String, String> labels = new HashMap<>();
            labels.put("clusterId", clusterId);
            logItem.setLabels(JSONObject.toJSONString(labels));
            logItem.setContents(logContents);

            logItems.getLogItems().add(logItem);
            logItemsList.add(logItems);
        }
        return logItemsList;
    }

    /**
     * 批量从队列获取指定大小【batchSize】的Log内容
     *
     * @param batchSize
     * @return
     */
    private List<LogContent> generateLogContentList(int batchSize) {
        int batchLength = batchSize < NumberConstant.ZERO ? BATCHSIZE_DEFAULT :
                batchSize;
        List<LogContent> contents = new ArrayList<>();
        if (LOG_CONTENT_QUEUE.size() == 0) {
            return contents;
        }
        if (batchLength > LOG_CONTENT_QUEUE.size()) {
            log.warn("--->不满足日志收集触发条件,LOG_CONTENT_QUEUE.size={},batchSize={}", LOG_CONTENT_QUEUE.size(), batchSize);
            return contents;
        }
        int surplusLogSize = LOG_CONTENT_QUEUE.size() % batchSize;
        for (; LOG_CONTENT_QUEUE.size() > surplusLogSize; ) {
            LogContent logContent = LOG_CONTENT_QUEUE.poll();
            if (logContent == null) {
                continue;
            }
            contents.add(logContent);
        }
        return contents;
    }

    public void pushLogContext(LogContent logContent, String groupId, String streamId) {
        if (LOG_CONTENT_QUEUE.size() + 1 > LOG_CONTENT_QUEUE.getQueueMaxSize()) {
            sentLog2Lts(groupId, streamId);
        }
        LOG_CONTENT_QUEUE.push(logContent);
    }
}
