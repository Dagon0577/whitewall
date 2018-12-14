package com.Dagon.whitewall.service;

import org.springframework.stereotype.Service;

@Service
public class WhitewallService {
    public String getMessage(int userId){
        return "Hello Message:" + String.valueOf(userId);
    }
}
