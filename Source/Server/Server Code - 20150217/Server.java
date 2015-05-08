import java.net.ServerSocket;
import java.io.IOException;
public class Server { 
	
	private static ServerSocket serverSocket = null;
	
	public static void main (String[] args) {
		try {
			serverSocket = new ServerSocket(63400);
		} catch(IOException e) {
			System.out.println(e);
		}
		
		while (true) {
			ClientWorker worker;
			try {
				worker = new ClientWorker(serverSocket.accept());
			    Thread t = new Thread(worker);
			    t.start();
			} catch (IOException e) {
			    System.out.println("Accept failed: 63400");
			    System.exit(-1);
			}
		}
	}
}
