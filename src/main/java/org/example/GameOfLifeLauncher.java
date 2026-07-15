package org.example;

import javafx.application.Application;

/**
 * Plain entry point for the Game of Life UI.
 *
 * <p>When the class the JVM launches directly extends {@link Application} and
 * JavaFX is on the classpath rather than the module path, the launcher aborts
 * with "JavaFX runtime components are missing". Running this class instead —
 * which does <em>not</em> extend {@link Application} — sidesteps that check, so
 * the app starts from the plain classpath (e.g. straight from the IDE).
 *
 * <p>{@code ./gradlew run} works from either entry point because the OpenJFX
 * Gradle plugin sets up the module path itself.
 */
public class GameOfLifeLauncher {
    public static void main(String[] args) {
        Application.launch(GameOfLifeApp.class, args);
    }
}
