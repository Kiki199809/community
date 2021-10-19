package com.it.community.controller;

import com.it.community.annotation.LoginRequired;
import com.it.community.entity.Comment;
import com.it.community.entity.Event;
import com.it.community.event.EventProducer;
import com.it.community.service.CommentService;
import com.it.community.service.DiscussPostService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.HostHolder;
import com.it.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: KiKi
 * @date: 2021/9/26 - 20:18
 * @project_name：community
 * @description:
 */

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Resource
    private CommentService commentService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private RedisTemplate redisTemplate;

    // 添加评论或回复
    @LoginRequired
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        // 补充comment所缺信息
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        if (comment.getTargetId() == null) {
            comment.setTargetId(0);
        }
        commentService.addComment(comment);

        // 触发评论事件
        // postId便于发送通知时生成这个帖子的连接
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);
        // 触发事件的实体的所属用户
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 如果评论的是帖子
            event.setEntityUserId(discussPostService.queryById(comment.getEntityId()).getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            // 如果评论的是评论
            event.setEntityUserId(commentService.queryById(comment.getEntityId()).getUserId());
        }
        // 发布事件（事件由消息队列处理，不影响当前线程继续执行）
        eventProducer.fireEvent(event);

        // 如果给帖子添加评论则触发发帖(修改)事件
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(comment.getUserId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, discussPostId);
        }

        // 重定向到该帖子详情页面
        return "redirect:/discuss/detail/" + discussPostId;
    }
}
