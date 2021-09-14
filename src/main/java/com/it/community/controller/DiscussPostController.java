package com.it.community.controller;

import com.it.community.entity.DiscussPost;
import com.it.community.service.DiscussPostService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (DiscussPost)表控制层
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */
@RestController
@RequestMapping("discussPost")
public class DiscussPostController {
    /**
     * 服务对象
     */
    @Resource
    private DiscussPostService discussPostService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public DiscussPost selectOne(Integer id) {
        return this.discussPostService.queryById(id);
    }

}