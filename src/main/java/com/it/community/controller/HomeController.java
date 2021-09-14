package com.it.community.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.it.community.entity.DiscussPost;
import com.it.community.entity.User;
import com.it.community.service.DiscussPostService;
import com.it.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    //@RequestMapping(path = "/index", method = RequestMethod.GET)
    @GetMapping("/index")
    public String getIndexPage(Model model, @RequestParam(value = "pageNum", required = false,
            defaultValue = "1") Integer pageNum) {

        //使用分页助手

        //此语句放在查询之前，查询sql会首先会查询所有的数量SELECT count(0) FROM community.discuss_post WHERE status != 2
        //再根据pageNum和pageSize添加limit条件查询所需信息
        PageHelper.startPage(pageNum, 5);
        List<DiscussPost> list = discussPostService.queryByUserIdAndStatus(null);
        //第一个参数是查询结果list，第二个参数是导航条长度
        PageInfo<DiscussPost> pageInfo = new PageInfo<>(list, 5);
        //将查出来的DiscussPost与对应的User信息一起封装到Map，再组装成为List
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost post : pageInfo.getList()) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                User user = userService.queryById(post.getUserId());
                map.put("user", user);
                discussPosts.add(map);
            }
        }
        //将discussPosts数据存入request域并请求转发
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("url", "/index");
        model.addAttribute("pageInfo", pageInfo);

        return "/index";
    }
}
