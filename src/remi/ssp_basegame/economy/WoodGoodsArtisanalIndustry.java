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
	
	BasicIndustryNeeds myBasicNeeds;
	Good wood;
	
	//TODO: i need lumber!
	protected WoodGoodsArtisanalIndustry(){
		createThis = Good.get("wood_goods");
		myBasicNeeds = new BasicIndustryNeeds(this)
				.addRawGood(Good.get("wood"), 2)
				.addToolGood(Good.get("wood_goods"), 1);
		myNeeds = myBasicNeeds;
		wood = Good.get("wood");
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
		System.out.println("Woods goods: i have "+prv.getIndustry(ptr).getStock().getLong(wood));

		//a kilo of goods per worker per day with a kilo of tools
		// the quarter if no tools
		float production = nbMens * 0.25f;;
		production += 0.75f * Math.max(prv.getIndustry(ptr).getStock().getLong(wood), nbMens) * createThis.getIndustryToolEfficiency();
		production *= durationInDay;
		
		// produce
		long intproduction = myBasicNeeds.useGoodsAndTools(indus, (int)production, durationInDay);
		super.sellProductToMarket(prv, intproduction, durationInDay);
	
		return intproduction;
	}
}
