package com.wmxyyy.rabbitmq;

import com.wmxyyy.domain.MiaoshaOrder;
import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.service.GoodsService;
import com.wmxyyy.service.MiaoshaService;
import com.wmxyyy.service.OrderService;
import com.wmxyyy.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

	@Autowired
	RedisService redisService;

	@Autowired
	GoodsService goodsService;

	@Autowired
	OrderService orderService;

	@Autowired
	MiaoshaService miaoshaService;

	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

	/**
	 * 真正存入mysql
	 * @param message
	 */
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
	public void receive(String message) {
		log.info("receive message:"+message);
		MiaoshaMessage mm  = RedisService.stringToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();

		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goods.getStockCount();
		if(stock <= 0) {
			return;
		}
		//判断是否已经秒杀到了
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(order != null) {
			return;
		}
		//减库存 下订单 写入秒杀订单
		miaoshaService.miaosha(user, goods);
	}

/*	*//**
	 * Direct模式
	 * @param message
	 *//*
	@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message) {
		log.info("receive message:"+message);

	}

	*//**
	 * Topic模式
	 * @param message
	 *//*
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
	public void receiveTopic1(String message) {
		log.info(" topic  queue1 message:"+message);
	}
	@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String message) {
		log.info(" topic  queue2 message:"+message);
	}

	*//**
	 * Header模式
	 * @param message
	 *//*
	@RabbitListener(queues=MQConfig.HEADER_QUEUE)
	public void receiveHeaderQueue(byte[] message) {
		log.info(" header  queue message:"+new String(message));
	}*/
}
