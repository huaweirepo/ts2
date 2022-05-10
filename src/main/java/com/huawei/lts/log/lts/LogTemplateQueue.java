package com.huawei.lts.log.lts;

import com.huawei.lts.common.constant.NumberConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Getter
@Setter
public class LogTemplateQueue<T> {
    // 队列大小
    public static final int QUEUE_MAX_SIZE = 1000;
    private int queueMaxSize;
    private BlockingQueue<T> blockingQueue = null;

    private LogTemplateQueue() {
    }

    private LogTemplateQueue(int queueMaxSize) {
        this.queueMaxSize = queueMaxSize < NumberConstant.ONE ? QUEUE_MAX_SIZE : queueMaxSize;
        blockingQueue = new LinkedBlockingQueue<>(queueMaxSize);
    }

    public static <T> LogTemplateQueue<T> getInstance(int queueMaxSize) {
        return new LogTemplateQueue<>(queueMaxSize);
    }

    /**
     * 消息入队
     *
     * @param log
     * @return 成功或失败
     */

    public boolean push(T log) {
        return blockingQueue.add(log);// 队列满了就抛出异常，不阻塞
    }

    /**
     * 消息出队
     *
     * @return 日志内容
     */

    public T poll() {
        return blockingQueue.poll();// 队列为空返回空，不阻塞
    }

    /**
     * 获取队列大小
     *
     * @return int
     */

    public int size() {
        return blockingQueue.size();
    }
}
