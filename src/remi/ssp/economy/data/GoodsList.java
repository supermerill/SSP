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
		create("woodenHouse", 0.99f, 1).setCanBeMoved(false);
		create("bigWoodenHouse", 0.99f, 10).setCanBeMoved(false);
		create("basicStoneHouse", 0.998f, 5).setCanBeMoved(false);
		create("bigStoneHouse", 0.998f, 100).setCanBeMoved(false);
		create("basicGoods",0.95f, 1);
		create("normalGoods",0.95f, 3);
		create("luxuryGoods",0.95f, 10);
		
	}
	

}
