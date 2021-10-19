package com.it.community.controller;

import com.it.community.annotation.LoginRequired;
import com.it.community.entity.Event;
import com.it.community.entity.User;
import com.it.community.event.EventProducer;
import com.it.community.service.FollowService;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/10/2 - 19:29
 * @project_name：community
 * @description:
 */

@Controller
public class FollowController implements CommunityConstant {

    @Resource
    private FollowService followService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    @Resource
    private EventProducer eventProducer;

    // 用户关注者页面
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, com.it.community.entity.Page page) {
        User user = userService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        // 分页信息
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> list = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (list != null) {
            for (Map<String, Object> map : list) {
                User u = (User) map.get("user");
                // 增加当前登录用户是否关注此用户的信息
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("followees", list);

        return "/site/followee";
    }

    // 用户粉丝页面
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, com.it.community.entity.Page page) {
        User user = userService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        // 分页信息
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> list = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (list != null) {
            for (Map<String, Object> map : list) {
                User u = (User) map.get("user");
                // 增加当前登录用户是否关注此用户的信息
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }

        model.addAttribute("followers", list);

        return "/site/follower";
    }

    // 判断当前登录用户是否关注此用户
    public boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

    // 关注
    @LoginRequired
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件（目前只能关注用户）
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    // 取消关注
    @LoginRequired
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注！");
    }
}
