package com.it.community.controller;

import com.it.community.annotation.LoginRequired;
import com.it.community.entity.*;
import com.it.community.event.EventProducer;
import com.it.community.service.CommentService;
import com.it.community.service.DiscussPostService;
import com.it.community.service.LikeService;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.HostHolder;
import com.it.community.util.RedisKeyUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author: KiKi
 * @date: 2021/9/21 - 15:01
 * @project_name：community
 * @description:
 */

@Controller
@RequestMapping("/discuss")
public class DisscussPostController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeService likeService;

    @Resource
    private EventProducer eventProducer;

    @Resource
    private RedisTemplate redisTemplate;

    // 拉黑（删除）
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 从ES中删除，触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);

        // 同步到ES，触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);

        // 同步到ES，触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


    // 帖子详情页面
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        // 帖子
        DiscussPost post = discussPostService.queryById(discussPostId);
        model.addAttribute("post", post);
        // 通过userId查询对应的user数据
        User user = userService.queryById(post.getUserId());
        model.addAttribute("user", user);

        // 帖子的点赞数量和点赞状态
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
            ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 将查出来的Comment与对应的User信息一起封装到Map，再组装成为List
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 此处的user是评论者，前处的user是发帖者
                commentVo.put("user", userService.queryById(comment.getUserId()));

                // 帖子的评论点赞数量和点赞状态
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复(不分页)
                List<Comment> replyList = commentService.queryByStatusAndEntity(ENTITY_TYPE_COMMENT, comment.getId());
                // 将查出来的回复与对应的User信息一起封装到Map，再组装成为List、
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 回复者
                        replyVo.put("user", userService.queryById(reply.getUserId()));
                        // 被回复者
                        User target = reply.getTargetId() == 0 ? null : userService.queryById(reply.getTargetId());
                        replyVo.put("target", target);

                        // 帖子的评论的评论（即回复）点赞数量和点赞状态
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVoList);
                // 回复数量
                int replyCount = commentService.queryCountByStatusAndEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        //将数据存入request域并请求转发
        model.addAttribute("comments", commentVoList);

        // 跳转到帖子详情页面
        return "/site/discuss-detail";
    }

    // 发帖
    @LoginRequired
    @PostMapping("/add")
    @ResponseBody // 异步的更新，返回值为JSON字符串
    public String addDiscussPost(String title, String context) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录！");
        }

        DiscussPost post = new DiscussPost();
        // type、status、commentCount、score默认为0
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(context);
        post.setCreateTime(new Date());
        post.setType(0);
        post.setStatus(0);
        post.setCommentCount(0);
        post.setScore(0.0);
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, post.getId());

        // 报错统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }
}
