package org.example.reactor;

import reactor.core.publisher.Flux;

import java.util.List;


public class Basics {
    public static void main(String[] args) throws InterruptedException {
        List<String> words = List.of("Quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog");
        Flux<String> from = Flux.fromIterable(words);
        Flux<String> flux = from
                .flatMap(w -> Flux.fromArray(w.split("")))
                .distinct()
                .zipWith(Flux.range(1, 100), (word, num) -> num + ": " + word);
        flux.subscribe(System.out::println);
    }

}

