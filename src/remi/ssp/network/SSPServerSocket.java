package remi.ssp.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SSPServerSocket extends java.net.ServerSocket implements Runnable{

	public static int port = 8945;
	
	public SSPServerSocket() throws IOException {
		super();
	}
	
	
	public void start(){
		try{
			this.bind(new InetSocketAddress("localhost", port));
			new Thread(this).start();
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}


	@Override
	public void run() {
		while(true){
			try {
				Socket client = accept();
				Runnable clientnet = new SSPClientConnection(client);
				new Thread(clientnet).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	

}
