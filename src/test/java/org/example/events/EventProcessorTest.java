package org.example.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventProcessorTest {
    
    private EventProcessor processor;
    
    @BeforeEach
    void setUp() {
        processor = new EventProcessor();
    }
    
    @Test
    void testProcessSingleLogEvent() {
        String logEvent = "2025-05-14T08:00:00 INFO User login: userId=42";
        String result = processor.processLogEvent(logEvent);
        
        // First event for this user, so no time difference should be added
        assertEquals(logEvent, result);
    }
    
    @Test
    void testProcessMultipleLogEventsWithSameUserId() {
        String firstEvent = "2025-05-14T08:00:00 INFO User login: userId=42";
        String secondEvent = "2025-05-14T08:04:30 INFO User logout: userId=42";
        
        // Process first event
        processor.processLogEvent(firstEvent);
        
        // Process second event
        String result = processor.processLogEvent(secondEvent);
        
        // Should include time difference (270 seconds)
        assertTrue(result.contains("(Δ 270s since last event for user 42)"));
    }
    
    @Test
    void testProcessLogEventsWithDifferentUserIds() {
        String firstEvent = "2025-05-14T08:00:00 INFO User login: userId=42";
        String secondEvent = "2025-05-14T08:10:00 INFO User login: userId=99";
        String thirdEvent = "2025-05-14T08:15:00 INFO User logout: userId=42";
        
        // Process first event (user 42)
        processor.processLogEvent(firstEvent);
        
        // Process second event (user 99)
        String result1 = processor.processLogEvent(secondEvent);
        
        // Process third event (user 42 again)
        String result2 = processor.processLogEvent(thirdEvent);
        
        // Second event should not have time difference (different user)
        assertEquals(secondEvent, result1);
        
        // Third event should have time difference from first event (900 seconds)
        assertTrue(result2.contains("(Δ 900s since last event for user 42)"));
    }
    
    @Test
    void testProcessInvalidLogFormat() {
        String invalidEvent = "Invalid log format";
        String result = processor.processLogEvent(invalidEvent);
        
        // Should return the original event unchanged
        assertEquals(invalidEvent, result);
    }
    
    @Test
    void testProcessLogEventWithoutUserId() {
        String eventWithoutUserId = "2025-05-14T08:00:00 INFO System startup complete";
        String result = processor.processLogEvent(eventWithoutUserId);
        
        // Should return the original event unchanged
        assertEquals(eventWithoutUserId, result);
    }
    
    @Test
    void testProcessMultipleLogEvents() {
        String[] events = {
            "2025-05-14T08:00:00 INFO User login: userId=42",
            "2025-05-14T08:05:00 INFO User action: userId=99",
            "2025-05-14T08:10:00 INFO User logout: userId=42"
        };
        
        String[] results = processor.processLogEvents(events);
        
        // First event for each user should not have time difference
        assertEquals(events[0], results[0]);
        assertEquals(events[1], results[1]);
        
        // Third event should have time difference for user 42 (600 seconds)
        assertTrue(results[2].contains("(Δ 600s since last event for user 42)"));
    }
    
    @Test
    void testResetProcessor() {
        String firstEvent = "2025-05-14T08:00:00 INFO User login: userId=42";
        String secondEvent = "2025-05-14T08:10:00 INFO User logout: userId=42";
        
        // Process first event
        processor.processLogEvent(firstEvent);
        
        // Reset processor
        processor.reset();
        
        // Process second event
        String result = processor.processLogEvent(secondEvent);
        
        // Should not include time difference because processor was reset
        assertEquals(secondEvent, result);
    }
}