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
		public static Good get(String name) { return goodList.get(name); }
		private static Good create(String name, int weight, float storageLossPerYear, int desirability){
			Good good = new Good();
			good.name = name;
			good.transportability = weight;
			good.storageLossPerYear = storageLossPerYear;
			good.desirability = desirability;
			goodList.put(name, good);
			return good;
		}

		private Good currentGood = null;
		private static GoodFactory me = new GoodFactory();;
		private GoodFactory(){}
		
		public static GoodFactory create(String name, float storageLossPerYear, int desirability){
			me.currentGood = create(name, 1000, storageLossPerYear, desirability);
			return me;
		}

		public GoodFactory setName(String name) { currentGood.name = name; return me; }
		public GoodFactory setTransportability(int transportability) { currentGood.transportability = transportability; return me; }
		public GoodFactory setCanBeMoved(boolean canBeMoved) { currentGood.canBeMoved = canBeMoved; return me; }
		public GoodFactory setNaval(boolean isNaval) { currentGood.isNaval = isNaval; return me; }
		public GoodFactory setStorageLossPerYear(float storageLossPerYear) { currentGood.storageLossPerYear = storageLossPerYear; return me; }
		public GoodFactory setCommerceCapacityPerMen(int commerceCapacityPerMen) { currentGood.commerceCapacityPerMen = commerceCapacityPerMen; return me; }
		public GoodFactory setIndustryToolEfficiency(int industryToolEfficiency) {	currentGood.industryToolEfficiency = industryToolEfficiency; return me; }
		public Good get() { return currentGood; }
	}
	public static final Good get(String name){
		return GoodFactory.get(name);
	}
	
	String name;
	
	
//	// 0=vital (nouriture), 1=nécécité, 2=commodité, 4=confort, 5=luxe
//	byte consumePriority = 5;
//	//number of this good a pop want for him
//	byte wantedStock = 0;
//	Needs buyingBehavior;
	
	int transportability; // 0 = dematerialized, 1 = 1 gram, 1000= a notebook (1kg, 1liter), 1 000 000 = 1 tonne, a car. -1 = not transportable (house)
	
	boolean canBeMoved = true;
	boolean isNaval = false;
	
	float storageLossPerYear = 0.95f;
	
	int desirability=0; //  +1 = 20% better, +5 = two time better!
	
	
	//stats
	int commerceCapacityPerMen = 0; //increase the commerce capacity of a men by a certain amount
	int industryToolEfficiency = 0;
	
	
	/**
	 * @param previousStock
	 * @return new stock
	 */
	public int storageLoss(int previousStock, int durationInDay) {
		//TODO: change his with tech
		//reduce the quantity by X% per month
		return (int)(previousStock * Math.pow(storageLossPerYear, durationInDay/360.0));
	}

	public boolean isNaval() { return false; }
	public int getWeight() { return transportability; }
	public String getName() { return name; }
	public int getTransportability() { return transportability; }
	public boolean isCanBeMoved() { return canBeMoved; }
	public float getStorageLossPerYear() { return storageLossPerYear; }
	public int getDesirability() { return desirability; }
	public int getCommerceCapacityPerMen() { return commerceCapacityPerMen; }
	public int getIndustryToolEfficiency() { return industryToolEfficiency;	}
	
	
	
	
	
	
}
