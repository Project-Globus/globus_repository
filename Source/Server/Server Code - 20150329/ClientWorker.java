import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;
class ClientWorker implements Runnable {
  private Socket client;

//Constructor
  ClientWorker(Socket client) {
    this.client = client;
  }

  public void run(){
      BufferedReader in = null;
      PrintWriter out = null;
      try{
    	  in = new BufferedReader(new 
    			  InputStreamReader(client.getInputStream()));
    	  out = new 
    			  PrintWriter(client.getOutputStream(), true);
      } catch (IOException e) {
    	  System.out.println("in or out failed");
    	  System.exit(-1);
      }

      while(true){
    	  try{
    		  String inputLine = in.readLine(), connection = "";
    		  String[] info = inputLine.split(";~;");
			  String[] userInfo = {null};
			  ArrayList<ArrayList<String>> membership = null;
				
			  System.out.println("Input: " + inputLine);
			  
			  if (!PGDB.getInit()) {
				  connection = PGDB.init();
				  System.out.println("Initializing Database Connection...");
			  }
			  
			  System.out.println(connection);
			  
//			  System.out.println("Printing Parsed Input...");
//			  
//			  for (int i = 0; i < info.length; ++i)
//				  System.out.println(info[i]);
				
			  // Name, Biography, Email, Password
			  if (info[0].equals("register")) {
				  userInfo = PGDB.createAccount(info[1], 
						info[2], info[3], info[4]);
				  String send = "";
				  for (int i = 0; i < userInfo.length; ++i)
						send += userInfo[i] + ";~;";
				  out.println(send);
				  System.out.println("Output: " + send);
				}
				else if (info[0].equals("login")) {
				   // Email, Password
				   userInfo = PGDB.login(info[1], info[2]);
				   String send = "";
				   for (int i = 0; i < userInfo.length; ++i)
						send += userInfo[i] + ";~;";
				   out.println(send);
				   System.out.println("Output: " + send);
				}
				else if (info[0].equals("membership")) {
					// user_id
					if (userInfo.length > 1)
						membership = PGDB.getMembership(userInfo);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					String send = "";
					for (int i = 0; i < membership.get(i).size(); ++i)
						for (int j = 0; j < 7; j++)
							send += membership.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
					
				} else if (info[0].equals("create")) {
					// user_id
					if (userInfo.length > 1)
						membership = PGDB.createGroup(userInfo, info[1], info[2], info[3]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					String send = "";
					for (int i = 0; i < membership.get(i).size(); ++i)
						for (int j = 0; j < 7; j++)
							send += membership.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
				} else if (info[0].equals("join")) {
					// group_id, group_password
					if (userInfo.length > 1)
						membership = PGDB.joinGroup(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					String send = "";
					for (int i = 0; i < membership.get(i).size(); ++i)
						for (int j = 0; j < 7; j++)
							send += membership.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
				} else { out.println("invalid command"); }
    		  
    		  
    	  }catch (IOException e) {
    		  System.out.println("Read failed");
    		  System.exit(-1);
    	  }
      }
  }
  protected void finalize(){
	//Objects created in run method are finalized when
	//program terminates and thread exits
	     try{
	        client.close();
	    } catch (IOException e) {
	        System.out.println("Could not close socket");
	        System.exit(-1);
	    }
	  }
}