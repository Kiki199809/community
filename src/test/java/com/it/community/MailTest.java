package com.it.community;

import com.it.community.util.MailClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author: KiKi
 * @date: 2021/9/14 - 16:03
 * @project_name：community
 * @description:
 */

@SpringBootTest(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void test() {
        mailClient.sendMail("qxiao@zju.edu.cn", "spring", "mail");
    }

    @Test
    public void test1() {
        Context context = new Context();
        //存入数据，嵌入模板
        context.setVariable("username", "sunday");

        //利用/mail/demo模板和thymeleaf模板引擎生成html网页（邮件的内容）
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("qxiao@zju.edu.cn", "spring", content);
    }

}
