package com.wmxyyy.service;

import com.wmxyyy.dao.OrderDao;
import com.wmxyyy.domain.MiaoshaOrder;
import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.domain.OrderInfo;
import com.wmxyyy.redis.OrderKey;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;

	@Autowired
	RedisService redisService;

	/**
	 * redid缓存优化
	 * 判断库存是否存在该用户id和秒杀商品id的订单
	 * @param userId
	 * @param goodsId
	 * @return
	 */
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
		//return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		//从redis取订单信息
		return redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+userId+"_"+goodsId, MiaoshaOrder.class);
	}

	/**
	 * 生成订单
	 * @param user
	 * @param goods
	 * @return
	 */
	@Transactional
	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goods.getId());
		orderInfo.setGoodsName(goods.getGoodsName());
		orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		orderInfo.setUserId(user.getId());
		long orderId = orderDao.insert(orderInfo);//用户id和商品id的订单的订单

		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goods.getId());
		miaoshaOrder.setOrderId(orderId);
		miaoshaOrder.setUserId(user.getId());
		//存入mysql
		orderDao.insertMiaoshaOrder(miaoshaOrder);//用户id和秒杀商品id的订单
		//存入redis
		redisService.set(OrderKey.getMiaoshaOrderByUidGid, ""+user.getId()+"_"+goods.getId(), miaoshaOrder);
		return orderInfo;
	}

	/**
	 * 根据订单号查询订单
	 * @param orderId
	 * @return
	 */
	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}

}
