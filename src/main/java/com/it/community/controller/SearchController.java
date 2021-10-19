package com.it.community.controller;

import com.it.community.entity.DiscussPost;
import com.it.community.service.ElasticsearchService;
import com.it.community.service.LikeService;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/10/7 - 18:25
 * @project_name：community
 * @description:
 */

@Controller
public class SearchController implements CommunityConstant {

    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    // search?keyword=xxx
    @GetMapping("/search")
    public String search(String keyword, Model model, com.it.community.entity.Page page) {

        // 搜索帖子(es的页数从0开始)
        Page<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 帖子作者
                map.put("user", userService.queryById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());

        model.addAttribute("discussPosts", discussPosts);

        return "/site/search";
    }
}
