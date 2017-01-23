package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from livestock (and grow the livestock)
public class HuntingIndustry extends Industry {

	static protected HuntingIndustry ptr;
	public static void load(){ ptr = new HuntingIndustry(); }
	public static HuntingIndustry get(){ return ptr; }

	BasicIndustryNeeds myBasicNeeds;

	protected HuntingIndustry() {
		createThis = Good.get("meat");
		myBasicNeeds = new BasicIndustryNeeds(this)
				.addToolGood(Good.get("wood_goods"), 1);
		myNeeds = myBasicNeeds;
	}
	
	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();


	
	//TODO: use tools
	int nbForest = (int) ( (prv.pourcentForet * prv.surface * 100 )); // 1 hectare per Rabbit
	int nbRabbit = (indus.getStock().getInt(Good.GoodFactory.get("meat"))); //100kg per Rabbit
	
	//temp booststrap (there are always at least two rabbit)
	if(nbRabbit == 0){
		indus.getStock().put(Good.GoodFactory.get("meat"), 2);
		nbRabbit = 2;
	}
	
	
	//multiplicate
	// * 1.2 every year => +0.05556% per day
	int newRabbit = (int) (nbRabbit * durationInDay * 0.00056);
	if(newRabbit <= 0){
		if( GlobalRandom.aleat.getInt(1000, (int)(prv.pourcentPrairie*1000000)) < (int) (nbRabbit * durationInDay * 1.39) ){
			newRabbit = 1;
		}
	}
	
	//if too many rabbit, reduce their birth
	if(nbRabbit>nbForest){
		newRabbit -= newRabbit * (1-(nbRabbit-nbForest)/(1+nbForest));
	}
	
	nbRabbit += newRabbit;
	
	
	//nb Rabbit to sell: because nbRabbit can't go higher than:
	// - X Rabbit per people * efficacity
	// - X Rabbit per prairie
	
	//TODO
	int nbRabbitToSell = 0;
	int nbMens = 0;
	for(Pop pop : pops){
		nbMens += pop.getNbMensEmployed().getInt(indus);
	}


	// reduce efficacity of hunting per rabbit density.
	//at full livestock, it can hunt 6 rabbit per day (food for 4 mens)
	nbRabbitToSell = (int)(durationInDay * 6 * nbMens * (nbRabbit / (float)nbForest));
	nbRabbit -= nbRabbitToSell;
	
	//note: over-hunt can happen easily (no hunter limit), it's intended.
		
	//set new livestock
	indus.getStock().put(Good.GoodFactory.get("meat"), nbRabbit);
	

	// produce
	int intproduction = myBasicNeeds.useGoodsAndTools(indus, (int)nbRabbitToSell, durationInDay);
	super.sellProductToMarket(prv, intproduction, durationInDay);

	return intproduction;
}

}
