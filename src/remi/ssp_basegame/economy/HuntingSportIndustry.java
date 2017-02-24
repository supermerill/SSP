package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.logln;

import java.util.Collection;

import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from livestock (and grow the livestock)
public class HuntingSportIndustry extends Industry {

	static protected HuntingSportIndustry ptr;
	public static void load(){ ptr = new HuntingSportIndustry(); Industry.put("bighunting", ptr); }
	public static HuntingSportIndustry get(){ return ptr; }

	protected HuntingSportIndustry() {
		createThis = Good.get("rare_meat");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi)
				.addToolGood(Good.get("wood_goods"), 1);
	}

	private BasicIndustryNeeds getNeed(ProvinceIndustry indus){
		return (BasicIndustryNeeds)indus.getNeed();
	}
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();


	
	//TODO: use tools
	long nbForest = (int) ( (prv.pourcentForet * prv.surface * 100 )); // 1 hectare per Rabbit
	long nbCerf = (indus.getStock().getLong(createThis)); //100kg per Rabbit
	
	//temp booststrap (there are always at least two rabbit)
	if(nbCerf == 0){
		indus.getStock().put(createThis, nbForest/2);
		nbCerf = nbForest/2;
	}
	
	
	//multiplicate
	// * 1.2 every year => +0.0005556% per day
	// * 2 every year => +0.0028
	long newCerf = 1+(int) (nbCerf * durationInDay * 0.0005556);
	if(newCerf <= 0){
		if( GlobalRandom.aleat.getInt(1000, (int)(prv.pourcentPrairie*1000000)) < (int) (nbCerf * durationInDay * 1.39) ){
			newCerf = 1;
		}
	}
	
	//if too many animals, reduce their birth
	if(nbCerf>nbForest){
		newCerf -= newCerf * (1-(nbCerf-nbForest)/(1+nbForest));
	}
	
	nbCerf += newCerf;
	
	
	//nb Rabbit to sell: because nbRabbit can't go higher than:
	// - X Rabbit per people * efficacity
	// - X Rabbit per prairie
	
	//TODO
	long nbRabbitToSell = 0;
	long nbMens = 0;
	for(Pop pop : pops){
		nbMens += pop.getNbMensEmployed(indus);
	}


	// reduce efficacity of hunting per rabbit density.
	//at full livestock, it can hunt 1 kilocerf per day (food for 0.6 mens)
	nbRabbitToSell = (int)(durationInDay * nbMens * (nbCerf / (float)nbForest));
	nbCerf -= nbRabbitToSell;

	logln(",\"nbCerfToSell\":"+nbRabbitToSell+",\"calculus:\":\"nbDay("+durationInDay+")*"+nbMens+(nbCerf / (float)nbForest)+"\"");
	
	//note: over-hunt can happen easily (no hunter limit), it's intended.
		
	//set new livestock
	indus.getStock().put(createThis, nbCerf);
	

	// produce
	long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)nbRabbitToSell, durationInDay);

	return intproduction;
}

}
