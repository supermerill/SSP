package remi.ssp.network;

import java.io.IOException;

import com.google.auto.service.AutoService;

import remi.ssp.Plugin;

@AutoService(Plugin.class)
public class NetworkSystemPlugin extends Plugin {
	private static final long serialVersionUID = 1L;

	public SSPServerSocket serverSocket;
	
	public void init(){
		try {
			SSPClientConnection.callable.put("get", new NetworkReflexion());
			SSPClientConnection.callable.put("call", new NetworkReflexionSave());
			
			serverSocket = new SSPServerSocket();
			serverSocket.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
