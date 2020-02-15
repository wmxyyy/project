package com.wmxyyy.controller;

import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.redis.GoodsKey;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.result.Result;
import com.wmxyyy.service.GoodsService;
import com.wmxyyy.service.MiaoshaUserService;
import com.wmxyyy.vo.GoodsDetailVo;
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
	 * 商品详情页静态化（html+json调用接口）
	 * @param request
	 * @param response
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
	@RequestMapping(value="/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
										@PathVariable("goodsId")long goodsId) {
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt ) {//秒杀还没开始，倒计时
			miaoshaStatus = 0;
			remainSeconds = (int)((startAt - now )/1000);
		}else  if(now > endAt){//秒杀已经结束
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else {//秒杀进行中
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoods(goods);
		vo.setUser(user);
		vo.setRemainSeconds(remainSeconds);
		vo.setMiaoshaStatus(miaoshaStatus);
		return Result.success(vo);
	}


}
