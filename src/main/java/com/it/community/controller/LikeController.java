package com.it.community.controller;

import com.it.community.annotation.LoginRequired;
import com.it.community.entity.Event;
import com.it.community.entity.User;
import com.it.community.event.EventProducer;
import com.it.community.service.LikeService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.HostHolder;
import com.it.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/9/29 - 20:11
 * @project_name：community
 * @description:
 */

@Controller
public class LikeController implements CommunityConstant {

    @Resource
    private LikeService likeService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 点赞操作
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 该实体点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 本用户对该实体的点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件（此时点赞操作已完成，status=1，取消点赞不需要）
        // postId便于发送通知时生成这个帖子的连接
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            // 发布事件（事件由消息队列处理，不影响当前线程继续执行）
            eventProducer.fireEvent(event);
        }

        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

}
