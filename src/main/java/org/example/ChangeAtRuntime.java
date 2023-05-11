package org.example;

public class ChangeAtRuntime {
    public static void main(String[] args) {
        Clazz c = new Clazz();

        //noinspection InfiniteLoopStatement
        while (true) {
            System.out.println(c.getValue());
            sleep();
        }
    }

    //region sleep
    private static void sleep() {
        try {Thread.sleep(400);} catch (InterruptedException e) {throw new RuntimeException(e);}
    }
    //endregion
}

class Clazz {
    public String getValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            builder.append(i);
            builder.append(" ");
        }
        return builder.toString();
    }

}