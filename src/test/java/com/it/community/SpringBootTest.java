package com.it.community;

import com.it.community.entity.DiscussPost;
import com.it.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author: KiKi
 * @date: 2021/10/18 - 21:39
 * @project_name：community
 * @description:
 */

@RunWith(SpringRunner.class)
@org.springframework.boot.test.context.SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTest {

    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    // 一个JUnit4的单元测试用例执行顺序为：
    // @BeforeClass -> @Before -> @Test -> @After -> @AfterClass;
    // 每一个测试方法的调用顺序为：
    // @Before -> @Test -> @After;

    // 针对所有测试，只执行一次，且必须为static void
    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    // 针对所有测试，只执行一次，且必须为static void
    @AfterClass
    public static void afterClass() {
        System.out.println("afterClass");
    }

    // 对于每一个测试方法都要执行一次
    @Before
    public void before() {
        System.out.println("before");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    // 对于每一个测试方法都要执行一次
    @After
    public void after() {
        System.out.println("after");

        // 删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.queryById(data.getId());
        // 断言：用于在代码中捕捉这些假设
        // 传入的object必须不能为空。如果为空就抛出异常
        Assert.assertNotNull(post);
        // 传入的两个值内容相等，不然抛出异常
        Assert.assertEquals(data.getTitle(), post.getTitle());
        Assert.assertEquals(data.getContent(), post.getContent());
    }

    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.00);
        Assert.assertEquals(1, rows);

        DiscussPost post = discussPostService.queryById(data.getId());
        Assert.assertEquals(2000.00, post.getScore(), 2);
    }

}
