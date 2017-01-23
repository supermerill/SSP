package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from fields
public class AgricultureIndustry extends Industry {

	static protected AgricultureIndustry ptr;
	public static void load(){ ptr = new AgricultureIndustry(); }
	public static AgricultureIndustry get(){ return ptr; }

	BasicIndustryNeeds myBasicNeeds;

	private AgricultureIndustry(){
		myBasicNeeds = new BasicIndustryNeeds(this)
				.addToolGood(Good.get("wood_goods"), 1);
		createThis = Good.get("crop");
		myNeeds = myBasicNeeds;
	}
	
	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		int production = 0;
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		for(Pop pop : pops){
			//
			int nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed().getInt(indus));
			production += prv.champsRendement * nbChampsUsed * 4;
			nbChamps -= nbChampsUsed;
			if(nbChamps == 0) break;
		}
		
		

		//produce
		int intproduction = myBasicNeeds.useGoodsAndTools(indus, (int)production, durationInDay);
		super.sellProductToMarket(prv, intproduction, durationInDay);

		return intproduction;
	}

}
