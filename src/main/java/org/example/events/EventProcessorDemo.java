package org.example.events;

import java.util.Arrays;
import java.util.List;

/**
 * Demo class to show the EventProcessor in action.
 */
public class EventProcessorDemo {
    public static void main(String[] args) {
        // Create sample log events
        List<String> logEvents = Arrays.asList(
                "2025-05-14T08:00:00 INFO User login: userId=42",
                "2025-05-14T08:03:15 DEBUG Data requested: endpoint=/api/data userId=42",
                "2025-05-14T08:04:30 INFO User logout: userId=42",
                "2025-05-14T08:10:00 INFO User login: userId=99",
                "2025-05-14T08:12:45 INFO User action: userId=99",
                "2025-05-14T08:15:00 INFO User login: userId=42",
                "2025-05-14T08:15:05 INFO User action: userId=99",
                "2025-05-14T08:15:45 INFO User action: userId=99"
        );

        // Create an EventProcessor
        EventProcessor processor = new EventProcessor();

        System.out.println("Processing log events...");
        System.out.println("------------------------");

        logEvents.forEach(logEvent -> {
            String result = processor.processLogEvent(logEvent);
            nop();
            System.out.println(result);
        });

    }

    private static void nop() {
    }
}