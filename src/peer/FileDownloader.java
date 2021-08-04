package peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class FileDownloader extends Thread {
	
	Socket socket = null;
    DataInputStream din = null;
    DataOutputStream dos = null;
    FileOutputStream fos = null;
    String fileToDownload = null;
    
    public FileDownloader(Socket socket, String fileName) {
    	System.out.println("File downloader activated");
    	fileToDownload = fileName;
        
    		this.socket = socket;
        try {
			din = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			// send file name to the other peer for downloading
			dos.writeUTF(fileToDownload);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
 
    @Override
    public void run() {
        try {
        		// create a new file object to be written
            File file = new File("/Users/Sevas/income/" + fileToDownload);
            fos = new FileOutputStream(file);
            
            int length;
            int maximumSize = 35536;	// maximum size
            byte[] data = new byte[maximumSize];

            System.out.println("display file");
            while ((length = din.read(data)) != -1) {
                fos.write(data, 0, length);
            }
            fos.close();
            din.close();
            System.out.println("Download Complete");
        } catch (IOException e) {
        } catch (Exception e) {
        }
    }
}
