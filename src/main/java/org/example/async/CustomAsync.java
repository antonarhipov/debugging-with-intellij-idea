package org.example.async;

import org.jetbrains.annotations.Async;

import java.util.ArrayList;
import java.util.List;

class Task {
    @Async.Execute
    public void run(){
        System.out.println("Running: " + this);
    }
}

public class CustomAsync {

    List<Task> tasks = new ArrayList<>();

    public void submitTask(@Async.Schedule Task task){
        System.out.println("Submitted: " + task);
        tasks.add(task);
    }

    public static void main(String[] args) {

        CustomAsync runner = new CustomAsync();

        for (int i = 0; i < 5; i++) {
            runner.submitTask(new Task());
        }

        runner.tasks.forEach(task -> task.run());

    }


}
