package project.app.humanelogistics.model;

import java.util.Arrays;
import java.util.List;

/**
 * Refactored Enum to fix Information Hiding.
 * Encapsulates the matching logic so external classes don't need to know about internal string representations.
 */
public enum DamageCategory {
    AFFECTED_PEOPLE("Affected People", Arrays.asList("death", "injury", "missing", "evacu")),
    ECONOMIC_IMPACT("Economic Production Disruption", Arrays.asList("farm", "factory", "economy", "job")),
    HOUSING_DAMAGE("Houses or Buildings Damaged", Arrays.asList("home", "roof", "collapse", "house")),
    LOSS_OF_BELONGINGS("Loss of Personal Belongings", Arrays.asList("vehicle", "car", "clothes", "belonging")),
    INFRASTRUCTURE_DAMAGE("Damaged Infrastructure", Arrays.asList("bridge", "road", "power", "grid")),
    OTHER("Other", Arrays.asList("damage", "broken")),
    UNKNOWN("Unknown", Arrays.asList());

    private final String displayName;
    private final List<String> keywords;

    DamageCategory(String displayName, List<String> keywords) {
        this.displayName = displayName;
        this.keywords = keywords;
    }

    public String getDisplayName() { return displayName; }

    // Factory method that hides implementation details
    public static DamageCategory fromText(String text) {
        if (text == null || text.trim().isEmpty()) return UNKNOWN;

        // 1. Try exact match (normalized)
        String normalized = text.trim().toUpperCase().replace(" ", "_");
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException ignored) { }

        // 2. Try partial match on name
        for (DamageCategory cat : values()) {
            if (normalized.contains(cat.name())) return cat;
        }

        // 3. (Optional) Try keyword matching if we wanted deeper analysis
        // return Arrays.stream(values()).filter(c -> c.matchesKeywords(text)).findFirst().orElse(OTHER);

        return OTHER;
    }

    private boolean matchesKeywords(String text) {
        String lower = text.toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }
}