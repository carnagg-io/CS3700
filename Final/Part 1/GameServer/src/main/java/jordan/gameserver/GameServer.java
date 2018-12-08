package jordan.gameserver;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class GameServer {
    
    private static final int MAX_PLAYERS = 2;
    private static GameMove[] _plays = {null, null};
    
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(23);
        Player players[] = new Player[MAX_PLAYERS];
        try {
            for(int i = 0; i < MAX_PLAYERS; i++)
                players[i] = new Player(i + 1, listener.accept());
            for(int i = 0; i < MAX_PLAYERS; i++)
                players[i].run();
        } finally {
            listener.close();
        }
        
    }
    
    private static class Player implements Runnable {
        final int id;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        
        int points;
        
        Player(int id, Socket socket) throws Exception {
            this.id = id;
            this.socket = socket;
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println(GameEvent.CONNECTED + " Connection successfully established. id = " + id);
            System.out.println(GameEvent.CONNECTED + " Connection successfully established. id = " + id);
            points = 0;
        }
        
        @Override
        public void run() {
            output.println(GameEvent.READY + " The session is full. Begin.");
            System.out.println(GameEvent.READY + " The session is full. Begin.");
            while(true);
        }
        
    }
    
}
