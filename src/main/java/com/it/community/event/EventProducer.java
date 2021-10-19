package com.it.community.event;

import com.alibaba.fastjson.JSONObject;
import com.it.community.entity.Event;
import com.it.community.util.CommunityUtil;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: KiKi
 * @date: 2021/10/4 - 17:01
 * @project_name：community
 * @description:
 */

@Component
public class EventProducer {

    @Resource
    private KafkaTemplate kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
