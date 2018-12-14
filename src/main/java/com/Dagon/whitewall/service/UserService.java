package com.Dagon.whitewall.service;

import com.Dagon.whitewall.dao.UserDAO;
import com.Dagon.whitewall.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserDAO userDAO;

    public User getUser(int id){
        return userDAO.selectById(id);
    }
}
