package org.example;

import java.math.BigDecimal;
import java.util.List;

import static java.lang.System.out;

public class ProductOfferCatalog {
    public static void main(String[] args) {
        // Goal: unique in-stock offers with positive prices.
        // Same product + numerically equal price means the same offer.
        // Columns: product, price, inStock.
        List<String> rows = List.of(
                " Keyboard,49.90,true ",
                "Mouse,0.00,true",
                "   ",
                "Monitor,199.00,false",
                "Keyboard,49.900,true",
                "Headphones,89.00,true"
        );

        List<String> offers = uniqueOffers(rows);

        // Expected: [Keyboard: 49.90, Headphones: 89.00].
        out.println(offers);
    }

    static List<String> uniqueOffers(List<String> rows) {
        return rows.stream()
                .map(String::strip)
                .filter(row -> !row.isEmpty())
                .map(row -> {
                    String[] fields = row.split(",");
                    return new Offer(fields[0], new BigDecimal(fields[1]),
                            Boolean.parseBoolean(fields[2]));
                })
                .filter(offer -> offer.inStock())
                .filter(offer -> offer.price().signum() > 0)
                .distinct()
                .map(offer -> "%s: %s".formatted(
                        offer.product(), offer.price().setScale(2).toPlainString()))
                .toList();
    }

    record Offer(String product, BigDecimal price, boolean inStock) {
        Offer {
            price = price.stripTrailingZeros();
        }
    }
}
