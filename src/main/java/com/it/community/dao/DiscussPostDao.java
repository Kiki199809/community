package com.it.community.dao;

import com.it.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DiscussPost)表数据库访问层
 *
 * @author makejava
 * @since 2021-09-13 15:54:54
 */
@Mapper
public interface DiscussPostDao {

    /**
     * Description: 修改帖子类型
     *
     * @param id:
     * @param type:
     * @return int:
     */
    int updateType(int id, int type);

    /**
     * Description: 修改帖子状态
     *
     * @param id:
     * @param status:
     * @return int:
     */
    int updateStatus(int id, int status);

    /**
     * Description: 修改帖子分数
     *
     * @param id:
     * @param score:
     * @return int:
     */
    int updateScore(int id, double score);

    /**
     * 通过id修改帖子评论数量
     *
     * @param id 帖子id
     * @param commentCount 贴子评论数量
     * @return 影响行数
     */
    int updateCommentCountById(int id, int commentCount);

    /**
     * Description: 通过userId查询所有数据（排除拉黑status != 2）
     * @param userId:
     * @param orderMode: 0-普通排序（置顶，时间） 1-热帖排序（置顶，分数，时间）
     * @return 对象列表
     */
    List<DiscussPost> queryByUserIdAndStatus(Integer userId, int orderMode);

    /**
     * Description: 通过userId查询所有数据（排除拉黑status != 2）
     * @param userId:
     * @param offset:
     * @param limit:
     * @param orderMode: 0-普通排序（置顶，时间） 1-热帖排序（置顶，分数，时间）
     * @return java.util.List<com.it.community.entity.DiscussPost>:
     */
    List<DiscussPost> selectDiscussPosts(Integer userId, int offset, int limit, int orderMode);


    /**
     * Description: 通过userId查询所有数据总数（排除拉黑status != 2）
     *
     * @param userId:
     * @return int:
     */
    int selectDiscussPostRows(Integer userId);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DiscussPost queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<DiscussPost> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
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
     * @return 影响行数
     */
    int deleteById(Integer id);

}