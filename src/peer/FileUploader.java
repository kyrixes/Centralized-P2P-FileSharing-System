package peer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileUploader extends Thread {
	ServerSocket serverSocket;
    Socket uploaderSocket;
    DataOutputStream dos;
    DataInputStream din;
    FileInputStream fis;
    BufferedInputStream bis;
    String fileToSend;
    
    public FileUploader() {
        try {
            serverSocket = new ServerSocket(7728);
            System.out.println("File uploader activated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
        		while (true) {
        			// catch an incoming socket that requested file
        			uploaderSocket = serverSocket.accept();
        			
        			// make streams for fire transfer
        			dos = new DataOutputStream(uploaderSocket.getOutputStream());
    		        din = new DataInputStream(uploaderSocket.getInputStream());
    	        		
    		        // get file name from other peer
    		        fileToSend = din.readUTF();	
    	        	
    	            String filePath = Peer.sharedDirectoryPath + "/" + fileToSend;
    	            
    	            // create new file object to be uploaded
    	            File file = new File(filePath);
    	            byte[] fileToByte = new byte[(int)file.length()];
    	            
    	            dos.writeUTF(filePath);
    	            
    	            fis = new FileInputStream(file);
    	            bis = new BufferedInputStream(fis);
    	 
    	            bis.read(fileToByte, 0, fileToByte.length);
    	            dos.write(fileToByte, 0, fileToByte.length);
    	            	 
    	            // clear streams
    	            dos.flush();
    	            dos.close();
    	            bis.close();
    	            fis.close();
    	            System.out.println("Upload Complete");
        		}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
