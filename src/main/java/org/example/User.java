package org.example;

public class User {
    String name;
    int age;

    public User(String name) {
        this.name = name;
//        throw new RuntimeException("HAHA!");
    }

    void inc(){
        age++;
    }

    @Override
    public String toString() {
        return name + ": " + age;
    }
}
