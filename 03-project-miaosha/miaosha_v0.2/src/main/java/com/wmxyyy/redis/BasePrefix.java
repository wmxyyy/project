package com.wmxyyy.redis;

public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds;
	
	private String prefix;

	public int expireSeconds() {
		return expireSeconds;
	}

	/**
	 * 前缀
	 * @return
	 */
	public String getPrefix() {
		String className = getClass().getSimpleName();
		return className+":" + prefix;//UserKey:id
	}

	/**
	 * 构造函数
	 * @param prefix
	 */
	public BasePrefix(String prefix) {//0代表永不过期
		this(0, prefix);
	}

	/**
	 * 构造函数
	 * @param expireSeconds
	 * @param prefix
	 */
	public BasePrefix(int expireSeconds, String prefix) {
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	



}
