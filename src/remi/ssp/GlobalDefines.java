package remi.ssp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

public class GlobalDefines {
	
	public static final int VERSION = 1;
	

	static DecimalFormat df = new DecimalFormat("#.###");
	public static String fm(double d){
		return df.format(d/1000f).replace(',', '.');
	}
	public static String f(double d){
		return df.format(d).replace(',', '.');
	}
	static DecimalFormat di = new DecimalFormat("#");
	public static String i(double d){
		return df.format(d);
	}
	
	
	private static PrintWriter out = null;
	public static void logFlush(){
		out.flush();
	}
	public static void plog(String st){
		log(st);
		System.out.print(st);
	}
	public static void plogln(String st){
		logln(st);
		System.out.println(st);
	}
	public static void log(String st){
		if(out==null){
			try {
				out= new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File("logs.json"))));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		out.print(st);
	}
	public static void logln(String st){
		if(out==null){
			try {
				out= new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File("logs.json"))));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		out.println(st);
	}
	public static void cleanup(){
		if(out!=null) out.close();
	}
	

}
