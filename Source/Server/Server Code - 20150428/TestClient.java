import java.net.Socket;
import java.util.Random;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
public class TestClient {

	private static Socket socket = null;
	private static PrintWriter toServer = null;
	private static BufferedReader fromServer = null;
	//private static BufferedReader fromUser = null;
	
	public static void main(String[] args) {
		
		// This try/catch block must be called in order to instantiate a client thread on the server.
		try {
			socket = new Socket("ec2-54-191-216-200.us-west-2.compute.amazonaws.com", 63400);
			
			toServer = new PrintWriter(socket.getOutputStream(), true);
		    fromServer = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		    //fromUser = new BufferedReader( new InputStreamReader(System.in));
		
		} catch(IOException e)
		{
			System.out.println(e);
		}
		
		
		String response = "";
		String userInput = "login;~;me@dcrane.us;~;password";
	    
        
        // Example: Sending a message to the server. Waiting a single response.
        //userInput = "register;~;Jesse Miller;~;Likes long walks on the beach!;~;millerja47@gmail.com;~;password";
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
//        
////        for (int i = 500; i < 510; ++i)
////        {
////        	userInput = "register;~;Test Account " + i + ";~;Description " + i + ";~;test" + i + "@test.com;~;password";
////            try {
////            	System.out.println("Client: " + userInput);
////                // Send the input to the server
////                toServer.println(userInput);
////                
////                response = fromServer.readLine();
////                
////                System.out.println("Server: " + response);
////                
////            } catch (IOException e) {
////    			e.printStackTrace();
////    			System.out.println("Read Failed");
////    		}
////        }
////        
//        userInput = "membership";
//        // Example: Sending a message to the server. Waiting a single response.
//        
//        try {
//        	System.out.println("Client: " + userInput);
//            // Send the input to the server
//            toServer.println(userInput);
//            
//            response = fromServer.readLine();
//            
//            System.out.println("Server: " + response);
//            
//        } catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("Read Failed");
//		}
////        
////        userInput = "create;~;TestGroup57;~;TestDescription57;~;TestPassword";
////        // Example: Sending a message to the server. Waiting a single response.
////        
////        try {
////        	System.out.println("Client: " + userInput);
////            // Send the input to the server
////            toServer.println(userInput);
////            
////            response = fromServer.readLine();
////            
////            System.out.println("Server: " + response);
////            
////        } catch (IOException e) {
////			e.printStackTrace();
////			System.out.println("Read Failed");
////		}
////        userInput = "membership";
////        // Example: Sending a message to the server. Waiting a single response.
////        
////        try {
////        	System.out.println("Client: " + userInput);
////            // Send the input to the server
////            toServer.println(userInput);
////            
////            response = fromServer.readLine();
////            
////            System.out.println("Server: " + response);
////            
////        } catch (IOException e) {
////			e.printStackTrace();
////			System.out.println("Read Failed");
////		}
//		
//		userInput = "";
//        
        System.out.println("Quitting...");	 
        toServer.close();
//		Random rand = new Random();
//		
//		for (int i = 0; i < 75; ++i) {
//			RunnableClient client;
//				client = new RunnableClient("" + (rand.nextInt((509 - 500) + 1) + 500));
//				
//			    Thread t = new Thread(client);
//			    t.start();
//			    System.out.println("Starting New Thread...");
//			    if (t.isAlive()) System.out.println(t.getName() + " has started.");
//		}
	}	
}
