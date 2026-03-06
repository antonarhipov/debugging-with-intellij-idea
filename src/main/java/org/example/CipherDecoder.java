package org.example;

import java.util.Scanner;

public class CipherDecoder {

    public static void main(String[] args) {
        Decoder decoder = new Decoder();

        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.println("Connecting to server...");
            // program blocks here — pause the program and inspect the threads
            // navigate the frames: main -> fetchMessage -> downloadChunks -> simulateLatency
            String message = decoder.fetchMessage();

            System.out.println("Message received. Starting decode...\n");

            // 'set local variable' — change 'shift' here to jump to the answer instantly
            for (int shift = 0; shift < 26; shift++) {
                // 'show method return value' — inspect decode() result before println
                System.out.println(decoder.decode(message, shift));
                sleep();
            }
        }
    }

    //region sleep
    static void sleep() {
        try {Thread.sleep(600);} catch (InterruptedException e) {throw new RuntimeException(e);}
    }
    //endregion
}

class Decoder {

    private static final String ENCODED = "CHNYFFCD CM GUECHA SIO VYFCYPY";

    // simulates a slow network fetch — deep enough call stack to make frame navigation interesting
    public String fetchMessage() {
        return downloadChunks();
    }

    private String downloadChunks() {
        simulateLatency();
        return ENCODED;
    }

    private void simulateLatency() {
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    public String decode(String message, int shift) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("Shift %2d:  ", shift));
        // 'reset frame' — re-execute from here to retry the current shift
        for (int i = 0; i < message.length(); i++) {
            // 'set local variable' on 'i' to skip to a specific character position
            char decoded = decodeChar(message.charAt(i), shift);
            result.append(decoded);
        }
        // 'force return' — bypass the result and inject the answer directly
        return result.toString();
    }

    // 'hotswap' — introduce an off-by-one: change (c - shift) to (c - shift - 1), then fix it live
    private char decodeChar(char c, int shift) {
        if (c == ' ') return ' ';
        return (char) ('A' + (c - 'A' - shift + 26) % 26);
    }
}