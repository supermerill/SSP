package remi.ssp.economy.data;

import java.util.Collection;

import remi.ssp.Pop;
import remi.ssp.Province;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;

public class HuntingIndustry extends Industry {

	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();

	
	//TODO: use tools
	int production = 0;
	int nbForest = (int) ( (prv.pourcentForet * prv.surface / 100 )); // 1 hectare per Rabbit
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
		newRabbit -= newRabbit * (1-(nbRabbit-nbForest)/nbForest);
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
	if(nbRabbit > 30 * nbMens){ //30 Rabbit per people: can sustain an other men
		nbRabbitToSell = nbRabbit - 30 * nbMens;
		nbRabbit -= nbRabbitToSell;
	}
	if(nbRabbit > nbFields){ //30 Rabbit per people: can sustain an other men
		nbRabbitToSell = nbRabbit - nbFields;
		nbRabbit -= nbRabbitToSell;
	}
		
	//set new livestock
	indus.getStock().put(Good.GoodFactory.get("meat"), nbRabbit);
	
	
	//TODO: sell more Rabbit if the price is high and famine is occurring.
	
	return nbRabbitToSell;
}

}
