package com.it.community.service;

import com.it.community.entity.DiscussPost;
import java.util.List;

/**
 * (DiscussPost)表服务接口
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */
public interface DiscussPostService {

    /**
     * Description: 通过userId查询所有数据（排除拉黑status != 2）
     * @param userId:
     * @return 对象列表
     */
    List<DiscussPost> queryByUserIdAndStatus(Integer userId);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DiscussPost queryById(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<DiscussPost> queryAllByLimit(int offset, int limit);
    
    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param discussPost 实例对象
     * @return 对象列表
     */
    List<DiscussPost> queryAll(DiscussPost discussPost);

    /**
     * 新增数据
     *
     * @param discussPost 实例对象
     * @return 影响行数
     */
    int insert(DiscussPost discussPost);

    /**
     * 修改数据
     *
     * @param discussPost 实例对象
     * @return 影响行数
     */
     int update(DiscussPost discussPost);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}