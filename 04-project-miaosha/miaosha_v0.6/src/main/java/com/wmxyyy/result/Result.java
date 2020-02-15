package com.wmxyyy.result;

/**
 * 结果集
 * @param <T>
 */
public class Result<T> {
	private int code;//状态码
	private String msg;//状态信息
	private T data;//数据

	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
	public T getData() {
		return data;
	}

	private Result(T data) {
		this.code = 0;
		this.msg = "success";
		this.data = data;
	}

	private Result(CodeMsg cm) {
		if(cm != null) {
			this.code = cm.getCode();
			this.msg = cm.getMsg();
		}
	}

	/**
	 * 成功调用
	 * @param data
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> success(T data){
		return new Result<T>(data);
	}

	/**
	 * 失败调用
	 * @param cm
	 * @param <T>
	 * @return
	 */
	public static <T> Result<T> error(CodeMsg cm){
		return new Result<T>(cm);
	}
}
