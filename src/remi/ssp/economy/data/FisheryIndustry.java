package remi.ssp.economy.data;

import java.util.Collection;

import remi.ssp.Pop;
import remi.ssp.Province;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;

//create food from fish
//TODO: create FisheryNeed to grab some boats and nets
public class FisheryIndustry extends Industry {

	static protected FisheryIndustry ptr;
	public static void load(){ ptr = new FisheryIndustry(); }
	public static FisheryIndustry get(){ return ptr; }

	private FisheryIndustry(){
		myNeeds = null;
		createThis = Good.get("fish");
	}
	
	
	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		if(!prv.isCoastal()) return 0;
		

		// check how many sea tiles
		int seaTiles = 0;
		for (int i = 0; i < prv.myPLots.length; i++) {
//						if (prv.proche[i] != null && prv.proche[i].surfaceSol < 10)
//							seaTiles++;
			if (prv.myPLots[i] != null && prv.myPLots[i].isSea) seaTiles ++;
		}
		
		//TODO: use boats, and other goods to increase production
		int production = 0;
		for(Pop pop : pops){
			//1 tile : 1,66 men, 2 tiles: 2 mens, 3t: 2.33, 4t: 2.66, 5t: 3  (max) 6t: 3.33 mens
			production += (int)( (2+seaTiles/2) * pop.getNbMensEmployed().getInt(indus) * 1);
		}
		
		//TODO set a rendement décroissant pour modeliser la surpeche
		
		return production * durationInDay;
	}

}
