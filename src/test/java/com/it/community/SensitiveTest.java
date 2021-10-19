package com.it.community;

import com.it.community.util.SensitiveFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: KiKi
 * @date: 2021/9/21 - 11:08
 * @project_name：community
 * @description:
 */

//springboot整合junit，引入starter-test起步依赖
@SpringBootTest(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test1() {
        String text = "扶持赌博吸毒的说法不，是比赌，博fsdfsjn赌+博吸+毒";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
