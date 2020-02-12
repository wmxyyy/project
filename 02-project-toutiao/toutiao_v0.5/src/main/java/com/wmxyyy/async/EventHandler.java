package com.wmxyyy.async;

import java.util.List;

/**
 * 事件处理适配器
 */
public interface EventHandler {
    void doHandle(EventModel model);//事件处理
    List<EventType> getSupportEventTypes();//处理事件的类型
}
