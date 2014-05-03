
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main thread running on the server side, it basically creates an
 * server socket and constantly listens from a fixed port, whenever a new
 * players comes, pass it to its own thread to handle all following commands.
 */
public class Part_III_Server {

    /**
     * Greeting
     */
    public static final String greeting = "Welcome to the Nim Game!";
    
    /**
     * This hashmap stores all players that is currently connected to this
     * server, by player id as the key
     */
    public static HashMap<String, Player> players;
    
    /**
     * This hashmap stores all games that is currently running on the server, by
     * key of gameId.
     */
    public static HashMap<String, Game> games;
    
    /**
     * As every game has a unique game id, it is automatically incremented every
     * time a game is constructed.
     */
    public static int id;
    
    /**
     * The listening socket of the server.
     */
    public static ServerSocket serverSocket;

    /**
     * this is the main method of the server side, it starts constructing and
     * initializing everything
     *
     * @param args
     */
    public static void main(String[] args) {
    	
        try {
        	
            //construct a serversocket that listening to user connectiong
            serverSocket = new ServerSocket(7992);
        } catch (IOException ex) {
            Logger.getLogger(Part_III_Server.class.getName()).log(Level.SEVERE, null, ex);
        }

        //initialize players and games hashmap
        players = new HashMap<>();
        games = new HashMap<>();
        
        //infinite loop, whenever a client connected to it, it creates a new player socket
        //and pass the player with the socket to its own thread
        while (true) {
        	
            try {
            	
                serverSocket = getServerSocket();
                //create a player socket whenever a client is connected to the server
                
                Socket playerOneSocket = serverSocket.accept();
                //create a bufferedreader as user input interface
                
                BufferedReader playerOneInput = new BufferedReader(new InputStreamReader(playerOneSocket.getInputStream()));
                
                //also a printwriter for reply
                PrintWriter playerOneReply = new PrintWriter(playerOneSocket.getOutputStream());
                
                //let the player handled by its own thread
                new Thread(new Player(playerOneSocket, playerOneInput, playerOneReply)).start();
                
            } catch (IOException ex) {
                Logger.getLogger(Part_III_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This help method is to return the server socket without any possibilities
     * that it could be reconstructed.
     *
     * @return a ServerSocket that if it has already been constructed, itself
     * otherwise, construct a new one.
     */
    public static ServerSocket getServerSocket() {
    	
        if (serverSocket == null) {
        	
            try {
                serverSocket = new ServerSocket(7992);
            } catch (IOException ex) {
                Logger.getLogger(Part_III_Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return serverSocket;
    }
}
