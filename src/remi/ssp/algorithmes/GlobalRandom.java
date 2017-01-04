package remi.ssp.algorithmes;

import java.util.Random;

public class GlobalRandom {

	public static GlobalRandom aleat = new GlobalRandom();
	
	private Random rand = new Random();
	
	public int getInt(int modulo, int salt){
		return (rand.nextInt() & 0x7FFFFFFF) % modulo;
	}
	
}
