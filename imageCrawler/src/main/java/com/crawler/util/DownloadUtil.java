package com.crawler.util;

import com.crawler.filter.Filter;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * Created by 周黎钢 on 2018/2/9.
 */
public class DownloadUtil {

    public static String getHtml(String url) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url1 = new URL(url);
            URLConnection connection = url1.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }


    public static void downloadImages(String urlpage,Filter filter) {
        Set<String> srcs = ParseHttpUtil.extractLinksByJsoup(urlpage,filter,"image");
        for (String src : srcs) {
            downloadImage(src);
        }
    }
    private static void downloadImage(String src){
        Date date = new Timestamp(System.currentTimeMillis());
        System.out.println(date);
        InputStream is = null;
        FileOutputStream fos = null;
        String imageName = src.substring(src.lastIndexOf("/") + 1, src.length());
        try {
            URL url = new URL(src);
            is = url.openStream();
            //文件目录
            String sc = "d:/src/image/";
            File scFile = new File(sc);
            if (!scFile.exists()) {
                scFile.mkdirs();
            }
            File file = new File(sc + imageName);
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = is.read(buf, 0, buf.length)) != -1) {
                fos.write(buf, 0, length);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(imageName + "下载完成");
    }
}
