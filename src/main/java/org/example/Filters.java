package org.example;

import java.util.List;

public class Filters {

    public static void main(String[] args) {
        List<User> users = List.of(
                new User("Anton"),
                new User("Anton"),
                new Admin("Jane"),
                new User("Joe")
        );
        for (int i = 0; i < 10; i++) {
            for (User user : users) {
                user.inc();
                System.out.println(user);
            }
        }
    }
}
