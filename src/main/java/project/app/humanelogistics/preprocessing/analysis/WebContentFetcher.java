package project.app.humanelogistics.preprocessing.analysis;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebContentFetcher {

    public String fetchUrlContent(String url) {
        if (url == null || url.isEmpty() || !url.startsWith("http")) return "";
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
            // Simple extraction of paragraph text
            return doc.select("p").text();
        } catch (Exception e) {
            System.err.println("Web Fetch Error (" + url + "): " + e.getMessage());
            return "";
        }
    }
}