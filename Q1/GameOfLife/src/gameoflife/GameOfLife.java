/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : Quiz 2
 * DUE         : 10 October 2018
 * DESCRIPTION : Simulates the game of life using multiple threads and phasers.
 ******************************************************************************/

package gameoflife;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Phaser;

public class GameOfLife {
    
    private static final int SIZE = 10;         // n value for an n x n board
    private static final int MAX_RUNS = 10;     // max number of runs for the game (generations)
    private static final int RATIO = 2;         // ratio of initial alive
    
    private static Thread[][] board = new Thread[SIZE][SIZE];       // the board of threads
    private static boolean[][] status = new boolean[SIZE][SIZE];    // alive/dead status of each thread--used by other threads to check on neighbors
    private static Phaser generation = new Phaser();                // phaser used to prevent threads from proceeding
    
    private static class Life implements Runnable {
        
        private int row;
        private int column;
        private boolean alive;
        
        Life(int row, int column, boolean alive) {
            this.row = row;
            this.column = column;
            this.alive = alive;
        }
        
        @Override
        public void run() {
            // notify phaser of arrival
            int phase = generation.arrive();
            int livingNeighbors = 0;
            
            // check left
            if(column - 1 >= 0)
                if(status[row][column - 1])
                    livingNeighbors++;
            
            // check right
            if(column + 1 < SIZE)
                if(status[row][column + 1])
                    livingNeighbors++;
            
            // check up
            if(row - 1 >= 0)
                if(status[row - 1][column])
                    livingNeighbors++;
            
            // check down
            if(row + 1 < SIZE)
                if(status[row + 1][column])
                    livingNeighbors++;
            
            // NOTE: diagonals are not considered neighbors
            
            // wait for other threads to do their work
            generation.awaitAdvance(phase);
            
            if(alive) {
                if(livingNeighbors <= 1 || livingNeighbors > 3) {
                    alive = false;
                    status[row][column] = false;
                }
            } else {
                if(livingNeighbors == 3) {
                    alive = true;
                    status[row][column] = true;
                }
            }
        }
    }
    
    // initializeBoard : Initializes the board randomly. Approximately 1/RATIO of
    // the board should be alive upon initialization.
    private static void initializeBoard() {
        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                boolean alive = false;
                if(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE) % RATIO == 1)
                    alive = true;
                board[i][j] = new Thread(new Life(i, j, alive));
                status[i][j] = alive;
            }
        }
    }
    
    // printStatus : Prints 1 for "living" threads, prints 0 for "dead" threads.
    private static void printStatus() {
        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                if(status[i][j])
                    System.out.print("1 ");
                else
                    System.out.print("0 ");
            }
            System.out.println();
        }
    }
    
    // printLivingNeighbors : Prints the number of living neighbors for each
    // thread.
    private static void printLivingNeighbors() {
        for(int i = 0; i < SIZE; i++) {
            for(int j = 0; j < SIZE; j++) {
                int livingNeighbors = 0;
                if(j - 1 >= 0)
                    if(status[i][j - 1])
                        livingNeighbors++;

                // check right
                if(j + 1 < SIZE)
                    if(status[i][j + 1])
                        livingNeighbors++;

                // check up
                if(i - 1 >= 0)
                    if(status[i - 1][j])
                        livingNeighbors++;

                // check down
                if(i + 1 < SIZE)
                    if(status[i + 1][j])
                        livingNeighbors++;
                
                System.out.print(livingNeighbors + " ");
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args) {
        // initialize the board
        initializeBoard();
        
        System.out.println("INITIAL BOARD:");
        printStatus();
        System.out.println();
        
        System.out.println("INITIAL NEIGHBORS:");
        printLivingNeighbors();
        System.out.println();
        
        for(int k = 0; k < MAX_RUNS; k++) {
            // create phaser for this phase (not the smartest use of phasers
            // but we are pressed for time)
            generation = new Phaser();
            
            // register calling thread
            generation.register();
            
            for(int i = 0; i < SIZE; i++) {
                for(int j = 0; j < SIZE; j++) {
                    // register the current thread/cell
                    generation.register();
                    board[i][j] = new Thread(new Life(i, j, status[i][j]));
                    board[i][j].start();
                }
            }
            
            // deregister calling thread
            generation.arriveAndDeregister();
            
            // allow threads to finish work
            try {
                for(int i = 0; i < SIZE; i++)
                    for(int j = 0; j < SIZE; j++)
                        board[i][j].join();
            } catch(InterruptedException e) { }
            
            System.out.println("----------------------\n");
            
            System.out.println("BOARD " + (k + 1) + ":");
            printStatus();
            System.out.println();
            
            System.out.println("NEIGHBORS " + (k + 1) + ":");
            printLivingNeighbors();
            System.out.println();
        }
    }
    
}
