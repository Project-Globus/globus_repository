import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
public class Client {

	private static Socket socket = null;
	private static PrintWriter toServer = null;
	private static BufferedReader fromServer = null;
	private static BufferedReader fromUser = null;
	
	public static void main(String[] args) {
		
		// This try/catch block must be called in order to instantiate a client thread on the server.
		try {
			socket = new Socket("ec2-54-191-216-200.us-west-2.compute.amazonaws.com", 63400);
			
			toServer = new PrintWriter(socket.getOutputStream(), true);
		    fromServer = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		    fromUser = new BufferedReader( new InputStreamReader(System.in));
		
		} catch(IOException e)
		{
			System.out.println(e);
		}
		
		
		String response = "";
		String userInput = "";
	    
		// Example: Sending a single message to the server. Not waiting a response.
        if (userInput != null) {
            System.out.println("Client: " + userInput);
            // Send the input to the server
            toServer.println(userInput);
        }
        
        // Example: Sending a message to the server. Waiting a single response.
        
        try {
        	System.out.println("Client: " + userInput);
            // Send the input to the server
            toServer.println(userInput);
            
            response = fromServer.readLine();
            
            System.out.println("Server: " + response);
            
        } catch (IOException e) {
			e.printStackTrace();
			System.out.println("Read Failed");
		}
	    
        // Example: Read messages from server until null message returned.
	    try {
			while ((response = fromServer.readLine()) != null) {
			    System.out.println("Server: " + fromServer);
			    if (response.equals("Bye."))
			        break;

			    userInput = fromUser.readLine();
			    while (userInput != null) {
			        System.out.println("Client: " + userInput);
			        toServer.println(userInput);
			    }
			    userInput = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Read Failed");
		}
			    
	}
	
}
