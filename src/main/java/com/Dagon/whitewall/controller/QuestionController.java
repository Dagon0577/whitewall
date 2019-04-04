package com.Dagon.whitewall.controller;

import com.Dagon.whitewall.async.EventModel;
import com.Dagon.whitewall.async.EventProducer;
import com.Dagon.whitewall.async.EventType;
import com.Dagon.whitewall.model.*;
import com.Dagon.whitewall.service.*;
import com.Dagon.whitewall.util.WhitewallUtil;
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
public class QuestionController {
    private static final Logger logger= LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    FollowService followService;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value = "/question/add",method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,@RequestParam("content")String content){
        try{
            Question question=new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            //question.setCommentCount(0);
            if(hostHolder.getUser()==null) {
                //question.setUserId(WhitewallUtil.ANONYMOUS_USERID);
                return WhitewallUtil.getJSONString(999);
            }else{
                question.setUserId(hostHolder.getUser().getId());
            }
            if(questionService.addQuestion(question)>0){
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                .setActorId(question.getUserId()).setEntityId(question.getId())
                .setExt("title", question.getTitle()).setExt("content", question.getContent()));
                return WhitewallUtil.getJSONString(0);
            }
        }catch (Exception e){
            logger.error("增加问题失败"+e.getMessage());
        }
        return WhitewallUtil.getJSONString(1,"失败");
    }

    @RequestMapping(value = "/question/{qid}")
    public String questionDetail(Model model,@PathVariable("qid")int qid){
        Question question=questionService.getById(qid);
        model.addAttribute("question",question);

        List<Comment> commentList=commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> comments=new ArrayList<>();
        for(Comment comment:commentList){
            ViewObject vo=new ViewObject();
            vo.set("comment",comment);
            if(hostHolder.getUser() == null){
                vo.set("liked", 0);
            }else{
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT,comment.getId()));
            }

            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));
            vo.set("user",userService.getUser(comment.getUserId()));
            comments.add(vo);
        }
        model.addAttribute("comments",comments);

        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }

        return "detail";
    }
}
