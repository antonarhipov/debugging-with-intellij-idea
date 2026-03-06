package org.example.events;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Demo class to show the EventProcessor in action.
 */
public class EventProcessorWithThreads {
    public static void main(String[] args) {
        BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
        EventProcessor processor = new EventProcessor();

        // Reader thread
        Thread readerThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if (line.equals("exit")) {
                    break;
                }
                try {
                    messageQueue.put(line);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        // Processor thread
        Thread processorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String message = messageQueue.take();
                    String result = processor.processLogEvent(message);
                    System.out.println(result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        System.out.println("Enter log events (type 'exit' to quit):");
        System.out.println("------------------------");

        readerThread.start();
        processorThread.start();
//
//        try {
//            readerThread.join();
//            processorThread.interrupt();
//            processorThread.join();
//        } catch (InterruptedException e) {
//            readerThread.interrupt();
//            processorThread.interrupt();
//        }
    }

    private static void nop() {
    }
}