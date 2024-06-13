package org.example.async;

import org.jetbrains.annotations.Async;

import java.util.ArrayList;
import java.util.List;


class MyTask {
    @AsyncExecuteAnnotation
    public void run(){
        System.out.println("Running: " + this);
    }
}

public class CustomAsyncWithCustomAnnotations {

    List<MyTask> tasks = new ArrayList<>();

    public void submitTask(@AsyncScheduleAnnotation MyTask task){
        System.out.println("Submitted: " + task);
        tasks.add(task);
    }

    public static void main(String[] args) {

        CustomAsyncWithCustomAnnotations runner = new CustomAsyncWithCustomAnnotations();

        for (int i = 0; i < 5; i++) {
            runner.submitTask(new MyTask());
        }

        runner.tasks.forEach(task -> task.run());

    }


}
