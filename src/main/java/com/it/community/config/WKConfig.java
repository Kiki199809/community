package com.it.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author: KiKi
 * @date: 2021/10/14 - 21:57
 * @project_name：community
 * @description:
 */

@Configuration
public class WKConfig {

    private static final Logger logger = LoggerFactory.getLogger(WKConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 目的是在项目启动之初就开始执行
    @PostConstruct
    public void init() {
        // 创建WK图片存放目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdirs();
            logger.info("创建WK图片存放目录：" + wkImageStorage);
        }
    }

}
