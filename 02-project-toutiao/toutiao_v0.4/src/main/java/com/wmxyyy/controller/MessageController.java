package com.wmxyyy.controller;

import com.wmxyyy.util.ToutiaoUtil;
import com.wmxyyy.model.HostHolder;
import com.wmxyyy.model.Message;
import com.wmxyyy.model.User;
import com.wmxyyy.model.ViewObject;
import com.wmxyyy.service.MessageService;
import com.wmxyyy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    //两个用户的消息记录
    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model, @RequestParam("conversationId") String conversationId) {
        try {
            List<ViewObject> messages = new ArrayList<>();
            List<Message> conversationList = messageService.getConversationDetail(conversationId, 0, 10);
            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message", msg);
                User user = userService.getUser(msg.getFromId());
                if (user == null) {
                    continue;
                }
                vo.set("headUrl", user.getHeadUrl());
                vo.set("userName", user.getName());
                messages.add(vo);
            }
            model.addAttribute("messages", messages);
            return "letterDetail";
        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letterDetail";
    }

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String conversationList(Model model) {
        try {
            int localUserId = hostHolder.getUser().getId();//获取登录的用户id
            List<ViewObject> conversations = new ArrayList<>();
            List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("conversation", msg);
                int targetId = msg.getFromId() == localUserId ? msg.getToId() : msg.getFromId();
                User user = userService.getUser(targetId);
                vo.set("headUrl", user.getHeadUrl());
                vo.set("userName", user.getName());
                vo.set("targetId", targetId);
                vo.set("totalCount", msg.getId());
                vo.set("unreadCount", messageService.getUnreadCount(localUserId, msg.getConversationId()));
                conversations.add(vo);
            }
            model.addAttribute("conversations", conversations);
            return "letter";
        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letter";
    }

    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId") int fromId,
                                   @RequestParam("toId") int toId,
                                   @RequestParam("content") String content) {
        Message msg = new Message();
        msg.setContent(content);
        msg.setCreatedDate(new Date());
        msg.setToId(toId);
        msg.setFromId(fromId);
        //小数在前面，大数在后面
        msg.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) :
                String.format("%d_%d", toId, fromId));

        messageService.addMessage(msg);
        return ToutiaoUtil.getJSONString(msg.getId());
    }
}
