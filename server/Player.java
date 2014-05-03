
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;

/**
 * This class defines a player and all its properties, and also it can be a
 * runnable thread, which can handle user input before the player enters a game.
 */
public class Player implements Runnable {

    /**
     * A unique id that a user defined.
     */
    private String uID;
    
    /**
     * To indicated whether the player is available.
     */
    private boolean available;
    
    /**
     * Stores when the date when the player logs onto the server.
     */
    private Date loginTime;
    
    /**
     * The socket used to communicated with the client.
     */
    private Socket playerSocket;
    
    /**
     * Input interface
     */
    private BufferedReader playerReceiver;
    
    /**
     * Output interface
     */
    private PrintWriter playerSender;
    
    /**
     * If it has logged in with a name
     */
    private boolean loggedIn;

    /**
     * Constructor with the following values, and also initialize it to
     * available, record the login time, and initialize the logged in to false,
     * because he has not input his uid yet
     *
     * @param playerSocket
     * @param playerReceiver
     * @param playerSender
     */
    public Player(Socket playerSocket,
            BufferedReader playerReceiver, PrintWriter playerSender) {
    	
        this.available = true;
        this.loginTime = new Date();
        this.playerSocket = playerSocket;
        this.playerReceiver = playerReceiver;
        this.playerSender = playerSender;
        this.loggedIn = false;
    }

    /**
     * Accessor method gets the communication socket of the player.
     *
     * @return a socket that can be used to communicated with the client
     */
    public Socket getPlayerSocket() {
        return playerSocket;
    }

    /**
     * Accessor method that gets the bufferedreader as user input interface.
     *
     * @return a BufferedReader instance as user input interface
     */
    public BufferedReader getPlayerReceiver() {
        return playerReceiver;
    }

    /**
     * Accessor method that gets the PrintWriter as user output interface.
     *
     * @return a PrintWriter instance as user output interface
     */
    public PrintWriter getPlayerSender() {
        return playerSender;
    }

    /**
     * Accessor method that returns the uID of the player.
     *
     * @return String type user unique id
     */
    public String getuID() {
        return uID;
    }

    /**
     * To check if the player is available.
     *
     * @return true if the player is not in a game
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * To set the availability of the player.
     *
     * @param available availability to set
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Accessor method to get the time when the player connected to the server.
     *
     * @return Date type indicated when did the player connected to the server
     */
    public Date getLoginTime() {
        return loginTime;
    }

