package com.wmxyyy.dao;

import com.wmxyyy.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {

	/**
	 * 根据id查询用户
	 * @param id
	 * @return
	 */
	@Select("select * from user where id = #{id}")
	public User getById(@Param("id") int id);

	/**
	 * 添加用户
	 * @param user
	 * @return
	 */
	@Insert("insert into user(id, name)values(#{id}, #{name})")
	public int insert(User user);
	
}
