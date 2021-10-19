package com.it.community.controller.interceptor;

import com.it.community.entity.LoginTicket;
import com.it.community.entity.User;
import com.it.community.service.UserService;
import com.it.community.util.CookieUtil;
import com.it.community.util.HostHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author: KiKi
 * @date: 2021/9/17 - 15:52
 * @project_name：community
 * @description:
 */

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    // 目标方法之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        //如果cookie中有ticket（即有用户登录后留下的信息）
        if (ticket != null) {
            // 根据ticket查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            // 能查询到loginTicket，且状态为有效，且超时时间在当前时间之后
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.queryById(loginTicket.getUserId());
                // 在本次请求中持有用户
                hostHolder.setUser(user);
                // 构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true;
    }

    // 目标方法之后执行(返回modelAndView、模板引擎之前)
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 获取user信息并存入modelAndView以便前端使用
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    // 返回ModelAndView之后（模板引擎之后）执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal中的user信息
        hostHolder.clear();
        // 清理用户认证的结果
        SecurityContextHolder.clearContext();
    }
}
