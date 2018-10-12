/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 4
 * DUE         : 15 October 2018
 * DESCRIPTION : Simulates the Dining Philosophers problem with the use of
 *               structured locks.
 ******************************************************************************/

package diningphilosophers;

import java.util.concurrent.ThreadLocalRandom;

// StructuredDiningPhilosophers : A class which facilitates the Dining 
// Philosophers problem with the aid of structured (synchronized, implicit)
// locks.
public class StructuredDiningPhilosophers {
    
    private final int NUM_PHILOS;           
    private final int MAX_THINK;
    private final int MAX_EAT;
    
    private Object forks[];
    private Thread philosophers[];
    
    // StructuredDiningPhilosophers : A constructor method which initializes
    // the number of philosophers at the table, the maximum amount of time
    // in seconds that they can think for, and the maximum amount of time in
    // seconds that they can eat for. Note that each fork is an Object
    // reference, as the philosophers will synchronize on them to simulate that
    // they have been picked up.
    StructuredDiningPhilosophers(int numPhilos, int maxThink, int maxEat) {
        NUM_PHILOS = numPhilos;
        MAX_THINK = maxThink;
        MAX_EAT = maxEat;
        forks = new Object[NUM_PHILOS];
        for(int i = 0; i < NUM_PHILOS; i++)
            forks[i] = new Object();
        philosophers = new Thread[NUM_PHILOS];
        for(int i = 0; i < NUM_PHILOS; i++) {
            try {
                // all philosophers pick up their left fork first
                philosophers[i]
                        = new Thread(new Philosopher(i + 1, forks[i], forks[i + 1]));
            } catch(IndexOutOfBoundsException e) {
                // except the last philosopher
                // NOTE : this philosopher will still say they are picking up
                // the left fork first but in reality, it is the right fork
                // ALSO NOTE : forcing the last philosopher to pick up their
                // right fork first guarantees that a deadlock cannot occur
                philosophers[i]
                        = new Thread(new Philosopher(i + 1, forks[0], forks[i]));
            }
        }
    }
    
    // Philosopher : A class which simulates a dining philosopher. Each
    // philosopher has an ID as well as both a left and right fork to choose
    // from.
    private class Philosopher implements Runnable {
        
        private final int id;
        private final Object left;
        private final Object right;
        
        // Philosopher : A constructor method which initializes their ID and
        // two respective forks.
        Philosopher(int id, Object left, Object right) {
            this.id = id;
            this.left = left;
            this.right = right;
        }
        
        // think : A method which causes the philosopher to think (sleep) for
        // a random amount of seconds between 1 and MAX_THINK, inclusive. When
        // this method is interrupted, it throws another interrupt to the
        // calling method, presumably run(), so it can be handled there as well.
        private void think(int loop) throws InterruptedException {
            int thinkSeconds
                    = ThreadLocalRandom.current().nextInt(1, MAX_THINK + 1);
            try {
                System.out.println("(" + loop + ") Philosopher " + id + ": "
                        + "Attempted to think for " + thinkSeconds
                        + " seconds.");
                Thread.sleep(thinkSeconds * 1000);
                System.out.println("(" + loop + ") Philosopher " + id + ": "
                        + "Thought for " + thinkSeconds + " seconds.");
            } catch(InterruptedException e) {
                throw new InterruptedException("(" + loop + ") Philosopher "
                        + id + ": Failed to think for " + thinkSeconds
                        + " seconds. (DINNER IS OVER)");
            }
        }
        
        // eat : A method which causes the philosopher to eat (sleep) for a
        // random amount of seconds between 1 and MAX_EAT, inclusive. When this
        // method is interrupted, it throws another interrupt to the calling
        // method, presumably run(), so it can be handled there as well.
        private void eat(int loop) throws InterruptedException {
            int eatSeconds
                    = ThreadLocalRandom.current().nextInt(1, MAX_EAT + 1);
            try {
                System.out.println("(" + loop + ") Philosopher " + id + ": "
                        + "Attempted to eat for " + eatSeconds + " seconds.");
                Thread.sleep(eatSeconds * 1000);
                System.out.println("(" + loop + ") Philosopher " + id + ": "
                        + "Ate for " + eatSeconds + " seconds.");
            } catch(InterruptedException e) {
                throw new InterruptedException("(" + loop + ") Philosopher "
                        + id + ": Failed to eat for " + eatSeconds
                        + " seconds. (DINNER IS OVER)");
            }
        }
        
        // run : Specifies the behavior of the dining philosopher. Specifically,
        // the philosopher tries to pick up their left fork, picks it up when it
        // is available, then tries to pick up their right fork, picks it up
        // when it is available, then eats, then puts the right fork down, then
        // puts the left fork down, then thinks. This process repeats until an
        // interrupt is caught.
        @Override
        public void run() {
            int loop = 0;       // keeps track of number of while-loop runs/eats
            try {
                while(true) {
                    System.out.println("(" + loop + ") Philosopher " + id 
                            + ": Attempted to acquire left fork.");
                    synchronized(left) {
                        System.out.println("(" + loop + ") Philosopher " + id 
                                + ": Acquired left fork.");
                        System.out.println("(" + loop + ") Philosopher " + id 
                                + ": Attempted to acquire right fork.");
                        synchronized(right) {
                            System.out.println("(" + loop + ") Philosopher " 
                                    + id + ": Acquired right fork.");
                            eat(loop);
                            System.out.println("(" + loop + ") Philosopher " 
                                    + id + ": Attempted to release right fork.");
                        }
                        System.out.println("(" + loop + ") Philosopher " + id 
                                + ": Released right fork.");
                        System.out.println("(" + loop + ") Philosopher " + id 
                                + ": Attempted to release left fork.");
                    }
                    System.out.println("(" + loop + ") Philosopher " + id 
                            + ": Released left fork.");
                    think(loop);
                    loop++;
                }
            } catch(InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        
    }
    
    // dine : A method which facilitates a structured dinner for an amount of
    // time in seconds.
    public void dine(int dineTime) {
        System.out.println(NUM_PHILOS + " philosophers will have a structured "
                + "dinner for " + dineTime + " seconds.");
        
        for(int i = 0; i < NUM_PHILOS; i++)
            philosophers[i].start();
        
        try {
            Thread.sleep(dineTime * 1000);
        } catch(InterruptedException e) {
            System.out.println("!!! ERROR: Failed to finish dining. !!!");
        }
        
        for(int i = 0; i < NUM_PHILOS; i++)
            philosophers[i].interrupt();
        
        try {
            for(int i = 0; i < NUM_PHILOS; i++)
                philosophers[i].join();
        } catch(InterruptedException e) {
            System.out.println("!!! ERROR: Failed to join all threads. !!!");
        }
    }
    
}
