import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
class ClientWorker implements Runnable {
  private Socket client;
  private BufferedReader in = null;
  private PrintWriter out = null;
  private PrintWriter log = null;
  private File file = null;

  // Can only be created with a valid socket called from the connection thread.
  ClientWorker(Socket client) {
    this.client = client;
  }
  
  // Universal logging function. Updates as commands are received and executed, for debugging
  private void log(String message, boolean toCommandLine, boolean toFile, boolean flush){
	  if (toCommandLine) System.out.println(message);
	  if (toFile) log.println(message);
	  if(flush) log.flush();
  }
  
  // Main Thread Execution Scheme
  public void run(){
      Date date = new Date();
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH|mm|ss");
      String instance = dateFormat.format(date);
      try{
    	  // Setup the socket reader and writer
    	  in = new BufferedReader(new 
    			  InputStreamReader(client.getInputStream()));
    	  out = new 
    			  PrintWriter(client.getOutputStream(), true);
    	  
    	  // Create a log file for the current thread
    	  file = new File("./logs/" + instance + " " + Thread.currentThread().getName() + ".log");
    	  
    	  if (!file.exists()) file.createNewFile();
    	  
    	  log = new PrintWriter(
				    new OutputStreamWriter(
				        new FileOutputStream(file),"UTF-8"));
      } catch (IOException e) {
    	  log("Failure to initialize writer: " + e.getMessage() + "\nThread Stopping.",true,true,true);
    	  return;
      }
      
      
      String[] userInfo = {"null"};
	  ArrayList<ArrayList<String>> twoDResult = null;  
	  
	  // Run until connection is severed or times out
      while(true){
    	  try{
    		  // Verify a connection to the database is possible
    		  if (!PGDB.getInit()) {
				  String connection = "";
				  System.out.println("Initializing Database Connection...");
				  connection = PGDB.init();
				  log(connection,true,true,true);
			  }		
    		  
    		  log("",true,true,true);
    		  
    		  String inputLine = in.readLine();
    		  log("Input: " + inputLine,true,true,true);
    		  
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
				  log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("login"))
				{
				   // Email, Password
				   userInfo = PGDB.login(info[1], info[2]);
				   String send = "";
				   for (int i = 0; i < userInfo.length; ++i)
						send += userInfo[i] + ";~;";
				   out.println(send);
				   log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("membership"))
				{									
					if (userInfo.length > 1)
						twoDResult = PGDB.getMembership(userInfo);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					String send = "";
					for (int i = 0; i < twoDResult.size(); ++i)
						for (int j = 0; j < twoDResult.get(i).size(); j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					log("Output: " + send,true,true,true);
				} 
				else if (info[0].equals("create"))
				{
					// group name, group description, group password
					String response = "";
					if (userInfo.length > 1)
						response = PGDB.createGroup(userInfo, info[1], info[2], info[3]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					
					out.println(response);
					log("Output: " + response,true,true,true);
				} 
				else if (info[0].equals("join"))
				{
					// group_id, group_password
					String response = "";
					if (userInfo.length > 1)
						response = PGDB.joinGroup(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(response);
					log("Output: " + response,true,true,true);;
					   
				} 
				else if (info[0].equals("leave"))
				{
					// group_id
					String response = "";
					if (userInfo.length > 1)
						response = PGDB.leaveGroup(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(response);
					log("Output: " + response,true,true,true);;
					   
				} 
				else if (info[0].equals("getmessages"))
				{
					// group_id, timestamp
					if (userInfo.length > 1)
						twoDResult = PGDB.getMessages(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					String send = "";
					for (int i = 0; i < twoDResult.get(i).size(); ++i)
						for (int j = 0; j < 5; j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					log("Output: " + send,true,true,true);
					   
				} 
				else if (info[0].equals("addmessage"))
				{
					// group_id, message, type, timestamp
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.addMessage(userInfo, info[1], info[2], info[3], info[4]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updateBio"))
				{
					// biography
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateBio(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("update"))
				{
					// timestamp
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateRecency(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("getmembers"))
				{
					// group_id
					String send = "";
					if (userInfo.length > 1)
						twoDResult = PGDB.getGroupMembers(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					for (int i = 0; i < twoDResult.get(i).size(); ++i)
						for (int j = 0; j < 4; j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updateGDesc"))
				{
					// groupID, Description
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGDesc(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updateGPass"))
				{
					// groupID, password
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGPass(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updateGName"))
				{
					// groupID, Name
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGDesc(userInfo, info[1], info[2]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updateGGoogle"))
				{
					// groupID, Google Username, Google Password
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateGoogleInfo(userInfo, info[1], info[2], info[3]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updateName"))
				{
					// name
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updateName(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("updatePass"))
				{
					// password
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.updatePass(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("getEvents")) 
				{
					// group_id
					String send = "";
					if (userInfo.length > 1)
						twoDResult = PGDB.getEvents(userInfo, info[1]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					for (int i = 0; i < twoDResult.get(i).size(); ++i)
						for (int j = 0; j < 6; j++)
							send += twoDResult.get(i).get(j) + ";~;";
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else if (info[0].equals("addEvent")) 
				{
					// cal_id, start, end, name, description
					String send = "";
					if (userInfo.length > 1)
						send = PGDB.addEvent(userInfo, info[1], info[2], info[3], info[4], info[5]);
					else {
						out.println("invalid user information. Please login again.");
						log("Output: " + "Bad user information...",true,true,true);
					}
					out.println(send);
					log("Output: " + send,true,true,true);
				}
				else { out.println("invalid command: " + info[0]); log("invalid command: " + info[0],true,true,true); }
    	  }catch (IOException e) {
    		  log("Read failed. Closing thread " + Thread.currentThread().getName() + "...",true,true,true); 
    		  finalize();
    		  return;
    	  } catch (NullPointerException e) {
    		  log("Connection severed. Closing thread " + Thread.currentThread().getName() + "...",true,true,true); 
    		  finalize();
    		  return;
    	  }
      }
  }
  protected void finalize(){
	//Objects created in run method are finalized when
	//program terminates and thread exits
	     try{
	        client.close();
			System.out.println("Socket closed.\n");
	    } catch (IOException e) {
	        System.out.println("Could not close socket!!!\n");
	    }
	     log.close();
	  }
}