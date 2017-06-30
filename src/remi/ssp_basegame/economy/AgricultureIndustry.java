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
	public static void load(){ ptr = new AgricultureIndustry(); Industry.put("agriculture", ptr);}
	public static AgricultureIndustry get(){ return ptr; }

	private AgricultureIndustry(){
		createThis = Good.get("crop");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi)
				.addToolGood(Good.get("wood_goods"), 1);
	}
	
	private BasicIndustryNeeds getNeed(ProvinceIndustry indus){
		return (BasicIndustryNeeds)indus.getNeed();
	}
	
	public float testefficiency = 1;
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay, long nbWhish) {
		Province prv = indus.getProvince();
		//TODO transform some "friche" to fields
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		long production = 0;
		long nbChamps = (long) ( (prv.pourcentChamps * prv.surface) * 10L); // 10 hectare per argi
		logln(",\"agrinbChamps\":"+nbChamps+", \"pourcentChamps\":"+prv.pourcentChamps);
		for(Pop pop : pops){
			//
			long nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed(indus));
			production += prv.champsRendement * nbChampsUsed * 8 * durationInDay;
			logln(", \"nbChampsUsed_"+pop+"\":"+nbChampsUsed+", \"prod_"+pop+"\":"+(prv.champsRendement * nbChampsUsed * 4 * durationInDay));
			nbChamps -= nbChampsUsed;
			if(nbChamps == 0) break;
		}
		

		//produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (long)(production * testefficiency), durationInDay);
		logln(", \"prod fianle with tools\":"+intproduction);
	
		//do not do that, economyplugin will call it after caling this
		//super.sellProductToMarket(prv, intproduction, durationInDay);

		//store prod
		indus.getStock().put(getGood(),indus.getStock().getLong(getGood())+intproduction);
		return intproduction;
	}
	
	@Override
	public long getMenWish(ProvinceIndustry provinceIndustry, double currentConsumptionPD) {
		Province prv = provinceIndustry.getProvince();
		// 1 men produce provinceIndustry.getProvince().champsRendement * 8 per day
		//to produce 1 per day, i need 1/(8*champsRendement) men
		long nbMensWish = 1+ (long) (currentConsumptionPD / (prv.champsRendement * 8));
		//reduce to max number of fields usable
		return Math.min(nbMensWish, (long) ((prv.pourcentChamps * prv.surface) * 10L));
	}

}
