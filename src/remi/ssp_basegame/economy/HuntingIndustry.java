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
public class HuntingIndustry extends Industry {

	static protected HuntingIndustry ptr;
	public static void load(){ ptr = new HuntingIndustry(); }
	public static HuntingIndustry get(){ return ptr; }

	protected HuntingIndustry() {
		createThis = Good.get("meat");
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
	long nbRabbit = (indus.getStock().getLong(Good.GoodFactory.get("meat"))); //100kg per Rabbit
	
	//temp booststrap (there are always at least two rabbit)
	if(nbRabbit == 0){
		indus.getStock().put(Good.GoodFactory.get("meat"), nbForest/2);
		nbRabbit = nbForest/2;
	}
	
	
	//multiplicate
	// * 1.2 every year => +0.05556% per day
	// * 2 every year => +0.0028
	long newRabbit = 1+(int) (nbRabbit * durationInDay * 0.0028);
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
	long nbRabbitToSell = 0;
	long nbMens = 0;
	for(Pop pop : pops){
		nbMens += pop.getNbMensEmployed(indus);
	}


	// reduce efficacity of hunting per rabbit density.
	//at full livestock, it can hunt 6 rabbit per day (food for 4 mens)
	nbRabbitToSell = (int)(durationInDay * 3 * nbMens * (nbRabbit / (float)nbForest));
	nbRabbit -= nbRabbitToSell;

	logln(",\"nbRabbitToSell\":"+nbRabbitToSell+",\"calculus:\":\"nbDay("+durationInDay+")*6*"+nbMens+(nbRabbit / (float)nbForest)+"\"");
	//note: over-hunt can happen easily (no hunter limit), it's intended.
		
	//set new livestock
	indus.getStock().put(Good.GoodFactory.get("meat"), nbRabbit);
	

	// produce
	long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)nbRabbitToSell, durationInDay);
	
//	//and rare_meat (note: industry is not designed to handle multi-products right now. be careful with these "bonus")
//	long price = prv.getStock().get(Good.GoodFactory.get("rare_meat")).getPriceSellToMarket(prv, durationInDay);
//	prv.addMoney(-price*nbRabbitToSell/10);
//	prv.getStock().get(Good.GoodFactory.get("rare_meat")).addStock(nbRabbitToSell/10);
//	prv.getIndustry(this).addMoney(price*nbRabbitToSell/10);

	return intproduction;
}

}