    /**
     * This run method of this class is to be called when a thread of it is
     * invoked it handles every user command before he enters a game.
     */
    @Override
    public void run() {
    	
        //make sure the user is not in a game, in which case the command will be handled
        //by the game class
        while (available) {
        	
            try {
            	
                //get the user input
                String[] command = playerReceiver.readLine().split(" ");
                
                //if the user wants to login, do the following
                if (command[0].equalsIgnoreCase("login")) {
                	
                    //extract the user wanted unique id as a string
                    //and assigin the name to the player instance
                    this.uID = command[1];
                    
                    //changed the state of the player
                    loggedIn = true;
                    
                    //send comfirmation to the player
                    playerSender.println("logged in with name " + uID);
                    playerSender.flush();
                    
                    //if the uid already exists, let the user know and let the user
                    //to input again
                    if (Part_III_Server.players.containsKey(uID)) {
                        playerSender.println("Error 104: the username already exists!");
                        playerSender.flush();
                        continue;
                    }
                    
                    //put the fully constructed player in the hashmap of the server
                    Part_III_Server.players.put(uID, this);
                    
                } //if the player wants to check onging games
                else if (command[0].equalsIgnoreCase("games")) {
                	
                    //make sure the player is already logged in
                    if (loggedIn) {
                    	
                        //give a list of onging games
                        displayGames(playerSender);
                    } else {
                    	
                        //inform the user with error message
                        playerSender.println("Error 103: log in first: ");
                    }
                } //if the player wants to see who is available, do the following
                else if (command[0].equalsIgnoreCase("who")) {
                	
                    //make sure the player has already logged in,
                    //give a list of available players
                    if (loggedIn) {
                    	
                        displayPlayers(playerSender);
                    } //inform the user with error message
                    else {
                        playerSender.println("Error 103: log in first: ");
                        playerSender.flush();
                    }
                    
                } //if the player wants to play with someone, do the following
                else if (command[0].equalsIgnoreCase("play") && loggedIn) {
                	
                    //if the opponent is available, set both of them to unavailable and start a game
                    if (Part_III_Server.players.containsKey(command[1]) && !command[1].equalsIgnoreCase(uID) && Part_III_Server.players.get(command[1]).isAvailable()) {
                    	
                        this.available = false;
                        Part_III_Server.players.get(command[1]).setAvailable(false);
                        Game newGame = new Game(this, Part_III_Server.players.get(command[1]), Part_III_Server.id);
                        
                        //add the new game into the hashmap on the server
                        Part_III_Server.games.put(String.valueOf(Part_III_Server.id), newGame);
                        
                        //increment the game id
                        Part_III_Server.id++;
                        
                        //start the game
                        new Thread(newGame).start();
                        
                        //stop the player thread
                        break;
//                        playerSender.println(Thread.interrupted());
//                        playerSender.flush();
                        
                    } //if the opponent is not exist or unavailable, inform the user for another input
                    else {
                    	
                        playerSender.println("Error 102: invalid opponent name or opponent not available");
                        playerSender.flush();
                        this.available = true;
                    }
                    
                } //the user should not issue a remove commmand here, so inform the user
                else if (command[0].equalsIgnoreCase("remove") && loggedIn) {
                	
                    playerSender.println("not supported, start a game first");
                    playerSender.flush();
                } //if the user wants to observe a game, do the following
                else if (command[0].equalsIgnoreCase("observe") && loggedIn) {
                	
                    //let the user know about it
                    playerSender.println("Start observing a game...");
                    playerSender.flush();
                    
                    //add the user into the observers list of the game
                    if (Part_III_Server.games.containsKey(command[1])) {
                        Part_III_Server.games.get(command[1]).getObserver().add(this);
                    } //if the game does not exist, inform the user
                    else {
                    	
                        playerSender.println("invalid game id, retry");
                        playerSender.flush();
                    }
                    
                } //if the user wants to unobserve,
                else if (command[0].equalsIgnoreCase("unobserve") && loggedIn) {
                	
                    //let the user know what he is doing
                    playerSender.println("Stop observing the game...");
                    playerSender.flush();
                    
                    //remove the user from the observer list of the game
                    if (Part_III_Server.games.containsKey(command[1])) {
                        Part_III_Server.games.get(command[1]).getObserver().remove(this);
                    } //inform the user about the error
                    else {
                    	
                        playerSender.println("invalid game id, retry");
                        playerSender.flush();
                    }
                } // in any other cases, tell the user that it is an invalid command
                else {
                	
                    playerSender.println("invalid command, no comamnd handled");
                    playerSender.flush();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * This help method is to display a list of games for the user to choose.
     *
     * @param pw the printwriter instance used to communicated with the user
     */
    public static void displayGames(PrintWriter pw) {
    	
        //get the game instance
        Iterator<Game> playerIterator = Part_III_Server.games.values().iterator();
        
        //table heads
        pw.println("Game ids:       usernames:");
        pw.flush();
        
        //loop every instance
        while (playerIterator.hasNext()) {
        	
            //game information for each game
            Game temp = playerIterator.next();
            pw.println(temp.getGameID() + "               " + 
                    temp.getPlayer1().getuID() + " vs " + 
                    temp.getPlayer2().getuID());
            pw.flush();
        }
    }

    /**
     * This help method is to display a list of available players to the user to
     * play with.
     *
     * @param pw the printwriter instance used to communicated with the user
     */
    public static void displayPlayers(PrintWriter pw) {
    	
        //if there is currently no players in the list, tell the user
        if (Part_III_Server.players.isEmpty()) {
        	
            pw.println("no players");
            pw.flush();
        } //get the players
        else {
        	
            //iterator of players 
            Iterator<Player> playerIterator = Part_III_Server.players.values().iterator();
            
            //loop for each player
            while (playerIterator.hasNext()) {
            	
                Player temp = playerIterator.next();
                
                //if the player is available, display the user name
                if (temp.isAvailable()) {
                    pw.println(temp.getuID());
                    pw.flush();
                }
            }
        }
    }
}
