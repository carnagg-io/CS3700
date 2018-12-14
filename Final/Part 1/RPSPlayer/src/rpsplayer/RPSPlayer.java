package rpsplayer;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RPSPlayer {
    
    /*  DEFAULT_ADDR: Default multicast address.
        DEFAULT_PORT: Default UDP port number.
        NUM_ROUNDS  : Number of rounds to play.
        RANDOM      : A Random object to generate random numbers.
        ID          : This player's ID.
        PLAYERS     : Hash map where K-V is ID-plays. */
    private static final String DEFAULT_ADDR = "228.5.6.7";
    private static final int DEFAULT_PORT = 5555;
    private static final int NUM_ROUNDS = 10;
    private static final Random RANDOM = new Random();
    private static final String ID = randomID();
    private static final HashMap<String, ArrayList<String>> PLAYERS = new HashMap<>();
    
    /*  _output: String to be output by sender, modified by receiver.
        _round : Current round. */
    private static AtomicReference<String> _output = new AtomicReference<>("JOIN " + ID);
    private static int _round = 1;
    
    
    /* RPSPlayer: Constructor which starts the sending and receiving threads. */
    public RPSPlayer(final int port, final String addr) {
        System.out.printf("[INFO] Assigned self ID = %s.", ID);
        System.out.printf("[INFO] Attempting to listen on port %d.\n", port);
        try {
            Thread receiver = new Thread(new ReceiverThread(port, addr));
            Thread sender = new Thread(new SenderThread(port, addr));
            receiver.start();
            sender.start();
            try {
                receiver.join();
                sender.join();
            } catch(Exception f) {
                System.out.println("[ERROR] Exception while joining threads.");
            }
        } catch(Exception e) {
            System.out.println("[ERROR] Exception while starting threads.");
        }
    }
    
    /* ReceiverThread: A class which implements a server-like thread for
       listening. */
    public static class ReceiverThread implements Runnable {
        
        private MulticastSocket socket;
        
        ReceiverThread(final int port, final String addr) throws Exception {
            socket = new MulticastSocket(port);
            socket.joinGroup(InetAddress.getByName(addr));
        }
        
        @Override
        public void run() {
            int count = 0;
            String nextMove = randomMove();
            while(_round <= NUM_ROUNDS) {
                byte buffer[] = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch(IOException e) {
                    System.out.println("[ERROR] IOException caught while "
                                    + "receiving packet.");
                }
                String message = new String(packet.getData(), 0, packet.getLength());
                if(message.startsWith("JOIN")) {
                    String id = message.substring(5);
                    if(!PLAYERS.containsKey(id) && PLAYERS.size() < 3) {
                        if(!id.equals(ID))
                            System.out.println("[JOIN] " + id + " has joined "
                                    + "the session.");
                        else
                            System.out.println("[JOIN] " + id + " (self) has "
                                    + "joined the session.");
                        PLAYERS.put(id, new ArrayList<>());
                    } else if(PLAYERS.size() == 3 && _round == 1) {
                        try {
                            Thread.sleep(2000);
                            _output.set("PLAY " + _round + " " + ID + " " + nextMove);
                        } catch(InterruptedException e) {
                            System.out.println("[ERROR] InterruptedException "
                                    + "caught while sleeping on JOIN.");
                        }
                    }
                } else if(message.startsWith("PLAY")) {
                    String id = message.split(" ")[2];
                    ArrayList<String> plays = PLAYERS.get(id);
                    if(plays.size() < _round) {
                        nextMove = randomMove();
                        String move = message.split(" ")[3];
                        if(!id.equals(ID))
                            System.out.println("[ROUND " + _round + "] " + id
                                    + " has played " + move + ".");
                        else
                            System.out.println("[ROUND " + _round + "] " + id
                                    + " (self) has played " + move + ".");
                        plays.add(move);
                        PLAYERS.put(id, plays);
                        count++;
                    } else if(roundFinished(_round)) {
                        count = 0;
                        _round++;
                        try {
                            Thread.sleep(2000);
                            _output.set("PLAY " + _round + " " + ID + " " + nextMove);
                        } catch(InterruptedException e) {
                            System.out.println("[ERROR] InterruptedException "
                                    + "caught while sleeping on PLAY.");
                        }
                    }
                }
            }
            printResults();
        }
        
    }
    
    /* SenderThread: A class which implements a client-like thread for
       broadcasting. */
    public static class SenderThread implements Runnable {
        
        private final int PORT;
        private final String ADDR;
        private MulticastSocket socket = null;
        
        SenderThread(final int port, final String addr) throws Exception {
            PORT = port;
            ADDR = addr;
            socket = new MulticastSocket();
        }
        
        @Override
        public void run() {
            while(_round <= NUM_ROUNDS) {
                byte buffer[] = _output.get().getBytes();
                try {
                    socket = new MulticastSocket();
                    DatagramPacket packet = new DatagramPacket(buffer,
                            buffer.length, InetAddress.getByName(ADDR), PORT);
                    socket.send(packet);
                    Thread.sleep(1000);
                } catch(UnknownHostException e) {
                    System.out.println("[ERROR] UnknownHostException caught in "
                            + "sender thread.");
                } catch(IOException e) {
                    System.out.println("[ERROR] IOException caught in sender "
                            + "thread.");
                } catch(InterruptedException e) {
                    System.out.println("[ERROR] InterruptedException caught in "
                            + "sender thread.");
                }
            }
                
        }
        
    }
    
    /* randomID: Returns a random integer as a string for an ID. */
    private static String randomID() {
        return String.valueOf(Math.abs(RANDOM.nextInt()));
    }
    
    /* randomMove: Generates a random rock-paper-scissors move. */
    private static String randomMove() {
        int rnd = RANDOM.nextInt() % 3;
        switch(rnd) {
            case 0:
                return "ROCK";
            case 1:
                return "PAPER";
            default:
                return "SCISSORS";
        }
    }
    
    /* roundFinished: Returns a Boolean value based on whether players have
       played a number of movies equal to the number of rounds passed in. */
    private static boolean roundFinished(int round) {
        for(Map.Entry<String, ArrayList<String>> entry : PLAYERS.entrySet())
            if(entry.getValue().size() != round)
                return false;
        return true;
    }
    
    /* getCounter: Returns the move that loses to the move passed in. */
    private static String counterMove(String move) {
        switch(move) {
            case "ROCK":
                return "SCISSORS";
            case "PAPER":
                return "ROCK";
            case "SCISSORS":
                return "PAPER";
            default:
                return "ERROR";
        }
    }
    
    /* printResults: Prints the results after the game has finished. This only 
       appears in the console. */
    private static void printResults() {
        HashMap<String, Integer> scores = new HashMap<>();
        for(Map.Entry<String, ArrayList<String>> entry : PLAYERS.entrySet())
            scores.put(entry.getKey(), 0);
        
        for(int i = 0; i < NUM_ROUNDS; i++) {
            HashMap<String, String> plays = new HashMap<>();
            for(Map.Entry<String, ArrayList<String>> entry : PLAYERS.entrySet())
                plays.put(entry.getKey(), entry.getValue().get(i));
            
            ArrayList<String> roundWinners = getWinners(plays);
            if(roundWinners.isEmpty())
                continue;
            int points = roundWinners.size() == 1 ? 2 : 1;
            
            for(int j = 0; j < roundWinners.size(); j++)
                scores.put(roundWinners.get(j), scores.get(roundWinners.get(j)) + points);
        }
        
        for(Map.Entry<String, Integer> player : scores.entrySet())
            System.out.println("[RESULT] " + player.getKey() + " earned "
                    + player.getValue() + " points total.");
    }
    
    /* getWinners: Determines the winners of the round given their plays. */
    private static ArrayList<String> getWinners(HashMap<String, String> plays) {
        int totalCounters = 0;
        HashMap<String, Integer> counters = new HashMap<>();
        for(Map.Entry<String, ArrayList<String>> entry : PLAYERS.entrySet())
            counters.put(entry.getKey(), 0);
        
        // for each player
        for(Map.Entry<String, String> player : plays.entrySet()) {
            for(Map.Entry<String, String> opponent : plays.entrySet()) {
                if(!opponent.getKey().equals(player.getKey())
                        && opponent.getValue().equals(counterMove(player.getValue()))) {
                    counters.put(player.getKey(), counters.get(player.getKey()) + 1);
                    totalCounters++;
                }
            }
        }
        
        if(totalCounters != 2)
            return new ArrayList<>();
        
        ArrayList<String> winners = new ArrayList<>();
        for(Map.Entry<String, Integer> player : counters.entrySet())
            if(player.getValue() > 0)
                winners.add(player.getKey());
        
        return winners;
    }
    
    public static void main(String args[]) {
        // TODO - add commandline argument capability
        new RPSPlayer(DEFAULT_PORT, DEFAULT_ADDR);
    }
    
}