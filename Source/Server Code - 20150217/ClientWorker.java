import java.net.Socket;
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
      String line;
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
    		  String inputLine = in.readLine();
    		  String[] info = inputLine.split(";~;");
			  String[] userInfo;
				
			  System.out.println("Input: " + inputLine);
				
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