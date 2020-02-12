package com.wmxyyy.async;

/**
 * 事件类型
 */
public enum EventType {
    LIKE(0),//点赞
    COMMENT(1),//评论
    LOGIN(2),//登录
    MAIL(3);//邮箱

    private int value;
    EventType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
