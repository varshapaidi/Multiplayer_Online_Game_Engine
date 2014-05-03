
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the core of this project, it handles the majority of the game
 * logic and take reactions.
 */
public class Game implements Runnable {

    /**
     * How many sets in the game.
     */
    private int setNum;
    
    /**
     * An array to tell how many objects are stored in each set.
     */
    private int[] remain;
    
    /**
     * Total objects left.
     */
    private int objectLeft;
    
    /**
     * Two player objects taking part in the game.
     */
    private Player player1;
    private Player player2;
    
    /**
     * To indicate if it is the first players' turn.
     */
    private boolean player1Turn;
    
    /**
     * The unique id of the game object.
     */
    private int gameID;
    
    /**
     * An arraylist to store all potential observers.
     */
    private ArrayList<Player> observer;
    
    /**
     * Tells if the other player has issued a bye command.
     */
    private boolean stops;

    /**
     * The constructor of the an game object, it generates the random sets and
     * random number of objects in each set, and then initialize other fields
     *
     * @param player1 the first player of the game
     * @param player2 the second player
     * @param id the unique id of the game
     */
    public Game(Player player1, Player player2, int id) {
    	
        //generate random number of sets
        setNum = (int) ((Math.random() * 3) + 3);
        
        //create an array which size is the same as the number of sets
        remain = new int[setNum];
        
        //assign random number of object to each set
        for (int i = 0; i < setNum; i++) {
            remain[i] = 1 + (int) (Math.random() * 7);
            objectLeft += remain[i];
        }
        
//        //determine if it should be player1's turn
//        if (player1.getLoginTime().before(player2.getLoginTime())) {
//            player1Turn = true;
//        } else {
//            player1Turn = false;
//        }
        
        this.player1Turn = true;
        this.player1 = player1;
        this.player2 = player2;
        this.gameID = id;
        player1.getPlayerSender().println("ini");
        player1.getPlayerSender().flush();
        observer = new ArrayList<>();
        stops = false;
    }

    /**
     * Accessor to the first player
     *
     * @return player 1
     */
    public Player getPlayer1() {
        return player1;
    }

    /**
     * Accessor to the second player.
     *
     * @return
     */
    public Player getPlayer2() {
        return player2;
    }

    /**
     * To get the game id of the game.
     *
     * @return the unique game id
     */
    public int getGameID() {
        return gameID;
    }

    /**
     * To indicate if it is the first player's turn now.
     *
     * @return a boolean value, whether or not it is the first player's turn
     */
    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    /**
     * To indicate the server who's turn it is now by setting whether it is player 1's
     * turn.
     *
     * @param player1Turn a boolean value that indicates whether it is player1's
     * turn
     */
    public void setPlayer1Turn(boolean player1Turn) {
        this.player1Turn = player1Turn;
    }

    /**
     * Accessor method to get who many set are there in the game.
     *
     * @return the number of sets in the game
     */
    public int getSetNum() {
        return setNum;
    }

    /**
     * Accessor method that returns the array of sets.
     *
     * @return the int array that contains the number of object left in each set
     */
    public int[] getRemain() {
        return remain;
    }

    /**
     * Accessor method that gets the total number of object left in the game.
     *
     * @return
     */
    public int getObjectLeft() {
        return objectLeft;
    }

    /**
     * Give a list of observers that are currently watching the game.
     *
     * @return an arraylist of observers watching the game
     */
    public ArrayList<Player> getObserver() {
        return observer;
    }

    /**
     * The basic operation of the game, to remove a specific number of objects
     * from a specific set.
     *
     * @param b how many object should be removed
     * @param a from which set
     * @return success or error message
     */
    public String remove(int b, int a) {
    	
        //because the sets in the array begins with 0 and for the user it begins with 1
        //so it needs to be decremented
        a--;
        
        //if it is not a valid set, return an error message
        if (a < 0 || a > this.setNum - 1) {
            return "Error 400: Set index out of bound.";
            
        } //if there is not enough object in the set, return an error message
        else if (b < 0 || b > this.remain[a]) {
            return "Error 401: Set invalid remove amount: either negative or more than remaining.";
            
        } //if not, do the normal remove
        else {
            this.remain[a] -= b;
            objectLeft -= b;
            return "200 OK";
        }
    }

