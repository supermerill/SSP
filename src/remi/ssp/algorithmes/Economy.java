package remi.ssp.algorithmes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import remi.ssp.Pop;
import remi.ssp.Province;
import remi.ssp.economy.Good;
import remi.ssp.economy.Needs;
import remi.ssp.economy.Needs.NeedWish;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;

public class Economy {
	//cache values
	Map<Good, GoodsProduced> oldStock = new HashMap<>();
	int bfrThisTurn = 0;
	
	//for(Province prv: allPrvs){
	public static class GoodsProduced{
//		Good good;
//		Province prv;
		int exportPrice;
		int nbExport;
		int importPrice;
		int nbImport;
		int prodPrice;
		int nbProd;
		int oldStock;
		int oldPrice;
		
		
	}

	public int getPriceSellToMarket(Province prv, Good item, int quantity, int durationInDay){
		float coeff = prv.getMoney() / (durationInDay*prv.getMoneyChangePerDay());
		coeff = ( 0.2f / (1+ coeff*coeff) );
		return (int)( prv.getStock().get(item).price * (1-coeff) );
	}
	
	public int getPriceBuyFromMarket(Province prv, Good item, int quantity, int durationInDay){
		float coeff = prv.getMoney() / (durationInDay*prv.getMoneyChangePerDay());
		coeff = ( 0.2f / (1+ coeff*coeff) );
		return (int)( prv.getStock().get(item).price * (1+coeff) );
	}
	

	public void sellToMarket(Province prv, Good item, int quantity, int price, int durationInDay){
		bfrThisTurn += price * quantity;
	}
	
	public void doTurn(Province prv, int durationInDay){
		oldStock.clear();
		bfrThisTurn = 0;
		//save previous stock to redo price
		for(Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()){
			GoodsProduced gp = new GoodsProduced();
			gp.oldStock = goodStock.getValue().stock;
			gp.oldPrice = goodStock.getValue().price;
//			gp.good = goodStock.getKey();
//			gp.prv = prv;
			oldStock.put(goodStock.getKey(), gp);
		}
		
		produce(prv, durationInDay);
		doImportExport(prv, durationInDay);
		consume(prv, durationInDay);
		setPrices(prv, durationInDay);
		moveWorkers(prv, durationInDay);
		popSellThingsIfNeedMoney(prv, durationInDay);

		//stock gaspi
		for(Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()){
			goodStock.getValue().stock = (goodStock.getKey().storageLoss(goodStock.getValue().stock, durationInDay));
		}
		
		prv.setMoneyChangePerDay(1 + bfrThisTurn/durationInDay);
	}

	private void doImportExport(Province prv, int durationInDay) {
		//create the list of imported & exported goods, sorted by benefice.
		List<TradeTry> listOfTrades = new ArrayList<>();
		
		//check our transport capacity (from number of merchant, efficacity, and merchant tools
		
		//importedGoods
		//for each nearby province
		int totalCapacitybestTR = 0;
		int bestProfitability = 0;
//		for(prv.)
			//look to each goods i can buy
			//look at how much it cost here (double if not present ever)
			//if profitable, add ; profitablity = gain in money per merchant
		//exported goods
		
		//sort
		

		// trade route numbers: from profitability(route,good), capacity(route) and number of merchant.
		
		//algo1: 
		// 70% don't look at profitability (or sightly): T(r,g) = (min(1, 10*profitablity/bestProfitability)) * N(m) / N(r)
		// 30% want the most profitable route (fill, with the leftover if anny)
		//for each trade route
			// do trade : buy & sell (benef or deficit are for the pop money)
			// use tools
		
		
		//if leftover, it's a loss for them.
		
		
		//then use merchant needs to buy merchant tools
		
	}
	protected static class TradeTry{
		int marge;
		Province from;
		Province to;
		Good good;
	}

