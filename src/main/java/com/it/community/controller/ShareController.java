package com.it.community.controller;

import com.it.community.entity.Event;
import com.it.community.event.EventProducer;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.Keymap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/10/14 - 22:03
 * @project_name：community
 * @description:
 */

@Controller
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Resource
    private EventProducer eventProducer;

    // http://localhost:8080
    @Value("${community.path.domain}")
    private String domain;

    // /community
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    // 给对应的网址生成长图片，返回成功信息和图片链接
    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl) {
        // 随机文件名
        String fileName = CommunityUtil.generateUUID();

        // 生成事件，异步地生成长图片
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回图片访问路径（通过http方式调用）
        Map<String, Object> map = new HashMap<>();
        //map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        // 七牛云空间图片访问路径（异步地生成长图片，所以此时链接不一定生效）
        map.put("shareUrl", shareBucketUrl + "/" + fileName);
        return CommunityUtil.getJSONString(0, null, map);
    }

    // 废弃
    // 通过图片链接获取长图
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String filename, HttpServletResponse response) {

        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }

        // 从本地获取图片，再通过字节流直接向网页输出
        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + filename + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }

    }

}
