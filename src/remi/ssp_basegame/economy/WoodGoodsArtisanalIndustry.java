package remi.ssp_basegame.economy;

import java.util.Collection;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create lumber from forest (and grow the forest)
public class WoodGoodsArtisanalIndustry extends Industry {

	static protected WoodGoodsArtisanalIndustry ptr;
	public static void load(){ ptr = new WoodGoodsArtisanalIndustry(); }
	public static WoodGoodsArtisanalIndustry get(){ return ptr; }
	
	Good wood;
	Good tools;
	
	//TODO: i need lumber!
	protected WoodGoodsArtisanalIndustry(){
		createThis = Good.get("wood_goods");
		wood = Good.get("wood");
		tools = Good.get("wood_goods");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi)
				.addRawGood(Good.get("wood"), 2)
				.addToolGood(Good.get("wood_goods"), 1);;
	}
	
	private BasicIndustryNeeds getNeed(ProvinceIndustry indus){
		return (BasicIndustryNeeds)indus.getNeed();
	}
	
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
		float production = nbMens * 0.25f;
		production += nbMens * 0.75f * Math.min(prv.getIndustry(ptr).getStock().getLong(tools), nbMens) * createThis.getIndustryToolEfficiency();
		production *= durationInDay;
		
		// produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)production, durationInDay);
	
		return intproduction;
	}
}
