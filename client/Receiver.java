
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a runnable class that can be used to create a new thread and
 * constantly reads from the server and display the reply on the console.
 */
public class Receiver implements Runnable {

    /**
     * constructor
     */
    public Receiver() {
    }

    /**
     * This method is what is to be running after creating a new thread of this,
     * it constantly reads the information from the server, handle it, and
     * display any necessary information on the console
     */
    @Override
    public void run() {
        try {
        	
            //an infinite loop for reading server reply unless interrupted by the
            //server confirmation of "bye"
            while (true) {
            	
                //reads a line
                String s = PartIIClient.reply.readLine();
                
                //in most cases, it displays the server reply
                if ((s != null) && (!s.equalsIgnoreCase("ini"))) {
                    System.out.println(s);
                    
                } //if the server says the client is the initiator, it informs the main thread
                else if (s.equalsIgnoreCase("ini")) {
                    PartIIClient.initiator = true;
                    
                } //if it gets the server confirmation, it exits
                else if (s.equalsIgnoreCase("comfirmed")) {
                    return;
                    
                } //detects other cases which is not likely to happen
                else {
                	
                    PartIIClient.reply = null;
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
