package com.wmxyyy.controller;

import com.wmxyyy.rabbitmq.MQSender;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.result.CodeMsg;
import com.wmxyyy.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

	@Autowired
    RedisService redisService;

	@Autowired
    MQSender sender;

/*    //Direct模式
	@RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq() {
		sender.send("hello,imooc");
        return Result.success("Hello，world");
    }
    //Topic模式
    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic() {
        sender.sendTopic("hello,imooc");
        return Result.success("Hello，world");
    }
    //Fanout模式
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout() {
        sender.sendFanout("hello,imooc");
        return Result.success("Hello，world");
    }
    //Header模式
    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header() {
        sender.sendHeader("hello,imooc");
        return Result.success("Hello，world");
    }*/
}
