package com.it.community.service;

import com.it.community.entity.LoginTicket;
import com.it.community.entity.User;
import com.it.community.dao.UserDao;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.MailClient;
import com.it.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * (User)表服务实现类
 *
 * @author makejava
 * @since 2021-09-13 15:45:24
 */
@Service("userService")
public class UserService implements  CommunityConstant {
    @Resource
    private UserDao userDao;

    //@Resource
    //private LoginTicketDao loginTicketDao;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private RedisTemplate redisTemplate;


    public User queryByName(String username) {
        return userDao.queryByUsername(username);
    }

    /**
     * Description: 构建用户认证权限
     *
     * @param userId:
     * @return java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>:
     */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {

        User user = this.queryById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }

    /**
     * Description: 修改密码
     *
     * @param userId:
     * @param oldPassword:
     * @param newPassword:
     * @return java.util.Map<java.lang.String, java.lang.Object>:
     */
    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {

        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userDao.queryById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        // 更新密码
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(newPassword + user.getSalt()));
        userDao.update(user);

        return map;
    }

    /**
     * Description: 通过id值更新user数据
     *
     * @param id:
     * @param headerUrl:
     * @return int:
     */
    public int updateHeaderUrlById(int id, String headerUrl) {
        int rows = userDao.updateHeaderUrlById(id, headerUrl);

        // 清除缓存
        clearCache(id);

        return rows;
    }

    /**
     * Description: 通过ticket查询用户凭证信息
     * @param ticket:
     * @return com.it.community.entity.LoginTicket:
     */
    public LoginTicket findLoginTicket(String ticket) {
        //return loginTicketDao.queryByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * Description:  用户登出方法
     * @param ticket:
     * @return void:
     */
    public void logout(String ticket) {
        // 改为无效状态，从Redis中取出修改再存回
        //loginTicketDao.updateStatusByTicket(ticket, 1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * Description: 用户登录方法
     *
     * @param username:
     * @param password:
     * @param expiredSeconds: 过期时间
     * @return java.util.Map<java.lang.String,java.lang.Object>:
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证账号是否存在,是否激活
        User user = userDao.queryByUsername(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活");
            return map;
        }

        // 验证密码
        // 传入的明文密码加密后再比较
        String passowrd = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(passowrd)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        // 登录成功，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        // loginTicketDao.insert(loginTicket);
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // Redis会自动将对象序列化为一个字符串
        redisTemplate.opsForValue().set(ticketKey, loginTicket);


        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    /**
     * Description: 激活用户
     * @param userId: 用户id
     * @param code: 激活码
     * @return int: 激活状态
     */
    public int activation(int userId, String code) {
        User user = userDao.queryById(userId);
        // 如果已激活
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 如果未激活且激活码符合，改变status状态使其激活
            user.setStatus(1);
            userDao.update(user);

            // 清除缓存
            clearCache(userId);

            return ACTIVATION_SUCCESS;
        } else {
            // 未激活成功
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * Description: 用户注册方法
     * @param user:
     * @return java.util.Map<java.lang.String,java.lang.Object>:
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        // 验证账号是否存在
        User user1 = userDao.queryByUsername(user.getUsername());
        if (user1 != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱是否存在
        User user2 = userDao.queryByEmail(user.getEmail());
        if (user2 != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 对密码加密，并注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);    // 普通用户
        user.setStatus(0);  // 未激活
        user.setActivationCode(CommunityUtil.generateUUID());   // 设置激活码
        // 设置随机头像链接，0~999
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date()); // 设置注册时间
        // 此处mybatis获取自动生成的id值并进行回填
        userDao.insert(user);

        // 激活邮件,thymeleaf模板引擎
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/id值/激活码值
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 生成html格式的邮件内容
        String content = templateEngine.process("/mail/activation", context);
        // 发送邮件
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userDao.queryById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        // 一个小时后过期
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }


    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    public User queryById(Integer id) {
        //return this.userDao.queryById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    public List<User> queryAllByLimit(int offset, int limit) {
        return this.userDao.queryAllByLimit(offset, limit);
    }
    
    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param user 实例对象
     * @return 对象列表
     */
    public List<User> queryAll(User user) {
        return this.userDao.queryAll(user);
    }
    
    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    public int insert(User user) {
        return this.userDao.insert(user);
    }

    /**
     * 修改数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    public int update(User user) {
        return this.userDao.update(user);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    public boolean deleteById(Integer id) {
        return this.userDao.deleteById(id) > 0;
    }
}