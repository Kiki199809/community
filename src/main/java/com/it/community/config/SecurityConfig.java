package com.it.community.config;

import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author: KiKi
 * @date: 2021/10/8 - 15:36
 * @project_name：community
 * @description:
 */

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Resource
    private UserService userService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略静态资源的拦截访问
        // ANT风格
        // ? 匹配任何单字符；*匹配0或者任意数量字符；**匹配0或者更多的目录
        web.ignoring().antMatchers("/resources/**");
    }

    // 认证采用自己之前的逻辑，不再重写方法
    // 但是需要构建用户认证的结果，并存入SecurityContext，以便于Security进行授权


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 授权配置（权限和路径的对应关系）
        // 第一个授权是需要登录才能访问的地址
        // 第二个授权是需要版主身份才能置顶加精，第三个授权是需要管理员身份才能删除
        // 禁用csrf
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "user/updatePassword",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/follow/**",
                        "/unfollow/**",
                        "/like"
                ).hasAnyAuthority(AUTHORITY_USER, AUTHORITY_ADMIN, AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                ).hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll()
                .and().csrf().disable();

        // 没有登录和访问权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登录
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String s = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equalsIgnoreCase(s)) {
                            // 如果是异步请求，返回403，交由js解析提示错误消息
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录！"));
                        } else {
                            // 如果是普通的请求，重定向到登录页面
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        String s = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equalsIgnoreCase(s)) {
                            // 如果是异步请求，返回403，交由js解析提示错误消息
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限！"));
                        } else {
                            // 如果是普通的请求，重定向到权限不足页面
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                        }
                    }
                });

        // Security底层默认会拦截/logout请求，进行退出处理
        // 覆盖他默认的逻辑，才能执行自己的退出代码
        http.logout().logoutUrl("/securityLogout");

    }
}
