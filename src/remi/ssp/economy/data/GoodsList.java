package remi.ssp.economy.data;

import remi.ssp.Plugin;
import static remi.ssp.economy.Good.GoodFactory.create;

public class GoodsList extends Plugin {
	private static final long serialVersionUID = 1L;
	
	
	@Override
	public void loadData() {
		super.loadData();
		

		FoodNeed.kJoules.put(create("crop",0.98f, 1).get(), 2000);
		FoodNeed.kJoules.put(create("fish",0.5f, 4).get(), 2000);
		FoodNeed.kJoules.put(create("rareFish",0.5f, 10).get(), 2000);
		FoodNeed.kJoules.put(create("meat",0.5f, 3).get(), 2000);
		FoodNeed.kJoules.put(create("rareMeat",0.5f, 6).get(), 2000);
		create("rawWood",0.99f, 1);
		create("plank",0.99f, 10);
		HouseNeed.houses.add(create("woodenHouse", 0.99f, 1).setCanBeMoved(false).get());
		HouseNeed.houses.add(create("bigWoodenHouse", 0.99f, 10).setCanBeMoved(false).get());
		HouseNeed.houses.add(create("basicStoneHouse", 0.998f, 6).setCanBeMoved(false).get());
		HouseNeed.houses.add(create("bigStoneHouse", 0.998f, 100).setCanBeMoved(false).get());
		create("woodenBasicGoods",0.95f, 1);
		create("woodenNormalGoods",0.95f, 3);
		create("woodenLuxuryGoods",0.95f, 10);

		create("coal",0.99f, 0);
		create("badSteel",0.99f, 0);
		create("goodSteel",0.99f, 0);
		create("premiumSteel",0.99f, 0);
		create("steelBasicGoods",0.99f, 0);
		create("steelNormalGoods",0.99f, 0);
		create("steelLuxuryGoods",0.99f, 0);
		
		
	}
	

}
