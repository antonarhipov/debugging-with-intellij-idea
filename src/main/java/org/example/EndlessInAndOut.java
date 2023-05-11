package org.example;

import java.io.IOException;

public class EndlessInAndOut {
    public static void main(String[] args) throws IOException {
        //noinspection InfiniteLoopStatement
        while (true) {
            int read = System.in.read();
            System.out.println("Input " + read);
            if(filter(read)){
                process(read);
            }
        }
    }

    static boolean filter(int read) {
        return read != '\n' && read != 'a';
    }

    static void process(int arg) {
        if (Math.max(arg, 90) % 2 == 0) {
            System.out.println("!");
        }
    }
}
