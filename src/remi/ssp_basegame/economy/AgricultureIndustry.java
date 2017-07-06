package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.logln;

import java.util.Collection;

import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.LongInterval;

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
	
	public final int baseEfficiency = 4;
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		//TODO transform some "friche" to fields
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		long production = 0;
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		logln(",\"agrinbChamps\":"+nbChamps+", \"pourcentChamps\":"+prv.pourcentChamps);
		for(Pop pop : pops){
			//
			long nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed(indus));
			production += prv.champsRendement * nbChampsUsed * baseEfficiency * durationInDay;
			logln(", \"nbChampsUsed_"+pop+"\":"+nbChampsUsed+", \"prod_"+pop+"\":"+(prv.champsRendement * nbChampsUsed * baseEfficiency * durationInDay));
			nbChamps -= nbChampsUsed;
			if(nbChamps == 0) break;
		}
		

		//produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (long)(production * testefficiency), durationInDay);
		logln(", \"prod fianle with tools\":"+intproduction);
	
		//do not do that, economyplugin will call it after caling this
		//super.sellProductToMarket(prv, intproduction, durationInDay);

		return intproduction;
	}


	
	/**
	 * Redefine this method to limit the number of people that can be hire here.
	 * Try to hire at least enough men to produce enough crop to supply consumption.
	 * Try to not hire more men than fields
	 */
	public LongInterval needHire(LongInterval toReturn, ProvinceIndustry provinceIndustry, Pop pop, int nbDays)
	{ 
		Province prv = provinceIndustry.getProvince();
		// 1 men produce provinceIndustry.getProvince().champsRendement * 8 per day
		//to produce 1 per day, i need 1/(8*champsRendement) men
		
		long nbEmployes = provinceIndustry.getEmployes();
		//reduce to max number of fields usable
		long maxEmployes = (long) ((prv.pourcentChamps * prv.surface) * 10L);
		//allow x time more mens than necessary (inefficiency)
		maxEmployes *=2;
		long nbMensWishMin = 1+ (long) ( provinceIndustry.getProvince().getStock().get(super.createThis).getNbConsumePerDayConsolidated() / (prv.champsRendement * baseEfficiency));
		long nbMenWishMax = Math.max(0, maxEmployes - nbEmployes);
		nbMensWishMin = Math.min(Math.max(0, nbMensWishMin - nbEmployes), nbMenWishMax);
		if(toReturn==null)toReturn=new LongInterval(nbMensWishMin, nbMenWishMax); return toReturn.set(nbMensWishMin,nbMenWishMax); 
	}
	

	
}
