Hey guys,

This build only has login and register functionality. I had to get the server running.

The server is currently running on our Amazon host at 
ec2-54-191-216-200.us-west-2.compute.amazonaws.com
under port 63400 for our app.

The server creates a new thread for every connection, so we don't have to constantly start/stop/restart the server, and properly closes sockets and connections when finished.

Information is currently passed via plaintext TCP datagrams. (SEE Client.java).

Any class communicating with the server must have the following objects declared. You will pass these objects around while maintaining a socket.

	private static Socket socket = null;
	private static PrintWriter toServer = null;
	private static BufferedReader fromServer = null;

When creating a connection, you must run the following block:

		// This try/catch block must be called in order to instantiate a client thread on the server.
		try {
			socket = new Socket("ec2-54-191-216-200.us-west-2.compute.amazonaws.com", 63400);
			
			toServer = new PrintWriter(socket.getOutputStream(), true);
		    fromServer = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		
		} catch(IOException e)
		{
			System.out.println(e);
		}

I'd recommend writing a "connect()" function that gets called after a decent delay of communcation with the server.
Any closed socket is handled by the server, and a new socket only creates a minor CPU load on our server (not scalable, yet).

Most functionality will be handled by Example 2 in Client.java: Sending a message to the server. Waiting a single response.

Messages sent to the server are strings, separated by the ";~;" string token. You can easily call STRING.split(";~;") to store the response values into a String array named STRING.
Messages received from the server are strings, separated by the ";~;" string token.

Current Message Formats:
login: "login;~;EMAIL_AS_USERNAME;~;PASSWORD"
register: "register;~;NAME;~;BIOGRAPHY;~;EMAIL_AS_USERNAME;~;PASSWORD"

Current Response Formats:
register, login: "QUERY_STATUS;~;NAME;~;USER_ID;~;EMAIL_AS_USERNAME;~;BIOGRAPHY;~;PASSWORD;~;LASTUPDATE"