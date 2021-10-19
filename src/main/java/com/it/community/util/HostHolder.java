package com.it.community.util;

import com.it.community.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

/**
 * @author: KiKi
 * @date: 2021/9/17 - 16:26
 * @project_name：community
 * @description: 持有用户的信息，用于代替Session对象(以当前线程为key存取数据)
 */

@Component
public class HostHolder {

    // 采用线程隔离的方式存放数据，可以避免多线程之间出现数据访问冲突
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    // 清除数据
    public void clear() {
        users.remove();
    }

}