	private void produce(Province prv, int durationInDay) {
		//produce
		for( ProvinceIndustry indus : prv.getIndustries()){
			//compute marge
//			ProvinceGoods goodPrice= prv.getStock().get(indus.getIndustry().getGood());
			int goodPrice = getPriceSellToMarket(prv, indus.getIndustry().getGood(), 1, durationInDay);
			int marge = goodPrice - indus.getRawGoodsCost();
			if(marge <= 0){
				//chomage technique
			}else{
				//produce & pay people
				
				//produce
				int quantity = indus.getIndustry().produce(indus, prv.getPops(), durationInDay); // here it should use our industry money
				indus.setPreviousProduction(quantity);
				
				//sell to market
				
				//give money to workers
				//compute how many we need to keep going (increase prod) 
				int moneyToKeep = indus.getRawGoodsCost() * quantity;
				float ratio = (indus.getMoney()/moneyToKeep);
				ratio = 1 - ( 1 / (1+ ratio*ratio) ); //asymptote en 1, if indus.getMoney() == moneyToKeep then ratio = 0.5
				int moneyToGive = (int) (marge * quantity * ratio);
				//give money to investors
				int moneyBonus = indus.getMoney() - moneyToKeep * 2;
				if( moneyBonus > 0){
					ratio = (moneyBonus / moneyToKeep);
					ratio = 0.2f - ( 0.2f / (1+ ratio*ratio) ); //asymptote en 0.2, if moneyBonus == moneyToKeep then ratio = 0.1
					moneyToGive += (int) (moneyBonus * ratio);
				}
				//get employes
				int nbEmployes = 0;
				for(Pop pop : prv.getPops()){
					nbEmployes += pop.getNbMensEmployed().get(indus);
				}
				//distribute money
				indus.setPreviousSalary(moneyToGive/nbEmployes);
				for(Pop pop : prv.getPops()){
					int employesHere = pop.getNbMensEmployed().get(indus);
					int money = 1 + (int)(moneyToGive * employesHere / (float)nbEmployes);
					indus.addMoney(-money);
					pop.addMoney(money);
				}
				if(indus.getMoney() < 0) System.err.println("Error, in industral money salary");
				
				//we will use our money for our need in the consume phase
			}
		}
	}

//	private void doImport(Province prv, int durationInDay) {
//		//look at each nearby province
//		//TODO: search lower price before
//		for(Province near : prv.proche){
//			//TODO: check country, legal issues, customs (intra, inter), etc
//			for(Entry<Good, ProvinceGoods> goodsStock : prv.getStock().entrySet()){
//				//can we import yours?
//				if(near.getStock().get(goodsStock.getKey()).stock > goodsStock.getValue().stock){
//					int opti = (near.getStock().get(goodsStock.getKey()).stock + goodsStock.getValue().stock) /2;
//					//TODO merchant: efficiency, customs, revenue, repartition inside pops
//					//TODO: road network efficiency
//					int nbMerchant = 0;
//					int nbPop = 0;
//					for(Pop pop: prv.getPops()){ nbMerchant += pop.getNbMensCommerce(); nbPop +=pop.getNbMens();}
//					//efficiency: TODO (like how many a men can transport? with goods? erf..
//					float efficiency = nbMerchant/(float)nbPop;
//					int goodsMovement = (opti - goodsStock.getValue().stock);
//					goodsMovement *= efficiency;
//					//money: for now, they do this gratis (and it's almost the same anyway because it's import, they consume it)
////					for(Pop pop: prv.getPops()){
////						int popGoodsMouvement = (goodsMovement * pop.getNbMensCommerce()) / nbMerchant;
////						//pop.addMoney(popGoodsMouvement*(Math.abs(goodsStock.getValue().price - );
////					}
//					
//					//compute new price
//					
//					//move goods
//					goodsStock.getValue().stock += goodsMovement;
//					near.getStock().get(goodsStock.getKey()).stock -= goodsMovement;
//					
//				}
//			}
//		}
//		//TODO: maritime routes
//	}

