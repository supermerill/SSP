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
	
	//TODO: i need lumber!
	private WoodGoodsArtisanalIndustry(){
		createThis = Good.get("wood_goods");
		myNeeds = new BasicIndustryNeeds(this)
				.addRawGood(Good.get("wood"), 2)
				.addToolGood(Good.get("wood_goods"), 1);
	}
	
	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		Object2IntMap<Good> stock = prv.getIndustry(ptr).getStock();
		
		int nbMens = 0;
		for(Pop pop : pops){
			//TODO tools
			nbMens += pop.getNbMensEmployed().getInt(indus);
			// take into account personal tools ?
		}

		//a kilo of goods per worker per day with a kilo of tools
		// the quarter if no tools
		float production = nbMens * 0.25f;;
		production += 0.75f * Math.max(prv.getIndustry(ptr).getStock().getInt(createThis), nbMens) * createThis.getIndustryToolEfficiency();
		production *= durationInDay;
		
		// break some tools (0.5% of tools break per use per day)
		stock.put(createThis, (int)(Math.min(stock.getInt(createThis), production) * 0.005f * durationInDay)  );
		
	
		return 0;
	}
}
