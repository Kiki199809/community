package com.it.community.controller;

import com.it.community.service.DataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author: KiKi
 * @date: 2021/10/9 - 16:01
 * @project_name：community
 * @description:
 */

@Controller
public class DataController {

    @Resource
    private DataService dataService;

    // 统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    // 统计网站uv独立访客
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {

        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);

        // 请求转发到/data方法，继续处理，返回页面
        // 转发是在一个请求内完成的，所以统计页面方法既有GET也有POST
        return "forward:/data";
    }

    // 统计网站DAU日活跃用户
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {

        long uv = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", uv);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);

        // 请求转发到/data方法，继续处理，返回页面
        // 转发是在一个请求内完成的，所以统计页面方法既有GET也有POST
        return "forward:/data";
    }
}
