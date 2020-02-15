package com.wmxyyy.controller;

import com.wmxyyy.domain.MiaoshaOrder;
import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.domain.OrderInfo;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.result.CodeMsg;
import com.wmxyyy.result.Result;
import com.wmxyyy.service.GoodsService;
import com.wmxyyy.service.MiaoshaService;
import com.wmxyyy.service.MiaoshaUserService;
import com.wmxyyy.service.OrderService;
import com.wmxyyy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {

	@Autowired
	MiaoshaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
    @RequestMapping(value = "/do_miaosha" , method=RequestMethod.POST)
	@ResponseBody
    public Result<OrderInfo> list(Model model, MiaoshaUser user,
								  @RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
    	//判断库存情况
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//获取该商品的信息对象
    	int stock = goods.getStockCount();

    	if(stock <= 0) {
    		model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
			return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//在订单生成来判断是否已经秒杀到
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);//数据库索引一个用户只能秒杀一个
    	if(order != null) {
    		model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}

    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);//事务

		return Result.success(orderInfo);
    }
}
