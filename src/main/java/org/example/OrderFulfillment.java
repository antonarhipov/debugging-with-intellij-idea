package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A batch finishes successfully, but a paid, in-stock order never ships.
 * Start with main(), then follow the investigation in docs/logpoints.md.
 */
public class OrderFulfillment {
    private final Map<String, Integer> stock = Map.of("KEYBOARD", 20, "MOUSE", 30, "MONITOR", 0);
    private final Set<String> supportedCountries = Set.of("NL", "BE", "DE", "GB");
    private final DispatchHistory dispatchHistory = new DispatchHistory();
    private final List<Rule> rules = List.of(
            new Rule("validation", this::isValid),
            new Rule("payment", this::isPaid),
            new Rule("stock", this::isInStock),
            new Rule("dispatch history", dispatchHistory::claim)
    );

    public static void main(String[] args) {
        OrderFulfillment fulfillment = new OrderFulfillment();
        List<String> dispatched = fulfillment.fulfill(sampleOrders());
        System.out.printf("Expected orders:   [ORD-1001, ORD-1002]%nDispatched orders: %s%n", dispatched);
    }

    List<String> fulfill(List<Order> orders) {
        List<String> dispatched = new ArrayList<>();
        for (Order order : orders) {
            if (isReadyToShip(order)) {
                dispatch(order, dispatched);
            }
        }
        return dispatched;
    }

    private boolean isReadyToShip(Order order) {
        for (Rule rule : rules) {
            boolean accepted = rule.check().test(order);
            //System.out.println(order.id() + " | " + rule.name() + " | accepted=" + accepted);
            if (!accepted) {
                return false;
            }
        }
        return true;
    }

    private boolean isValid(Order order) {
        return order.quantity() > 0 && supportedCountries.contains(order.country());
    }

    private boolean isPaid(Order order) {
        return order.paid();
    }

    private boolean isInStock(Order order) {
        return stock.getOrDefault(order.sku(), 0) >= order.quantity();
    }

    private void dispatch(Order order, List<String> dispatched) {
        dispatched.add(order.id());
    }

    static List<Order> sampleOrders() {
        // Both purchases are paid, in stock, and going to the same supported country.
        return List.of(
                new Order("ORD-1001", "C-01", "KEYBOARD", 1, true, "NL"), // First purchase
                new Order("ORD-1001", "C-01", "KEYBOARD", 1, true, "NL"), // Replay: ignore
                new Order("ORD-1002", "C-01", "KEYBOARD", 2, true, "NL")  // New purchase: ship
        );
    }

    record Order(String id, String customerId, String sku, int quantity, boolean paid, String country) {
    }

    private record Rule(String name, Predicate<Order> check) {
    }

    private static class DispatchHistory {
        private final Map<DispatchKey, String> dispatchedOrders = new HashMap<>();

        boolean claim(Order order) {
            DispatchKey key = new DispatchKey(order.id(), order.sku());
            String previousOrderId = dispatchedOrders.putIfAbsent(key, order.id());
            return previousOrderId == null;
        }
    }

    private record DispatchKey(String customerId, String sku) {
    }
}
