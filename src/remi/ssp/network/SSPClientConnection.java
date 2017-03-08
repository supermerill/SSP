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

import remi.ssp.GlobalDefines;

public class SSPClientConnection implements Runnable {
	
	public static Map<String, Function<String, String>> callable = new HashMap<>();
	

	private Socket client;
	private BufferedReader reader;
	private PrintWriter writer;

	public SSPClientConnection(Socket client) {
		this.client = client;
		try {
			GlobalDefines.plogln(",\"new client\":"+client.getLocalPort());
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
				GlobalDefines.plogln(",\"receive\":\""+line+"\"");
				
				//basic check
				parsing: if(line.charAt(0) != '#' && line.length()>10 && line.length()<10000){
					//get command
					int pos = line.indexOf(' ');
					if(pos<0){
						GlobalDefines.plogln(",\"return\":\"error parse cmd\"");
						writer.println("Error: can't parse your string '"+line+"'");
						break parsing;
					}

					String command = line.substring(0,pos);
					String values = line.substring(pos+1);
					GlobalDefines.plogln(",\"command\":\""+command+"\"");
					GlobalDefines.plogln(",\"values\":\""+values+"\"");
					
					//try to find the command
					Function<String, String> func = callable.get(command);
					if(func != null){
						writer.println(func.apply(values));
						GlobalDefines.plogln(",\"return\":\"call \"");
					}else{
						writer.println("Error: cmd '"+command+"' not finded");
					}
					
				}else{
					GlobalDefines.plogln(",\"return\":\"error parse\"");
					writer.println("Error: can't parse your string '"+line+"'");
				}

				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
			
		}
		
	}

}
