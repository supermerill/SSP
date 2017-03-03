package remi.ssp.network;

import java.io.IOException;

import remi.ssp.Plugin;

public class NetworkSystemPlugin extends Plugin {
	private static final long serialVersionUID = 1L;

	public SSPServerSocket serverSocket;
	
	public void init(){
		try {
			SSPClientConnection.callable.put("call", new NetworkReflexion());
			
			serverSocket = new SSPServerSocket();
			serverSocket.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
