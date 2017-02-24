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
		tools = Good.get("wood_goods");
		ptr = new WoodHouseIndustry();
		Industry.put("wood_house", ptr);
	}
	public static WoodHouseIndustry get(){ return ptr; }

	BasicIndustryNeeds myBasicNeeds;
	
	private WoodHouseIndustry(){
		createThis = Good.get("wood_house");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi)
				.addRawGood(Good.get("wood"), 2)
				.addToolGood(Good.get("wood_goods"), 1);;
	}
	
	private BasicIndustryNeeds getNeed(ProvinceIndustry indus){
		return (BasicIndustryNeeds)indus.getNeed();
	}

	static protected Good tools;
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		
		long nbMens = 0;
		for(Pop pop : pops){
			//TODO tools
			nbMens += pop.getNbMensEmployed(indus);
			// take into account personal tools ?
		}

		//a kilo of goods per worker per day with a kilo of tools
		// the quarter if no tools
		float production = nbMens * 0.25f;
		production += 0.75f * Math.max(prv.getIndustry(ptr).getStock().getLong(tools), nbMens) * createThis.getIndustryToolEfficiency();
		production *= durationInDay * 100;
		

		// produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)production, durationInDay);
	
		return intproduction;
	}
}
