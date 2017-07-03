package remi.ssp.algorithmes;
import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import remi.ssp.GlobalDefines;

public class GlobalRandom {

	public static GlobalRandom aleat;
	static{
		long time = System.currentTimeMillis();
		aleat = new GlobalRandom();
//		aleat.rand = new Random();
//		aleat.rand = new Random(1486746923541l);
		aleat.rand = new Random(1487782673837l);
		logln(", \"create Aleat with seed\":"+time);
	}
	
	private Random rand;
	
	public int getInt(int modulo, int salt){
		if(modulo<=0) return 0;
		return ((rand.nextInt()+salt) & 0x7FFFFFFF) % modulo;
	}
	
	public void shuffle(List<?> list) {
        Collections.shuffle(list, rand);
    }

	public int nextInt(int modulo) {
		return rand.nextInt(modulo);
	}

	

	public long round(double number) {
		return (long)(number) + getLeftover(number);
	}
	
	/**
	 * 
	 * @param leftover between 0 and 1;
	 * @return 0 or 1 (always 1 if param is <0)
	 */
	public int getLeftover(double leftover) {
//		System.err.println("leftover: "+leftover);
		if(leftover>1){
			leftover = leftover - (long)leftover;
		}
		if(leftover==0) return 0;
		if(leftover<0.2){
//			System.err.println("(1/leftover) : "+(1/leftover));
			double inv = Math.max(1,(int)(1/leftover));
//			System.err.println("rand on : "+(100*inv));
			int chances = getInt((int)(100*inv), (int)inv);
//			System.err.println("chances: "+chances);
			if(chances < 100){
//				System.err.println("YES! (<100)");
				return 1;
			}
		}else{
//			System.err.println("rand on : "+(10000));
			int chances = getInt((int)(10000), (int)(1/leftover));
//			System.err.println("chances: "+chances);
			if(chances < 10000*leftover){
//				System.err.println("YES! (<"+(10000*leftover)+")");
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * very rough approximation of a poisson law
	 * @param nbInd ex: pop.getnbAdult()
	 * @param esperance ex: nbStarvingAdults
	 * @param coeff ex: nbdays/halflife
	 * @return ex: nbAdult to die
	 */
	public int poissonLaw(int nbInd, int esperance, double coeff){
		int retVal = (int)(esperance * Math.exp(-0.69*coeff));

		return Math.min(normalLaw(retVal), nbInd);
	}

	public int normalLaw(double esperance){
		int espInt = (int)esperance;
//		System.out.print(" espe="+esperance);
		return ((esperance>=1)?(getInt(espInt+1, espInt) + getInt(espInt+1, espInt)): 0) + getLeftover(esperance);
//		int left = getLeftover(esperance);
//		System.out.print(" left="+left);
//		if(esperance>1){
//			int i1 = getInt(espInt+1, espInt);
//			System.out.print(" i1="+i1);
//			int i2 = getInt(espInt+1, espInt);
//			System.out.print(" i2="+i2);
//			System.out.println(" tot="+(i1+i2+left));
//			return (i1+i2+left);
//		}
//		System.out.println();
//		return left;
	}
	
//	public static void main(String[] args) {
//		
//		double esp = 13.68;
//		long accu = 0;
//		for(int i=0;i<10000;i++){
//			accu += GlobalRandom.aleat.normalLaw(esp);
//		}
//		System.out.println(accu/10000.0);
//	}
}
