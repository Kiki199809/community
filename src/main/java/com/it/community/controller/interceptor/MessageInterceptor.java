package com.it.community.controller.interceptor;

import com.it.community.entity.User;
import com.it.community.service.MessageService;
import com.it.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: KiKi
 * @date: 2021/10/6 - 19:43
 * @project_name：community
 * @description:
 */

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        // 已经登录，消息信息通过modelandview携带
        if (user != null && modelAndView != null) {
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            int letterUnreadCount = messageService.queryLettersCountByUserId(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
