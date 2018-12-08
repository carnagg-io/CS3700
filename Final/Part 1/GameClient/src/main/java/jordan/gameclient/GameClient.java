package jordan.gameclient;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.NullPointerException;
import java.net.UnknownHostException;
import java.io.IOException;

import java.net.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class GameClient extends JFrame {
    
    private JTextArea _events;
    private JList _moves;
    private JButton _submit;
    
    private Socket _socket;
    
    private BufferedReader _input;
    private PrintWriter _output;
    
    GameClient() {
        _events = new JTextArea(5, 75);
        _events.setEditable(false);
        
        _moves = new JList(GameMove.values());
        _moves.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _moves.setLayoutOrientation(JList.VERTICAL);
        
        _submit = new JButton("Waiting For Players");
        _submit.setEnabled(false);
        _submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                GameMove selected = (GameMove)_moves.getSelectedValue();
                if(selected == null) {
                    JOptionPane.showMessageDialog(GameClient.this,
                            "Please select a move before playing.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JButton obj = (JButton)event.getSource();
                    obj.setEnabled(false);
                }
            }
        });
        
        JPanel contentPanel = new JPanel(new GridLayout(3, 1));
        contentPanel.add(new JScrollPane(_events,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        contentPanel.add(_moves);
        contentPanel.add(_submit);
        
        this.setTitle("Rock-Paper-Scissors Client");
        this.setContentPane(contentPanel);
        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
    
    private void connect() throws NullPointerException {
        try {
            String addrs[] = JOptionPane.showInputDialog(this,
                    "Welcome to Rock-Paper-Scissors!\n\n"
                            + "To enter the session, enter the socket address of\n"
                            + "the host server (e.g. 192.168.1.176:23):",
                    "Rock-Paper-Scissors Client Setup",
                    JOptionPane.QUESTION_MESSAGE).split(":");
            if(addrs.length == 2 && isValidIP(addrs[0]) && isValidPort(addrs[1])) {
                _socket = new Socket(addrs[0], Integer.valueOf(addrs[1]));
                _input = new BufferedReader(
                        new InputStreamReader(_socket.getInputStream()));
                _output = new PrintWriter(_socket.getOutputStream(), true);
                while(true) {
                    String line = _input.readLine();
                    if(line.startsWith(GameEvent.CONNECTED.toString())) {
                        _events.append(line + "\n");
                    } else if(line.startsWith(GameEvent.READY.toString())) {
                        _submit.setText("Play Move!");
                        _submit.setEnabled(true);
                        _events.append(line + "\n");
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid socket address.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                connect();
            }
        } catch(NullPointerException e) {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } catch(UnknownHostException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid socket address.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            connect();
        } catch(IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Connection timed out.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            connect();
        }
    }
    
    private boolean isValidIP(String ip) {
        String octets[] = ip.split("\\.");
        if(octets.length == 4) {
            for(int i = 0; i < 4; i++)
                if(!octets[i].matches("[0-9]+") || Integer.valueOf(octets[i]) > 255)
                    return false;
            return true;
        }
        return false;
    }
    
    private boolean isValidPort(String port) {
        return port.matches("[0-9]+") && Integer.valueOf(port) < 65536;
    }
    
    private void play() {
        
    }
    
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GameClient client = new GameClient();
        client.connect();
    }
    
}
