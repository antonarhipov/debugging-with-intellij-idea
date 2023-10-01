package org.example;

import java.io.IOException;
import java.util.Scanner;

public class ChangeAtRuntime {
    public static void main(String[] args) throws IOException {
        Clazz c = new Clazz();

        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.println(c.getValue());
            sleep();
        }
    }

    //region sleep
    private static void sleep() {
        try {Thread.sleep(800);} catch (InterruptedException e) {throw new RuntimeException(e);}
    }
    //endregion
}

class Clazz {
    public String getValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            String string = read();
            builder.append(string);
            builder.append("-");
            builder.append(i);
            builder.append(" ");
        }
        return builder.toString();
    }

    private String read() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}