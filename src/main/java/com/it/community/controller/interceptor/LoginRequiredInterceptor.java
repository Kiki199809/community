package com.it.community.controller.interceptor;

import com.it.community.annotation.LoginRequired;
import com.it.community.util.HostHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author: KiKi
 * @date: 2021/9/18 - 19:50
 * @project_name：community
 * @description:
 */

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Resource
    private HostHolder hostHolder;

    // 在请求初判断是否登录
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果拦截到的是一个方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法对象
            Method method = handlerMethod.getMethod();
            // 取方法的@LoginRequired注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 如果当前方法需要登录且你未登录，拒绝请求，重定向到登录页面
                //response.sendRedirect(request.getContextPath() + "/login");

                // 如果是 ajax 请求响应头会有，x-requested-with
                // 如果是 ajax 请求，则设置 "REDIRECT" 标识 、CONTEXTPATH 的路径值
                if (request.getHeader("x-requested-with") != null && "XMLHttpRequest".equalsIgnoreCase(request.getHeader("x-requested-with"))) {

                    // 自定义响应头 告诉ajax我是重定向，用ajax进行判断。
                    response.setHeader("REDIRECT", "REDIRECT");
                    // 自定义响应头 告诉ajax我重定向的路径
                    response.setHeader("CONTEXTPATH", request.getContextPath() + "/login");
                    // 设置这次请求状态码为403 禁止访问
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    // 如果不是 ajax 请求，则直接跳转即可
                    response.sendRedirect(request.getContextPath() + "/login");
                }

                return false;
            }
        }
        return true;
    }

}
