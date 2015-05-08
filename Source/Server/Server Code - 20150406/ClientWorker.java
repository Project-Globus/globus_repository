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
    	  return;
      }
      
      String[] userInfo = {"null"};
	  ArrayList<ArrayList<String>> twoDResult = null;  
	  
      while(true){
    	  try{
    		  if (!PGDB.getInit()) {
				  String connection = "";
				  System.out.println("Initializing Database Connection...");
				  connection = PGDB.init();
				  System.out.println(connection);
			  }		
    		  
    		  System.out.println("");
    		  
    		  String inputLine = in.readLine();
    		  System.out.println("Input: " + inputLine);
    		  
    		  String[] info = inputLine.split(";~;");
			 		
			    // Name, Biography, Email, Password
			    if (info[0].equals("register"))
			    {
				  userInfo = PGDB.createAccount(info[1], 
						info[2], info[3], info[4]);
				  String send = "";
				  for (int i = 0; i < userInfo.length; ++i)
						send += userInfo[i] + ";~;";
				  out.println(send);
				  System.out.println("Output: " + send);
				}
				else if (info[0].equals("login"))
				{
				   // Email, Password
				   userInfo = PGDB.login(info[1], info[2]);
				   String send = "";
				   for (int i = 0; i < userInfo.length; ++i)
						send += userInfo[i] + ";~;";
				   out.println(send);
				   System.out.println("Output: " + send);
				}
				else if (info[0].equals("membership"))
				{									
					if (userInfo.length > 1)
						twoDResult = PGDB.getMembership(userInfo);
					else {
						out.println("invalid user information. Please login again.");
						System.out.println("Bad user information...");
						return;
					}
					String send = "";
					for (int i = 0; i < twoDResult.size(); ++i)
						for (int j = 0; j < twoDResult.get(i).size(); j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
				} 
				else if (info[0].equals("create"))
				{
					// group name, group description, group password
					String response = "";
					if (userInfo.length > 1)
						response = PGDB.createGroup(userInfo, info[1], info[2], info[3]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}

					out.println(response);
					   System.out.println("Output: " + response);
				} 
				else if (info[0].equals("join"))
				{
					// group_id, group_password
					String response = "";
					if (userInfo.length > 1)
						response = PGDB.joinGroup(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(response);
					   System.out.println("Output: " + response);
					   
				} 
				else if (info[0].equals("getmessages"))
				{
					// group_id, timestamp
					if (userInfo.length > 1)
						twoDResult = PGDB.getMessages(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					String send = "";
					for (int i = 0; i < twoDResult.get(i).size(); ++i)
						for (int j = 0; j < 5; j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
				} 
				else if (info[0].equals("addmessage"))
				{
					// group_id, message, type, timestamp
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.addMessage(userInfo, info[1], info[2], info[3], info[4]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updateBio"))
				{
					// biography
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateBio(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("update"))
				{
					// timestamp
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateRecency(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("getmembers"))
				{
					// group_id
					String send = "";
					if (userInfo.length > 1)
						twoDResult = PGDB.getGroupMembers(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					for (int i = 0; i < twoDResult.get(i).size(); ++i)
						for (int j = 0; j < 4; j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updateGDesc"))
				{
					// groupID, Description
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGDesc(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updateGPass"))
				{
					// groupID, password
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGPass(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updateGName"))
				{
					// groupID, Name
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGDesc(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updateGGoogle"))
				{
					// groupID, Google Username, Google Password
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGoogleInfo(userInfo, info[1], info[2], info[3]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updateName"))
				{
					// name
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateName(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("updatePass"))
				{
					// password
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updatePass(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("getEvents")) 
				{
					// group_id
					String send = "";
					if (userInfo.length > 1)
						twoDResult = PGDB.getEvents(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					for (int i = 0; i < twoDResult.get(i).size(); ++i)
						for (int j = 0; j < 6; j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else if (info[0].equals("addEvent")) 
				{
					// cal_id, start, end, name, description
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.addEvent(userInfo, info[1], info[2], info[3], info[4], info[5]);
					else {
						out.println("invalid user information. Please login again.");
						return;
					}
					out.println(send);
					   System.out.println("Output: " + send);
				}
				else { out.println("invalid command: " + info[0]); System.out.println("invalid command: " + info[0]); }
    	  }catch (IOException e) {
    		  System.out.println("Read failed");
    		  return;
    	  }
      }
  }
  protected void finalize(){
	//Objects created in run method are finalized when
	//program terminates and thread exits
	     try{
	        client.close();
			System.out.println("Socket closed.");
	    } catch (IOException e) {
	        System.out.println("Could not close socket");
	    }
	  }
}