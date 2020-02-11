package com.wmxyyy.travel.service;

import com.wmxyyy.travel.domain.PageBean;
import com.wmxyyy.travel.domain.Route;

/**
 * 线路Service
 */
public interface RouteService {

    /**
     * 根据类别进行分页查询
     * @param cid
     * @param currentPage
     * @param pageSize
     * @return
     */
    public PageBean<Route> pageQuery(int cid,int currentPage,int pageSize);

}
