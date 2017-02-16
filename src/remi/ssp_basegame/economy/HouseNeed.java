package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//TODO: redo it, with a different one for each pop type, for better clarity.
// and up the need
public class HouseNeed extends PopNeed{

	public static List<Good> houses = new ArrayList<>();

	public HouseNeed(Pop pop){super(pop);}
	
	@Override
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult();
		NeedWish wish = new NeedWish(0, 0, 0);
		
		long moneyObj = (long) (totalMoneyThisTurn * 0.25);
		
		//get the prices (1 house = 4 hab, per default), 1 tonne
		long nbHousesNeeded = 1000*nbMensInPop/4;
		List<Good> lowPriceHouse = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for(Good house : houses){
			lowPriceHouse.add(house);
			nbHousesNeeded -= myPop.getStock().getLong(house);
			goodPrice.put(house, goodStock.get(house).getPriceBuyFromMarket(nbDays));
			logln("price of "+house+" is "+goodPrice.getLong(house));
		}
		
		//vital: at least the worst house for every houseless
		if(nbHousesNeeded > 0){
			lowPriceHouse.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
			ListIterator<Good> it = lowPriceHouse.listIterator();
			while(it.hasNext()){
				Good house = it.next();
				logln("check "+house);
				if(nbHousesNeeded <= goodStock.get(house).getStock()){
					wish.vitalNeed += nbHousesNeeded * goodPrice.getLong(house);
					nbHousesNeeded = 0;
					break;
				}else{
					wish.vitalNeed += goodStock.get(house).getStock() * goodPrice.getLong(house);
					nbHousesNeeded -= goodStock.get(house).getStock();
					it.remove();
					logln("i can buy "+goodStock.get(house).getStock());
				}
			}
			if(nbHousesNeeded > 0){
				//manque => certain peuvent mourir de froid et/ou maladie
				wish.normalNeed = 0;
				wish.luxuryNeed = wish.vitalNeed;
				logln("--- house need? not enough ---- "+wish);
				return wish;
			}
		}
		
		//normal: upgrade some house (up to normal). Do not consume too much money
		moneyObj -= wish.vitalNeed;
		if(moneyObj <0){
			moneyObj = (long) (wish.vitalNeed * 0.2);
		}
		wish.normalNeed = (long) (moneyObj * 0.5);
		
		
		//luxury: upgrade some house (up to lux). Do not consume too much money
		wish.luxuryNeed = (long) (moneyObj * 0.5);

		logln("--- house need? ---- "+wish);
		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult();
		final Object2LongMap<Good> currentPopStock = myPop.getStock();
		long moneyUsed = 0;
		
		//get the prices (1 house = 4 hab, per default, 1tonne)
		long nbHousesNeeded = 1000*nbMensInPop/4;
		List<Good> lowPriceHouse = new ArrayList<>();
		List<Good> normalHouse = new ArrayList<>();
		List<Good> luxuryHouse = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for(Good house : houses){
			
			nbHousesNeeded -= myPop.getStock().getLong(house);
			goodPrice.put(house, goodStock.get(house).getPriceBuyFromMarket(nbDays));
			
			lowPriceHouse.add(house);
			if(house.getDesirability()<20){
				normalHouse.add(house);
			}
			if(house.getDesirability()>20){
				luxuryHouse.add(house);
			}
		}

		long nbCoins = maxMoneyToSpend.vitalNeed;
		Object2LongMap<Good> nbGoods = new Object2LongOpenHashMap<>();
		//first, buy all basic houses.
		if(nbHousesNeeded > 0){
			lowPriceHouse.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
			for(Good house : lowPriceHouse){
				long totalPrice = goodStock.get(house).getStock() * goodPrice.getLong(house);
				if(totalPrice == 0) continue; //no house here
				if(nbCoins <= totalPrice){
					long quantityPicked = (long)(nbCoins / goodPrice.getLong(house));
					nbGoods.put(house, quantityPicked);
					logln("HOUSE REALLY WANT "+quantityPicked+" "+house.getName());
					nbHousesNeeded -= quantityPicked;
					nbCoins = 0 ;
					moneyUsed += nbCoins;
					break;
				}else{
					nbCoins -= totalPrice;
					moneyUsed += totalPrice;
					long quantityPicked = goodStock.get(house).getStock();
					nbHousesNeeded -= quantityPicked;
					nbGoods.put(house, quantityPicked);
					logln("HOUSE REALLY WANT "+quantityPicked+" "+house.getName());
				}
			}
			if(nbHousesNeeded > 0){
				//make homeless die
				// 0.3% per month ->3.6% per year (it's 2% per year in paris 2013 450/22500)
				int nbPopToDie = 1+ (int)(Math.min(Integer.MAX_VALUE, nbHousesNeeded * 0.0000001 * nbDays)); 
				//TODO: random chance if >0 and < 1 instead of +1
				//TODO also remove child & elder
				log("homeless die! ( "+nbHousesNeeded+" houme needed => "+nbPopToDie+" victim");
				myPop.addAdult(-nbPopToDie);
			}
		}
		
