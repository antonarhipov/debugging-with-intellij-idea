package org.example;

import java.util.*;

public class Cache {

    static Cache instance;

    Map<String, User> map = new HashMap<>();

    static Cache getInstance(int i) {
        return getInstance();
    }

    static Cache getInstance() {
        if (instance == null) {
            instance = new Cache();
        }
        return instance;
    }

    public User get(String id) {
        return map.get(id);
    }

    public void put(String id, User user) {
        map.put(id, user);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(getInstance(i));
        }
    }
}
