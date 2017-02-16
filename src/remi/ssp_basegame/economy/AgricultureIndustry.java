package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.logln;

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
		logln(",\"agrinbChamps\":"+nbChamps+", \"pourcentChamps\":"+prv.pourcentChamps);
		for(Pop pop : pops){
			//
			long nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed(indus));
			production += prv.champsRendement * nbChampsUsed * 4 * durationInDay;
			logln(", \"nbChampsUsed_"+pop+"\":"+nbChampsUsed+", \"prod_"+pop+"\":"+(prv.champsRendement * nbChampsUsed * 4 * durationInDay));
			nbChamps -= nbChampsUsed;
			if(nbChamps == 0) break;
		}
		

		//produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)production, durationInDay);
		logln(", \"prod fianle with tools\":"+intproduction);
	
		//do not do that, economyplugin will call it after caling this
		//super.sellProductToMarket(prv, intproduction, durationInDay);

		return intproduction;
	}

}
