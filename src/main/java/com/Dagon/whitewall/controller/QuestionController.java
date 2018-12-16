package com.Dagon.whitewall.controller;

import com.Dagon.whitewall.model.HostHolder;
import com.Dagon.whitewall.model.Question;
import com.Dagon.whitewall.service.QuestionService;
import com.Dagon.whitewall.service.UserService;
import com.Dagon.whitewall.util.WhitewallUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class QuestionController {
    private static final Logger logger= LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @RequestMapping(value = "/question/add",method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,@RequestParam("content")String content){
        try{
            Question question=new Question();
            question.setContent(content);
            question.setTitle(title);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);
            if(hostHolder.getUser()==null) {
                //question.setUserId(WhitewallUtil.ANONYMOUS_USERID);
                return WhitewallUtil.getJSONString(999);
            }else{
                question.setUserId(hostHolder.getUser().getId());
            }
            if(questionService.addQuestion(question)>0){
                return WhitewallUtil.getJSONString(0);
            };
        }catch (Exception e){
            logger.error("增加问题失败"+e.getMessage());
        }
        return WhitewallUtil.getJSONString(1,"失败");
    }

    @RequestMapping(value = "/question/{qid}")
    public String questionDetail(Model model,@PathVariable("qid")int qid){
        Question question=questionService.selectById(qid);
        model.addAttribute("question",question);
        model.addAttribute("user",userService.getUser(question.getUserId()));
        return "detail";
    }
}
