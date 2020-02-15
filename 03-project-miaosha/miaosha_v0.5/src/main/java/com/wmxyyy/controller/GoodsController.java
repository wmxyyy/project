package com.wmxyyy.controller;

import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.service.GoodsService;
import com.wmxyyy.service.MiaoshaUserService;
import com.wmxyyy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	MiaoshaUserService userService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	RedisService redisService;

	/**
	 * 商品列表
	 * @param model
	 * @param user
	 * @return
	 */
    @RequestMapping("/to_list")
    public String list(Model model, MiaoshaUser user) {
    	model.addAttribute("user", user);
		//查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		return "goods_list";
    }

	/**
	 * 商品详细信息
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping("/to_detail/{goodsId}")
	public String detail(Model model,MiaoshaUser user,
						 @PathVariable("goodsId")long goodsId) {
		model.addAttribute("user", user);

		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);

		long startAt = goods.getStartDate().getTime();//秒杀开始
		long endAt = goods.getEndDate().getTime();//秒杀结束
		long now = System.currentTimeMillis();//当前时刻

		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);//还剩多少秒
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		return "goods_detail";
	}
}
