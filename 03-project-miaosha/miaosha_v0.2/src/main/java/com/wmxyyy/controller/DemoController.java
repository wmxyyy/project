package com.wmxyyy.controller;

import com.wmxyyy.domain.User;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.redis.UserKey;
import com.wmxyyy.result.CodeMsg;
import com.wmxyyy.result.Result;
import com.wmxyyy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {
	@Autowired
	UserService userService;

	@Autowired
	RedisService redisService;

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World!";
	}

	//1.rest api json输出 2.页面
	@RequestMapping("/hello")
	@ResponseBody
	public Result<String> hello() {
		return Result.success("hello,imooc");
	}

	@RequestMapping("/helloError")
	public @ResponseBody Result<String> helloError() {
		return Result.error(CodeMsg.SERVER_ERROR);
	}

	@RequestMapping("/thymeleaf")
	public String  thymeleaf(Model model) {
		model.addAttribute("name", "Joshua");
		return "hello";
	}
	@RequestMapping("/db/get")
	@ResponseBody
	public Result<User>  dbGet() {
		User user = userService.getById(1);
		return Result.success(user);
	}
	@RequestMapping("/db/tx")
	@ResponseBody
	public Result<Boolean>  dbTx() {
		userService.tx();
		return Result.success(true);
	}

	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<User> redisGet() {
		User  user  = redisService.get(UserKey.getById, ""+1, User.class);//""+1是序号 {"id": 1,"name":"1111"}
		return Result.success(user);
	}

	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> redisSet() {
		User user  = new User();
		user.setId(1);
		user.setName("1111");
		redisService.set(UserKey.getById, ""+1, user);//redis数据库的key是 UserKey:id1
		return Result.success(true);
	}
}
