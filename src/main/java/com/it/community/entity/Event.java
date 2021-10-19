package com.it.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/10/4 - 16:56
 * @project_name：community
 * @description:
 */

public class Event {

    private String topic;

    /**
     * 事件触发者;
     */
    private int userId;

    private int entityType;

    private int entityId;

    /**
     * 触发的事件实体所属对象;
     */
    private int entityUserId;

    /**
     * 补充信息;
     */
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
