package org.example;

/*
 * Conway's Game of Life
 *
 * Rules, applied to every cell simultaneously each generation:
 *   - A live cell with 2 or 3 live neighbours survives.
 *   - A dead cell with exactly 3 live neighbours becomes alive.
 *   - Everything else dies or stays dead.
 *
 * Sanity check: a "blinker" (three cells in a row) is a period-2
 * oscillator. Its population is ALWAYS 3, and it should flip between
 * horizontal and vertical forever:
 *
 *     . . .        . # .        . . .
 *     # # #   ->   . # .   ->   # # #   ->  ...
 *     . . .        . # .        . . .
 *
 * Run it and watch the board generation by generation.
 */
public class GameOfLife {

    static final int ALIVE = 1;
    static final int DEAD  = 0;

    public static void main(String[] args) {
        int generations = 6;

        int[][] grid = seedBlinker(7, 9);
        // int[][] grid = seedGlider(12, 12);   // swap in for a moving pattern

        for (int gen = 0; gen <= generations; gen++) {
            print(grid);
            grid = step(grid);
        }
    }

    /** Advance the whole board by one generation. */
    static int[][] step(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbours = countNeighbours(grid, r, c);
                boolean alive = grid[r][c] == ALIVE;

                if (alive && (neighbours < 2 || neighbours > 3)) {
                    grid[r][c] = DEAD;
                } else if (!alive && neighbours == 3) {
                    grid[r][c] = ALIVE;
                }
                // otherwise the cell keeps its current state
            }
        }
        return grid;
    }

    /** Count the live neighbours surrounding cell (r, c). */
    static int countNeighbours(int[][] grid, int r, int c) {
        int rows = grid.length;
        int cols = grid[0].length;
        int count = 0;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue; // skip the cell itself
                }
                int nr = r + dr;
                int nc = c + dc;
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    count += grid[nr][nc];
                }
            }
        }
        return count;
    }

    static int population(int[][] grid) {
        int total = 0;
        for (int[] row : grid) {
            for (int cell : row) {
                total += cell;
            }
        }
        return total;
    }

    static void print(int[][] grid) {
        for (int[] row : grid) {
            StringBuilder sb = new StringBuilder();
            for (int cell : row) {
                sb.append(cell == ALIVE ? '#' : '.');
                sb.append(' ');
            }
            System.out.println(sb);
        }
    }

    // --- seed patterns -------------------------------------------------

    static int[][] seedBlinker(int rows, int cols) {
        int[][] grid = new int[rows][cols];
        int r = rows / 2;
        int c = cols / 2;
        grid[r][c - 1] = ALIVE;
        grid[r][c]     = ALIVE;
        grid[r][c + 1] = ALIVE;
        return grid;
    }

    static int[][] seedGlider(int rows, int cols) {
        int[][] grid = new int[rows][cols];
        grid[0][1] = ALIVE;
        grid[1][2] = ALIVE;
        grid[2][0] = ALIVE;
        grid[2][1] = ALIVE;
        grid[2][2] = ALIVE;
        return grid;
    }
}