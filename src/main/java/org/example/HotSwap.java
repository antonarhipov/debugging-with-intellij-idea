package org.example;

public class HotSwap {
    public static void main(String[] args) {
        Clazz c = new Clazz();
        while (true) {
            System.out.println(c.getValue());
        }
    }
}

class Clazz {
    public String getValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i);
//            builder.append(" ");
        }
        return builder.toString();
    }

}