package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class FileETL {

    public static void main(String[] args) throws IOException {
        ComplexWorkflow wf = new ComplexWorkflow();
        try (BufferedReader reader = new BufferedReader(new FileReader("lines.txt"))) {
            List<User> lines = reader.lines()
                    .filter(l -> l.length() > 3)
                    .filter(l -> !l.startsWith("a"))
//                    .map(wf::process)
                    .map(User::new)
                    .toList();
        }
    }
}

