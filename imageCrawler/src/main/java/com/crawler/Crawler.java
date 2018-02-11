package com.crawler;

import com.crawler.filter.Filter;
import com.crawler.util.DownloadUtil;
import com.crawler.util.ParseHttpUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public class Crawler {
    private static final ConcurrentSkipListSet<String> unVisitedUrls = new ConcurrentSkipListSet();
    private static final ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();
    private volatile ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 100, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private static final ThreadLocal<Filter> filter = new ThreadLocal<Filter>() {
        @Override
        protected Filter initialValue() {
            return new Filter() {
                @Override
                public boolean acceptHttp(String link) {
                    if (link.startsWith("http") && !link.endsWith(".js") && !link.endsWith(".css") && link.contains("group")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean acceptImage(String link) {
                    if (link.startsWith("http") && (link.endsWith(".webp") || link.endsWith(".bmp") || link.endsWith(".gif") || link.endsWith(".png") || link.endsWith(".jpg") || link.endsWith(".ico"))) {
                        return true;
                    }
                    return false;
                }
            };
        }
    };

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.crawling("http://www.douban.com/group/haixiuzu/discussion?start=0");
//        crawler.crawling("https://m.5173.com");
//        crawler.crawling("https://weibo.com/doubandushu?is_hot=1");
    }

    private void crawling(String url) {
        unVisitedUrls.add(url);
        while (unVisitedUrls.size() > 0) {
            executor.submit(new ExecuteCrawling());
        }
    }

    class ExecuteCrawling implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            String unVisitedUrl = unVisitedUrls.pollFirst();
            if (StringUtils.isBlank(unVisitedUrl)) {
                Thread.sleep(5L);
            }
            try {
                DownloadUtil.downloadImages(unVisitedUrl, filter.get());
                visitedUrls.add(unVisitedUrl);
            } catch (Exception e) {
                unVisitedUrls.add(unVisitedUrl);
            }
            Set<String> links = ParseHttpUtil.extractLinksByJsoup(unVisitedUrl, filter.get(), "link");
            for (String link : links) {
                if (visitedUrls.contains(link)) {
                    continue;
                }
                unVisitedUrls.add(link);
            }
            return true;
        }
    }
}
