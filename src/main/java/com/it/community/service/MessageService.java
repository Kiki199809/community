package com.it.community.service;

import com.it.community.entity.Message;
import com.it.community.dao.MessageDao;
import com.it.community.util.SensitiveFilter;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Message)表服务实现类
 *
 * @author makejava
 * @since 2021-09-27 10:08:45
 */
@Service
public class MessageService {

    @Resource
    private SensitiveFilter sensitiveFilter;

    @Resource
    private MessageDao messageDao;

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageDao.selectNotices(userId, topic, offset, limit);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return messageDao.selectNoticeUnreadCount(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageDao.selectNoticeCount(userId, topic);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageDao.selectLatestNotice(userId, topic);
    }

    public int readMessage(List<Integer> ids) {
        return messageDao.updateStatusByIds(ids, 1);
    }

    public int insert(Message message) {
        // 转义标签和过滤敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageDao.insert(message);
    }

    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageDao.selectConversations(userId, offset, limit);
    }

    public int queryConversationCountByUserId(int userId) {
        return messageDao.queryConversationCountByUserId(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageDao.selectLetters(conversationId, offset, limit);
    }

    public int queryLettersCountByConversationId(String conversationId) {
        return messageDao.queryLettersCountByConversationId(conversationId);
    }

    public int queryLettersCountByUserId(int userId, String conversationId) {
        return messageDao.queryLettersCountByUserId(userId, conversationId);
    }
}