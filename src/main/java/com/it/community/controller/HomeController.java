package com.it.community.controller;

import com.it.community.entity.DiscussPost;
import com.it.community.entity.Page;
import com.it.community.entity.User;
import com.it.community.service.DiscussPostService;
import com.it.community.service.LikeService;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/9/13 - 19:01
 * @project_name：community
 * @description:
 */

@Controller
public class HomeController implements CommunityConstant {

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    // 拒绝访问时的提示页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

    // 返回错误页面
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    // 返回首页
    @GetMapping("/index")
    public String getIndexPage(Model model, Page page,
                               @RequestParam(value = "orderMode", defaultValue = "0") int orderMode) {
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(null));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService
                .findDiscussPosts(null, page.getOffset(), page.getLimit(), orderMode);
        //将查出来的DiscussPost与对应的User信息一起封装到Map，再组装成为List
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.queryById(post.getUserId());
                map.put("user", user);

                // 查询赞的数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        //将discussPosts数据存入request域并请求转发
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);

        //resources/templates下
        return "/index";
    }
}
