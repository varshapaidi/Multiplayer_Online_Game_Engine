
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the main part runs on the client side, reads the user input and
 * send it to the server.
 */
public class PartIIClient {

    /**
     * global reply for the reader for server reply
     */
	
    public static BufferedReader reply;
    /**
     * indicates whether this client is the initiator of the running game
     */
    public static boolean initiator = false;
    
    /**
     * The help text, which should not be changed during the executing of the
     * program
     */
    
    final static String helpCommands = "****List of commands****\n- help: displays this list of commands\n- login <username>: enter username to begin game"
            + "\n- remove <x> <y>: specify number to remove from specified object in the format - 'remove x from set y' "
            + "\n                  where x is the amount to be removed and y is the the object to remove from"
            + "\n- games: a list of current ongoing games, return the game ids and player names"
            + "\n- who: return a list of players that are curently logged-in and ready to play"
            + "\n- observe <gameID>: observe an ongoing game"
            + "\n- unobserve <gameID>: unobserve an ongoing game"
            + "\n- play <username>: input username of opponent "
            + "\n- bye: quit game\n\n";

    /**
     * main method of the class, which starts the execution of the client
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //indicates whether it is the first round after the game starts
        boolean firstRound = true;

        //the initial greeting of the client program
        System.out.print("      Game of Nim\n" + helpCommands);
        try {
        	
            //create socket that connect to the server
            Socket socket = new Socket("127.0.0.1", 7992);

            //input reader
            Scanner input = new Scanner(System.in);

            //this PrintWriter object is to send command to the server through the desinated socket
            PrintWriter output = new PrintWriter(socket.getOutputStream());

            //init the BufferedReader that reads from the server
            reply = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //reads the input from the reader
            String command = input.nextLine();

            //show help text if the user wants, otherwise, send the command to the server
            if (command.equalsIgnoreCase("help")) {
                System.out.println(helpCommands);
            } else {
                output.println(command);
                output.flush();
            }

            //this is the important part of client side, it starts a new thread to constantly
            //monitor the reply from the server and show the reply to the user
            Receiver receiver = new Receiver();
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();

            //start an infinite loop to read user input
            while (true) {
            	
                //get a new user command
                command = input.nextLine();
                if (reply == null) {
                    break;
                    
                } //show the help text anytime as the user want
                else if (command.equalsIgnoreCase("help")) {
                    System.out.println(helpCommands);
                    continue;
                    
                } //or if the user wants to exit, inform the server and exit after getting the comfirm
                else if (command.equalsIgnoreCase("bye")) {
                    break;
                }
                
                //send the command to the server
                output.println(command);
                output.flush();
                
                //this part is a tricky part, if it is the first round, and the remove command
                //is already read by the player receiver, which means it does not exist any more, then
                //we need to send the command again to the game thread and set the first round to false
                if ((!initiator) && (firstRound && command.split(" ")[0].equalsIgnoreCase("remove"))) {
                    output.println(command);
                    output.flush();
                    firstRound = false;
                }
            }

            //tell the server that this client wants to leave and stops reading user input
            //and the receiver side will exit after getting the server confirmation
            output.println("bye");
            output.flush();
            
            System.out.println("game stops...");
            System.exit(0);
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(PartIIClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PartIIClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
