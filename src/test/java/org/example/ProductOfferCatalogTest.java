package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductOfferCatalogTest {
    @Test
    void preservesDifferentProductsAndPricesWhenRemovingScaleOnlyDuplicates() {
        assertEquals(
                List.of("Keyboard: 50.00", "Keyboard: 49.90", "Mouse: 50.00"),
                ProductOfferCatalog.uniqueOffers(List.of(
                        "Keyboard,50,true",
                        "Keyboard,50.000,true",
                        "Keyboard,49.90,true",
                        "Mouse,50.00,true",
                        "Keyboard,-1.00,true"
                ))
        );
    }

    @Test
    void returnsUniqueInStockOffersWithPositivePrices() {
        List<String> rows = List.of(
                " Keyboard,49.90,true ",
                "Mouse,0.00,true",
                "   ",
                "Monitor,199.00,false",
                "Keyboard,49.900,true",
                "Headphones,89.00,true"
        );

        assertEquals(
                List.of("Keyboard: 49.90", "Headphones: 89.00"),
                ProductOfferCatalog.uniqueOffers(rows)
        );
    }
}
