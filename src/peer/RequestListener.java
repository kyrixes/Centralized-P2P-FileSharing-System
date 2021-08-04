package peer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class RequestListener extends Thread {
	
	Socket listenerSocket = null;
	DataInputStream in = null;
	
	RequestListener(Socket socket) {
		this.listenerSocket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
		} catch(IOException e) {
			
		}
	}
	
	public void run() {
		while(in != null) {
			try {
				// gets IP address from indexing server and remove '/'
				// ex) '/127.0.0.1' -> '127.0.0.1'
				Peer.targetPeerIP = in.readUTF();
				Peer.targetPeerIP = Peer.targetPeerIP.substring(1);
				Peer.targetPeerIPs.add(Peer.targetPeerIP);
				System.out.println(Peer.targetPeerIPs + "has(ve) the file");
			} catch(IOException e) {
			}
		}
	}
}