	private void consume(Province prv, int durationInDay) {
		
		
		//particuliers
		for(Pop pop : prv.getPops()){
			//on scinde de 3
			float repartitionRiche = pop.getRepartitionRevenu(5);
			float repartitionMoyen = pop.getRepartitionRevenu(30) - repartitionRiche;
			float repartitionPoor = 1.0f - (repartitionMoyen + repartitionRiche);
			int nbRiche = pop.getNbMens() / 20;
			int nbMoyen = pop.getNbMens() /  4;
			int nbPoor = pop.getNbMens() - (nbRiche + nbMoyen);
			int moneyRiche = (int)(pop.getMoney() * repartitionRiche);
			int moneyMoyen = (int)(pop.getMoney() * repartitionMoyen);
			int moneyPoor = (int)(pop.getMoney() * repartitionPoor);
			//test edge case when low pop
			if(nbRiche == 0) moneyMoyen += moneyRiche;
			if(nbMoyen == 0) moneyPoor += moneyMoyen;
			if(nbPoor == 0){
				if(pop.getNbMens()>0) nbPoor = pop.getNbMens();
				else continue;
			}
			//on fait la répartition de besoin pour chaque catégorie

			//les riches achetent en premier
			popBuy(pop, nbRiche, moneyRiche, durationInDay);
			popBuy(pop, nbMoyen, moneyMoyen, durationInDay);
			popBuy(pop, nbPoor, moneyPoor, durationInDay);
			
		}
		

		//industry
		for(ProvinceIndustry indus : prv.getIndustries()){
			Needs need = indus.getIndustry().getMyNeeds();
			NeedWish wish = need.moneyNeeded(indus.getPreviousProduction(), prv.getStock().values(), indus.getStock(), indus.getMoney(), durationInDay);
			int moneyLeft = indus.getMoney();
			if(wish.vitalNeed > moneyLeft){
				wish.vitalNeed = moneyLeft;
				wish.normalNeed = 0;
				wish.luxuryNeed = 0;
			}
			moneyLeft -= wish.vitalNeed;
			if(wish.normalNeed > moneyLeft){
				wish.normalNeed = moneyLeft;
				wish.luxuryNeed = 0;
			}
			moneyLeft -= wish.normalNeed;
			if(wish.luxuryNeed > moneyLeft){
				wish.luxuryNeed = moneyLeft;
			}
			int moneySpent = need.spendMoney(indus.getPreviousProduction(), prv.getStock().values(), indus.getStock(), wish, durationInDay);
			indus.addMoney(-moneySpent);
			if(indus.getMoney() < 0) System.err.println("Error, in industral money buy");
		}
	}
	
	private void popBuy(Pop pop, int nb, int money, int durationInDay){
		Map<Needs, NeedWish> moneyNeeded = new HashMap<>();
		NeedWish wantedMoney = new NeedWish(0, 0, 0);
		for(Needs need : pop.getPopNeeds()){
			NeedWish moneyNeeds = need.moneyNeeded(nb, pop.getProvince().getStock().values(), pop.getStock(), money, durationInDay);
			wantedMoney.add(moneyNeeds);
			moneyNeeded.put(need, moneyNeeds);
		}
		
		if(money < wantedMoney.vitalNeed){
			//shortage! easy decision
			float coeff = money / (float) wantedMoney.vitalNeed;
			for(Needs need : pop.getPopNeeds()){
				NeedWish wish = moneyNeeded.get(need);
				wish.luxuryNeed = 0;
				wish.normalNeed = 0;
				wish.vitalNeed *= coeff;
			}
		}else{
			final int useableMoney = money - wantedMoney.vitalNeed;

			//look if we can fulfill the normal need
			int luxuryMoney = useableMoney;
			if(useableMoney < wantedMoney.normalNeed){
				// keep a little bit for luxury
				float coeff = useableMoney * 0.9f / (float)wantedMoney.normalNeed;
				for(Needs need : pop.getPopNeeds()){
					NeedWish wish = moneyNeeded.get(need);
					wish.normalNeed = (int) (wish.normalNeed * coeff);
					luxuryMoney -= wish.normalNeed;
				}
			}else{
				luxuryMoney -= wantedMoney.normalNeed;
			}
			if(luxuryMoney < 0) System.err.println("Error, in luxury money assignment");
			
			//try to fulfill the luxury need
			int leftoverMoney = luxuryMoney;
			if(luxuryMoney > 0){
				float coeff = luxuryMoney / (float)wantedMoney.luxuryNeed;
				for(Needs need : pop.getPopNeeds()){
					NeedWish wish = moneyNeeded.get(need);
					wish.luxuryNeed = (int) (wish.luxuryNeed * coeff);
					leftoverMoney -= wish.luxuryNeed;
				}
				
				//add the leftover to normal and luxury need
				float coeffLeftoverNormal = 0.2f * leftoverMoney / (float)wantedMoney.normalNeed;
				float coeffLeftoverLuxury = 0.8f * leftoverMoney / (float)wantedMoney.luxuryNeed;
				int moneyNotSpent = leftoverMoney;
				for(Needs need : pop.getPopNeeds()){
					NeedWish wish = moneyNeeded.get(need);
					int temp = (int) (wish.normalNeed * coeffLeftoverNormal);
					wish.normalNeed += temp;
					moneyNotSpent -= temp;
					temp = (int) (wish.normalNeed * coeffLeftoverLuxury);
					wish.luxuryNeed += temp;
					moneyNotSpent -= temp;
				}
				if(moneyNotSpent < 0) System.err.println("Error, in leftover money assignment");
				//maybe some centimes are left
			}
		}
		
		for(Needs need : pop.getPopNeeds()){
			NeedWish wish = moneyNeeded.get(need);
			int moneySpent = need.spendMoney(nb, pop.getProvince().getStock().values(), pop.getStock(), wish, durationInDay);
			pop.addMoney(- moneySpent);
		}
		if(pop.getMoney() < 0) System.err.println("Error, in pop money buy");
	}

//	private void doExport(Province prv, int durationInDay) {
//		// TODO Auto-generated method stub
//	}

