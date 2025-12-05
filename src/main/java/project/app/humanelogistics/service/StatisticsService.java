package project.app.humanelogistics.service;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class StatisticsService {

    private final Map<String, MediaRepository> repoMap = new LinkedHashMap<>();
    private final SummaryGenerator summaryGenerator;

    public StatisticsService(SummaryGenerator summaryGenerator) {
        this.summaryGenerator = summaryGenerator;
    }

    public void addRepository(String label, MediaRepository repo) {
        this.repoMap.put(label, repo);
    }

    public int getTotalPostCount(String topic) {
        int total = 0;
        for (MediaRepository repo : repoMap.values()) {
            total += repo.findByTopic(topic).size();
        }
        return total;
    }

    public double getOverallAverageScore(String topic) {
        double totalScore = 0.0;
        int count = 0;
        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            for (Media post : posts) {
                if (post.getSentiment().getValue() != 0.0) {
                    totalScore += post.getSentiment().getValue();
                    count++;
                }
            }
        }
        return count == 0 ? 0.0 : totalScore / count;
    }

    public TimeSeriesCollection getSentimentData(String topic, int targetYear) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (Map.Entry<String, MediaRepository> entry : repoMap.entrySet()) {
            String seriesName = entry.getKey();
            MediaRepository repo = entry.getValue();
            List<Media> posts = repo.findByTopic(topic);
            Map<LocalDate, Double> dailyTotal = new TreeMap<>();
            Map<LocalDate, Integer> dailyCount = new HashMap<>();
            boolean hasData = false;

            for (Media post : posts) {
                if (post.getTimestamp() == null || post.getSentiment().getValue() == 0.0) continue;
                LocalDate localDate = post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (targetYear != 0 && localDate.getYear() != targetYear) continue;

                dailyTotal.put(localDate, dailyTotal.getOrDefault(localDate, 0.0) + post.getSentiment().getValue());
                dailyCount.put(localDate, dailyCount.getOrDefault(localDate, 0) + 1);
                hasData = true;
            }

            TimeSeries series = new TimeSeries(seriesName);
            for (LocalDate date : dailyTotal.keySet()) {
                double avg = dailyTotal.get(date) / dailyCount.get(date);
                series.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), avg);
            }
            if (hasData) dataset.addSeries(series);
        }
        return dataset;
    }

    public DefaultCategoryDataset getDamageData(String topic) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<DamageCategory, Integer> counts = new HashMap<>();

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            for (Media post : posts) {
                DamageCategory type = post.getDamageCategory();
                if (type != null && type != DamageCategory.UNKNOWN) {
                    counts.put(type, counts.getOrDefault(type, 0) + 1);
                }
            }
        }

        for (Map.Entry<DamageCategory, Integer> entry : counts.entrySet()) {
            dataset.addValue(entry.getValue(), "Damage Reports", entry.getKey().getDisplayName());
        }

        return dataset;
    }

    public String getTopDamageCategory(String topic) {
        DefaultCategoryDataset dataset = getDamageData(topic);
        String topDmg = "None";
        double maxVal = 0;

        if (dataset != null) {
            for(int i=0; i<dataset.getColumnCount(); i++) {
                Number val = dataset.getValue(0, i);
                if(val != null && val.doubleValue() > maxVal) {
                    maxVal = val.doubleValue();
                    Comparable key = dataset.getColumnKey(i);
                    topDmg = (key != null) ? key.toString() : "Unknown";
                }
            }
        }
        return topDmg;
    }

    public String generateTopicInsight(String topic) {
        if (summaryGenerator == null) return "Summary generator not initialized.";

        int total = getTotalPostCount(topic);
        double score = getOverallAverageScore(topic);
        String topDamage = getTopDamageCategory(topic);

        return summaryGenerator.generateSummary(topic, total, score, topDamage);
    }
}