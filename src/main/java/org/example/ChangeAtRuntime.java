package org.example;

import java.io.IOException;
import java.util.Scanner;

public class ChangeAtRuntime {
    public static void main(String[] args) throws IOException {
        Clazz c = new Clazz();

        //noinspection InfiniteLoopStatement
        while (true) {
            //remove println, and keep c.getValue to demonstrate the 'show method return value' setting
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
        //reset loop variable to affect the result for the builder
        for (int i = 0; i < 5; i++) {
            String string = read();
            builder.append(string);
            builder.append("-");
            builder.append(i);
            builder.append(" ");
        }
        //force return instead of adjusting the value
        return builder.toString();
    }

    // show hotswap
    private String read() {
//        return "-";
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