	private void setPrices(Province prv, int durationInDay) {
		for(Entry<Good, GoodsProduced> entry : oldStock.entrySet()){ 
			GoodsProduced gp = entry.getValue();
			long newPrice = gp.exportPrice*(long)gp.nbExport;
			newPrice += gp.importPrice*(long)gp.nbImport;
			newPrice += gp.prodPrice*(long)gp.nbProd;
			newPrice += gp.oldPrice*(long)gp.oldStock;
			newPrice /= gp.nbExport+gp.nbImport+gp.nbProd+gp.oldStock;
			prv.getStock().get(entry.getKey()).price = (int)newPrice;

			//test for shortage/overproduction
			int newStock = prv.getStock().get(entry.getKey()).stock;
			//TODO: move quicker as it move away of the threshold
			if(newStock <= prv.getNbHabitants() * entry.getKey().getNeeds().getMinimumStockPerPop()){
				//test for shortage : increase price
				newPrice *= 1.01;
				newPrice += 1; // to increase even very small values.
			}else if(newStock >= prv.getNbHabitants() * entry.getKey().getNeeds().getOptimalStockPerPop()){ 
				//test for over abundance : decrease price
				newPrice *= 0.99;
				newPrice -= 1; // to decrease even very small values.
			}
		}
	}

	private void moveWorkers(Province prv, int durationInDay) {
		for(Pop pop : prv.getPops()){
			//compute mean revenu
			long mean = 0;
			for(Entry<ProvinceIndustry, Integer> indus : pop.getNbMensEmployed().entrySet()){
				mean += indus.getValue() * indus.getKey().getPreviousSalary(); //TODO multiply by an "efficiency" from education or other things.
			}
			mean /= pop.getNbMens();
			//for each indus
			for(Entry<ProvinceIndustry, Integer> indus : pop.getNbMensEmployed().entrySet()){
				//if revenu < mean
				int revenu = indus.getValue() * indus.getKey().getPreviousSalary(); //TODO multiply by an "efficiency" from education or other things.
				if(revenu < mean){
					//for each indus
					for(Entry<ProvinceIndustry, Integer> indusCible : pop.getNbMensEmployed().entrySet()){
						int nbMove = (int)( 1 + indus.getValue() * 0.01f * (mean/(float)revenu) * (indusCible.getValue()/(float)pop.getNbMens()));
						// move 1 + 0.01 * mean/revenu * nbMenIndusCible/nbMens
						indus.setValue(indus.getValue() - nbMove);
						indusCible.setValue(indusCible.getValue() + nbMove);
						if(indus.getValue() < 0){
							indusCible.setValue(indusCible.getValue() + indus.getValue());
							indus.setValue(0);
							break;
						}
					}
				}
			}
		}
		
	}

}
