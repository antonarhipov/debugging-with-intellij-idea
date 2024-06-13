package org.example.async;

import java.util.*;
import java.util.concurrent.*;

public class AsyncExample {

    static List<Task> tasks = new ArrayList<>();
    static ExecutorService executor = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) {
        createTasks();
        executeTasks();
    }

    private static void createTasks() {
        for (int i = 0; i < 20; i++) {
            tasks.add(new Task(i));
        }
    }

    private static void executeTasks() {
        for (Task task : tasks) {
            executor.submit(task);
        }
    }

    static class Task extends Thread {

        int num;

        public void run() {
            try {
                Thread.sleep(new Random().nextInt(2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printNum();
        }

        private void printNum() {
            // Set a breakpoint at the following line
            System.out.print(num + " ");
        }

        public Task(int num) {
            this.num = num;
        }
    }
}