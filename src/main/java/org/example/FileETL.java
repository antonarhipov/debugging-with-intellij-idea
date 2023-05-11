package org.example;

import java.io.IOException;
import java.util.List;

public class FileETL {

    public static void main(String[] args) throws IOException {
        ComplexWorkflow wf = new ComplexWorkflow();
        List<String> reader = List.of("aaaa", "bbbbb", "ccc", "dddd");
        List<User> lines = reader.stream()
                .filter(l -> l.length() > 3)
                .filter(l -> !l.startsWith("a"))
                .map(wf::process)
                .toList();
        System.out.println(lines);
    }
}
