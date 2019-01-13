package com.Dagon.whitewall;

import com.Dagon.whitewall.dao.QuestionDAO;
import com.Dagon.whitewall.dao.UserDAO;
import com.Dagon.whitewall.model.EntityType;
import com.Dagon.whitewall.model.Question;
import com.Dagon.whitewall.model.User;
import com.Dagon.whitewall.service.FollowService;
import com.Dagon.whitewall.service.SensitiveService;
import com.Dagon.whitewall.util.JedisAdapter;
import com.Dagon.whitewall.util.WhitewallUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WhitewallApplication.class)
@WebAppConfiguration
@Sql("/init-schema.sql")
public class InitDatabaseTests {

	@Autowired
	UserDAO userDAO;

	@Autowired
	QuestionDAO questionDAO;

	@Autowired
	SensitiveService sensitiveUtil;

	@Autowired
	FollowService followService;

	@Autowired
	JedisAdapter jedisAdapter;

	@Test
	public void initDatabase() {
		Random random=new Random();
		for(int i=0;i<11;i++){
			User user=new User();
			user.setHeadUrl(String.format("http://localhost:8080/images/res/head/%d.jpg",random.nextInt(26)));
			user.setName(String.format("USER%d",i + 1));
			user.setPassword("");
			user.setSalt("");
			userDAO.addUser(user);
			user.setPassword(WhitewallUtil.MD5("xx"+user.getSalt()));
			userDAO.updatePassword(user);

			//互相关注
			for(int j=1; j < i; ++j){
				followService.follow(j, EntityType.ENTITY_USER, i);
			}

			Question question=new Question();
			question.setCommentCount(0);
			Date date=new Date();
			date.setTime(date.getTime() + 1000 * 3600 * i);
			question.setCreatedDate(date);
			question.setUserId(i + 1);
			question.setTitle(String.format("TITLE{%d}",i));
			question.setContent(String.format("Dagon Content %d",i));

			questionDAO.addQuestion(question);
		}

		Assert.assertEquals(WhitewallUtil.MD5("xx"+userDAO.selectById(1).getSalt()),userDAO.selectById(1).getPassword());
		//userDAO.deleteById(1);
		//Assert.assertNull(userDAO.selectById(1));

		//System.out.println(questionDAO.selectLatestQuestions(0,0,10));
	}

}

