package com.it.community.service.impl;

import com.it.community.entity.User;
import com.it.community.dao.UserDao;
import com.it.community.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (User)表服务实现类
 *
 * @author makejava
 * @since 2021-09-13 15:45:24
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public User queryById(Integer id) {
        return this.userDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<User> queryAllByLimit(int offset, int limit) {
        return this.userDao.queryAllByLimit(offset, limit);
    }
    
    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param user 实例对象
     * @return 对象列表
     */
    @Override
    public List<User> queryAll(User user) {
        return this.userDao.queryAll(user);
    }
    
    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    @Override
    public int insert(User user) {
        return this.userDao.insert(user);
    }

    /**
     * 修改数据
     *
     * @param user 实例对象
     * @return 影响行数
     */
    @Override
    public int update(User user) {
        return this.userDao.update(user);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.userDao.deleteById(id) > 0;
    }
}