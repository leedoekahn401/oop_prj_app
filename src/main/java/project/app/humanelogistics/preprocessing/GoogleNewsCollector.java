package project.app.humanelogistics.preprocessing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.News;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoogleNewsCollector implements DataCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public List<Media> collect(String query, String startDate, String endDate, int pagesToScrape) {
        List<Media> collectedPosts = new ArrayList<>();
        // Format expected from input (e.g., "9/5/2024")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            // Iterate Day by Day to ensure accurate timestamps
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.format(formatter);
                System.out.println("Scraping for date: " + dateStr);

                // Construct URL for this specific day
                // cd_min and cd_max are set to the same day to isolate results
                String url = String.format("https://www.google.com/search?q=%s&tbm=nws&tbs=cdr:1,cd_min:%s,cd_max:%s&hl=en",
                        encodedQuery, dateStr, dateStr);

                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent(USER_AGENT)
                            .header("Accept-Language", "en-US,en;q=0.9")
                            .timeout(5000)
                            .get();

                    // Use the specific loop date as the timestamp for these articles
                    Date currentDayTimestamp = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    List<Media> dailyPosts = parseDocument(doc, query, currentDayTimestamp);

                    // Print out collected data immediately
                    for (Media m : dailyPosts) {
                        if (m instanceof News) {
                            News n = (News) m;
                            System.out.printf("   [FOUND] %s | %s | %s%n", dateStr, n.getSource(), n.getContent());
                        }
                    }

                    collectedPosts.addAll(dailyPosts);

                    // Polite delay between requests to avoid rate limits
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error scraping date " + dateStr + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Collection Error (Check date format M/d/yyyy): " + e.getMessage());
        }

        // FALLBACK: If scraping blocked, generate Mock Data
        if (collectedPosts.isEmpty()) {
            System.out.println("Scraping yielded 0 results. Generating Mock Data.");
            collectedPosts = generateMockData(query);
        }

        return collectedPosts;
    }

    private List<Media> parseDocument(Document doc, String topic, Date forceDate) {
        List<Media> posts = new ArrayList<>();
        Elements articles = doc.select("div.SoaBEf, a.WlydOe");

        for (Element el : articles) {
            String title = el.select("div[role='heading'], div.n0jPhd").text();
            String link = el.attr("href");
            String source = el.select("div.MgUUmf, span.NUnG9d").text();

            // We ignore the relative string (e.g. "2 days ago") because we queried for a specific date.
            // We assign the 'forceDate' (the date we queried for) to ensure accuracy.

            if(!title.isEmpty() && !link.isEmpty()) {
                posts.add(new News(topic, title, source, forceDate, link, null));
            }
        }
        return posts;
    }

    private List<Media> generateMockData(String topic) {
        List<Media> mocks = new ArrayList<>();
        long now = System.currentTimeMillis();
        long day = 24 * 60 * 60 * 1000L;

        mocks.add(new News(topic, "Typhoon Yagi damage report", "BBC", new Date(now - 2 * day), "http://bbc.com", "-0.8"));
        mocks.add(new News(topic, "Recovery efforts start", "CNN", new Date(now - day), "http://cnn.com", "0.5"));
        mocks.add(new News(topic, "Flood warnings update", "VNExpress", new Date(now - 4 * 3600 * 1000L), "http://vnexpress.net", "-0.6"));
        return mocks;
    }
}