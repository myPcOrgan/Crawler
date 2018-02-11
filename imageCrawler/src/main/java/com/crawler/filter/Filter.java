package com.crawler.filter;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public interface Filter {
    //筛选出是HTTP请求的link
    boolean acceptHttp(String link);
    //筛选出是图片地址的link
    boolean acceptImage(String link);
}
