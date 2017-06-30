package remi.ssp.economy;

import java.util.HashMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import remi.ssp.politic.Province;

/**
 * A Pop want:
 * Food
 * House
 * Goods
 * 
 * An industry may want:
 * Tools
 * Factory
 * Boats
 * 
 * 
 * @author Admin
 *
 */
public abstract class Needs implements Comparable<Needs>{

	
	public static class NeedStorage{
		public static HashMap<String, Needs> goodList = new HashMap<>();
		public static Needs get(String name) { return goodList.get(name); }
		public static void put(String name, Needs obj) { goodList.put(name, obj); }
	}
	public static Needs get(String name){
		return NeedStorage.get(name);
	}

	String name;
	
//	byte importance;

//	float minimumPerPop=1;
//	float optimalPerPop=2;
	float minimumStockPerPop = 0.01f; 
	float optimalStockPerPop = 0.1f; // if the stock increase for more than that, it will decrease in price. (like 0.1 for house, 200 for food)
	

	public String getName() { if(name!=null)return name; else return getClass().getName(); }
	public float getOptimalStockPerPop() { return optimalStockPerPop; }
	public float getMinimumStockPerPop() { return minimumStockPerPop; }
	

	/**
	 * How many of any goods you need for you?
	 * @param prv province
	 * @param totalMoneyThisTurn money available
	 * @param nbDays nbDays before the next tick
	 * @return good-> nb wished
	 */
	public abstract Object2LongMap<Good> goodsNeeded(Province prv, long totalMoneyThisTurn, int nbDays);
	
	
	/**
	 * You should watch the BasicIndutryNeed or FoodNeed instead.
	 * @param prv
	 * @param nb
	 * @param currentStock
	 * @param totalMoneyThisTurn
	 * @param nbDays
	 * @return
	 */
	public abstract NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays);

	/**
	 * You should watch the BasicIndutryNeed or FoodNeed instead.
	 * @param prv
	 * @param nb
	 * @param currentStock
	 * @param maxMoneyToSpend
	 * @param nbDays
	 * @return
	 */
	public abstract long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays);
	
	
	@Override
	public int compareTo(Needs o) {
//		int val = Byte.compare(importance,  o.importance);
		return name.compareTo(o.name);
	}
	

	public static class NeedWish{
		public long vitalNeed; // MUST be completed. No need for more.
		public long normalNeed; // should be completed. Okay to have some more
		public long luxuryNeed; // may be completed. Put as many more as you want into this.
		public NeedWish(long vital, long normal, long luxury){
			vitalNeed = vital;
			normalNeed = normal;
			luxuryNeed = luxury;
		}
		public void add(NeedWish moneyNeeds) {
			vitalNeed += moneyNeeds.vitalNeed;
			normalNeed += moneyNeeds.normalNeed;
			luxuryNeed += moneyNeeds.luxuryNeed;
		}
		
		public String toString(){
			return vitalNeed+":"+normalNeed+":"+luxuryNeed;
		}
		public long getMoney() {
			return vitalNeed+normalNeed+luxuryNeed;
		}
	}
	
}
