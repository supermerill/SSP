package remi.ssp_basegame.economy;

import static remi.ssp.economy.Good.GoodFactory.create;

import com.google.auto.service.AutoService;

import remi.ssp.Plugin;
import remi.ssp.economy.PopNeed;
import remi.ssp.politic.Pop;

@AutoService(Plugin.class)
public class BasicEconomyPlugin extends Plugin {
	private static final long serialVersionUID = 1L;
	
	
	@Override
	public void loadGoods() {
		super.loadGoods();
		

		FoodNeed.kJoules.put(create("crop",0.1f, 1).setOptimalNbDayStock(300).setVolatility(1f).get(), 2000);
		FoodNeed.kJoules.put(create("fish",0.5f, 4).setOptimalNbDayStock(2).setVolatility(2f).get(), 2000);
//		FoodNeed.kJoules.put(create("rare_fish",0.9f, 10).setOptimalNbDayStock(1).get(), 2000);
		FoodNeed.kJoules.put(create("meat",0.5f, 3).setOptimalNbDayStock(2).setVolatility(2f).get(), 2000);
		FoodNeed.kJoules.put(create("rare_meat",0.9f, 6).setOptimalNbDayStock(1).setVolatility(1f).get(), 2000);
		//create("rawWood",0.9f, 1);
		//create("plank",0.95f, 10);
		create("wood",0.95f, 10).setOptimalNbDayStock(300); //for now, split it in rawwood & plank later (extension)
		HouseNeed.houses.add(create("wood_house", 0.95f, 1).setCanBeMoved(false).setVolatility(0.2f).get());
//		HouseNeed.houses.add(create("stone_house", 0.98f, 10).setCanBeMoved(false).setVolatility(0.1f).get());
//		HouseNeed.houses.add(create("manoir_house", 0.99f, 100).setCanBeMoved(false).setVolatility(0.05f).get());
//		create("woodenBasicGoods",0.7f, 1); //extension
//		create("woodenNormalGoods",0.7f, 3);
//		create("woodenLuxuryGoods",0.7f, 10);
		create("wood_goods",0.7f, 10).setVolatility(0.5f).setIndustryToolEfficiency(1);

//		create("coal",0.99f, 0);
//		create("badSteel",0.9f, 0); //later
//		create("goodSteel",0.9f, 0);
//		create("premiumSteel",0.9f, 0);
//		create("steel",0.9f, 0);
//		create("steelBasicGoods",0.9f, 0); //later
//		create("steelNormalGoods",0.9f, 0);
//		create("steelLuxuryGoods",0.9f, 0);
//		create("steel_goods",0.9f, 10).setIndustryToolEfficiency(10);
		

		create("service", 0.001f, 100).setCanBeMoved(false).setVolatility(1.2f);
	}
	
	@Override
	public void loadIndustry() {
		super.loadIndustry();

		AgricultureIndustry.load();
		ElevageIndustry.load();
		FisheryIndustry.load();
		HuntingIndustry.load();
		HuntingSportIndustry.load();
		WoodcutterIndustry.load();
		WoodGoodsArtisanalIndustry.load();
		WoodHouseIndustry.load();
		PersonalServiceIndustry.load();
		SubsistanceIndustry.load();

		PopNeed.PopNeedFactoryStorage.put("food", pop -> new FoodNeed(pop));
		PopNeed.PopNeedFactoryStorage.put("house", pop -> new HouseNeed(pop));
		PopNeed.PopNeedFactoryStorage.put("middle_service", pop -> new MiddleNeedService(pop));
		PopNeed.PopNeedFactoryStorage.put("rich_service", pop -> new RichNeedService(pop));
		PopNeed.PopNeedFactoryStorage.put("goods", pop -> new GoodsNeed(pop));
		
	}
	

}
