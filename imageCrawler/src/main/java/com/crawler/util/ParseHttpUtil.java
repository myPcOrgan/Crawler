package com.crawler.util;

import com.crawler.filter.Filter;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public class ParseHttpUtil {
    public static Set<String> extractLinks(String url, Filter filter) {
        Set<String> links = new HashSet<>();
        try {
            Parser parser = new Parser(url);
            NodeFilter nodeFilter = new NodeFilter() {
                @Override
                public boolean accept(Node node) {
                    if (node.getText().startsWith("frame src=")) {
                        return true;
                    }
                    return false;
                }
            };
            //过滤<a>和<frame>标签
            OrFilter orFilter = new OrFilter(new NodeClassFilter(LinkTag.class), nodeFilter);
            NodeList nodeList = parser.extractAllNodesThatMatch(orFilter);
            for (int i = 0; i < nodeList.size(); i++) {
                Node node = nodeList.elementAt(i);
                //要么是<a>标签，要么是<frame>标签
                if (node instanceof LinkTag) {
                    LinkTag tag = (LinkTag) node;
                    String link = tag.getLink();
                    if (filter.acceptHttp(link)) {
                        links.add(link);
                    }
                } else {
                    // 提取 frame 里 src 属性的链接如 <frame src="test.html"/>
                    String frame = node.getText();
                    frame = frame.substring(frame.indexOf("src="));
                    int end = frame.indexOf(" ");
                    if (end == -1) {
                        end = frame.indexOf(">");
                    }
                    frame = frame.substring(5, end - 1);
                    if (filter.acceptHttp(frame)) {
                        links.add(frame);
                    }
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return links;
    }

    /**
     * 根据需求筛选出页面或者图片的地址集合
     *
     * @param url
     * @param filter
     * @param need
     * @return
     */
    public static Set<String> extractLinksByJsoup(String url, Filter filter, String need) {
        Set<String> links = new HashSet<>();
        Set<String> imageLinks = new HashSet<>();
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .timeout(600000)
                    .get();
            ;
            //获取带有href属性的a元素
//            document = Jsoup.parse(html, "", org.jsoup.parser.Parser.htmlParser());
            Elements elements = document.select("a[href]");
            for (Element e : elements) {
                String link = e.attr("href");
                if (link != null && filter.acceptImage(link)) {
                    imageLinks.add(link);
                } else if (link != null && filter.acceptHttp(link)) {
                    links.add(link);
                }
            }
            //获取带有src属性的img元素
//            elements = document.select("img[src]");
            elements=document.getElementsByTag("img");
            for (Element e : elements) {
                String link = e.attr("src");
                if (link != null && filter.acceptImage(link)) {
                    imageLinks.add(link);
                } else if (link != null && filter.acceptHttp(link)) {
                    links.add(link);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (need.equals("image")) {
            return imageLinks;
        } else {
            return links;
        }
    }
}
