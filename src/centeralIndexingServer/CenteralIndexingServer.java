package centeralIndexingServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class CenteralIndexingServer {

	HashMap<InetAddress, ArrayList<String>> Index; // A data structure to store the list of files from clients.
	ServerSocket indexingServerSocket; // A server socket for accepting connection from clients.

	/*
	 * CenteralIndexingServer Constructor: 
	 * Initialize the HashMap to index the list of files from clients.
	 */
	CenteralIndexingServer() {
		Index = new HashMap<InetAddress, ArrayList<String>>();
		Collections.synchronizedMap(Index);
	}

	/*
	 * Main Method: 
	 * Activate this indexing server itself.
	 */
	public static void main(String[] args) {
		new CenteralIndexingServer().activate();
	}

	public void activate() {
		Socket nameSocket = null;

		try {
			indexingServerSocket = new ServerSocket(9211);
			System.out.println("Indexing Server Activated");
			System.out.println("Waiting for connections");

			while (true) {
				nameSocket = indexingServerSocket.accept();
				ServerProcessor serverProcessor = new ServerProcessor(nameSocket);
				serverProcessor.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ServerProcessor extends Thread {

		Socket processorSocket = null;

		DataInputStream din = null;
		DataOutputStream dout = null;

		String queryString = null;

		ServerProcessor(Socket clientSocket) {
			this.processorSocket = clientSocket;
			try {
				din = new DataInputStream(processorSocket.getInputStream());
				dout = new DataOutputStream(processorSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				
				while (din != null) {
					queryString = din.readUTF();
					
					// search the index and return the address of the peer that have the file.
					if (queryString != null && queryString.startsWith("lookup.")) {
						lookup(queryString);
						queryString = null;
					}
					
					else if (queryString != null && queryString.equals("refresh")) {
						refresh(processorSocket.getInetAddress());
						queryString = null;
					}
					
					// register clients and it's files by calling 'registry'.
					else if (queryString != null) {
						registry(processorSocket.getInetAddress(), queryString);
						queryString = null;
					}
				}
			} catch (IOException e) {

			} finally {
				Index.clear();
				System.out.println("[" + processorSocket.getInetAddress() + ":" + processorSocket.getPort() + "]"	+ "closed connection");
			}
		}

		/*
		 * lookup: 
		 * search the index and return all the matching peers to the requestor.
		 */
		public void lookup(String fileName) {

			String searchQuery = null;
			String searchString[] = null;

			searchString = fileName.split("lookup.");
			searchQuery = searchString[1];
			System.out.println("lookup, request file name: "+searchQuery);

			// An ArrayList for storing addresses of peer that have the file
			ArrayList<String> targetAddresses = new ArrayList<String>();
			
			// Iterate hash map and find the peer that have the requested file
			for (Map.Entry<InetAddress, ArrayList<String>> entry : Index.entrySet()) {
				InetAddress targetAddr = entry.getKey();
				ArrayList<String> file = entry.getValue();
				for (String string : file) {
					if(searchQuery.equals(string)) {
						targetAddresses.add(targetAddr.toString());
					}
				}
			}
			System.out.println("requested file found");
			// Send the list of peers that have the requested file
			for(int i = 0; i<targetAddresses.size(); i++) {
				try {
					dout.writeUTF(targetAddresses.get(i).toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * registry: 
	 * register all of the files each of clients have. If a peer has
	 * been already registered, then only add the file name to the Index.
	 * Otherwise, add an IP address as a key and file name as a value.
	 */
	public void registry(InetAddress peerAddr, String fileName) {
		if (Index.containsKey(peerAddr)) {
			Index.get(peerAddr).add(fileName);
			System.out.println(fileName + " added from client " + peerAddr);
		} else {
			Index.put(peerAddr, new ArrayList<String>());
			Index.get(peerAddr).add(fileName);
			System.out.println(fileName + " added from client " + peerAddr);
		}
	}
	
	/*
	 * refresh:
	 * If a client modifies or deletes some files registered at this server,
	 * this server update a file list from the client.
	 */
	public void refresh(InetAddress peerAddr) {
		System.out.println("refresh index");
		Index.get(peerAddr).clear();
	}
}
