package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.IndustryNeed;
import remi.ssp.economy.IndustryNeed.IndustryNeedsFactoryStorage;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from fields
public class AgricultureIndustry extends Industry {

	static protected AgricultureIndustry ptr;
	public static void load(){ ptr = new AgricultureIndustry(); }
	public static AgricultureIndustry get(){ return ptr; }

	private AgricultureIndustry(){
		createThis = Good.get("crop");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi)
				.addToolGood(Good.get("wood_goods"), 1);
	}
	
	private BasicIndustryNeeds getNeed(ProvinceIndustry indus){
		return (BasicIndustryNeeds)indus.getNeed();
	}
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		//TODO transform some "friche" to fields
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		int production = 0;
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		System.out.println("nbChamps="+nbChamps+" : "+prv.pourcentChamps);
		for(Pop pop : pops){
			//
			long nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed().getLong(indus));
			production += prv.champsRendement * nbChampsUsed * 8 * durationInDay;
			System.out.println("nbChampsUsed="+nbChampsUsed+", prod ="+production);
			nbChamps -= nbChampsUsed;
			if(nbChamps == 0) break;
		}
		

		//produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)production, durationInDay);
		System.out.println(", prod with tools="+intproduction);
		super.sellProductToMarket(prv, intproduction, durationInDay);

		return intproduction;
	}

}
