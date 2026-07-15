package org.example;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

/**
 * A JavaFX front-end for Conway's Game of Life.
 *
 * The cellular-automaton rules live in {@link GameOfLife}; this class only
 * deals with rendering the board, running the animation clock, and letting the
 * user paint cells and drive the simulation.
 *
 * <p>Note: {@link GameOfLife#step(int[][])} advances the board in place, which
 * corrupts neighbour counts mid-sweep. For a live UI we need a correct
 * generation, so {@link #nextGeneration(int[][])} below double-buffers the
 * board while reusing the pure {@link GameOfLife#countNeighbours} helper.
 *
 * <p>Run with: {@code ./gradlew run}
 */
public class GameOfLifeApp extends Application {

    private static final int ROWS = 40;
    private static final int COLS = 60;
    private static final int CELL = 16;          // pixels per cell
    private static final int GRID_LINE = 1;

    private static final Color BG_COLOR   = Color.web("#1e1f22");
    private static final Color LINE_COLOR = Color.web("#2b2d30");
    private static final Color CELL_COLOR = Color.web("#57c7ff");

    private int[][] grid = new int[ROWS][COLS];
    private long generation = 0;

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline clock;

    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        canvas = new Canvas(COLS * CELL, ROWS * CELL);
        gc = canvas.getGraphicsContext2D();

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::paintCell);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::paintCell);

        // Animation clock — one tick advances one generation. Rate is driven by
        // the speed slider (see buildToolbar), so the base duration is arbitrary.
        // Must exist before buildToolbar(), which wires the slider to it.
        clock = new Timeline(new KeyFrame(Duration.millis(120), e -> advance()));
        clock.setCycleCount(Animation.INDEFINITE);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setTop(buildToolbar());
        root.setStyle("-fx-background-color: " + toHex(BG_COLOR) + ";");

        seedGlider();
        draw();

        Scene scene = new Scene(root);
        stage.setTitle("Conway's Game of Life");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private HBox buildToolbar() {
        ToggleButton play = new ToggleButton("▶ Play");
        play.setOnAction(e -> {
            if (play.isSelected()) {
                clock.play();
                play.setText("⏸ Pause");
            } else {
                clock.pause();
                play.setText("▶ Play");
            }
        });

        Button step = new Button("Step");
        step.setTooltip(new Tooltip("Advance one generation"));
        step.setOnAction(e -> advance());

        Button clear = new Button("Clear");
        clear.setOnAction(e -> {
            grid = new int[ROWS][COLS];
            generation = 0;
            draw();
        });

        Button random = new Button("Random");
        random.setOnAction(e -> {
            seedRandom();
            draw();
        });

        Button blinker = new Button("Blinker");
        blinker.setOnAction(e -> {
            grid = new int[ROWS][COLS];
            generation = 0;
            seedBlinker();
            draw();
        });

        Button glider = new Button("Glider");
        glider.setOnAction(e -> {
            grid = new int[ROWS][COLS];
            generation = 0;
            seedGlider();
            draw();
        });

        Slider speed = new Slider(1, 30, 8);   // generations per second
        speed.setPrefWidth(140);
        speed.valueProperty().addListener((obs, old, val) -> clock.setRate(val.doubleValue() / 8.0));
        clock.setRate(speed.getValue() / 8.0);

        statusLabel = new Label();
        statusLabel.setTextFill(Color.web("#c9ccd1"));

        HBox bar = new HBox(8, play, step, clear, random, blinker, glider,
                new Label("Speed") {{ setTextFill(Color.web("#c9ccd1")); }},
                speed, statusLabel);
        bar.setPadding(new Insets(10));
        bar.setStyle("-fx-alignment: center-left;");
        return bar;
    }

    /** Toggle the cell under the cursor (used for both press and drag). */
    private void paintCell(MouseEvent e) {
        int col = (int) (e.getX() / CELL);
        int row = (int) (e.getY() / CELL);
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return;
        }
        // On a fresh press, toggle; while dragging, only turn cells on so a
        // stroke doesn't flicker cells off as the pointer re-enters them.
        if (e.getEventType() == MouseEvent.MOUSE_PRESSED) {
            grid[row][col] = grid[row][col] == GameOfLife.ALIVE ? GameOfLife.DEAD : GameOfLife.ALIVE;
        } else {
            grid[row][col] = GameOfLife.ALIVE;
        }
        draw();
    }

    private void advance() {
        grid = nextGeneration(grid);
        generation++;
        draw();
    }

    /**
     * Correct, non-mutating generation step: every cell's fate is decided from
     * the previous board, written into a fresh grid. Reuses the pure
     * {@link GameOfLife#countNeighbours} helper.
     */
    private static int[][] nextGeneration(int[][] current) {
        int rows = current.length;
        int cols = current[0].length;
        int[][] next = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbours = GameOfLife.countNeighbours(current, r, c);
                boolean alive = current[r][c] == GameOfLife.ALIVE;
                boolean survives = alive ? (neighbours == 2 || neighbours == 3) : (neighbours == 3);
                next[r][c] = survives ? GameOfLife.ALIVE : GameOfLife.DEAD;
            }
        }
        return next;
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        gc.setFill(CELL_COLOR);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] == GameOfLife.ALIVE) {
                    gc.fillRect(c * CELL + GRID_LINE, r * CELL + GRID_LINE,
                            CELL - GRID_LINE, CELL - GRID_LINE);
                }
            }
        }

        gc.setStroke(LINE_COLOR);
        gc.setLineWidth(GRID_LINE);
        for (int c = 0; c <= COLS; c++) {
            gc.strokeLine(c * CELL, 0, c * CELL, h);
        }
        for (int r = 0; r <= ROWS; r++) {
            gc.strokeLine(0, r * CELL, w, r * CELL);
        }

        if (statusLabel != null) {
            statusLabel.setText(String.format("  Gen %d   Population %d",
                    generation, GameOfLife.population(grid)));
        }
    }

    // --- seed patterns, centred on the board --------------------------------

    private void seedBlinker() {
        int r = ROWS / 2;
        int c = COLS / 2;
        grid[r][c - 1] = GameOfLife.ALIVE;
        grid[r][c]     = GameOfLife.ALIVE;
        grid[r][c + 1] = GameOfLife.ALIVE;
    }

    private void seedGlider() {
        int r = ROWS / 2 - 1;
        int c = COLS / 2 - 1;
        grid[r][c + 1]     = GameOfLife.ALIVE;
        grid[r + 1][c + 2] = GameOfLife.ALIVE;
        grid[r + 2][c]     = GameOfLife.ALIVE;
        grid[r + 2][c + 1] = GameOfLife.ALIVE;
        grid[r + 2][c + 2] = GameOfLife.ALIVE;
    }

    private void seedRandom() {
        Random rnd = new Random();
        generation = 0;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = rnd.nextInt(100) < 25 ? GameOfLife.ALIVE : GameOfLife.DEAD;
            }
        }
    }

    private static String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
