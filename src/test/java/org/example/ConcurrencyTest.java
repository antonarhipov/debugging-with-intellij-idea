package org.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrencyTest {

    @Test
    void test() throws InterruptedException {
        assertSame(1, work().size());
    }


    static List<Integer> work() throws InterruptedException {
        final List<Integer> list = Collections.synchronizedList(new ArrayList<>());
        Thread thread = new Thread(() -> addIfAbsent(list, 17));
        thread.start();
        addIfAbsent(list, 17);
        thread.join();
//        System.out.println("Elements: " + list);
        return list;
    }

    private static void addIfAbsent(List<Integer> list, int x) {
        if (!list.contains(x)) {
            list.add(x);
        }
    }
}
