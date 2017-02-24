package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.logln;

import java.util.Collection;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.IndustryNeed;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create food from animals (and grow the cheptel)
//needs:  TODO: buy sheep from market if possible
public class ElevageIndustry extends Industry {

	static protected ElevageIndustry ptr;
	public static void load(){ ptr = new ElevageIndustry(); Industry.put("elevage", ptr); }
	public static ElevageIndustry get(){ return ptr; }
	
	private ElevageIndustry(){
		createThis = Good.get("meat");
		myNeedsFactory = pi -> new ElevageNeeds(pi);
	}
	
	private ElevageNeeds getNeed(ProvinceIndustry indus){
		return (ElevageNeeds)indus.getNeed();
	}
	
	public class ElevageNeeds extends IndustryNeed{
		
		BasicIndustryNeeds basicIndustryNeeds;
		
		ElevageNeeds(ProvinceIndustry pi){
			super(pi);
			basicIndustryNeeds = new BasicIndustryNeeds(pi)
					.addToolGood(Good.get("wood_goods"), 1);
		}

		@Override
		public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
			Object2LongMap<Good> currentStock = myIndus.getStock();
			long nb = 0;
			for(Pop pop : prv.getPops()){
				nb += pop.getNbMensEmployed(prv.getIndustry(ElevageIndustry.this));
			}
			NeedWish wishes = basicIndustryNeeds.moneyNeeded(prv, totalMoneyThisTurn, nbDays);
			long nbSheep = (currentStock.getLong(Good.GoodFactory.get("meat"))/100);
			
			logln(", \"Need more sheeps?\":\" "+nbSheep+" ? "+nb*10+"\"");
			if(nbSheep<nb*10){
				//try to buy some meat
				long nbNeedToBuy = nb*10 - nbSheep;
				nbNeedToBuy *= 100;
				long price = prv.getStock().get(Good.get("meat")).getPriceBuyFromMarket(nbDays);
				wishes.vitalNeed += nbNeedToBuy * price*5;
				wishes.normalNeed += nbNeedToBuy * price*5;
			}
			
			return wishes;
		}

		@Override
		public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
			Object2LongMap<Good> currentStock = myIndus.getStock();
			logln(", \"elevageSpendMoney\":{\"before elevage has\":\""+ currentStock+"\"");
//			long nbPrevProd = prv.getIndustry(ptr).getPreviousProduction();
			long spent = 0;
			long nb = 0;
			for(Pop pop : prv.getPops()){
				nb += pop.getNbMensEmployed(prv.getIndustry(ElevageIndustry.this));
			}
			//first, buy sheeps
			long nbSheep = (currentStock.getLong(Good.GoodFactory.get("meat"))/100);
			if(nbSheep<nb*10){
				logln(", \"not enough sheeps\":"+nbSheep+", \"<\":"+nb*10+", \"vitalmoney\":"+maxMoneyToSpend.vitalNeed+", \"normalmoney\":"+maxMoneyToSpend.normalNeed);
				Good meat = Good.get("meat");
				//try to buy some meat
				long nbNeedToBuy = nb*10 - nbSheep;
				nbNeedToBuy *= 100;
				long price = prv.getStock().get(meat).getPriceBuyFromMarket(nbDays);
				
				long vitalSpend = Math.min(maxMoneyToSpend.vitalNeed, nbNeedToBuy * price);
				long quantityBuy = Math.min(vitalSpend / (price*100), prv.getStock().get(meat).getStock()/100);
				nbSheep += quantityBuy;
				quantityBuy  *= 100;
				maxMoneyToSpend.vitalNeed -= vitalSpend;

				//buy
				prv.addMoney(quantityBuy * price);
				myIndus.addMoney(-quantityBuy * price);
				currentStock.put(meat,nbSheep*100);
//				prv.getStock().get(meat).addNbConsumePerDay(quantityBuy / (float)nbDays);
				prv.getStock().get(meat).addStock( -quantityBuy);
				spent += quantityBuy * price;
				logln(", \"buyvital_qt\":"+quantityBuy+",\"vital_price\":"+price+", \"vital_spent\":"+(quantityBuy * price)+"");
				
				
				//second round
				long normalSpend = Math.min(maxMoneyToSpend.normalNeed, Math.max(0, nbNeedToBuy * price - vitalSpend));
				quantityBuy = Math.min(normalSpend / (price*100), prv.getStock().get(meat).getStock()/100);
				nbSheep += quantityBuy;
				quantityBuy  *= 100;

				//buy
//				prv.addMoney(quantityBuy * price);
//				prv.getStock().get(meat).addNbConsumePerDay(quantityBuy / (float)nbDays);
//				currentStock.put(meat, nbSheep*100);
//				prv.getStock().get(meat).addStock( -quantityBuy);
//				spent += quantityBuy * price;
//				maxMoneyToSpend.normalNeed -= normalSpend;
				super.storeProductFromMarket(meat, quantityBuy, nbDays);
				logln(", \"buy_normal_nb\":"+quantityBuy+"");
				
			}
			
			//then, buy some tools
			spent += basicIndustryNeeds.spendMoney(prv, maxMoneyToSpend, nbDays);

			
			logln(", \"now elevage has nbKiloSheep\":"+ currentStock.getLong(createThis)+"}");
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
		long nbSheep = (indus.getStock().getLong(createThis)/100); //100kg per sheep
		logln(", \"produceSheep\":{\"nbSheepsInit\":"+nbSheep);
		//temp booststrap TODO replace by needs
		if(nbSheep == 0){
			indus.getStock().put(createThis, 100);
			nbSheep = 1;
			logln(", \"no sheepbefore, but now\":1");
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
		logln(", \"nbSheeps_after_birth\":"+nbSheep);
		
		//nb sheep to sell: because nbSheep can't go higher than:
		// - X sheep per people * efficacity
		// - X sheep per prairie
		
		
		int nbSheepToSell = 0;
		int nbMens = 0;
		for(Pop pop : pops){
			nbMens += pop.getNbMensEmployed(indus);
		}
		if(nbSheep > 30 * nbMens){ //30 sheep per people: can sustain an other men
			nbSheepToSell = 30 * nbMens;
			nbSheep -= nbSheepToSell;
		}
		if(nbSheep > nbFields){ //30 sheep per people: can sustain an other men
			nbSheepToSell += (nbSheep - nbFields) * nbMens / (10+nbMens);
			nbSheep -= nbSheepToSell;
		}
		logln(", \"i have to sell\":"+nbSheep);

		if(nbSheep > nbFields){ //fall into a canyon
			logln(", \"sheeps lost\":"+(nbSheep - nbFields));
			nbSheep -= nbSheep - nbFields;
		}
		
		//set new livestock
		logln(", \"i have previously_kgSheeps\":"+indus.getStock().getLong(createThis));
		indus.getStock().put(createThis, nbSheep*100);
		logln(", \"i have now kgSheeps\":"+indus.getStock().getLong(createThis)+"}");
		
		
		//TODO: sell more sheep if the price is high and famine is occurring.
		
		//sell sheep to province market
		long intproduction = getNeed(indus).basicIndustryNeeds.useGoodsAndTools(indus, (long)nbSheepToSell * 100, durationInDay);

		return intproduction;
	}

}
