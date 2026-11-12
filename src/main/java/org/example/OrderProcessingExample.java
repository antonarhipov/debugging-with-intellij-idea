package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates runtime behavior modification and inspection in the IntelliJ IDEA Debugger.
 *
 * <ol>
 *   <li><b>Program Pause:</b> Click Pause (||) when the app hangs on startup to inspect thread state.</li>
 *   <li><b>Modify Variable:</b>
 *     <ul>
 *       <li>Change 'waiting' to false (F2) to unhang the program.</li>
 *       <li>Correct invalid 'price' on ORD-102 (-20.0 -> 20.0).</li>
 *       <li>Rewind loop counter 'i' (i = i - 1) to re-process an order.</li>
 *     </ul>
 *   </li>
 *   <li><b>HotSwap:</b> Fix the integer division bug in calculateDiscount() at runtime.</li>
 *   <li><b>Reset Frame:</b> Drop processOrder() frame to rewind back to caller and re-run with fresh logic.</li>
 *   <li><b>Force Return:</b> Force return a specific discount value (e.g. 50.0) from calculateDiscount() instead of calculating it.</li>
 *   <li><b>Throw Exception:</b> Enforce throwing PaymentException from chargePayment() at runtime when code seemingly shouldn't throw, testing recovery.</li>
 *   <li><b>Show Method Return Values:</b> Inspect unassigned method returns in Variables view.</li>
 * </ol>
 */
public class OrderProcessingExample {

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        //noinspection InfiniteLoopStatement
        while (true) {
            // FEATURE 7: Demonstrate 'Show Method Return Values'
            // Enable 'Show Method Return Values' in Debugger Settings (gear icon) -> Step Over processBatch()
            List<OrderProcessor.OrderResult> results = processor.processBatch();
            System.out.printf("Batch finished: %d orders processed.%n%n", results.size());
            sleep();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static class PaymentException extends Exception {
        public PaymentException(String message) {
            super(message);
        }
    }
}

class OrderProcessor {
    public record Order(String id, double price) {}

    // Accumulated order processing result used for Show Method Return Values and final output inspection
    public record OrderResult(String id, double price, double discount, double total, String status) {}

    @SuppressWarnings("FieldMayBeFinal") // Modified at runtime via debugger 'Set Value' (F2)
    private volatile boolean waiting = true;

    public List<OrderResult> processBatch() {
        // FEATURE 1: Program Pause & FEATURE 2A: Modify Variable (to unhang)
        // App hangs immediately on startup. Click Pause (||), set waiting = false (F2), and Resume (F9).
        if (waiting) {
            waitForConfirmation();
        }

        List<Order> batch = List.of(
                new Order("ORD-101", 100.0), // HotSwap, Reset Frame & Show Return Values
                new Order("ORD-102", -20.0), // Modify Local Variable (Set Value on 'price')
                new Order("ORD-103", 600.0), // Force Return (custom discount) & Throw Exception (payment failure)
                new Order("ORD-104", 80.0)   // Clean order
        );

        List<OrderResult> results = new ArrayList<>();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < batch.size(); i++) {
            Order order = batch.get(i);

            OrderResult result = processOrder(order);
            results.add(result);
            System.out.println(" [*] " + result);
        }

        return results;
    }

    // FEATURE 4: Reset Frame
    // Extracted order processing method returning accumulated data:
    // - Reset Frame: step into this method or calculateDiscount(), fix calculateDiscount() via HotSwap,
    //   then click 'Reset Frame' in Debugger toolbar to drop this frame and rewind back to processBatch()!
    public OrderResult processOrder(Order order) {
        // FEATURE 2B: Modify local variable - Data correction
        // ORD-102 has price = -20.0. Change 'price' to 20.0 via Set Value (F2) to pass validation.
        double price = Math.abs(order.price());
        if (price <= 0) {
            System.out.println(" [!] " + order.id() + ": invalid price " + price);
            return new OrderResult(order.id(), price, 0.0, 0.0, "INVALID_PRICE");
        }

        // FEATURE 7: Demonstrate 'Show Method Return Values'
        // Step over validateOrder() to see its boolean return value in the Variables pane without a variable
        if (!validateOrder(price)) {
            System.out.println(" [!] " + order.id() + ": validation failed");
            return new OrderResult(order.id(), price, 0.0, 0.0, "VALIDATION_FAILED");
        }

        // FEATURE 3: HotSwap & FEATURE 4: Reset Frame (on ORD-101)
        // Step into calculateDiscount(price):
        // 1. HotSwap: fix integer division bug (10 / 100) -> return price * 0.10; and reload classes.
        // 2. Reset Frame: click 'Reset Frame' to rewind back to processBatch() and re-enter with fix!
        //
        // FEATURE 5: Force Return (on ORD-103)
        // Step into calculateDiscount(price). Instead of calculating the discount via the formula,
        // force return a specific value (e.g. 50.0) to test a fixed promotional discount:
        // Right-click 'calculateDiscount' in Frames view -> Force Return -> enter: 50.0
        double discount = calculateDiscount(price);
        double total = price - discount;

        // FEATURE 6: Throw Exception (on ORD-103)
        // chargePayment(total) seemingly shouldn't throw an exception under normal execution.
        // To test error handling without altering code, enforce throwing an error at runtime:
        // Step into or pause at chargePayment() -> Run -> Debugging Actions -> Throw Exception
        // -> enter: new OrderProcessingExample.PaymentException("Payment gateway timeout")
        // Execution jumps directly to the catch block below, demonstrating recovery.
        try {
            chargePayment(total);
            return new OrderResult(order.id(), price, discount, total, "SUCCESS");
        } catch (OrderProcessingExample.PaymentException e) {
            return new OrderResult(order.id(), price, discount, total, "FAILED: " + e.getMessage());
        }
    }

    // FEATURE 7: Demonstrate 'Show Method Return Values'
    private boolean validateOrder(double price) {
        return price < 1000;
    }

    // FEATURE 3: HotSwap - Live Bug Fix
    // Fix integer division (10 / 100) -> return price * 0.10; and reload classes.
    //
    // FEATURE 5: Force Return - Return specific value instead of calculating
    // Step into this method when processing ORD-103.
    // Instead of calculating the discount formula, force return a specific value (e.g. 50.0)
    // to test a custom voucher or VIP promotional discount:
    // Right-click 'calculateDiscount' frame in Frames view -> Force Return -> enter: 50.0
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private double calculateDiscount(double price) {
        // Change to:
         return price * 0.10;
//        return price * (10 / 100);
    }

    // FEATURE 1: Program Pause & FEATURE 2A: Modify Variable (to unhang)
    @SuppressWarnings("BusyWait")
    private void waitForConfirmation() {
        System.out.println("Connecting to order service... (Program hangs - click Pause Program ||)");
        while (waiting) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Service connected!\n");
    }

    // FEATURE 6: Throw Exception
    // At runtime, payment processing normally succeeds without throwing an exception.
    // To test error handling and recovery without editing code or simulating network outages:
    // Pause inside chargePayment() -> Run -> Debugging Actions -> Throw Exception
    // -> enter: new OrderProcessingExample.PaymentException("Payment gateway timeout")
    private void chargePayment(double amount) throws OrderProcessingExample.PaymentException {
        // Seemingly normal execution - payment succeeds without errors
        System.out.printf("   [Payment] Successfully charged $%.2f%n", amount);
    }
}
