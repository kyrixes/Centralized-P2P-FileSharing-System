package peer;
import java.net.*;
import java.io.*;
import java.util.*;

public class Peer {
	
	public static final int filePort = 7728;
	public static final int serverPort = 9211;
	
	public static final String serverIP = "127.0.0.1"; // must be modified according to the environment
	
	protected static String targetPeerIP;	// a peer IP for connecting to the target peer for file downloading  
	public static ArrayList<String> targetPeerIPs = new ArrayList<String>();	// an arraylist to store multiple peer list
	static String tempFileName;	// a temporary memory for storing file name
	static String retrieveFileName; // used to file transfer between clients (downloader send the file name to the uploader)
	
	// lists for maintaining valid files to be shared
	static File[] fileList;
	static String[] stringFileList;
	
	// a path of shared directory and an actual directory
	protected static String sharedDirectoryPath = "/Users/Sevas/incoming";
	public static File sharedDirectory;
	
	/*
	 * retrieve(String fileName):
	 * 1. Gets file name as a parameter to download.
	 * 2. Select a peer from the peer list to connect and download.
	 * 3. Starts a downloader thread.
	 */
	public static void retrieve(String fileName) {
		Scanner sc = new Scanner(System.in);
		try {
			
			retrieveFileName = fileName;
			int select;
			System.out.println("Select the peer to connect for retrieving");
			System.out.println("(Please enter 0 if you want to select first, enter 1 if you want to select second)");
			System.out.println("Peer List: " + targetPeerIPs);
			
			while (true) {
				select = sc.nextInt();
				if(select < 0 || (select > targetPeerIPs.size())) {
					System.out.println("Invalid input");
				} else {
					break;
				}
			}
			Socket retrievingSocket = new Socket(targetPeerIPs.get(select), filePort); 
			Thread fileDownloader = new Thread(new FileDownloader(retrievingSocket, retrieveFileName));
			fileDownloader.start();
			targetPeerIPs.clear();
		} catch (Exception e) {
			
		}
	}
	
	/*
	 * listup():
	 * enlists the files to 'fileList' and 'stringFileList' to maintain the list of files.
	 * The result (list of files) is used to maintain shareable files of this peer and register to the
	 * indexing server as well.
	 */
	public static boolean listup() {
		sharedDirectory = new File(sharedDirectoryPath);
		
		// check whether the path is valid shared directory or not
		if(!sharedDirectory.isDirectory()) {
			System.out.println("Warning: " + sharedDirectoryPath + " is not a directory");
			return false;
		}
		// store list of files to the local
		fileList = sharedDirectory.listFiles();
		stringFileList = sharedDirectory.list();	
		
		return true;
	}
	
	/*
	 * checkUpdated():
	 * If a user adds or removes some files which are already registered to the indexing server,
	 * this method detect the change and register new list to the indexing server. 
	 */
	public static boolean checkUpdated() {
		File newSharedDirectory = new File(sharedDirectoryPath);
		String[] tempStringFileList = newSharedDirectory.list();
		
		if(stringFileList.length == tempStringFileList.length) {
			return false;
		} else {
			fileList = newSharedDirectory.listFiles();
			stringFileList = newSharedDirectory.list();	
			return true;
		}
	}

	/*
	 * register the list of files to the indexing server
	 */
	static void register(Socket socket) {
		try {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			for (int i = 0; i < stringFileList.length; i++) {
				System.out.println("<Filename>" + fileList[i] + " is ready to send");
				dos.writeUTF(stringFileList[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			// try connect to the indexing server
			Socket socket = new Socket(serverIP, serverPort);
			System.out.println("Connected");
			
			// enlist local file names of this peer and register to the indexing server
			listup();
			register(socket);
			
			// have a request listener from indexing server and file uploader to be ready
			Thread requestListener = new Thread(new RequestListener(socket));
			Thread fileUploader = new Thread(new FileUploader());
			
			// start the request listener and file uploader
			requestListener.start();
			fileUploader.start();
			
			// a data output stream to the indexing server (used for sending name)
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			// a String object to search a file from indexing server
			String searchQuery;
			Scanner scanner = new Scanner(System.in);
			
			while (true) {
				
				/*
				 * check on every iteration whether there was some change in the shared directory.
				 * If change was detected, send new list to the indexing server.
				 */
				if(checkUpdated()) {
					listup();
					dos.writeUTF("refresh");
					register(socket);
				}
				
				/* Official Menu of the Peer */
				System.out.println("1.search 2.retrieve q.Quit");
				String choice = scanner.next();
				
				// 1. File Search
				if(choice.equals("1")) {
					System.out.println("Please write down a file name to searching");
					searchQuery = scanner.next();
					tempFileName = searchQuery;
					dos.writeUTF("lookup." + searchQuery);
					
				// 2. Retrieve File
				} else if(choice.equals("2")) {
					retrieve(tempFileName);
					
				// q. Quit Program	
				} else if(choice.equals("q")) {
					System.out.println("Thank you");
					System.exit(0);
					
				// Invalid Input
				} else {
					System.out.println("Invalid input");
					continue;
				}
				//getAveTime();
			}
		} catch (ConnectException ce) {
			ce.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
}
