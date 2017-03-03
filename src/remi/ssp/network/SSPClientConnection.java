package remi.ssp.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SSPClientConnection implements Runnable {
	
	public static Map<String, Function<String, String>> callable = new HashMap<>();
	

	private Socket client;
	private BufferedReader reader;
	private PrintWriter writer;

	public SSPClientConnection(Socket client) {
		this.client = client;
		try {
			reader = new BufferedReader(new InputStreamReader(client.getInputStream(), Charset.forName("UTF-8")));
			writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), Charset.forName("UTF-8")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		String line;
		while(true){
			try {
				//read a line
				line = reader.readLine();
				
				//basic check
				parsing: if(line.charAt(0) != '#' && line.length()>10 && line.length()<10000){
					//get command
					int pos = line.indexOf(' ');
					if(pos<0) break parsing;

					String command = line.substring(0,pos);
					String values = line.substring(pos+1);
					
					//try to find the command
					Function<String, String> func = callable.get(command);
					if(func != null){
						func.apply(values);
					}
					
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}

}
