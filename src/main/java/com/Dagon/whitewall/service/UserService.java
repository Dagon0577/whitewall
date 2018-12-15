package com.Dagon.whitewall.service;

import com.Dagon.whitewall.dao.LoginTicketDAO;
import com.Dagon.whitewall.dao.UserDAO;
import com.Dagon.whitewall.model.LoginTicket;
import com.Dagon.whitewall.model.User;
import com.Dagon.whitewall.util.WhitewallUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LoginTicketDAO loginTicket;

    public Map<String,String> register(String userName, String passWord){
        Map<String,String> map=new HashMap<>();
        if(StringUtils.isBlank(userName)){
            map.put("msg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(passWord)){
            map.put("msg","密码不能为空！");
            return map;
        }
        User user=userDAO.selectByName(userName);
        if(user !=null){
            map.put("msg","用户名已经被注册过！");
            return map;
        }

        user=new User();
        user.setName(userName);
        user.setSalt(UUID.randomUUID().toString().substring(0,10));
        user.setHeadUrl(String.format("../images/res/head/%d.jpg",new Random().nextInt(26)));
        user.setPassword(WhitewallUtil.MD5(passWord+user.getSalt()));
        userDAO.addUser(user);

        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);

        return map;
    }


    public Map<String,String> login(String userName, String passWord){
        Map<String,String> map=new HashMap<>();
        if(StringUtils.isBlank(userName)){
            map.put("msg","用户名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(passWord)){
            map.put("msg","密码不能为空！");
            return map;
        }
        User user=userDAO.selectByName(userName);
        if(user ==null){
            map.put("msg","用户名不存在！");
            return map;
        }
        if (!WhitewallUtil.MD5(passWord+user.getSalt()).equals(user.getPassword())) {
            map.put("msg", "密码错误，请重新输入！");
            return map;
        }
        String ticket=addLoginTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }


    public String addLoginTicket(int userId){
        Date date=new Date();
        LoginTicket ticket=new LoginTicket();
        ticket.setUserId(userId);
        date.setTime(date.getTime()+1000*3600);
        ticket.setExpired(date);
        ticket.setStatus(0);
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicket.addTicket(ticket);
        return ticket.getTicket();
    }

    public void logout(String ticket){
        loginTicket.updateStatus(ticket,1);
    }


    public User getUser(int id){
        return userDAO.selectById(id);
    }
}
