package io.diendanx.labbooking.Helpers;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * DateTimeFormatter for displaying timestamps in a human-friendly way for chat messages.
 *
 * - Under 1 minute: "X seconds ago"
 * - Under 1 hour: "X minutes ago"
 * - Under 1 day: "X hours ago"
 * - In current week: "HH:mm, Thứ Hai"
 * - Otherwise: "dd/MM/yyyy"
 */
public class DateTimeFormatter {
    private final Map<Long, Function<Instant, String>> strategyMap = new LinkedHashMap<>();

    public DateTimeFormatter() {
        strategyMap.put(86400L, this::formatInHours);        // dưới 1 ngày
        strategyMap.put(Long.MAX_VALUE, this::formatInDay);  // còn lại
    }

    public String format(Instant instant) {
        long elapsedSeconds = ChronoUnit.SECONDS.between(instant, Instant.now());

        var strategy = strategyMap.entrySet().stream()
                .filter(entry -> elapsedSeconds < entry.getKey())
                .findFirst()
                .orElseThrow();

        return strategy.getValue().apply(instant);
    }

    private String formatInHours(Instant instant) {
        LocalDateTime time = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm, EEEE", new Locale("vi"));
        return time.format(formatter);
    }

    private String formatInDay(Instant instant) {
        LocalDateTime time = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        // Xác định đầu và cuối tuần hiện tại
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        if (!time.isBefore(startOfWeek) && time.isBefore(endOfWeek)) {
            // Trong tuần này → HH:mm, Thứ X
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm, EEEE", new Locale("vi"));
            return time.format(formatter);
        } else {
            // Ngoài tuần này → dd/MM/yyyy
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return time.format(formatter);
        }
    }
}
