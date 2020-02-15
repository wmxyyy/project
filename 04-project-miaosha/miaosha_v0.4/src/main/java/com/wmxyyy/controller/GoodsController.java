package com.wmxyyy.controller;

import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
    @RequestMapping("/to_list")
    public String list(Model model, MiaoshaUser user) {
    	model.addAttribute("user", user);
        return "goods_list";
    }
    
}
