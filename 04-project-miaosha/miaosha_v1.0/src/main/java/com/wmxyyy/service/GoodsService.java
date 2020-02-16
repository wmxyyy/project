package com.wmxyyy.service;

import com.wmxyyy.dao.GoodsDao;
import com.wmxyyy.domain.MiaoshaGoods;
import com.wmxyyy.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
	
	@Autowired
	GoodsDao goodsDao;

	/**
	 * 商品列表
	 * @return
	 */
	public List<GoodsVo> listGoodsVo(){
		return goodsDao.listGoodsVo();
	}

	/**
	 * 根据id查询商品信息
	 * @param goodsId
	 * @return
	 */
	public GoodsVo getGoodsVoByGoodsId(long goodsId) {
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	/**
	 * 库存减一
	 * @param goods
	 */
	public boolean reduceStock(GoodsVo goods) {
		MiaoshaGoods g = new MiaoshaGoods();
		g.setGoodsId(goods.getId());
		int ret = goodsDao.reduceStock(g);
		return ret > 0;
	}

}
