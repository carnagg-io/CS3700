/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 3-3
 * DUE         : 8 October 2018
 * DESCRIPTION : Simulates the ordered election of N "official" threads with
 *               the use of wait() and notify().
 ******************************************************************************/

package threadedelection;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadedElection {

    private static final int MAX_OFFICIALS = 100;
    private static final int MIN_RANK = Integer.MIN_VALUE;
    private static final int MAX_RANK = Integer.MAX_VALUE;
    
    private static final Object VOTE_LOCK = new Object();           // ranker waits on this lock to read candidate info
    private static final Object INFORM_LOCK = new Object();         // officials wait on this lock to read leader info

    private static Official leader;
    private static Official candidate;
    private static Thread ranker;
    private static ArrayList<Thread> officials;
    
    private static AtomicInteger numVoted;
    private static AtomicBoolean isWaiting;

    private static class Ranker implements Runnable {

        @Override
        public void run(){
            synchronized (VOTE_LOCK) {
                while (numVoted.get() < MAX_OFFICIALS) {
                    try {
                        VOTE_LOCK.wait();
                        if (candidate.rank() > leader.rank()) {
                            System.out.println("! NEW CANDIDATE SELECTED -> "
                                    + "Official-" + candidate.id()
                                    + ", Rank " + candidate.rank()
                                    + " has been chosen");
                            leader = candidate;
                            while(!isWaiting.get());
                            synchronized(INFORM_LOCK) {
                                for(int i = 0; i < numVoted.get(); i++)
                                    INFORM_LOCK.notifyAll();
                            }
                        } else {
                            synchronized(INFORM_LOCK) {
                                INFORM_LOCK.notify();
                            }
                        }
                    } catch (InterruptedException e) { }
                }
            }
            
            System.out.println("UPDATED BALLOTS FOR ALL VOTERS");
                for(int i = 0; i < numVoted.get(); i++)
                    officials.get(i).interrupt();
        
        }

    }

    private static class Official implements Runnable {

        private final int ID;
        private final int RANK;
        private Official vote;

        Official(int id, int rank) {
            ID = id;
            RANK = rank;
            vote = this;
            synchronized(VOTE_LOCK) {
                numVoted.incrementAndGet();
                candidate = vote;
                System.out.println("+ NEW BALLOT CAST -> Official-" + ID
                        + ", Rank " + RANK + " has voted for Official-"
                        + vote.id() + ", Rank " + vote.rank()
                        + " (Total Ballots: " + numVoted + ")");
                VOTE_LOCK.notify();
            }
        }

        public int id() {
            return ID;
        }

        public int rank() {
            return RANK;
        }

        public void printBallot() {
            System.out.print("---------------------------------------------\n");
            System.out.print("VOTER ID: " + ID + "   VOTER RANK: " + RANK + "\n");
            System.out.print("CANDIDATE ID: " + vote.id()
                    + "   CANDIDATE RANK: " + vote.rank() + "\n");
            System.out.print("---------------------------------------------\n");
        }

        @Override
        public void run() {
            synchronized (INFORM_LOCK) {
                while (numVoted.get() < MAX_OFFICIALS) {
                    try {
                        isWaiting.set(true);
                        INFORM_LOCK.wait(1000);
                        vote = leader;
                        System.out.println("- BALLOT UPDATED -> Official-" + ID
                                + ", Rank " + RANK + " is now voting for Official-"
                                + vote.id() + ", Rank " + vote.rank());
                    } catch (InterruptedException e) {
                        vote = leader;
                        System.out.println("= BALLOT FINALIZED -> Official-" + ID
                                + ", Rank " + RANK + " is now voting for Official-"
                                + vote.id() + ", Rank " + vote.rank());
                    } catch(Exception e) {
                        
                    }
                }
            }
        }
        
    }

    public static void main(String[] args) {
        numVoted = new AtomicInteger(-2);
        isWaiting = new AtomicBoolean(false);
        leader = new Official(-1, MIN_RANK);
        candidate = new Official(-1, MIN_RANK);
        ranker = new Thread(new Ranker());
        officials = new ArrayList<>();
        
        ranker.start();
        for (int i = 0; i < MAX_OFFICIALS; i++) {
            isWaiting.set(false);
            officials.add(new Thread(new Official(i, ThreadLocalRandom.current().nextInt(MIN_RANK, MAX_RANK))));
            officials.get(i).start();
        }
        try {
            ranker.join();
            for(int i = 0; i < MAX_OFFICIALS; i++)
                officials.get(i).join();
        } catch(InterruptedException e) { }
    }

}