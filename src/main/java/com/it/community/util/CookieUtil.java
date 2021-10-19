package com.it.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author: KiKi
 * @date: 2021/9/17 - 15:58
 * @project_name：community
 * @description:
 */
public class CookieUtil {

    /**
     * Description: 从request中获取指定name的cookie的值
     * @param request:
     * @param name:
     * @return java.lang.String:
     */
    public static String getValue(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空！");
        }
        //遍历cookies数组找到对应cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
