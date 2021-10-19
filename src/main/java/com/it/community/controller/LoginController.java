package com.it.community.controller;

import com.google.code.kaptcha.Producer;
import com.it.community.entity.User;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: KiKi
 * @date: 2021/9/14 - 16:53
 * @project_name：community
 * @description:
 */

@Controller
public class LoginController implements CommunityConstant {

    //日志对象
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @Resource
    private UserService userService;

    @Resource
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private RedisTemplate redisTemplate;

    // 访问注册页面，目的是不直接在前端暴露页面实际地址
    @GetMapping("/register")
    public String getRegisterPage() {
        // 重定向到templates下的目录文件
        return "/site/register";
    }

    // 访问登录页面
    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        //作为参数的User user会随着注入model并在请求转发时再次被传走

        Map<String, Object> map = userService.register(user);
        // map没有内容，注册成功，注册成功后跳转到中转页面/site/operate-result
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了激活邮件！");
            model.addAttribute("target", "/index"); //中转页面自动跳回首页
            return "/site/operate-result";
        } else {    // 注册失败，跳回注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/id值/激活码值
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            // 激活成功先跳转到中转页面，再跳转到登录页面
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            // 重复激活先跳转到中转页面，再跳转到首页
            model.addAttribute("msg", "重复激活，无效操作！");
            model.addAttribute("target", "/index");
        } else {
            // 激活失败先跳转到中转页面，再跳转到首页
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        // 生成验证码图片
        BufferedImage image = kaptchaProducer.createImage(text);

        // 验证码存入Session以便验证
        //session.setAttribute("kaptcha", text);

        // 验证码的归属owner,作为临时值存入cookie，60s后失效
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis，60s后失效
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器，字节流
        // response由springmvc管理，会自动关闭流
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "PNG", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, /*HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //普通参数String boolean不会被注入model，但是会存在于request域中，请求转发时会再次带上

        // 取出验证码，首先判断验证码是否符合
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        // 检查账号密码
        long expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            //登录成功
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            //把ticket登录凭证设置为cookie，并对整个项目都有效
            cookie.setPath(contextPath);
            cookie.setMaxAge((int) expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            //登陆失败
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        //@CookieValue从Cookie中获取数据传入
        userService.logout(ticket);
        // 清理用户认证的结果
        SecurityContextHolder.clearContext();
        //重定向到登录页面(默认为get请求)
        return "redirect:/login";
    }
}
