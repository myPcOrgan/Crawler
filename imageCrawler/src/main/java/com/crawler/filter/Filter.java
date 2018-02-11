package com.crawler.filter;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public interface Filter {
    boolean acceptHttp(String link);
    boolean acceptImage(String link);
}
