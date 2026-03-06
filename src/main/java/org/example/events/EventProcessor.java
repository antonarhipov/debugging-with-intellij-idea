package org.example.events;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes log events and calculate time elapsed since the previous event with the same user ID.
 */
public class EventProcessor {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Pattern USER_ID_PATTERN = Pattern.compile("userId=(\\d+)");

    private final Map<String, LocalDateTime> lastEventTimeByUserId = new HashMap<>();

    /**
     * Processes a log event string and calculates time elapsed since the previous event with the same user ID.
     * 
     * @param logEvent A log event string containing timestamp, level, user ID, and message
     * @return Processed log event with time-elapsed information, or the original log event if no user ID is found
     */
    public String processLogEvent(String logEvent) {
        // Parse the log event
        String[] parts = logEvent.split(" ", 3);
        if (parts.length < 3) {
            return logEvent; // Return original if the format is invalid
        }

        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(parts[0], FORMATTER);
        } catch (Exception e) {
            return logEvent; // Return original if timestamp parsing fails
        }

        String level = parts[1];
        String message = parts[2];

        // Extract user ID from the message
        Matcher matcher = USER_ID_PATTERN.matcher(message);
        if (!matcher.find()) {
            return logEvent; // Return original if no user ID found
        }

        String userId = matcher.group(1);

        // Calculate time elapsed since the previous event with the same user ID
        StringBuilder result = new StringBuilder(logEvent);
        if (lastEventTimeByUserId.containsKey(userId)) {
            Duration duration = calculateDuration(userId, timestamp);
            result.append(" (Δ ").append(duration.toSeconds()).append("s since last event for user ").append(userId).append(")");
//            result.append(" (Δ ").append(duration.toMinutes()).append("m ").append(duration.getSeconds()%60).append("s since last event for user ").append(userId).append(")");
        }

        // Update the last event time for this user ID
        lastEventTimeByUserId.put(userId, timestamp);

        return result.toString();
    }

    private Duration calculateDuration(String userId, LocalDateTime timestamp) {
        LocalDateTime previousTime = lastEventTimeByUserId.get(userId);
        return Duration.between(previousTime, timestamp);
    }

    /**
     * Processes multiple log events.
     * 
     * @param logEvents Array of log event strings
     * @return Array of processed log events
     */
    public String[] processLogEvents(String[] logEvents) {
        String[] results = new String[logEvents.length];
        for (int i = 0; i < logEvents.length; i++) {
            results[i] = processLogEvent(logEvents[i]);
        }
        return results;
    }

    /**
     * Resets the internal state of the processor.
     */
    public void reset() {
        lastEventTimeByUserId.clear();
    }
}
