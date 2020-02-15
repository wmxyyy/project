package com.wmxyyy.service;

import com.wmxyyy.domain.MiaoshaOrder;
import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.domain.OrderInfo;
import com.wmxyyy.redis.MiaoshaKey;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;

	@Autowired
	RedisService redisService;

	/**
	 * 秒杀
	 * @param user
	 * @param goods
	 * @return
	 */
	@Transactional
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
		//减库存 下订单 写入秒杀订单
		boolean success = goodsService.reduceStock(goods);
		if (success){
			return orderService.createOrder(user, goods);
		}else {
			setGoodsOver(goods.getId());//
			return null;
		}
		//order_info maiosha_order

	}

	/**
	 * 秒杀结果
	 * @param userId
	 * @param goodsId
	 * @return
	 */
	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		if(order != null) {//秒杀成功得到订单号
			return order.getOrderId();
		}else {
			boolean isOver = getGoodsOver(goodsId);//秒杀失败的情况
			if(isOver) {
				return -1;//秒杀失败
			}else {
				return 0;//排队中
			}
		}
	}

	private void setGoodsOver(Long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}

	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);
	}
}
