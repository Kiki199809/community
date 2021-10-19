package com.it.community.service;

import com.it.community.entity.Comment;
import com.it.community.dao.CommentDao;
import com.it.community.util.CommunityConstant;
import com.it.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Comment)表服务实现类
 *
 * @author makejava
 * @since 2021-09-22 12:31:23
 */
@Service("commentService")
public class CommentService implements CommunityConstant {

    @Resource
    private CommentDao commentDao;

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private DiscussPostService discussPostService;

    /**
     * Description: 添加评论
     *
     * @param comment:
     * @return int:
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 转义html字符和过滤敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 添加评论
        int rows = commentDao.insert(comment);
        // 更新帖子评论数量（对评论的评论则不增加）
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentDao.queryCountByStatusAndEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCountById(comment.getEntityId(), count);
        }
        return rows;
    }

    /**
     * Description: 通过状态(=0)和Entity查询Comment的数量
     *
     * @param entityType:
     * @param entityId:
     * @return int:
     */
    public int queryCountByStatusAndEntity(int entityType, int entityId) {
        return commentDao.queryCountByStatusAndEntity(entityType, entityId);
    }

    /**
     * Description: 通过状态(=0)和Entity查询Comment
     *
     * @param entityType:
     * @param entityId:
     * @return java.util.List<com.it.community.entity.Comment>:
     */
    public List<Comment> queryByStatusAndEntity(int entityType, int entityId) {
        return commentDao.queryByStatusAndEntity(entityType, entityId);
    }

    /**
     * Description: 通过状态(=0)和Entity查询Comment
     * @param entityType:
     * @param entityId:
     * @param offset:
     * @param limit:
     * @return java.util.List<com.it.community.entity.Comment>:
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentDao.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    public Comment queryById(Integer id) {
        return this.commentDao.queryById(id);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    public List<Comment> queryAllByLimit(int offset, int limit) {
        return this.commentDao.queryAllByLimit(offset, limit);
    }
    
    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param comment 实例对象
     * @return 对象列表
     */
    public List<Comment> queryAll(Comment comment) {
        return this.commentDao.queryAll(comment);
    }


    /**
     * 修改数据
     *
     * @param comment 实例对象
     * @return 影响行数
     */
    public int update(Comment comment) {
        return this.commentDao.update(comment);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    public boolean deleteById(Integer id) {
        return this.commentDao.deleteById(id) > 0;
    }
}