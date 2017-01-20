package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from animals (and grow the cheptel)
//needs:  TODO: buy sheep from market if possible
public class ElevageIndustry extends Industry {

	static protected ElevageIndustry ptr;
	public static void load(){ ptr = new ElevageIndustry(); }
	public static ElevageIndustry get(){ return ptr; }
	
	private ElevageIndustry(){
		myNeeds = new BasicIndustryNeeds(this)
				.addToolGood(Good.get("wood_goods"), 1);
		createThis = Good.get("meat");
	}
	
	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();

		
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		int production = 0;
		int nbFields = (int) ( (prv.pourcentPrairie * prv.surface * 100 )); // 1 hectare per sheep
		int nbSheep = (indus.getStock().getInt(Good.GoodFactory.get("meat"))/100); //100kg per sheep
		
		//temp booststrap TODO replace by needs
		if(nbSheep == 0){
			indus.getStock().put(Good.GoodFactory.get("meat"), 100);
			nbSheep = 1;
		}
		
		
		//multiplicate
		// * 1.5 every year => +0.13% per day
		int newSheeps = (int) (nbSheep * durationInDay * 0.00139);
		if(newSheeps <= 0){
			if( GlobalRandom.aleat.getInt(1000, (int)(prv.pourcentPrairie*1000000)) < (int) (nbSheep * durationInDay * 1.39) ){
				newSheeps = 1;
			}
		}
		nbSheep += newSheeps;
		
		//nb sheep to sell: because nbSheep can't go higher than:
		// - X sheep per people * efficacity
		// - X sheep per prairie
		
		//TODO
		int nbSheepToSell = 0;
		int nbMens = 0;
		for(Pop pop : pops){
			nbMens += pop.getNbMensEmployed().getInt(indus);
		}
		if(nbSheep > 30 * nbMens){ //30 sheep per people: can sustain an other men
			nbSheepToSell = nbSheep - 30 * nbMens;
			nbSheep -= nbSheepToSell;
		}
		if(nbSheep > nbFields){ //30 sheep per people: can sustain an other men
			nbSheepToSell = nbSheep - nbFields;
			nbSheep -= nbSheepToSell;
		}
			
		//set new livestock
		indus.getStock().put(Good.GoodFactory.get("meat"), nbSheep);
		
		
		//TODO: sell more sheep if the price is high and famine is occurring.
		
		return nbSheepToSell * 100;
	}

}
