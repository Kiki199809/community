package com.it.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.it.community.annotation.LoginRequired;
import com.it.community.entity.Message;
import com.it.community.entity.Page;
import com.it.community.entity.User;
import com.it.community.service.MessageService;
import com.it.community.service.UserService;
import com.it.community.util.CommunityConstant;
import com.it.community.util.CommunityUtil;
import com.it.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author: KiKi
 * @date: 2021/9/27 - 14:50
 * @project_name：community
 * @description:
 */

@Controller
public class MessageController implements CommunityConstant {

    @Resource
    private MessageService messageService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private UserService userService;

    // 显示通知详情
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic") String topic, Model model, Page page) {

        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVOList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 通知的内容，解构通知message的content中信息(对象类转化为JSON字符串会有一些转义字符)
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.queryById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知的作者（System）
                map.put("fromUser", userService.queryById(notice.getFromId()));

                noticeVOList.add(map);
            }
        }

        model.addAttribute("notices", noticeVOList);

        // 设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }

    // 显示系统通知列表
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        // 查询评论类最新一条通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        if (message != null) {
            messageVO.put("message", message);

            // 解构通知message的content中信息(对象类转化为JSON字符串会有一些转义字符)
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.queryById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 查询评论类通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            // 查询评论类未读通知数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);

            model.addAttribute("commentNotice", messageVO);
        }

        // 查询点赞类通知(与评论类类似，复用message和messageVO)
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if (message != null) {
            messageVO.put("message", message);

            // 解构通知message的content中信息
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.queryById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            // 查询评论类通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);
            // 查询评论类未读通知数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);

            model.addAttribute("likeNotice", messageVO);
        }

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if (message != null) {
            messageVO.put("message", message);

            // 解构通知message的content中信息
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user", userService.queryById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            // 关注类没有postId，不用链接到帖子页面

            // 查询评论类通知数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);
            // 查询评论类未读通知数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);

            model.addAttribute("followNotice", messageVO);
        }

        // 查询未读消息数量(私信和系统通知)
        int letterUnreadCount = messageService.queryLettersCountByUserId(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    // 发送私信
    @LoginRequired
    @PostMapping("/letter/send")
    @ResponseBody   // 异步的更新，返回值为JSON字符串
    public String sendLetter(String toName, String content) {

        // 通过toName得到私信对象的id
        User target = userService.queryByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        // 不能自己给自己发私信(此处判断Integer值是否相等)
        if (target.getId().equals(hostHolder.getUser().getId())) {
            return CommunityUtil.getJSONString(2, "不能给自己发私信！");
        }
        // 补充message信息
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        // ConversationId由两者userId拼接，小的在前
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        // 默认未读
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.insert(message);

        return CommunityUtil.getJSONString(0,"发送成功！");
    }

    // 显示私信详情
    @LoginRequired
    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,
                                  Model model,
                                  Page page) {

        User user = hostHolder.getUser();
    // 分页信息
    page.setLimit(5);
    page.setPath("/letter/detail/" + conversationId);
    page.setRows(messageService.queryLettersCountByConversationId(conversationId));

    // 私信列表
    List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
    List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                // 单条私信
                map.put("letter", message);
                // 私信发送方（自己或会话另一方，有来有回）
                map.put("fromUser", userService.queryById(message.getFromId()));

                letters.add(map);
            }
        }
        // 会话的另一方
        model.addAttribute("target", getLetterTarget(conversationId));
        // 将数据存入request域并请求转发
        model.addAttribute("letters", letters);

        // 设置未读消息为已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        // resources/templates下
        return "/site/letter-detail";
    }

    // 返回当前用户为接收方且消息是未读状态的消息id集合
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                // 如果当前用户是消息的接受方且消息是未读状态
                // 此处返回值为Integer，不能直接使用==判断
                if (hostHolder.getUser().getId().equals(message.getToId()) && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    // 从conversationId获取Letter的Target（对方）
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.queryById(id1);
        } else {
            return userService.queryById(id0);
        }
    }

    // 显示私信列表
    @LoginRequired
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {

        User user = hostHolder.getUser();
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.queryConversationCountByUserId(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                // 每个会话只返回一条最新的私信
                map.put("conversation", message);
                // 每个会话的消息数量
                map.put("letterCount", messageService.queryLettersCountByConversationId(message.getConversationId()));
                // 每个会话的未读消息数量
                map.put("unreadCount", messageService.queryLettersCountByUserId(user.getId(), message.getConversationId()));
                // 会话的另一方的信息
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.queryById(targetId));

                conversations.add(map);
            }
        }

        // 查询未读消息数量(私信和系统通知)
        int letterUnreadCount = messageService.queryLettersCountByUserId(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        //将数据存入request域并请求转发
        model.addAttribute("conversations", conversations);

        //resources/templates下
        return "/site/letter";
    }
}
