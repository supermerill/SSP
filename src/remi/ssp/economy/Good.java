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
	
	public static HashMap<String, Good> goodList = new HashMap<>();
	static{
		create("Crop",0.98f, 1);
		create("Fish",0.5f, 4);
		create("rareFish",0.5f, 10);
		create("Meat",0.5f, 3);
		create("rareMeat",0.5f, 6);
		create("rawWood",0.99f, 1);
		create("plank",0.99f, 10);
		create("woodenHouse", 0.99f, 1).setCanBeMoved(false);
		create("bigWoodenHouse", 0.99f, 10).setCanBeMoved(false);
		create("basicStoneHouse", 0.998f, 5).setCanBeMoved(false);
		create("bigStoneHouse", 0.998f, 100).setCanBeMoved(false);
		create("basicGoods",0.95f, 1);
		create("normalGoods",0.95f, 3);
		create("luxuryGoods",0.95f, 10);
	}
	
	private static Good create(String name, int weight, float storageLossPerMonth, int desirability){
		Good good = new Good();
		good.name = name;
		good.transportability = weight;
		good.storageLossPerMonth = storageLossPerMonth;
		good.desirability = desirability;
		goodList.put(name, good);
		return good;
	}
	private static Good create(String name, float storageLossPerMonth, int desirability){
		return create(name, 1000, storageLossPerMonth, desirability);
	}
	
	String name;
	
	
	ArrayList<Good> needsToProduce;
	IntArrayList needsQuantity;
	
//	// 0=vital (nouriture), 1=nécécité, 2=commodité, 4=confort, 5=luxe
//	byte consumePriority = 5;
//	//number of this good a pop want for him
//	byte wantedStock = 0;
//	Needs buyingBehavior;
	
	int transportability; // 0 = dematerialized, 1 = 1 gram, 1000= a notebook (1kg, 1liter), 1 000 000 = 1 tonne, a car. -1 = not transportable (house)
	
	boolean canBeMoved = true;
	boolean isNaval = false;
	
	float storageLossPerMonth = 0.95f;
	
	int desirability=0; //  +1 = 20% better, +5 = two time better!
	
	//stats
	int commerceCapacityPerMen = 0; //increase the commerce capacity of a men by a certain amount
	
	
	/**
	 * @param previousStock
	 * @return new stock
	 */
	public int storageLoss(int previousStock, int durationInDay) {
		//TODO: change his with tech
		//reduce the qaunty by X% per month
		return (int)(previousStock * Math.pow(storageLossPerMonth, durationInDay/30.0));
	}

//	public Needs getNeeds() { return buyingBehavior; }
	public boolean isNaval() { return false; }
	public int getWeight() { return transportability; }

	
	public Good setName(String name) { this.name = name; return this; }
	public Good setNeedsToProduce(ArrayList<Good> needsToProduce) { this.needsToProduce = needsToProduce; return this; }
	public Good setNeedsQuantity(IntArrayList needsQuantity) { this.needsQuantity = needsQuantity; return this; }
	public Good setTransportability(int transportability) { this.transportability = transportability; return this; }
	public Good setCanBeMoved(boolean canBeMoved) { this.canBeMoved = canBeMoved; return this; }
	public Good setNaval(boolean isNaval) { this.isNaval = isNaval; return this; }
	public Good setStorageLossPerMonth(float storageLossPerMonth) { this.storageLossPerMonth = storageLossPerMonth; return this; }
	public Good setCommerceCapacityPerMen(int commerceCapacityPerMen) { this.commerceCapacityPerMen = commerceCapacityPerMen; return this; }
	
	
	
}
