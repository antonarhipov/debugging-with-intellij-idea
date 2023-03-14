package org.example;

import java.io.IOException;


// Pause (suspend) -> Threads, Application state, Frames
// Resume
// Hitting the break point twice at System.in.read()
// Setting breakpoints on every line (via multiple cursors), and disabling via alt+click
// mute breakpoints action
// Shift+Cmd+F8 / Bookmarks - view all the breakpoints
// Step over, Step into -> Step out
// Force step into, Shift+Option+F7 (for Math.max, or System.out.println)
// Run to cursor
// Frame Colors
// Analyzing complex expression (Math.max) - Alt+click
// Evaluate expressions
// Variables -> Watches / Expressions
// Set value
// Force return


public class EndressInAndOut {
    public static void main(String[] args) throws IOException {
        while (true) {
            int read = System.in.read();
            System.out.println("Input " + read);
            if(filter(read)){
                process(read);
            }
        }
    }

    static boolean filter(int read) {
        return read != '\n' && read != 'a';
    }

    static void process(int arg) {
        if (Math.max(arg, 90) % 2 == 0) {
            System.out.println("!");
        }
    }
}
