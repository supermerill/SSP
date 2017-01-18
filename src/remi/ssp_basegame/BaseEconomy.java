package remi.ssp_basegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.algorithmes.Economy;
import remi.ssp.economy.Good;
import remi.ssp.economy.Job;
import remi.ssp.economy.Needs;
import remi.ssp.economy.Needs.NeedWish;
import remi.ssp.politic.Carte;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.economy.ProvinceCommerce;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.economy.TradeRoute;

public class BaseEconomy extends Economy {
	
	//cache values
	Map<Province, Map<Good, GoodsProduced>> oldStock = new HashMap<>();
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


	

	public void sellToMarket(Province prv, Good item, int quantity, int price, int durationInDay){
		bfrThisTurn += price * quantity;
	}
	
	/**
	 * TODO: rework: instead of a province-loop, it should be a:
	 *  - produce goods in all province in the world (no order)
	 *  - move goods via merchants for all provinces (ordered by the richest/pop to the poorest)
	 *  - consume goods in each province (no order)
	 *  - set prices in each province (no order)
	 */
	public void doTurn(Carte map, int durationInDay){
		final Object2IntMap<Province> prv2Wealth = new Object2IntOpenHashMap<Province>();
		final List<Province> allPrvs = new ArrayList<>();
		for( List<Province> prvs: map.provinces){
			//init map to compute prices
			for( Province prv: prvs){
				if(!oldStock.containsKey(prv))oldStock.put(prv, new HashMap<>());
				oldStock.get(prv).clear();
				bfrThisTurn = 0;
				//save previous stock to redo price
				for(Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()){
					GoodsProduced gp = new GoodsProduced();
					gp.oldStock = goodStock.getValue().stock;
					gp.oldPrice = goodStock.getValue().price;
	//				gp.good = goodStock.getKey();
	//				gp.prv = prv;
					oldStock.get(prv).put(goodStock.getKey(), gp);
				}
				
				//init map to compute wealth order
				allPrvs.add(prv);
				int richesse = 0;
				for(Pop pop: prv.getPops()){
					//TODO: this is not the wealth but the current bank account... change it (when you have time)
					richesse = pop.getMoney();
				}
				prv2Wealth.put(prv, richesse/prv.getNbMens());
				
				//procude goods
				produce(prv, durationInDay);
			}
		}
		
		//sort list to have the first one with the biggest value.
		allPrvs.sort( (prv1,prv2) -> -Integer.compare(prv2Wealth.getInt(prv1),prv2Wealth.getInt(prv2)) );
		for(Province prv: allPrvs){
			doNavalImportExport(prv, durationInDay);
			doLandImportExport(prv, durationInDay);
		}
		for(Province prv: allPrvs){
			consume(prv, durationInDay);
			//popSellThingsIfNeedMoney(prv, durationInDay);
			setPrices(prv, durationInDay);
			moveWorkers(prv, durationInDay); //ie: changejob
			
			
			//stock gaspi
			for(Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()){
				goodStock.getValue().stock = (goodStock.getKey().storageLoss(goodStock.getValue().stock, durationInDay));
			}
			//useful accumulators for computing prices
			prv.setMoneyChangePerDay(1 + bfrThisTurn/durationInDay);
		}
		//TODO
//		for(Province prv: allPrvs){
//			emigration(prv, durationInDay);
//		}
	}
	@Deprecated
	private void doTurn(Province prv, int durationInDay){
		oldStock.get(prv).clear();
		bfrThisTurn = 0;
		//save previous stock to redo price
		for(Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()){
			GoodsProduced gp = new GoodsProduced();
			gp.oldStock = goodStock.getValue().stock;
			gp.oldPrice = goodStock.getValue().price;
//			gp.good = goodStock.getKey();
//			gp.prv = prv;
			oldStock.get(prv).put(goodStock.getKey(), gp);
		}
		
		produce(prv, durationInDay);
		doNavalImportExport(prv, durationInDay);
		doLandImportExport(prv, durationInDay);
		consume(prv, durationInDay);
		setPrices(prv, durationInDay);
		moveWorkers(prv, durationInDay);
		//popSellThingsIfNeedMoney(prv, durationInDay);

		//stock gaspi
		for(Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()){
			goodStock.getValue().stock = (goodStock.getKey().storageLoss(goodStock.getValue().stock, durationInDay));
		}
		
		prv.setMoneyChangePerDay(1 + bfrThisTurn/durationInDay);
	}

