package remi.ssp.algorithmes;
import static remi.ssp.GlobalDefines.logln;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GlobalRandom {

	public static GlobalRandom aleat;
	static{
		long time = System.currentTimeMillis();
		aleat = new GlobalRandom();
		aleat.rand = new Random(1486746923541l);
		logln(", \"create Aleat with seed\":"+time);
	}
	
	private Random rand;
	
	public int getInt(int modulo, int salt){
		return (rand.nextInt() & 0x7FFFFFFF) % modulo;
	}
	
	public void shuffle(List<?> list) {
        Collections.shuffle(list, rand);
    }

	public int nextInt(int modulo) {
		return rand.nextInt(modulo);
	}
	
}
