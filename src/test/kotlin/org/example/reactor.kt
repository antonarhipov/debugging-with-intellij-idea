package org.example

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalTime

class ReactorTests{

    @Test
    fun basic(){
        val words = listOf("Quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog")
        val from = Flux.fromIterable(words)
        val flux = from
            .flatMap { w: String ->
                Flux.fromArray(w.split("".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
            }
            .distinct()
            .zipWith(
                Flux.range(1, 100)
            ) { word: String, num: Int -> "$num: $word" }
        flux.subscribe { x: String? -> println(x) }
    }

    @Test
    fun clocks() {
        val fast = Flux.interval(Duration.ofSeconds(1)).map { tick -> "fast: $tick" }
        val slow = Flux.interval(Duration.ofSeconds(2)).map { tick -> "fast: $tick" }

        val clock = Flux.merge(
            fast.filter { true } ,
            slow.filter { true } 
        )

        val feed = Flux.interval(Duration.ofSeconds(1)).map { _ -> LocalTime.now() }

        clock.zipWith(feed) { tick, time -> { "$tick $time" } }

        Thread.sleep(5000)
    }

}