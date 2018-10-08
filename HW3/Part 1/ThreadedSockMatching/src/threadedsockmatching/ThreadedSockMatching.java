/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : 3-1
 * DUE         : 8 October 2018
 * DESCRIPTION : Simulates the production, matching, and washing of socks
 *               using multithreading and blocking queues.
 ******************************************************************************/

package threadedsockmatching;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadLocalRandom;

public class ThreadedSockMatching {
    
    private static final int MAX_PRODUCIBLE = 100;
    
    private static final BlockingQueue<Sock> matchPile = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Sock> washPile = new LinkedBlockingQueue<>();
    
    private static final AtomicInteger[] colorsProduced = new AtomicInteger[4];
    private static final AtomicInteger[] colorsMatched = new AtomicInteger[4];
    private static final AtomicInteger[] colorsWashed = new AtomicInteger[4];
    
    private static boolean finishedProduction = false;
    private static boolean finishedMatching = false;
    
    private static class SockProducer implements Runnable {
        
        private final int EXPECTED_YIELD
                = ThreadLocalRandom.current().nextInt(1, MAX_PRODUCIBLE + 1);
        private int currentYield = 0;
        private int[] colorYield = new int[4];
        
        @Override
        public void run() {
            while(currentYield < EXPECTED_YIELD) {
                int colorCode = ThreadLocalRandom.current().nextInt(0, 4);
                try {
                    switch(colorCode) {
                        case 0:
                            matchPile.put(new Sock(Color.RED));
                            break;
                        case 1:
                            matchPile.put(new Sock(Color.ORANGE));
                            break;
                        case 2:
                            matchPile.put(new Sock(Color.GREEN));
                            break;
                        case 3:
                            matchPile.put(new Sock(Color.BLUE));
                            break;
                    }
                    currentYield++;
                    colorYield[colorCode]++;
                    colorsProduced[colorCode].incrementAndGet();
                } catch(InterruptedException e) { }
            }
            
            long producerID = Thread.currentThread().getId();
            for(int i = 0; i < 4; i++) {
                System.out.println("PRODUCER-" + producerID + ": Made "
                        + colorYield[i] + " " + colorToString(i) + " socks.");
            }
        }
        
    }
    
    private static class SockMatcher implements Runnable {
        
        private boolean match() {
            boolean matchFound = false;
            if(!matchPile.isEmpty()) {
                try {
                    Sock first = matchPile.take();
                    int mismatches = 0;
                    matchFound = false;

                    while(mismatches < matchPile.size()) {
                        Sock second = matchPile.take();

                        if(second.color() == first.color()) {
                            matchFound = true;
                            washPile.put(first);
                            washPile.put(second);
                            colorsMatched[colorToInt(first.color())]
                                    .incrementAndGet();
                            break;
                        } else {
                            matchPile.put(second);
                            mismatches++;
                        }
                    }

                    if(matchFound)
                        System.out.println("MATCHER: Sent "
                                + colorToString(first.color())
                                + " pair to washer.");
                    else
                        matchPile.put(first);
                } catch(InterruptedException e) { }
            }
            return matchFound;
        }
        
        @Override
        public void run() {
            while(!finishedProduction || matchPile.size() > 4)
                match();
            for(int i = 0; i < 2; i++)
                match();
        }
        
    }
    
    private static class SockWasher implements Runnable {
        
        private boolean wash() {
            boolean matchFound = false;
            if(!washPile.isEmpty()) {
                try {
                    Sock first = washPile.take();
                    int mismatches = 0;
                    matchFound = false;

                    while(mismatches < washPile.size()) {
                        Sock second = washPile.take();

                        if(second.color() == first.color()) {
                            matchFound = true;
                            colorsWashed[colorToInt(first.color())]
                                    .incrementAndGet();
                            break;
                        } else {
                            washPile.put(second);
                            mismatches++;
                        }
                    }

                    if(matchFound)
                        System.out.println("WASHER: Washed "
                                + colorToString(first.color())
                                + " pair.");
                    else
                        washPile.put(first);
                } catch(InterruptedException e) { }
            }
            return matchFound;
        }
        
        @Override
        public void run() {
            while(!finishedProduction || !finishedMatching
                    || washPile.size() > 0) {
                wash();
            }
        }
        
    }
    
    private static void printQueue(BlockingQueue<Sock> q) {
        for(Sock s : q)
            System.out.print(colorToString(s.color()) + " ");
        System.out.println();
    }
    
    private static String colorToString(Color color) {
        switch(color) {
            case RED:
                return "RED";
            case ORANGE:
                return "ORANGE";
            case GREEN:
                return "GREEN";
            case BLUE:
                return "BLUE";
            default:
                return "";
        }
    }
    
    private static String colorToString(int i) {
        switch(i) {
            case 0:
                return colorToString(Color.RED);
            case 1:
                return colorToString(Color.ORANGE);
            case 2:
                return colorToString(Color.GREEN);
            case 3:
                return colorToString(Color.BLUE);
            default:
                return "";
        }
    }
    
    private static int colorToInt(Color color) {
        switch(color) {
            case RED:
                return 0;
            case ORANGE:
                return 1;
            case GREEN:
                return 2;
            case BLUE:
                return 3;
            default:
                return -1;
        }
    }
    
    private static Color intToColor(int i) {
        switch(i) {
            case 0:
                return Color.RED;
            case 1:
                return Color.ORANGE;
            case 2:
                return Color.GREEN;
            default:
                return Color.BLUE;
        }
    }
    
    public static void main(String[] args) {
        try { 
            for(int i = 0; i < 4; i++) {
                colorsProduced[i] = new AtomicInteger();
                colorsMatched[i] = new AtomicInteger();
                colorsWashed[i] = new AtomicInteger();
            }
            
            int numProducers = 4;

            Thread[] producers = new Thread[numProducers];
            
            for(int i = 0; i < numProducers; i++)
                producers[i] = new Thread(new SockProducer());

            Thread matcher = new Thread(new SockMatcher());
            Thread washer = new Thread(new SockWasher());
            
            for(int i = 0; i < numProducers; i++)
                producers[i].start();
            
            matcher.start();
            washer.start();

            for(int i = 0; i < numProducers; i++)
                producers[i].join();
            
            finishedProduction = true;
            
            matcher.join();
            
            finishedMatching = true;
            
            washer.join();
            
            System.out.println("---------------------------------------");
            for(int i = 0; i < 4; i++) {
                String colorString = colorToString(i);
                System.out.println("TOTAL " + colorString
                        + " SOCKS PRODUCED: " + colorsProduced[i]);
                System.out.println("TOTAL " + colorString
                        + " PAIRS MATCHED: " + colorsMatched[i]);
                System.out.println("TOTAL " + colorString
                        + " PAIRS WASHED: " + colorsWashed[i]);
                
                Color thisColor = intToColor(i);
                int unmatched = 0;
                for(int j = 0; j < matchPile.size(); j++) {
                    try {
                        Sock sock = matchPile.take();
                        matchPile.put(sock);
                        if(sock.color() == thisColor) {
                            unmatched++;
                            break;
                        }
                    } catch(InterruptedException e) { }
                }
                System.out.println("TOTAL " + colorString
                        + " SOCKS REMAINING: " + unmatched);
                System.out.println("---------------------------------------");
            }
            
        } catch(Exception e) { }
        
    }
    
}
