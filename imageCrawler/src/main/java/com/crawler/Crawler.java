package com.crawler;

import com.crawler.filter.Filter;
import com.crawler.util.DownloadUtil;
import com.crawler.util.ParseHttpUrl;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public class Crawler {
    private static final ConcurrentSkipListSet<String> unVisitedUrls = new ConcurrentSkipListSet();
    private static final ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.crawling("http://www.douban.com/group/haixiuzu/discussion?start=0");
//        crawler.crawling("https://m.5173.com");
    }

    private void crawling(String url) {
        Filter filter = new Filter() {
            @Override
            public boolean acceptHttp(String link) {
                if (link.startsWith("http") && !link.endsWith(".js") && !link.endsWith(".css")) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean acceptImage(String link) {
                if (link.startsWith("http") && (link.endsWith(".bmp") || link.endsWith(".gif") || link.endsWith(".png") || link.endsWith(".jpg"))) {
                    return true;
                }
                return false;
            }
        };
        unVisitedUrls.add(url);
        while (unVisitedUrls.size() > 0) {
            String unVisitedUrl = unVisitedUrls.pollFirst();
            try {
                DownloadUtil.downloadImages(unVisitedUrl, filter);
                visitedUrls.add(unVisitedUrl);
            } catch (Exception e) {
                unVisitedUrls.add(unVisitedUrl);
            }
            Set<String> links = ParseHttpUrl.extractLinksByJsoup(unVisitedUrl, filter);
            unVisitedUrls.addAll(links);
        }
    }
}
