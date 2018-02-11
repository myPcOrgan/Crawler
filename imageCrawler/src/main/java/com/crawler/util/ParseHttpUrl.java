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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public class ParseHttpUrl {
    public static Set<String> extractLinks(String url, Filter filter) {
        Set<String> links = new HashSet<>();
        links = Collections.synchronizedSet(links);
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

    public static Set<String> extractLinksByJsoup(String url, Filter filter) {
        Set<String> links = new HashSet<>();
        links = Collections.synchronizedSet(links);
        Document document = null;
        try {
            document = Jsoup.connect(url).get();
            Elements elements = document.select("[href]");
            for (Element e : elements) {
                String link = e.attr("href");
                if (link != null && filter.acceptHttp(link)) {
                    links.add(link);
                }
            }
            elements = document.select("[src]");
            for (Element e : elements) {
                String link = e.attr("src");
                if (link != null && filter.acceptHttp(link)) {
                    links.add(link);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return links;
    }
}
