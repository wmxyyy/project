package com.wmxyyy.dao;

import com.wmxyyy.domain.MiaoshaOrder;
import com.wmxyyy.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {
	/**
	 * 根据用户id和秒杀商品id查询订单
	 * @param userId
	 * @param goodsId
	 * @return
	 */
	@Select("select * from miaosha_order where user_id=#{userId} and goods_id=#{goodsId}")
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

	/**
	 * 秒杀订单的详情
	 * @param orderInfo
	 * @return
	 */
	@Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
			+ "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
	@SelectKey(keyColumn="id", keyProperty="id", resultType=long.class, before=false, statement="select last_insert_id()")
	public long insert(OrderInfo orderInfo);

	/**
	 * 用户id和商品id生成订单
	 * @param miaoshaOrder
	 * @return
	 */
	@Insert("insert into miaosha_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
	public int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

	/**
	 * 根据订单id查询订单信息
	 * @param orderId
	 * @return
	 */
	@Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId") long orderId);
}
