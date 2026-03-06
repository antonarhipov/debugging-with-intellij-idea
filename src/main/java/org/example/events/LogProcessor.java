package org.example.events;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class LogProcessor {
    public static void main(String[] args) {
        List<String> logs = Arrays.asList(
                "2025-05-14T08:00:00 INFO User login: userId=42",
                "2025-05-14T08:03:15 INFO Data requested: endpoint=/api/data",
                "2025-05-14T08:04:30 INFO User logout: userId=42",
                "2025-05-14T08:10:00 INFO User login: userId=99"
        );

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime previousTime = null;

        for (String log : logs) {
            String[] parts = log.split(" ", 3);
            LocalDateTime timestamp = LocalDateTime.parse(parts[0], formatter);
            String level = parts[1];
            String message = parts[2];

            StringBuilder output = new StringBuilder();
            output.append("[").append(level).append("] ");
            output.append(message);

            if (previousTime != null) {
                Duration duration = Duration.between(previousTime, timestamp);
                output.append(" (Δ ").append(duration.getSeconds()).append("s since last event)");
            }

            System.out.println(output);
            previousTime = timestamp;
        }
    }
}