    /**
     * This is what this thread does, it runs the game, reads user input, and do
     * response.
     */
    @Override
    public void run() {
    	
        //display greeting and set the two players not available and then display the sets and objects
        multiCast("Game begun!");
        player1.setAvailable(false);
        player2.setAvailable(false);
        displayRemain();
        
        //the body of the game, a loop that constantly reads user input and do
        //proper operation
        while (true) {
            try {
            	
                //players take turns to give commands
                if (player1Turn) {
                	
                    //tell the player who's turn it is now
                    multiCast(player1.getuID() + "'s turn");
                    
                    //reads user input
                    String currentCommandOne = player1.getPlayerReceiver().readLine();
                    
                    //if the other player wants to leave the game, it will give this player
                    //a notice, and pass the play back to its own thread and then kill this thread
                    if (stops) {
                    	
                        player1.getPlayerSender().println("opponent leaves the game, going back to the main menu...");
                        player1.getPlayerSender().flush();
                        Part_III_Server.games.remove(String.valueOf(this.gameID));
                        Part_III_Server.players.remove(player2.getuID());
                        player1.setAvailable(true);
                        
                        new Thread(player1).start();
                        return;
                    }
                    
                    //extract the user command
                    String[] command = currentCommandOne.split(" ");
                    
                    //do remove operation if it is a remove command
                    if (command[0].equalsIgnoreCase("remove")) {
                    	
                        String result = remove(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
                        
                        //if not successed, tell the player why
                        if (!result.equalsIgnoreCase("200 OK")) {
                            player1.getPlayerSender().println(result);
                            player1.getPlayerSender().flush();
                            
                        } //normal case, the remove operation succeeds
                        else {
                        	
                            String response = player1.getuID() + " takes " + command[1] + " object(s) from set " + command[2];
                            multiCast(response);
                            displayRemain();
                        }
                        
                        //after remove, if the number of remaining object is 0, that means this player wins
                        if (objectLeft == 0) {
                        	
                            //tell the result to the player
                            player1.getPlayerSender().println("you win!");
                            player2.getPlayerSender().println("you lose");
                            player1.getPlayerSender().flush();
                            player2.getPlayerSender().flush();
                            
                            //set the player status to available
                            player1.setAvailable(true);
                            player2.setAvailable(true);
                            
                            //and inform all the observers
                            for (int i = 0; i < this.observer.size(); i++) {
                            	
                                observer.get(i).getPlayerSender().println(player1.getuID() + " wins");
                                observer.get(i).getPlayerSender().flush();
                            }
                            
                            startOver();
                            //stop the game and pass the two players back to their own threads
//                            Part_III_Server.games.remove(String.valueOf(this.gameID));
//                            new Thread(player1).start();
//                            new Thread(player2).start();
//                            break;
                            
                        }
                    } //if the user wants to exit, confirm it, stop the game, and set the other player to
                    //available, close this player's socket, and then pass the other player back to 
                    //its own thread.
                    else if (command[0].equalsIgnoreCase("bye")) {
                    	
                        stops = true;
                        multiCast("Game is over...");
                        player1.getPlayerSender().println("comfirmed");
                        player1.getPlayerSender().flush();
                        player1.getPlayerSocket().close();
                        Part_III_Server.games.remove(String.valueOf(this.gameID));
                        Part_III_Server.players.remove(player1.getuID());
                        player2.setAvailable(true);
//                        player1 = null;
                        
                        new Thread(player2).start();
                        return;
                        
                    } //if the player gives an invalid command, inform the player
                    else {
                    	
                        player1.getPlayerSender().println("Error 105: invalid game command");
                        player1.getPlayerSender().flush();
                        player1.getPlayerSender().println("still your turn.");
                        player1.getPlayerSender().flush();
                        continue;
                    }
                    
                    //set the other player's turn
                    setPlayer1Turn(false);
                    
                    //multiCast(player2.getuID() + "'s turn");
                }

                //This part below has exactly the same method as above
                if (!player1Turn) {
                	
                    multiCast(player2.getuID() + "'s turn");
                    String currentCommandTwo = player2.getPlayerReceiver().readLine();
                    
                    if (stops) {
                    	
                        player2.getPlayerSender().println("opponent leaves the game, going back to the main menu...");
                        player2.getPlayerSender().flush();
                        Part_III_Server.games.remove(String.valueOf(this.gameID));
                        Part_III_Server.players.remove(player1.getuID());
                        player2.setAvailable(true);
                        
                        new Thread(player2).start();
                        return;
                    }
                    
                    String[] command = currentCommandTwo.split(" ");
                    
                    if (command[0].equalsIgnoreCase("remove")) {
                    	
                        String result = remove(Integer.parseInt(command[1]), Integer.parseInt(command[2]));
                        
                        if (!result.equalsIgnoreCase("200 OK")) {
                        	
                            player2.getPlayerSender().println(result);
                            player2.getPlayerSender().flush();
                            
                        } else {
                        	
                            String response = player2.getuID() + " takes " + command[1] + " object(s) from set " + command[2];
                            multiCast(response);
                            displayRemain();
                        }
                        if (objectLeft == 0) {
                        	
                            player1.getPlayerSender().println("you lose!");
                            player2.getPlayerSender().println("you win!");
                            player1.getPlayerSender().flush();
                            player2.getPlayerSender().flush();
                            player1.setAvailable(true);
                            player2.setAvailable(true);
                            
                            for (int i = 0; i < this.observer.size(); i++) {
                                observer.get(i).getPlayerSender().println(player2.getuID() + " wins");
                                observer.get(i).getPlayerSender().flush();
                            }
                            
                            startOver();
                            
//                            Part_III_Server.games.remove(String.valueOf(this.gameID));
//                            new Thread(player1).start();
//                            new Thread(player2).start();
//                            break;
                        }
                    } else if (command[0].equalsIgnoreCase("bye")) {
                    	
                        stops = true;
                        multiCast("Game is over...");
                        player2.getPlayerSender().println("comfirmed");
                        player2.getPlayerSender().flush();
                        player2.getPlayerSocket().close();
                        Part_III_Server.games.remove(String.valueOf(this.gameID));
                        Part_III_Server.players.remove(player2.getuID());
                        player1.setAvailable(true);
//                        player2 = null;
                        
                        new Thread(player1).start();
                        return;
                    } else {
                    	
                        player2.getPlayerSender().println("Error 105: invalid game command");
                        player2.getPlayerSender().flush();
                        player2.getPlayerSender().println("still your turn.");
                        player2.getPlayerSender().flush();
                        setPlayer1Turn(false);
                        continue;
                    }
                }
                
                setPlayer1Turn(true);
                //multiCast(player1.getuID() + "'s turn");
            } catch (IOException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    /*
     * This help method is for sending a designated message to both players and each observers.
     */
    public void multiCast(String msgToSend) {
    	
        player1.getPlayerSender().println(msgToSend);
        player2.getPlayerSender().println(msgToSend);
        player1.getPlayerSender().flush();
        player2.getPlayerSender().flush();
        
        for (int i = 0; i < this.observer.size(); i++) {
            observer.get(i).getPlayerSender().println(msgToSend);
            observer.get(i).getPlayerSender().flush();
        }
    }

    /**
     * This help method is to display the numbers of object left in each set and
     * send the information to each player and observer.
     */
    public void displayRemain() {
    	
        String setsRemain = "Set:  ";
        
        for (int i = 1; i <= setNum; i++) {
            setsRemain += " " + i;
        }
        
        setsRemain += "\nSize: ";
        
        for (int j = 0; j < setNum; j++) {
            setsRemain += " " + remain[j];
        }
        
        multiCast(setsRemain);
    }

    /**
     * To start the game again if both of the players want to.
     */
    public void startOver() {
    	
        setNum = (int) ((Math.random() * 3) + 3);
        remain = new int[setNum];
        
        for (int i = 0; i < setNum; i++) {
            remain[i] = 1 + (int) (Math.random() * 7);
            objectLeft += remain[i];
        }
        
        displayRemain();
        multiCast("Play again!");
    }
}
