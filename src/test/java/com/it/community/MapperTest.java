package com.it.community;

import com.it.community.entity.DiscussPost;
import com.it.community.entity.User;
import com.it.community.service.DiscussPostService;
import com.it.community.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author: KiKi
 * @date: 2021/9/10 - 21:25
 * @project_name：community
 * @description:
 */

//springboot整合junit，引入starter-test起步依赖
@SpringBootTest(classes = CommunityApplication.class)
public class MapperTest {


    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void testSelect() {
        List<User> userList = userService.queryAll(null);
        userList.forEach(System.out::println);
    }

    @Test
    public void test2() {
        User user = new User();
        user.setId(101);
        List<User> users = userService.queryAll(user);
        users.forEach(System.out::println);
    }

    @Test
    public void test3() {
        List<DiscussPost> discussPosts = discussPostService.queryByUserIdAndStatus(102);
        discussPosts.forEach(System.out::println);
    }

    @Test
    public void test4() {
        User user = userService.queryById(null);
        System.out.println(user);
    }

}
