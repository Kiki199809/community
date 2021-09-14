package com.it.community.service.impl;

import com.it.community.entity.DiscussPost;
import com.it.community.dao.DiscussPostDao;
import com.it.community.service.DiscussPostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (DiscussPost)表服务实现类
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */
@Service("discussPostService")
public class DiscussPostServiceImpl implements DiscussPostService {
    @Resource
    private DiscussPostDao discussPostDao;

    /**
     * Description: 通过userId查询所有数据（排除拉黑status != 2）
     * @param userId: null即为查询所有
     * @return 对象列表
     */
    public List<DiscussPost> queryByUserIdAndStatus(Integer userId) {
        return this.discussPostDao.queryByUserIdAndStatus(userId);
    };


    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DiscussPost queryById(Integer id) {
        return this.discussPostDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<DiscussPost> queryAllByLimit(int offset, int limit) {
        return this.discussPostDao.queryAllByLimit(offset, limit);
    }
    
    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param discussPost 实例对象
     * @return 对象列表
     */
    @Override
    public List<DiscussPost> queryAll(DiscussPost discussPost) {
        return this.discussPostDao.queryAll(discussPost);
    }
    
    /**
     * 新增数据
     *
     * @param discussPost 实例对象
     * @return 影响行数
     */
    @Override
    public int insert(DiscussPost discussPost) {
        return this.discussPostDao.insert(discussPost);
    }

    /**
     * 修改数据
     *
     * @param discussPost 实例对象
     * @return 影响行数
     */
    @Override
    public int update(DiscussPost discussPost) {
        return this.discussPostDao.update(discussPost);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.discussPostDao.deleteById(id) > 0;
    }
}