package org.example;

public class ComplexWorkflow {

    public static void main(String[] args) {
        ComplexWorkflow workflow = new ComplexWorkflow();
        workflow.process("asdf");
    }

    public User process(String id){
        step1(id);
        return new User(id);
    }

    private void step1(String id) {
        step2(id);
    }

    private void step2(String id) {
        step3(id);
        step4(id);
    }

    private void step3(String id) {
        output(id);
    }
    
    private void step4(String id) {
        action(id);
    }

    void action(String id){
        output(id);
    }

    private void output(String id) {
        System.err.println(id);
    }
}
