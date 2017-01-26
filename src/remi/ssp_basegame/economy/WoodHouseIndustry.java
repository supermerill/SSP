package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create lumber from forest (and grow the forest)
public class WoodHouseIndustry extends Industry {

	static protected WoodHouseIndustry ptr;
	public static void load(){
		good = Good.get("wood_goods");
		ptr = new WoodHouseIndustry();
	}
	public static WoodHouseIndustry get(){ return ptr; }

	BasicIndustryNeeds myBasicNeeds;
	
	private WoodHouseIndustry(){
		createThis = Good.get("wood_house");
		myBasicNeeds = new BasicIndustryNeeds(this)
				.addRawGood(Good.get("wood"), 2)
				.addToolGood(Good.get("wood_goods"), 1);
		myNeeds = myBasicNeeds;
	}
	
	static protected Good good;
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		
		long nbMens = 0;
		for(Pop pop : pops){
			//TODO tools
			nbMens += pop.getNbMensEmployed().getLong(indus);
			// take into account personal tools ?
		}

		//a kilo of goods per worker per day with a kilo of tools
		// the quarter if no tools
		float production = nbMens * 0.25f;;
		production += 0.75f * Math.max(prv.getIndustry(ptr).getStock().getLong(createThis), nbMens) * createThis.getIndustryToolEfficiency();
		production *= durationInDay;

		// produce
		long intproduction = myBasicNeeds.useGoodsAndTools(indus, (int)production, durationInDay);
		super.sellProductToMarket(prv, intproduction, durationInDay);
	
		return intproduction;
	}
}
