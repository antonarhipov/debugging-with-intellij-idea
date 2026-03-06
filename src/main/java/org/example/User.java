package org.example;

import java.util.List;

public class User {
    String name;
    int age;
    List<String> privileges;

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

    public boolean isAdmin() {
        return privileges.contains("admin");
    }
}
