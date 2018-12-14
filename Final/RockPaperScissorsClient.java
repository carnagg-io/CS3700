package rockpaperscissorsclient;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class RockPaperScissorsClient {
    
    private static final int DEFAULT_PORT = 5555;
    private static final String DEFAULT_ADDR = "228.5.6.7";
    private static final int DELAY = 1000;
    private static final Random RANDOM = new Random();
    private static final int PLAYERS = 3;
    private static final int NUM_ROUNDS = 10;
    
    private static String _id;
    private static ArrayList<String> _hosts;
    private static AtomicReference<String> _send;
    
    public RockPaperScissorsClient(final int port, final String addr) {
        _id = String.valueOf(Math.abs(RANDOM.nextInt()));
        _hosts = new ArrayList<>();
        _send = new AtomicReference<>("HST " + _id);
        System.out.printf(">SYSMSG: Acquired new host ID = %s.\n", _id);
        System.out.printf(">SYSMSG: Attempting to listen on port %d.\n", port);
        try {
            Thread receiver = new Thread(new ReceiverThread(port, addr));
            Thread sender = new Thread(new SenderThread(port, addr));
            
            receiver.start();
            sender.start();
            
            try {
                receiver.join();
                sender.join();
            } catch(Exception f) { }
        } catch(Exception e) {
            System.out.println(">SYSERR: Exception in constructor.");
            e.printStackTrace();
        }
        
    }
    
    public static class ReceiverThread implements Runnable {
        
        private MulticastSocket socket = null;
        
        ReceiverThread(final int port, final String addr) throws Exception {
            socket = new MulticastSocket(port);
            socket.joinGroup(InetAddress.getByName(addr));
        }
        
        @Override
        public void run() {
            int currentRound = 1;
            String currentMove = randomMove();
            HashMap<String, String> currentMsgs = new HashMap<>();
            while(currentRound <= NUM_ROUNDS) {
                byte buffer[] = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                } catch(IOException e) {
                    System.out.println(">SYSERR: IOException caught in receiver "
                            + "thread.");
                }
                String message = new String(packet.getData(), 0, packet.getLength());
                if(message.startsWith("HST")) {
                    String id = message.substring(4);
                    if(!idInList(_hosts, id)) {
                        if(!id.equals(_id))
                            System.out.println(">SYSMSG: Host ID = " + id + " has joined the session.");
                        else
                            System.out.println(">SYSMSG: Host ID = " + id + " (self) has joined the session.");
                        _hosts.add(id);
                    }
                    if(_hosts.size() == PLAYERS) {
                        try {
                            Thread.sleep(PLAYERS * DELAY);
                            _send.set("PLY " + currentRound + " " + _id + " " + currentMove);
                        } catch(InterruptedException e) { }
                    }
                } else if(message.startsWith("PLY")) {
                    String id = message.split(" ")[2];
                    if(!idInMap(currentMsgs, id)) {
                        String move = message.split(" ")[3];
                        if(!id.equals(_id))
                            System.out.println(">RPSMSG: Host ID = " + id + " played " + move + " in round " + currentRound + ".");
                        else
                            System.out.println(">RPSMSG: Host ID = " + id + " (self) played " + move + " in round " + currentRound + ".");
                        currentMsgs.put(id, move);
                    }
                }
            }
        }
        
        private boolean idInList(ArrayList<String> list, String id) {
            for(String member : list)
                if(member.equals(id))
                    return true;
            return false;
        }
        
        private boolean idInMap(HashMap<String, String> map, String id) {
            for(Map.Entry<String, String> entry : map.entrySet())
                if(entry.getKey().equals(id))
                    return true;
            return false;
                    
        }
    }
    
    public static class SenderThread implements Runnable {
        
        private int port;
        private String addr;
        private MulticastSocket socket;
        
        SenderThread(final int port, final String addr) throws IOException {
            this.port = port;
            this.addr = addr;
            socket = new MulticastSocket();
        }
        
        @Override
        public void run() {
            while(true) {
                byte buffer[] = _send.get().getBytes();
                try {
                    socket = new MulticastSocket();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(addr), port);
                    socket.send(packet);
                    Thread.sleep(DELAY);
                } catch(UnknownHostException e) {
                    System.out.println(">SYSERR: UnknownHostException caught in "
                            + "sender thread.");
                } catch(IOException e) {
                    System.out.println(">SYSERR: IOException caught in sender "
                            + "thread.");
                } catch(InterruptedException e) {
                    System.out.println(">SYSERR: InterruptedException caught in "
                            + "sender thread.");
                }
            }
                
        }
        
    }
    
    private static String randomMove() {
        switch(RANDOM.nextInt(3)) {
            case 0:
                return "ROCK";
            case 1:
                return "PAPER";
            default:
                return "SCISSORS";
        }
    }
    
    public static void main(String args[]) {
        new RockPaperScissorsClient(DEFAULT_PORT, DEFAULT_ADDR);
    }
    
}