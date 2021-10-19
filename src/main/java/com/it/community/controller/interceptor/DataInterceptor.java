package com.it.community.controller.interceptor;

import com.it.community.entity.User;
import com.it.community.service.DataService;
import com.it.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: KiKi
 * @date: 2021/10/9 - 15:56
 * @project_name：community
 * @description:
 */

@Component
public class DataInterceptor implements HandlerInterceptor {

    @Resource
    private DataService dataService;

    @Resource
    private HostHolder hostHolder;

    // 在所有请求初，根据IP和当前用户统计UV和DAU
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 统计UV
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);

        // 统计DAU
        User user = hostHolder.getUser();
        if (user != null) {
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}
