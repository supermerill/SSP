package remi.ssp.economy.data;

import remi.ssp.Plugin;
import static remi.ssp.economy.Good.GoodFactory.create;

public class BasicEconomyPlugin extends Plugin {
	private static final long serialVersionUID = 1L;
	
	
	@Override
	public void loadGoods() {
		super.loadGoods();
		

		FoodNeed.kJoules.put(create("crop",0.8f, 1).get(), 2000);
		FoodNeed.kJoules.put(create("fish",0.1f, 4).get(), 2000);
		FoodNeed.kJoules.put(create("rare_fish",0.1f, 10).get(), 2000);
		FoodNeed.kJoules.put(create("meat",0.1f, 3).get(), 2000);
		FoodNeed.kJoules.put(create("rare_meat",0.1f, 6).get(), 2000);
		//create("rawWood",0.9f, 1);
		//create("plank",0.95f, 10);
		create("wood",0.95f, 10); //for now, split it in rawwood & plank later (extension)
		HouseNeed.houses.add(create("wooden_house", 0.95f, 1).setCanBeMoved(false).get());
		HouseNeed.houses.add(create("stone_house", 0.98f, 10).setCanBeMoved(false).get());
		HouseNeed.houses.add(create("manoir_house", 0.99f, 100).setCanBeMoved(false).get());
//		create("woodenBasicGoods",0.7f, 1); //extension
//		create("woodenNormalGoods",0.7f, 3);
//		create("woodenLuxuryGoods",0.7f, 10);
		create("woodenGoods",0.7f, 10).setIndustryToolEfficiency(1);

		create("coal",0.99f, 0);
//		create("badSteel",0.9f, 0); //later
//		create("goodSteel",0.9f, 0);
//		create("premiumSteel",0.9f, 0);
		create("steel",0.9f, 0);
//		create("steelBasicGoods",0.9f, 0); //later
//		create("steelNormalGoods",0.9f, 0);
//		create("steelLuxuryGoods",0.9f, 0);
		create("steel_goods",0.9f, 10).setIndustryToolEfficiency(10);
		
		
	}
	
	@Override
	public void loadIndustry() {
		super.loadIndustry();

		AgricultureIndustry.load();
		ElevageIndustry.load();
		FisheryIndustry.load();
		HuntingIndustry.load();
		WoodcutterIndustry.load();
		WoodGoodsArtisanalIndustry.load();
		WoodHouseIndustry.load();
		
		
	}
	

}
