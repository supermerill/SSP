package remi.ssp.testnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.Charset;

import remi.ssp.network.SSPServerSocket;

public class TestNet {

	public static void main(String[] args) throws IOException {
		
		String in;
		
		Socket server = new Socket("localhost", SSPServerSocket.port);
		BufferedReader serverIn = new BufferedReader(new InputStreamReader(server.getInputStream()));
		PrintStream serverOut = new PrintStream(server.getOutputStream());
		
		BufferedReader inl = new BufferedReader(new InputStreamReader(System.in, Charset.forName("UTF-8")));

		System.out.print("cmd> ");
		in = inl.readLine();
		while(!in.equals("quit")){
			
			//send it to server
			serverOut.println(in);

			System.out.println("result:");
			System.out.println(serverIn.readLine());

			System.out.print("cmd> ");
			in = inl.readLine();
		}

	}

}
