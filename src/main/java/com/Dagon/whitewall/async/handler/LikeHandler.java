package com.Dagon.whitewall.async.handler;

import com.Dagon.whitewall.async.EventHandler;
import com.Dagon.whitewall.async.EventModel;
import com.Dagon.whitewall.async.EventType;
import com.Dagon.whitewall.model.Message;
import com.Dagon.whitewall.model.User;
import com.Dagon.whitewall.service.MessageService;
import com.Dagon.whitewall.service.UserService;
import com.Dagon.whitewall.util.WhitewallUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WhitewallUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        message.setContent("用户" + user.getName()
            + "赞了你的评论,http://127.0.0.1:8080/question/" + model.getExt("questionId"));

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
