package remi.ssp.economy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import remi.ssp.economy.Needs.NeedWish;

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
	

	public abstract NeedWish moneyNeeded(int nb, Collection<ProvinceGoods> prices, Object2IntMap<Good> currentStock, int totalMoneyThisTurn, int nbDays);
	public abstract int spendMoney(int nb, Collection<ProvinceGoods> prices, Object2IntMap<Good> currentStock, NeedWish maxMoneyToSpend, int nbDays);
	
	
	@Override
	public int compareTo(Needs o) {
//		int val = Byte.compare(importance,  o.importance);
		return name.compareTo(o.name);
	}
	
	
	public static class NeedWish{
		public int vitalNeed; // MUST be completed. No need for more.
		public int normalNeed; // sould be completed. Okay to have some more
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
