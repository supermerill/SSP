package remi.ssp.economy;

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
	
	String name;
	
	
	Good[] needsToProduce;
	int[] needsQuantity;
	
//	// 0=vital (nouriture), 1=nécécité, 2=commodité, 4=confort, 5=luxe
//	byte consumePriority = 5;
//	//number of this good a pop want for him
//	byte wantedStock = 0;
	Needs buyingBehavior;
	
	int transportability; // 0 = dematerialized, 1 = 1 gram, 1000= a notebook (1kg, 1liter), 1 000 000 = 1 tonne, a car. -1 = not transportable (house)
	
	int halfLiveInDay;
	boolean canBeMoved;
	
	/**
	 * @param previousStock
	 * @return new stock
	 */
	public int storageLoss(int previousStock, int durationInDay) {
		//reduce the qaunty by 5% per month
		return (int)(previousStock * Math.pow(0.95, durationInDay/30.0));
	}

	public Needs getNeeds() {
		return buyingBehavior;
	}

}
