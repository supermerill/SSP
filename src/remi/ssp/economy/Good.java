package remi.ssp.economy;

import java.util.ArrayList;
import java.util.HashMap;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * exemples: 
 * basic food
 * good food
 * luxurious food
 * basic goods
 * comfort goods
 * luxurious goods
 * tools for artisan
 * tools for farmers
 * tools for miners
 * tools for heavy industry
 * basic house
 * comfort house
 * luxuriousHouse
 * 
 * @author Merill
 *
 */
public class Good {
	
	public static class GoodFactory{
		public static HashMap<String, Good> goodList = new HashMap<>();
		public static Good get(String name) {
			Good dood = goodList.get(name); 
			if(dood == null) throw new RuntimeException("error, try to get the good '"+name+"' but it doesn't exist");
			return dood;
		}
		private static Good create(String name, int weight, float storageLossPerYear, int desirability){
			Good good = new Good();
			good.name = name;
			good.transportability = weight;
			good.storageKeepPerYear = storageLossPerYear;
			good.desirability = desirability;
			goodList.put(name, good);
			return good;
		}

		protected Good currentGood = null;
		private static GoodFactory me = new GoodFactory();;
		protected GoodFactory(){}
		
		public static GoodFactory create(String name, float storageRetainPerYear, int desirability){
			me.currentGood = create(name, 1000, storageRetainPerYear, desirability);
			return me;
		}

		public GoodFactory setName(String name) { currentGood.name = name; return me; }
		public GoodFactory setTransportability(int transportability) { currentGood.transportability = transportability; return me; }
		public GoodFactory setCanBeMoved(boolean canBeMoved) { currentGood.canBeMoved = canBeMoved; return me; }
		public GoodFactory setNaval(boolean isNaval) { currentGood.isNaval = isNaval; return me; }
		public GoodFactory setStorageKeepPerYear(float storageKeepPerYear) { currentGood.storageKeepPerYear = storageKeepPerYear; return me; }
		public GoodFactory setCommerceCapacityPerMen(int commerceCapacityPerMen) { currentGood.commerceCapacityPerMen = commerceCapacityPerMen; return me; }
		public GoodFactory setIndustryToolEfficiency(int industryToolEfficiency) {	currentGood.industryToolEfficiency = industryToolEfficiency; return me; }
		public GoodFactory setOptimalNbDayStock(float optimalStockPerMen) {	currentGood.optimalStockNbDays = optimalStockPerMen; return me; }
		public GoodFactory setVolatility(float volatility) {	currentGood.volatility = volatility; return me; }
		public Good get() { return currentGood; }
	}
	public static Good get(String name){
		if(GoodFactory.get(name) == null) System.err.println("error, good '"+name+"' doesn't exist");
		return GoodFactory.get(name);
	}
	
	protected String name;
	
	
//	// 0=vital (nouriture), 1=nécécité, 2=commodité, 4=confort, 5=luxe
//	byte consumePriority = 5;
//	//number of this good a pop want for him
//	byte wantedStock = 0;
//	Needs buyingBehavior;
	
	protected int transportability; // 0 = dematerialized, 1 = 1 gram, 1000= a notebook (1kg, 1liter), 1 000 000 = 1 tonne, a car. -1 = not transportable (house)
	
	protected boolean canBeMoved = true;
	protected boolean isNaval = false;
	
	protected float storageKeepPerYear = 0.95f;
	
	protected int desirability=0; //  +1 = 20% better, +5 = two time better!
	
	//nb days of stock is healthy for a marketplace
	protected float optimalStockNbDays = 10f;
	// coeff for the price increase and decrease algorithm
	protected float volatility = 1f;
	
	//stats
	protected int commerceCapacityPerMen = 0; //increase the commerce capacity of a men by a certain amount
	protected int industryToolEfficiency = 0;
	
	
	/**
	 * @param previousStock
	 * @return new stock
	 */
	public long storageLoss(long previousStock, long durationInDay) {
		//TODO: change his with tech
		//reduce the quantity by X% per month
		return (long)(previousStock * Math.pow(storageKeepPerYear, durationInDay/360.0));
	}

	public boolean isNaval() { return false; }
	public int getWeight() { return transportability; }
	public String getName() { return name; }
	public int getTransportability() { return transportability; }
	public boolean isCanBeMoved() { return canBeMoved; }
	public float getStorageKeepPerYear() { return storageKeepPerYear; }
	public int getDesirability() { return desirability; }
	public int getCommerceCapacityPerMen() { return commerceCapacityPerMen; }
	public int getIndustryToolEfficiency() { return industryToolEfficiency;	}
	public float getOptimalNbDayStock() { return optimalStockNbDays;	}
	public float getVolatility() { return volatility; }

	@Override
	public String toString() {
		return getName();
	}
	
}
