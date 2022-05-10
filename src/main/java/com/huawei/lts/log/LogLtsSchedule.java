package com.huawei.lts.log;

import com.huawei.lts.log.lts.LogItemsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class LogLtsSchedule {
    @Autowired
    private LogItemsUtil logItemsUtil;

    @Scheduled(cron = "${thread.check.interval}")
    public void schedulePushLogContent2Lts() {
        logItemsUtil.sentLog2Lts(null, null);
    }
}
