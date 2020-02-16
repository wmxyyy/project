package com.wmxyyy.service;

import com.wmxyyy.dao.MiaoshaUserDao;
import com.wmxyyy.domain.MiaoshaUser;
import com.wmxyyy.exception.GlobalException;
import com.wmxyyy.redis.MiaoshaUserKey;
import com.wmxyyy.redis.RedisService;
import com.wmxyyy.result.CodeMsg;
import com.wmxyyy.util.MD5Util;
import com.wmxyyy.util.UUIDUtil;
import com.wmxyyy.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {
	public static String COOKI_NAME_TOKEN = "token";

	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;
	
	public MiaoshaUser getById(long id) {
		//return miaoshaUserDao.getById(id);
		//取缓存
		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
		if(user != null) {
			return user;
		}
		//取数据库
		user = miaoshaUserDao.getById(id);
		if(user != null) {
			redisService.set(MiaoshaUserKey.getById, ""+id, user);
		}
		return user;
	}

	/**
	 * 更新密码
	 * 如果数据有更新，缓存也要更新
	 * @param token
	 * @param id
	 * @param formPass
	 * @return
	 * // http://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323
	 */
	public boolean updatePassword(String token, long id, String formPass) {
		//取user
		MiaoshaUser user = getById(id);
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//更新数据库
		MiaoshaUser toBeUpdate = new MiaoshaUser();
		toBeUpdate.setId(id);
		toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
		miaoshaUserDao.update(toBeUpdate);

		//修改密码处理缓存
		redisService.delete(MiaoshaUserKey.getById, ""+id);

		user.setPassword(toBeUpdate.getPassword());
		redisService.set(MiaoshaUserKey.token, token, user);//更新token

		return true;
	}


	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();

		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//密码正确后生成token存储再redis后，作为cookie返回给客户端
		String token = UUIDUtil.uuid();
		addCookie(response,token, user);
		return true;
	}

	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {

		//把SessionID和对应的用户写到redis
		redisService.set(MiaoshaUserKey.token,token,user);
		//服务器通过cookie返回到客户端
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN,token);
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());//过期时间与session一致
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	/**
	 * 从redis获取token
	 * @param response
	 * @param token
	 * @return
	 */
	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		//从redis取出token
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期,重新存储cookie
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}



}
