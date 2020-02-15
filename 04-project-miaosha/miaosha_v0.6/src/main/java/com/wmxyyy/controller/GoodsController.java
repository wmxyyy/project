package com.wmxyyy.controller;

import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.redis.GoodsKey;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.service.GoodsService;
import com.wmxyyy.service.MiaoshaUserService;
import com.wmxyyy.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;

	@Autowired
	ApplicationContext applicationContext;
	/**
	 * 商品列表（没有优化之前）
	 * 	 QPS:1267 load:15 mysql
	 * 	 5000 * 10
	 *
	 * 	 缓存之后
	 * 	 QPS:2884, load:5
	 *
	 * 	 页面缓存60秒
	 * @param model
	 * @param user
	 * @return
	 */
    @RequestMapping(value = "/to_list",produces="text/html")
	@ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {
    	model.addAttribute("user", user);
    	//取缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if (!StringUtils.isEmpty(html)){
			return html;
		}
		//从mysql查询商品列表
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		//业务逻辑数据
		IWebContext ctx = new WebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap());
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if(!StringUtils.isEmpty(html)) {
			//存储到redis做缓存
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
    }

	/**
	 * 商品详细信息
	 *
	 * URL缓存，不同URL页面就会有不同缓存（也是一种页面缓存）
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value = "/to_detail/{goodsId}",produces="text/html")
	public String detail(HttpServletRequest request, HttpServletResponse response,
						 Model model,MiaoshaUser user,
						 @PathVariable("goodsId")long goodsId) {
		model.addAttribute("user", user);

		//取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		//从mysql查询商品详情内容缓存到redis
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

		//业务逻辑数据
		IWebContext ctx = new WebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap());
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
	}
}
