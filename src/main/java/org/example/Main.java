package org.example;

public class Main {
    public static void main(String[] args) {
        foo(bar());
    }

    private static void foo(Object bar) {
        System.out.println(bar);
    }

    private static Object bar() {
        return null;
    }
}