package com.Dagon.whitewall.controller;

import com.Dagon.whitewall.async.EventModel;
import com.Dagon.whitewall.async.EventProducer;
import com.Dagon.whitewall.async.EventType;
import com.Dagon.whitewall.model.Comment;
import com.Dagon.whitewall.model.EntityType;
import com.Dagon.whitewall.model.HostHolder;
import com.Dagon.whitewall.service.CommentService;
import com.Dagon.whitewall.service.LikeService;
import com.Dagon.whitewall.util.WhitewallUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {
    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId")int commentId){
        if(hostHolder.getUser() == null){
            return WhitewallUtil.getJSONString(999);
        }


        Comment comment = commentService.getCommentById(commentId);


        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(hostHolder.getUser().getId())
                .setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT)
                .setEntityOwnerId(comment.getUserId())
                .setExt("questionId", String.valueOf(comment.getEntityId())));

        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WhitewallUtil.getJSONString(0, String.valueOf(likeCount));
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId")int commentId){
        if(hostHolder.getUser() == null){
            return WhitewallUtil.getJSONString(999);
        }

        long likeCount = likeService.disLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WhitewallUtil.getJSONString(0, String.valueOf(likeCount));
    }

}