	private void doNavalImportExport(Province prv, int durationInDay) {
		//check if this province can make naval commerce
		//TODO buy a boat with pop money if none are present, same with commerce guys.
		//TODO like land, but with boats instead of cart (1cart=1men but 1boat=many mens)
	}
	private void doLandImportExport(Province prv, int durationInDay) {
		//create the list of imported & exported goods, sorted by benefice.
		//List<TradeTry> listOfTrades = new ArrayList<>();

		final Object2IntMap<TradeRoute> trAlreadyTaken = new Object2IntOpenHashMap<>();
		for(TradeRoute tr : prv.getTradeRoute()){
			trAlreadyTaken.put(tr, 0);
		}
		
		//for each pop TODO: richer pop first
		for(Pop pop: prv.getPops()){
			final List<TradeRoute> listOfTrades = new ArrayList<>();
			
			//check our transport capacity (from number of merchant, efficiency, and merchant tools
			final ProvinceCommerce data = pop.getLandCommerce();
			final int transportCapa = data.computeCapacity(pop.getNbMensEmployed().getInt(pop.getLandCommerce()));
			final float capaPerMen = transportCapa / (float)pop.getNbMensEmployed().getInt(pop.getLandCommerce());
			
			//init : give all pop money to merchant (loan)
			data.setPreviousSalary(0);
			final int loan = pop.getMoney();
			data.setMoney(data.getMoney() + loan);
			
			//importedGoods
			final Object2DoubleMap<TradeRoute> maxProfitability = new Object2DoubleOpenHashMap<>();
			final Map<TradeRoute, List<TradeTry>> maxProfitabilityTR = new HashMap<>();
			//for each nearby province
			int totalCapacityTR = 0;
			double bestProfitability = 0;
			for(TradeRoute tr : prv.getTradeRoute()){
				if(tr.isBoatTR()) continue;
				double bestProfitabilityTR = 0;
				Province otherOne = tr.getOtherEnd(prv);
				maxProfitabilityTR.put(tr, new ArrayList<TradeTry>());
				//look to each goods i can buy
				for(Entry<Good, ProvinceGoods> otherGoods : otherOne.getStock().entrySet()){
					Good good = otherGoods.getKey();
					//look at how much it cost here (double if not present ever)
					int hisBuyCost = otherGoods.getValue().getPriceBuyFromMarket(otherOne, durationInDay);
					int hisSellCost = otherGoods.getValue().getPriceSellToMarket(otherOne,durationInDay);
					int myBuyCost = hisSellCost;
					int mySellCost = hisBuyCost;
					ProvinceGoods myGoods = prv.getStock().get(good);
					if(myGoods == null || myGoods.stock == 0){
						if(myGoods == null){
							prv.getStock().put(good, new ProvinceGoods());
						}
						myBuyCost *= 2;
						//can't move naval stuff by land
						// & if prv is not coastal i don't want it anyway.
						if(good.isNaval()){
							myBuyCost = 0;
							mySellCost = Integer.MAX_VALUE;
						}
					}else{
						myBuyCost = prv.getStock().get(good).getPriceBuyFromMarket(prv, durationInDay);
						mySellCost = prv.getStock().get(good).getPriceSellToMarket(prv, durationInDay);
					}
					//if profitable, add ; profitablity = gain in money per merchant
					//note: we choose the kilogram (liter) as the base unit for transortation
					//so it's "base import per kilo"
					double diffImport = (1000 * (myBuyCost - hisSellCost) / (double)good.getWeight());
					double diffExport = (1000 * (hisBuyCost - mySellCost) / (double)good.getWeight());
					if(diffImport > diffExport){ //IMPORT
						if(diffImport <= 0) continue; //no way
						
						if(bestProfitability<diffImport) bestProfitability = diffImport;
						if(bestProfitabilityTR<diffImport)bestProfitabilityTR = diffImport;
						maxProfitabilityTR.get(tr).add(new TradeTry(good, otherOne, prv, myBuyCost, hisSellCost, diffImport));
					}else{						// EXPORT
						if(diffExport <= 0) continue; //no way
						
						if(bestProfitability<diffExport) bestProfitability = diffExport;
						if(bestProfitabilityTR<diffExport)bestProfitabilityTR = diffExport;
						maxProfitabilityTR.get(tr).add(new TradeTry(good, prv, otherOne, hisBuyCost, mySellCost, diffExport));
					}
					
				}
				maxProfitability.put(tr, bestProfitabilityTR);
				totalCapacityTR += tr.getMaxMerchant();
				listOfTrades.add(tr);
				maxProfitabilityTR.get(tr).sort(new Comparator<TradeTry>(){
					@Override public int compare(TradeTry arg0, TradeTry arg1) {
						return Integer.compare(arg1.buyerPrice - arg1.sellerPrice, arg0.buyerPrice - arg0.sellerPrice);
					}});
			}
			
			//sort
			listOfTrades.sort(new Comparator<TradeRoute>(){
				@Override public int compare(TradeRoute arg0, TradeRoute arg1) {
					return Double.compare(maxProfitability.getDouble(arg1), maxProfitability.getDouble(arg0));
				}});

			// trade route numbers: from profitability(route,good), capacity(route) and number of merchant.
			
			//algo1: 
			// 70% don't look at profitability (or sightly): T(r,g) = (min(1, 10*profitablity/bestProfitability)) * N(m) / N(r)
			// 30% want the most profitable route (fill, with the leftover if anny)
			//for each trade route
				// do trade : buy & sell (benef or deficit are for the pop money)
				// use tools
			
			//careless merchants
			final int nbMerchantDontCare = (int) (pop.getNbMens() * 0.7f);
			int merchantToFill = pop.getNbMens();
			for(TradeRoute tr : listOfTrades){
				Province otherOne = tr.getOtherEnd(prv);
				int maxMerchantForThisTR = tr.getMaxMerchant() - trAlreadyTaken.getInt(tr);
				if(maxMerchantForThisTR > 0){
					//get the number of merchant we have here.
					final int trMerchantUsed = (int) Math.min(maxMerchantForThisTR, nbMerchantDontCare * tr.getMaxMerchant() /(float)totalCapacityTR );
					merchantToFill -= trMerchantUsed;
					int availableMerchantSlot = trMerchantUsed;
					trAlreadyTaken.put(tr, trAlreadyTaken.getInt(tr) + trMerchantUsed);
					//get the best trade here
					List<TradeTry> bestGoods = maxProfitabilityTR.get(tr);
					int maxCapa = 0;
					for(TradeTry tradeOffer : bestGoods){
						maxCapa += tradeOffer.from.getStock().get(tradeOffer.good).stock;
					}
					//TODO: 30/70 repartiion between avid and careless, like for the TR
					for(TradeTry tradeOffer : bestGoods){
						//get the valued exchange (and can't grab more than half the stock, because it would be harsh)
						int stock = tradeOffer.from.getStock().get(tradeOffer.good).stock /2;
						float capPercent = stock / maxCapa;
						int nbGoodsToChange = (int)( capPercent * capaPerMen * trMerchantUsed);
						if(nbGoodsToChange > stock){
							//max it
							nbGoodsToChange = stock;
							//other merchants do nothing, they lost the contract!
						}
						availableMerchantSlot -= (int)(nbGoodsToChange / (capPercent * capaPerMen));
						//change stock
						tradeOffer.from.getStock().get(tradeOffer.good).stock -= nbGoodsToChange;
						tradeOffer.to.getStock().get(tradeOffer.good).stock += nbGoodsToChange;
						tradeOffer.from.setMoney(tradeOffer.from.getMoney() + nbGoodsToChange * tradeOffer.sellerPrice ); //he sell, we (the merchant) give him money
						tradeOffer.from.setMoney(tradeOffer.from.getMoney() - nbGoodsToChange * tradeOffer.buyerPrice ); //he buy, he give us (merchant) money
						data.addMoney(nbGoodsToChange * (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
//						data.addToPreviousSalary(nbGoodsToChange * (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
						
						if(availableMerchantSlot<0) break;
					}
					if(availableMerchantSlot<0) System.err.println("Error, phantom merchants");
					// Remaining merchants are loosers, or they try their chance at an other place
					merchantToFill += availableMerchantSlot;
				}
			}
			
			//avid merchants
			for(TradeRoute tr : listOfTrades){
				Province otherOne = tr.getOtherEnd(prv);
				int maxMerchantForThisTR = tr.getMaxMerchant() - trAlreadyTaken.getInt(tr);
				if(maxMerchantForThisTR > 0){
					final int trMerchantUsed = (int) Math.min(maxMerchantForThisTR, merchantToFill);
					merchantToFill -= trMerchantUsed;
					int availableMerchantSlot = trMerchantUsed;
					trAlreadyTaken.put(tr, trAlreadyTaken.getInt(tr) + trMerchantUsed);
					//get the best trade here
					List<TradeTry> bestGoods = maxProfitabilityTR.get(tr);
					
					
					for(TradeTry tradeOffer : bestGoods){
						//get the valued exchange (and can't grab more than half the stock, because it would be harsh)
						int stock = tradeOffer.from.getStock().get(tradeOffer.good).stock /2;
						int nbGoodsToChange = Math.min(stock, (int)( availableMerchantSlot * capaPerMen));
						availableMerchantSlot -= (int)(nbGoodsToChange / (capaPerMen));
						//change stock
						tradeOffer.from.getStock().get(tradeOffer.good).stock -= nbGoodsToChange;
						tradeOffer.to.getStock().get(tradeOffer.good).stock += nbGoodsToChange;
						tradeOffer.from.setMoney(tradeOffer.from.getMoney() + nbGoodsToChange * tradeOffer.sellerPrice ); //he sell, we (the merchant) give him money
						tradeOffer.from.setMoney(tradeOffer.from.getMoney() - nbGoodsToChange * tradeOffer.buyerPrice ); //he buy, he give us (merchant) money
						data.addMoney(nbGoodsToChange * (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
//						data.addToPreviousSalary(nbGoodsToChange * (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
						
						if(availableMerchantSlot<0) break;
					}
					if(availableMerchantSlot<0) System.err.println("Error, phantom merchants");
					// Remaining merchants are loosers, or they try their chance at an other place
					merchantToFill += availableMerchantSlot;
				}
			}
			
			
			//if leftover, it's a loss for them.
			

			// rembourse the loan
			data.setMoney(data.getMoney() - loan);
			
			//for all our carts, destroy x% of them
			
			//then use merchant needs with merchant money to buy merchant tools
			//TODO (and do it on consume phase plz)
			// get the number of merchant without carts
			//get the best carts for them
			
			// or check our worst carts
			// get amount of good carts in stocks
			//iterate: replace best cart for worst cart
				//selling : half value because used, and you put only half of the used carts into the market because some are broken.
			
			//salary : give 50% money to pop, to smooth things
			data.addToPreviousSalary(data.getMoney()/2);
			pop.addMoney(-data.getMoney()/2);
			data.setMoney(0);
			
			
			
		}
	}
	
	protected static class TradeTry{
		double beneficePerKilo;
		int buyerPrice;
		int sellerPrice;
		Province from;
		Province to;
		Good good;
		public TradeTry(Good good, Province from, Province to, int buyerPrice, int sellerPrice, double beneficePerKilo) {
			super();
			this.good = good;
			this.from = from;
			this.to = to;
			this.buyerPrice = buyerPrice;
			this.sellerPrice = sellerPrice;
			this.beneficePerKilo = beneficePerKilo;
		}
		
		
	}

	private void produce(Province prv, int durationInDay) {
		//produce
		for( ProvinceIndustry indus : prv.getIndustries()){
			//compute marge
//			ProvinceGoods goodPrice= prv.getStock().get(indus.getIndustry().getGood());
			int goodPrice = prv.getStock().get(indus.getIndustry().getGood()).getPriceSellToMarket(prv, durationInDay);
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
				//TODO : change share (salary vs marge (profit+investment)) from civilization vars 
				//	=> if low marge, low insvestment => decline. If high marge => increase of inequality (or not), unhappiness of poor people
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
					nbEmployes += pop.getNbMensEmployed().getInt(indus);
				}
				//distribute money
				indus.setPreviousSalary(moneyToGive/nbEmployes);
				for(Pop pop : prv.getPops()){
					int employesHere = pop.getNbMensEmployed().getInt(indus);
					int money = 1 + (int)(moneyToGive * employesHere / (float)nbEmployes);
					indus.addMoney(-money);
					pop.addMoney(money);
				}
				if(indus.getMoney() < 0) System.err.println("Error, in industral money salary");
				
				//we will use our money for our need in the consume phase (or for selling)
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
		//TODO SELL THINGS IF IN NEED OF MONEY (and it's possible)
		
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
			NeedWish wish = need.moneyNeeded(prv, indus.getPreviousProduction(), indus.getStock(), indus.getMoney(), durationInDay);
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
			int moneySpent = need.spendMoney(prv, indus.getPreviousProduction(), indus.getStock(), wish, durationInDay);
			indus.addMoney(-moneySpent);
			if(indus.getMoney() < 0) System.err.println("Error, in industral money buy");
		}
	}
	
	private void popBuy(Pop pop, int nb, int money, int durationInDay){
		Map<Needs, NeedWish> moneyNeeded = new HashMap<>();
		NeedWish wantedMoney = new NeedWish(0, 0, 0);
		for(Needs need : pop.getPopNeeds()){
			NeedWish moneyNeeds = need.moneyNeeded(pop.getProvince(), nb, pop.getStock(), money, durationInDay);
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
			int moneySpent = need.spendMoney(pop.getProvince(), nb, pop.getStock(), wish, durationInDay);
			pop.addMoney(- moneySpent);
		}
		if(pop.getMoney() < 0) System.err.println("Error, in pop money buy");
	}

//	private void doExport(Province prv, int durationInDay) {
//		// TODO Auto-generated method stub
//	}

	private void setPrices(Province prv, int durationInDay) {

//		Object2IntMap<Good> mimumStock = new Object2IntOpenHashMap<>();
//		Object2IntMap<Good> optimalStock = new Object2IntOpenHashMap<>();
//		for(Pop pop : prv.getPops()){
//			for(Needs need : pop.getPopNeeds()){
//				need.getMinimumStockPerPop()
//			}
//		}
		
		for(Entry<Good, GoodsProduced> entry : oldStock.get(prv).entrySet()){ 
			GoodsProduced gp = entry.getValue();
			long newPrice = gp.exportPrice*(long)gp.nbExport;
			newPrice += gp.importPrice*(long)gp.nbImport;
			newPrice += gp.prodPrice*(long)gp.nbProd;
			newPrice += gp.oldPrice*(long)gp.oldStock;
			newPrice /= gp.nbExport+gp.nbImport+gp.nbProd+gp.oldStock;

			//test for shortage/overproduction
			ProvinceGoods prvGood = prv.getStock().get(entry.getKey());
			int newStock = prvGood.stock;
			//max 50% (asymptote)
			// 2 * need for a step is the objective.
			// 5 * need => -22%
			// 3 * need => -10%
			// 1.5 * need => +7%
			// 1 * need => +16%
			double ratio = newStock / (double)(prvGood.nbConsumePerDay * durationInDay * 2.0);
			newPrice = (int)( newPrice * (0.5+(1/(1+ratio))) );
			
			prv.getStock().get(entry.getKey()).price = (int)newPrice;
		}
	}

	private void moveWorkers(Province prv, int durationInDay) {
		
		//note: as we used the "job" abstraction, it count commerce and industry on an equal foot. TODO: Do we do the same for the army?
		for(Pop pop : prv.getPops()){
			//sort job by best salary
			List<Job> bestJob = new ArrayList<>( pop.getNbMensEmployed().keySet());
			bestJob.sort((i1,i2) -> - Integer.compare( i1.getPreviousSalary(), i2.getPreviousSalary()));
			
			//compute mean revenu
			long mean = 0;
			float nbEmployed = 0;
			for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Job> indus : pop.getNbMensEmployed().object2IntEntrySet()){
				mean += indus.getIntValue() * indus.getKey().getPreviousSalary(); //TODO multiply by an "efficiency" from education or other things.
				nbEmployed += indus.getIntValue();
			}
			mean /= pop.getNbMens();
			
			//1 : put some men from inefficient industry into chomage.
			//asymptote 1/(98x+1), => f(1) = 0.01 (1% turnover per month), so f(0.5) => ~0.02 
			//		and f(0.1) => 0.085 (if the salaries earn only 10% of the mean salary, 8.5% of them will leave each month)
			// 	lim(f, oo)=0				f(100%)=1%	f(50%)=2%	f(25%)=4%	f(10%)=9% 	f(0)=100% .. too steep
			// new func: (2^(-5.64x))/2 =>	f(1)=1%,	f(0.5)=7%,	f(25%)=19% 	f(10%)=34% 	f(0)=50% .. not steep enough
			// new func: 1/(98x²+1) => 		f(1)=1%		f(0.5)=3.8%	f(25%)=12.3	f(10%)=33.5	f(0)=50% .. not a good form (bell shape)
			// new func: 1/(70*(x+0.17)²)	f(1)=1%		f(0.5)=3.1%	f(25%)=8%	f(10%)=19.6	f(0)=49% .. seems fine
			
			// for each indus, remove employes with this asymptote.
			// if an industry has less than 10 mens (and in bad shape), remove all.
			for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Job> indus : pop.getNbMensEmployed().object2IntEntrySet()){
				float result = indus.getKey().getPreviousSalary()/(float)mean + 0.17f;
				result *= result;
				result *= 70;
				result = 1/result;
				//don't fire more people than are employed inside the company
				int nbFired = Math.min(indus.getIntValue(),(int) result);

				pop.addNbMensChomage(nbFired);
				indus.setValue(indus.getIntValue()-nbFired);
			}
			
			//then, for each job (more prosperous can grab more mens, as they have a share on the bigger pool).
			for( Job indus : bestJob){
				int nbMensEmployed = pop.getNbMensEmployed().getInt(indus);
				//compute the % of mens employed inside vs all employed.
				float ratioEmployed = nbMensEmployed / (float)nbEmployed;
				//TODO (later) compute the factor from edu and adu needed then passed in a f(eduratio, %popemployed) => ratioEduHire (you can't hire dumb people if all smart one are taken)
				//	you should checj each indus to see how many smarts people are taken, and how many you can grab now.
				float ratioEduHire = 0.8f;
				//grab ratioEduHire * employedratio * min(nbemployes/2, nbchomage/2)
				int nbHire = 1+ (int)(ratioEmployed * ratioEduHire * Math.min(nbMensEmployed/2, pop.getNbMensChomage()/2));
				nbHire = Math.min(nbHire, pop.getNbMensChomage()); //safeguard for the +1
				
				pop.addNbMensChomage(-nbHire);
				pop.getNbMensEmployed().put(indus, nbMensEmployed+nbHire);
			}
			
			//TODO: army can also grab unemployed
			
		}
		
	}

}
