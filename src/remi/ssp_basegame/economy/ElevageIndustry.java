package remi.ssp_basegame.economy;

import java.util.Collection;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.Needs;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from animals (and grow the cheptel)
//needs:  TODO: buy sheep from market if possible
public class ElevageIndustry extends Industry {

	static protected ElevageIndustry ptr;
	public static void load(){ ptr = new ElevageIndustry(); }
	public static ElevageIndustry get(){ return ptr; }

	ElevageNeeds myElevageNeeds;
	
	private ElevageIndustry(){
		myElevageNeeds = new ElevageNeeds();
		createThis = Good.get("meat");
		myNeeds = myElevageNeeds;
	}
	
	public class ElevageNeeds extends Needs{
		
		BasicIndustryNeeds basicIndustryNeeds;
		
		ElevageNeeds(){
			basicIndustryNeeds = new BasicIndustryNeeds(ElevageIndustry.this)
					.addToolGood(Good.get("wood_goods"), 1);
		}

		@Override
		public NeedWish moneyNeeded(Province prv, long nbPrevProd, Object2LongMap<Good> currentStock, long totalMoneyThisTurn,
				int nbDays) {
			long nb = 0;
			for(Pop pop : prv.getPops()){
				nb += pop.getNbMensEmployed().get(prv.getIndustry(ElevageIndustry.this));
			}
			NeedWish wishes = basicIndustryNeeds.moneyNeeded(prv, nb, currentStock, totalMoneyThisTurn, nbDays);
			long nbSheep = (currentStock.getLong(Good.GoodFactory.get("meat"))/100);
			
			System.out.println("Need more sheeps? "+nbSheep+" ? "+nb*10);
			if(nbSheep<nb*10){
				//try to buy some meat
				long nbNeedToBuy = nb*10 - nbSheep;
				nbNeedToBuy *= 100;
				long price = prv.getStock().get(Good.get("meat")).getPriceBuyFromMarket(prv, nbDays);
				wishes.vitalNeed += nbNeedToBuy * price*5;
				wishes.normalNeed += nbNeedToBuy * price*5;
			}
			
			return wishes;
		}

		@Override
		public long spendMoney(Province prv, long nbPrevProd, Object2LongMap<Good> currentStock, NeedWish maxMoneyToSpend,
				int nbDays) {
			long spent = 0;
			long nb = 0;
			for(Pop pop : prv.getPops()){
				nb += pop.getNbMensEmployed().get(prv.getIndustry(ElevageIndustry.this));
			}
			//first, buy sheeps
			long nbSheep = (currentStock.getLong(Good.GoodFactory.get("meat"))/100);
			if(nbSheep<nb*10){
				System.out.println("not enough sheeps: "+nbSheep+" < "+nb*10+", to buy, i have "+maxMoneyToSpend.vitalNeed+" & "+maxMoneyToSpend.normalNeed);
				Good meat = Good.get("meat");
				//try to buy some meat
				long nbNeedToBuy = nb*10 - nbSheep;
				nbNeedToBuy *= 100;
				long price = prv.getStock().get(meat).getPriceBuyFromMarket(prv, nbDays);
				
				long vitalSpend = Math.min(maxMoneyToSpend.vitalNeed, nbNeedToBuy * price);
				long quantityBuy = vitalSpend / (price*100);
				nbSheep += quantityBuy;
				quantityBuy  *= 100;

				//buy
				prv.addMoney(quantityBuy * price);
				prv.getStock().get(meat).addNbConsumePerDay(quantityBuy / (float)nbDays);
				currentStock.put(meat,nbSheep*100);
				prv.getStock().get(meat).addStock( -quantityBuy);
				spent += quantityBuy * price;
				System.out.println("buy "+quantityBuy+" vital sheeps");
				
				long normalSpend = Math.min(maxMoneyToSpend.normalNeed, Math.max(0, nbNeedToBuy * price - vitalSpend));
				quantityBuy = normalSpend / (price*100);
				nbSheep += quantityBuy;
				quantityBuy  *= 100;

				//buy
				prv.addMoney(quantityBuy * price);
				prv.getStock().get(meat).addNbConsumePerDay(quantityBuy / (float)nbDays);
				currentStock.put(meat, nbSheep*100);
				prv.getStock().get(meat).addStock( -quantityBuy);
				spent += quantityBuy * price;
				System.out.println("buy "+quantityBuy+" normal sheeps");
				
			}
			
			//then, buy some tools
			spent += basicIndustryNeeds.spendMoney(prv, nb, currentStock, maxMoneyToSpend, nbDays);
			
			
			System.out.println("now elevage has "+ currentStock);
			return spent;
		}
		
	}
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();

		
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		long nbFields = (int) ( (prv.pourcentPrairie * prv.surface * 100 )); // 1 hectare per sheep
		long nbSheep = (indus.getStock().getLong(Good.GoodFactory.get("meat"))/100); //100kg per sheep
		System.out.println("");
		System.out.println("i have "+nbSheep+" sheeps");
		//temp booststrap TODO replace by needs
		if(nbSheep == 0){
			indus.getStock().put(Good.GoodFactory.get("meat"), 100);
			nbSheep = 1;
			System.out.println(" no sheep, now 1");
		}
		
		
		//multiplicate
		// * 1.5 every year => +0.13% per day
		long newSheeps = (int) (nbSheep * durationInDay * 0.00139);
		if(newSheeps <= 0){
			if( GlobalRandom.aleat.getInt(1000, (int)(prv.pourcentPrairie*1000000)) < (int) (nbSheep * durationInDay * 1.39) ){
				newSheeps = 1;
			}
		}
		nbSheep += newSheeps;
		System.out.println("i have now "+nbSheep+" sheeps with birth");
		
		//nb sheep to sell: because nbSheep can't go higher than:
		// - X sheep per people * efficacity
		// - X sheep per prairie
		
		
		int nbSheepToSell = 0;
		int nbMens = 0;
		for(Pop pop : pops){
			nbMens += pop.getNbMensEmployed().getLong(indus);
		}
		if(nbSheep > 30 * nbMens){ //30 sheep per people: can sustain an other men
			nbSheepToSell = 30 * nbMens;
			nbSheep -= nbSheepToSell;
		}
		if(nbSheep > nbFields){ //30 sheep per people: can sustain an other men
			nbSheepToSell += (nbSheep - nbFields) * nbMens / (10+nbMens);
			nbSheep -= nbSheepToSell;
		}
		System.out.println("i have to sell "+nbSheep+" nbSheep");

		if(nbSheep > nbFields){ //fall into a canyon
			System.out.println("sheeps lost: "+(nbSheep - nbFields));
			nbSheep -= nbSheep - nbFields;
		}
		
		//set new livestock
		System.out.println(" i have previously "+indus.getStock().getLong(Good.GoodFactory.get("meat"))+" sheep");
		indus.getStock().put(Good.GoodFactory.get("meat"), nbSheep*100);
		System.out.println(" i have now "+indus.getStock().getLong(Good.GoodFactory.get("meat"))+" sheep");
		
		
		//TODO: sell more sheep if the price is high and famine is occurring.
		
		//sell sheep to province market
		long intproduction = myElevageNeeds.basicIndustryNeeds.useGoodsAndTools(indus, (long)nbSheepToSell * 100, durationInDay);
		super.sellProductToMarket(prv, intproduction, durationInDay);

		return intproduction;
	}

}
