package com.knubisoft.utils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class PageLinkCollector implements Callable<Map<String, Integer>> {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    private final String link;

    @Override
    public Map<String, Integer> call() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        try {
            Document doc = Jsoup.connect(link).get();
            for (Element linkTag : doc.select("a[href]")) {
                String url = linkTag.absUrl("href");
                int status = getStatusCode(url);
                result.put(url, status);

                System.out.println(url + (status >= 200 && status < 300 ? " >>> VALID " : " >>> " + status));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    @SneakyThrows
    private int getStatusCode(String url) {
        Response response = null;
        try {
            Request request = new Request.Builder().url(url).get().build();
            response = CLIENT.newCall(request).execute();
            return response.code();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
    }
}