package project.app.humanelogistics.preprocessing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import project.app.humanelogistics.model.Media; // Changed from MediaItem to Media
import project.app.humanelogistics.model.News;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoogleNewsCollector implements DataCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int TIMEOUT_MS = 5000;

    @Override
    public List<Media> collect(String query, String startDate, String endDate, int pagesToScrape) {
        List<Media> collectedPosts = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            for (int page = 0; page < pagesToScrape; page++) {
                int start = page * 10;
                String url = buildUrl(encodedQuery, startDate, endDate, start);

                System.out.println("Scraping page " + (page + 1) + "...");

                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .header("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7")
                        .timeout(TIMEOUT_MS)
                        .get();

                List<Media> pagePosts = parseDocument(doc, query);
                collectedPosts.addAll(pagePosts);

                if (page < pagesToScrape - 1) {
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            System.err.println("Error during collection: " + e.getMessage());
        }

        return collectedPosts;
    }

    private String buildUrl(String query, String start, String end, int startIndex) {
        return String.format(
                "https://www.google.com/search?q=%s&tbm=nws&tbs=cdr:1,cd_min:%s,cd_max:%s&hl=vi&gl=VN&start=%d",
                query, start, end, startIndex
        );
    }

    private List<Media> parseDocument(Document doc, String topic) {
        List<Media> posts = new ArrayList<>();

        Elements cards = doc.select("div.SoaBEf, div[role='heading']");
        if (cards.isEmpty()) {
            cards = doc.select("div.g");
        }

        for (Element card : cards) {
            String title = extractText(card, "div[role='heading'], h3");
            String source = extractText(card, ".NUnG9d, .MgUUmf span");
            String dateStr = extractText(card, ".OSrXXb span");
            String link = extractAttr(card, "a", "href");

            if (isValid(title, link)) {
                // Use the News constructor matching your News model
                News newsItem = new News(
                        topic,
                        title,
                        source,
                        new Date(),     // Simplified timestamp
                        link,
                        null            // Sentiment
                );
                posts.add(newsItem);
            }
        }
        return posts;
    }

    private boolean isValid(String title, String link) {
        return !"N/A".equals(title) && !"N/A".equals(link) && !title.isEmpty();
    }

    private String extractText(Element parent, String selector) {
        Element el = parent.selectFirst(selector);
        return (el != null) ? el.text() : "N/A";
    }

    private String extractAttr(Element parent, String selector, String attr) {
        Element el = parent.selectFirst(selector);
        return (el != null) ? el.attr(attr) : "N/A";
    }
}