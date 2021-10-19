package com.it.community.dao;

import com.it.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (Comment)表数据库访问层
 *
 * @author makejava
 * @since 2021-09-22 12:31:22
 */
@Mapper
public interface CommentDao {

    /**
     * Description: 通过状态(=0)和Entity查询Comment的数量
     *
     * @param entityType:
     * @param entityId:
     * @return int:
     */
    int queryCountByStatusAndEntity(int entityType, int entityId);

    /**
     * Description: 通过状态(=0)和Entity查询Comment
     * @param entityType:
     * @param entityId:
     * @param offset:
     * @param limit:
     * @return java.util.List<com.it.community.entity.Comment>:
     */
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * Description: 通过状态(=0)和Entity查询Comment
     *
     * @param entityType:
     * @param entityId:
     * @return java.util.List<com.it.community.entity.Comment>:
     */
    List<Comment> queryByStatusAndEntity(int entityType, int entityId);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Comment queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Comment> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param comment 实例对象
     * @return 对象列表
     */
    List<Comment> queryAll(Comment comment);

    /**
     * 新增数据
     *
     * @param comment 实例对象
     * @return 影响行数
     */
    int insert(Comment comment);

    /**
     * 修改数据
     *
     * @param comment 实例对象
     * @return 影响行数
     */
    int update(Comment comment);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}