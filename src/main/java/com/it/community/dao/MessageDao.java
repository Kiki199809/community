package com.it.community.dao;

import com.it.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (Message)表数据库访问层
 *
 * @author makejava
 * @since 2021-09-27 10:08:45
 */
@Mapper
public interface MessageDao {

    /**
     * Description: 查询某个主题所包含的通知列表
     *
     * @param userId:
     * @param topic:
     * @return java.util.List<com.it.community.entity.Message>:
     */
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

    /**
     * Description: 查询某个主题下最新的通知
     *
     * @param userId:
     * @param topic:
     * @return com.it.community.entity.Message:
     */
    Message selectLatestNotice(int userId, String topic);

    /**
     * Description: 查询某个主题所包含的通知数量
     *
     * @param userId:
     * @param topic:
     * @return int:
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * Description: 查询未读的通知的数量(所有的或者某个主题的)
     *
     * @param userId:
     * @param topic:
     * @return int:
     */
    int selectNoticeUnreadCount(int userId, String topic);

    /**
     * Description: 通过主键批量修改消息的状态（删除和已读）
     *
     * @param ids:
     * @param status:
     * @return int:
     */
    int updateStatusByIds(List<Integer> ids, int status);

    /**
     * Description: 查询当前用户的会话列表，针对每个会话只返回一条最新的私信
     *
     * @param userId:
     * @param offset:
     * @param limit:
     * @return java.util.List<com.it.community.entity.Message>:
     */
    List<Message> selectConversations(int userId, int offset, int limit);

    /**
     * Description: 查询当前用户的会话数量
     *
     * @param userId:
     * @return int:
     */
    int queryConversationCountByUserId(int userId);

    /**
     * Description: 查询某个会话所包含的私信列表
     *
     * @param conversationId:
     * @param offset:
     * @param limit:
     * @return java.util.List<com.it.community.entity.Message>:
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * Description: 查询某个会话所包含的私信数量
     *
     * @param conversationId:
     * @return int:
     */
    int queryLettersCountByConversationId(String conversationId);


    /**
     * Description: 查询未读私信数量(所有的和单个会话的)
     *
     * @param userId:
     * @param conversationId:
     * @return int:
     */
    int queryLettersCountByUserId(int userId, String conversationId);

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Message queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Message> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询（null为查询全部）
     *
     * @param message 实例对象
     * @return 对象列表
     */
    List<Message> queryAll(Message message);

    /**
     * 新增数据
     *
     * @param message 实例对象
     * @return 影响行数
     */
    int insert(Message message);

    /**
     * 修改数据
     *
     * @param message 实例对象
     * @return 影响行数
     */
    int update(Message message);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}