package remi.ssp.economy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import remi.ssp.economy.Needs.NeedWish;
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

	String name;
	
//	byte importance;

//	float minimumPerPop=1;
//	float optimalPerPop=2;
	float minimumStockPerPop = 0.01f; 
	float optimalStockPerPop = 0.1f; // if the stock increase for more than that, it will decrease in price. (like 0.1 for house, 200 for food)
	

	public String getName() { return name; }
	public float getOptimalStockPerPop() { return optimalStockPerPop; }
	public float getMinimumStockPerPop() { return minimumStockPerPop; }
	
	/**
	 * You should watch the BasicIndutryNeed or FoodNeed instead.
	 * @param prv
	 * @param nb
	 * @param currentStock
	 * @param totalMoneyThisTurn
	 * @param nbDays
	 * @return
	 */
	public abstract NeedWish moneyNeeded(Province prv, int nb, Object2IntMap<Good> currentStock, int totalMoneyThisTurn, int nbDays);

	/**
	 * You should watch the BasicIndutryNeed or FoodNeed instead.
	 * @param prv
	 * @param nb
	 * @param currentStock
	 * @param maxMoneyToSpend
	 * @param nbDays
	 * @return
	 */
	public abstract int spendMoney(Province prv, int nb, Object2IntMap<Good> currentStock, NeedWish maxMoneyToSpend, int nbDays);
	
	
	@Override
	public int compareTo(Needs o) {
//		int val = Byte.compare(importance,  o.importance);
		return name.compareTo(o.name);
	}
	
	
	public static class NeedWish{
		public int vitalNeed; // MUST be completed. No need for more.
		public int normalNeed; // should be completed. Okay to have some more
		public int luxuryNeed; // may be completed. Put as many more as you want into this.
		public NeedWish(int vital, int normal, int luxury){
			vitalNeed = vital;
			normalNeed = normal;
			luxuryNeed = luxury;
		}
		public void add(NeedWish moneyNeeds) {
			vitalNeed += moneyNeeds.vitalNeed;
			normalNeed += moneyNeeds.normalNeed;
			luxuryNeed += moneyNeeds.luxuryNeed;
		}
	}
	
}