		//then, upgrade random houses to better ones (max normal)
		nbHousesNeeded = 0;
		nbCoins += maxMoneyToSpend.normalNeed;
		long chunkCoins = nbCoins / 10;
		normalHouse.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
		//Use this money to buy 10 chunk of random house.
		for(int i=0;i<10 && nbCoins > 0;i++){
			Good house = normalHouse.get(GlobalRandom.aleat.getInt(normalHouse.size(), (int)nbMensInPop));
			long maxStock = goodStock.get(house).getStock() - nbGoods.getLong(house);
			long price = goodPrice.getLong(house);
			if(maxStock == 0) continue; //no house here
			if(price == 0){System.err.println("Error in house needs: no price"); continue;}
			long nbPicked = Math.min(Math.max(1, chunkCoins / price), maxStock);
			if(nbCoins >= nbPicked * price){
				nbCoins -= nbPicked * price;
				moneyUsed += nbPicked * price;
				logln("HOUSE WANT "+nbPicked+" "+house.getName()+" / "+nbGoods.getLong(house)+" maxStock="+maxStock);
				nbGoods.put(house, nbGoods.getLong(house) + nbPicked);
				nbHousesNeeded -= nbPicked;
			}
			if(i==8){
				chunkCoins = nbCoins;
			}
		}
		
		//upgrade to luxurious houses, if possible
		nbCoins += maxMoneyToSpend.luxuryNeed;
		chunkCoins = nbCoins / 10;
		luxuryHouse.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
		//Use this money to buy 10 chunk of random house.
		for(int i=0;i<10 && nbCoins > 0;i++){
			Good house = luxuryHouse.get(GlobalRandom.aleat.getInt(luxuryHouse.size(), (int)nbMensInPop));
			long maxStock = goodStock.get(house).getStock() - nbGoods.getLong(house);
			long price = goodPrice.getLong(house);
			if(maxStock == 0) continue; //no house here
			if(price == 0){System.err.println("Error in house needs: no price"); continue;}
			long nbPicked = Math.min(Math.max(1, chunkCoins / price), maxStock);
			if(nbCoins >= nbPicked * price){
				nbCoins -= nbPicked * price;
				moneyUsed += nbPicked * price;
				logln("HOUSE WANT MAYBE "+nbPicked+" "+house.getName()+" / "+nbGoods.getLong(house));
				nbGoods.put(house, nbGoods.getLong(house) + nbPicked);
				nbHousesNeeded -= nbPicked;
			}
			if(i==8){
				chunkCoins = nbCoins;
			}
		}
		
		// send empty houses to stock
		long chunkHouses = Math.max(1, -nbHousesNeeded / 10);
		while(nbHousesNeeded < 0 && lowPriceHouse.size()>0){
//			int randomHouseToSell = GlobalRandom.aleat.getInt(lowPriceHouse.size(), (int)nbMensInPop);
//			randomHouseToSell += GlobalRandom.aleat.getInt(lowPriceHouse.size(), (int)nbMensInPop+7);
//			randomHouseToSell += GlobalRandom.aleat.getInt(lowPriceHouse.size(), (int)nbMensInPop+13);
//			// randomHouseToSell is now between 0 and 3*lowPriceHouse.size, with a mean at lowPriceHouse.size*1.5
//			randomHouseToSell = (int)(Math.abs( (randomHouseToSell - nbMensInPop * 1.5)/1.5));
			int idx = Math.min(GlobalRandom.aleat.nextInt(lowPriceHouse.size()), lowPriceHouse.size()-1);
			Good house = lowPriceHouse.get(idx);
			//sell it
			//TODO create a renovation industry (sell house to renovation -> then sell house to market)
			long price = goodStock.get(house).getPriceSellToMarket(nbDays);
			long nbInstock = myPop.getStock().getLong(house);
			logln("HOUSE DON4T WANT "+chunkHouses+" "+house.getName()+" / "+nbGoods.getLong(house));
			nbGoods.put(house, nbGoods.getLong(house) - chunkHouses);
			nbHousesNeeded += chunkHouses;
			long totalCost = chunkHouses * price;
			moneyUsed -= totalCost;
			nbCoins += totalCost;
		}
		
		//now tranfert the properties
		moneyUsed = 0;
		for(it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entry : nbGoods.object2LongEntrySet()){
			Good house = entry.getKey();
			long nbBuy = entry.getLongValue();
			currentPopStock.put(house, currentPopStock.getLong(house)+nbBuy);
//			goodStock.get(house).addNbConsumePerDay(nbBuy / (float)nbDays);
			goodStock.get(house).addStock( -nbBuy);
			logln("HOUSE NEED buy "+nbBuy+" "+house.getName());
		}

		prv.addMoney(moneyUsed);
		myPop.addMoney(-moneyUsed);
		return moneyUsed;
	}

}
