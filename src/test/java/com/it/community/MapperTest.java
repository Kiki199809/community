package com.it.community;

import com.it.community.dao.LoginTicketDao;
import com.it.community.entity.DiscussPost;
import com.it.community.entity.LoginTicket;
import com.it.community.entity.User;
import com.it.community.service.DiscussPostService;
import com.it.community.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * @author: KiKi
 * @date: 2021/9/10 - 21:25
 * @project_name：community
 * @description:
 */

//springboot整合junit，引入starter-test起步依赖
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {


    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LoginTicketDao loginTicketDao;

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
    public void test4() {
        User user = userService.queryById(null);
        System.out.println(user);
    }

    @Test
    public void test5() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(10);
        loginTicket.setTicket("fsdfsdf");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketDao.insert(loginTicket);
    }

    @Test
    public void test6() {
        LoginTicket loginTicket = loginTicketDao.queryByTicket("fsdfsdf");
        System.out.println(loginTicket);

        loginTicketDao.updateStatusByTicket("fsdfsdf", 1);
    }

    @Test
    public void test() {
        merge(new int[]{1,2,3,0,0,0}, 3, new int[]{2,5,6}, 3);
    }

    public void merge(int[] nums1, int m, int[] nums2, int n) {
        if(n == 0) return;
        int[] temp = new int[m + n];
        int j = 0, k = 0;
        for(int i = 0; i < m + n; i++) {
            if(nums1[j] < nums2[k]) {
                temp[i] = nums1[j];
                j++;
            } else {
                temp[i] = nums2[k];
                k++;
            }
        }
        for(int i = 0; i < m + n; i++) {
            nums1[i] = temp[i];
        }
    }
}
