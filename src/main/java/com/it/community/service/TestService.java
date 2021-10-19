package com.it.community.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author: KiKi
 * @date: 2021/10/10 - 16:17
 * @project_name：community
 * @description:
 */

@Service
public class TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    // 让该方法在多线程环境下,被异步的调用.
    @Async
    public void execute1() {
        logger.debug("execute1");
    }

    // 定时任务，让该方法在多线程运行环境下,自动周期运行.
    //@Scheduled(initialDelay = 10000, fixedRate = 1000)
    //public void execute2() {
    //    logger.debug("execute2");
    //}
}
