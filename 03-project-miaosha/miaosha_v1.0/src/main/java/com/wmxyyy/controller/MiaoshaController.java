package com.wmxyyy.controller;

import com.wmxyyy.access.AccessLimit;
import com.wmxyyy.domain.MiaoshaOrder;
import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.domain.OrderInfo;
import com.wmxyyy.rabbitmq.MQSender;
import com.wmxyyy.rabbitmq.MiaoshaMessage;
import com.wmxyyy.redis.GoodsKey;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.result.CodeMsg;
import com.wmxyyy.result.Result;
import com.wmxyyy.service.GoodsService;
import com.wmxyyy.service.MiaoshaService;
import com.wmxyyy.service.MiaoshaUserService;
import com.wmxyyy.service.OrderService;
import com.wmxyyy.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

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

	@Autowired
	MQSender sender;

	//redis内存标记，秒杀完成后不能访问redis
	private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();

	/**
	 * 系统初始化,预存库存数量到redis
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
		if (goodsVoList == null){
			return;
		}
		for (GoodsVo goods : goodsVoList){
			redisService.set(GoodsKey.getMiaoshaGoodsStock,""+ goods.getId(),goods.getStockCount());
			localOverMap.put(goods.getId(), false);
		}
	}

	/**
	 * 秒杀
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 */
    @RequestMapping(value = "/{path}/do_miaosha" , method=RequestMethod.POST)
	@ResponseBody
    public Result<Integer> list(Model model, MiaoshaUser user,
								  @RequestParam("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}
		//内存标记，减少redis访问
		boolean over = localOverMap.get(goodsId);
		if(over) {
			return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
    	//预减库存
		Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
    	if (stock < 0){
			localOverMap.put(goodsId, true);
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return Result.error(CodeMsg.REPEATE_MIAOSHA);
		}
		//入队
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(mm);
		return Result.success(0);//排队中...

    }

	/**
	 * 秒杀结果
	 * @param model
	 * @param user
	 * @param goodsId
	 * @return
	 * orderId ：成功
	 * 	-1 ：秒杀失败
	 * 	0 ：排队中
	 */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
									  @RequestParam("goodsId")long goodsId) {
		model.addAttribute("user", user);
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result  =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}

	@AccessLimit(seconds=5, maxCount=5, needLogin=true)
	@RequestMapping(value="/path", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
										 @RequestParam("goodsId")long goodsId,
										 @RequestParam(value="verifyCode", defaultValue="0")int verifyCode
	) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
		if(!check) {
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		String path  =miaoshaService.createMiaoshaPath(user, goodsId);
		return Result.success(path);
	}


	@RequestMapping(value="/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
											  @RequestParam("goodsId")long goodsId) {
		if(user == null) {
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		try {
			BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		}catch(Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_FAIL);
		}
	}
}
