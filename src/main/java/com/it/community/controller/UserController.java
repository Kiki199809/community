package com.it.community.controller;

import com.it.community.annotation.LoginRequired;
import com.it.community.entity.User;
import com.it.community.service.FollowService;
import com.it.community.service.LikeService;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author: KiKi
 * @date: 2021/9/17 - 18:37
 * @project_name：community
 * @description:
 */

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    //日志对象
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload-path}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private LikeService likeService;

    @Resource
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    // 个人主页(任何人都可以访问)
    @GetMapping("/profile/{userId}")
    public String getProfile(@PathVariable("userId") int userId, Model model) {
        User user = userService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 用户信息
        model.addAttribute("user", user);
        // 用户收到的点赞
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注(用户)数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前用户对此用户是否已关注(未登录显示未关注，与ajax修改显示关注或取消关注不同)
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    // 访问用户设置页面，目的是不直接在前端暴露页面实际地址
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model) {

        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息，响应码0代表成功
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传七牛云的凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "site/setting";
    }

    // 更新针对于七牛云的头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }
        // 针对于七牛云的头像路径
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeaderUrlById(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    // 修改密码
    @LoginRequired
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map == null || map.isEmpty()) {
            // 成功修改密码则重定向到登出功能，登出再重定向到登录页面
            return "redirect:/logout";
        } else {
            model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
            // 出现错误，返回设置页面
            return "/site/setting";
        }
    }

    // 废弃
    // 上传并修改用户头像
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 没有成功上传图像
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        // 获取上传文件名的后缀
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        // 把图像写入文件
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }

        // 更新当前用户的头像路径（web路径而不是本地路径）
        // 当前端th:src="http://localhost:8080/community/user/header/xxx.png"
        // 又会调用springmvc映射的方法来向浏览器输出服务器存储的图片
        String headUrl = domain + contextPath + "/user/header/" + filename;
        // 把hostholder当成一个session来使用
        User user = hostHolder.getUser();
        userService.updateHeaderUrlById(user.getId(), headUrl);

        // 更新成功,重定向到首页
        // return “/index”是返回一个模板路径，本次请求没有处理完，
        //      DispatcherServlet会将Model中的数据和对应的模板提交给模板引擎，让它继续处理完这次请求。
        // return "redirect:/index"是重定向，表示本次请求已经处理完毕，但是没有什么合适的数据展现给客户端，
        //      建议客户端再发一次请求，访问"/index"以获得合适的数据。
        return "redirect:/index";
    }

    // 废弃
    // 外界从头像存储位置获取头像（来向浏览器输出服务器存储的图片）
    @GetMapping("header/{filename}")
    public void getHeader(@PathVariable("filename")String fileName, HttpServletResponse response) {
        // 服务器存放图片路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀（去除.）
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);
        // ()自动加上finally，一个输入流一个输出流
        try (
                FileInputStream fis = new FileInputStream(fileName);
        ){
            // springmvc管理会自动关闭
            ServletOutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取图片失败：" + e.getMessage());
        }
    }
}
