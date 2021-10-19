package com.it.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: KiKi
 * @date: 2021/10/10 - 15:58
 * @project_name：community
 * @description:
 */

@Configuration
// 使@Schedule定时任务注解功能可用的注解
@EnableScheduling
// 使@Async异步注解功能可用的注解
@EnableAsync
public class ThreadPoolConfig {
}
