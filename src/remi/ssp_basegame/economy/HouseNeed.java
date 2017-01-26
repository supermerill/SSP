package remi.ssp_basegame.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.ComparatorValueDesc;


public class HouseNeed extends PopNeed{

	public static List<Good> houses = new ArrayList<>();

	public HouseNeed(Pop pop){super(pop);}
	
	@Override
	public NeedWish moneyNeeded(Province prv, long nbMensInPop, Object2LongMap<Good> currentPopStock,
			long totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		
		NeedWish wish = new NeedWish(0, 0, 0);
		
		long moneyObj = (long) (totalMoneyThisTurn * 0.25);
		
		//get the prices (1 house = 4 hab, per default)
		long nbHousesNeeded = nbMensInPop/4;
		List<Good> lowPriceHouse = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for(Good house : houses){
			
			nbHousesNeeded -= myPop.getStock().getLong(house);
			goodPrice.put(house, goodStock.get(house).getPriceBuyFromMarket(prv, nbDays));
		}
		
		//vital: at least the worst house for every clodo
		if(nbHousesNeeded > 0){
			lowPriceHouse.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
			ListIterator<Good> it = lowPriceHouse.listIterator();
			while(it.hasNext()){
				Good house = it.next();
				if(nbHousesNeeded <= goodStock.get(house).getStock()){
					wish.vitalNeed += nbHousesNeeded * goodPrice.getLong(house);
					nbHousesNeeded = 0;
					break;
				}else{
					wish.vitalNeed += goodStock.get(house).getStock() * goodPrice.getLong(house);
					nbHousesNeeded -= goodStock.get(house).getStock();
					it.remove();
				}
			}
			if(nbHousesNeeded > 0){
				//manque => certain peuvent mourir de froid et/ou maladie
				wish.normalNeed = 0;
				wish.luxuryNeed = wish.vitalNeed;
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
		
		return wish;
	}

	@Override
	public long spendMoney(Province prv, long nbMensInPop, Object2LongMap<Good> currentPopStock,
			NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		
		long moneyUsed = 0;
		
		//get the prices (1 house = 4 hab, per default)
		long nbHousesNeeded = nbMensInPop/4;
		List<Good> lowPriceHouse = new ArrayList<>();
		List<Good> normalHouse = new ArrayList<>();
		List<Good> luxuryHouse = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for(Good house : houses){
			
			nbHousesNeeded -= myPop.getStock().getLong(house);
			goodPrice.put(house, goodStock.get(house).getPriceBuyFromMarket(prv, nbDays));
			
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
			ListIterator<Good> it = lowPriceHouse.listIterator();
			while(it.hasNext()){
				Good house = it.next();
				long totalPrice = goodStock.get(house).getStock() * goodPrice.getLong(house);
				if(totalPrice == 0) continue; //no house here
				if(nbCoins <= totalPrice){
					long quantityPicked = (long)(nbCoins / goodPrice.getLong(house));
					nbGoods.put(house, quantityPicked);
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
				}
			}
			if(nbHousesNeeded > 0){
				//make homeless die
				// 0.3% per month ->3.6% per year (it's 2% per year in paris 2013 450/22500)
				int nbPopToDie = 1+ (int)(Math.min(Integer.MAX_VALUE, nbHousesNeeded * 0.0001 * nbDays)); 
				System.out.print("homeless die! ( "+nbHousesNeeded+" houme needed => "+nbPopToDie+" victim");
				while(nbPopToDie > 0 && nbMensInPop > 0){
					if(GlobalRandom.aleat.getInt(2, nbPopToDie) == 0){
						//don't discriminate
						int age = GlobalRandom.aleat.getInt(100, nbPopToDie);
						if(myPop.getNombreHabitantsParAge(age)>0){
							myPop.removeHabitants(age, 1);
							nbPopToDie--;
							nbMensInPop--;
						}
					}
				}
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
			if(price == 0){System.err.println("Error in food needs: no price"); continue;}
			long nbPicked = Math.min(Math.max(1, chunkCoins / price), maxStock);
			if(nbCoins >= nbPicked * price){
				nbCoins -= nbPicked * price;
				moneyUsed += nbPicked * price;
				nbGoods.put(house, nbGoods.get(house) + nbPicked);
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
			if(price == 0){System.err.println("Error in food needs: no price"); continue;}
			long nbPicked = Math.min(Math.max(1, chunkCoins / price), maxStock);
			if(nbCoins >= nbPicked * price){
				nbCoins -= nbPicked * price;
				moneyUsed += nbPicked * price;
				nbGoods.put(house, nbGoods.get(house) + nbPicked);
				nbHousesNeeded -= nbPicked;
			}
			if(i==8){
				chunkCoins = nbCoins;
			}
		}
		
		// send empty houses to stock
		long chunkHouses = Math.max(1, -nbHousesNeeded / 10);
		while(nbHousesNeeded < 0){
			int randomHouseToSell = GlobalRandom.aleat.getInt(lowPriceHouse.size(), (int)nbMensInPop);
			randomHouseToSell += GlobalRandom.aleat.getInt(lowPriceHouse.size(), (int)nbMensInPop+7);
			randomHouseToSell += GlobalRandom.aleat.getInt(lowPriceHouse.size(), (int)nbMensInPop+13);
			// randomHouseToSell is now between 0 and 3*lowPriceHouse.size, with a mean at lowPriceHouse.size*1.5
			randomHouseToSell = (int)(Math.abs( (randomHouseToSell - nbMensInPop * 1.5)/1.5));
			Good house = lowPriceHouse.get(Math.min(randomHouseToSell, lowPriceHouse.size()-1));
			//sell it
			//TODO create a renovation industry (sell house to renovation -> then sell house to market)
			long price = goodStock.get(house).getPriceSellToMarket(prv, nbDays);
			nbGoods.put(house, nbGoods.get(house) - chunkHouses);
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
			goodStock.get(house).addStock( -nbBuy);
			currentPopStock.put(house, currentPopStock.get(house)+nbBuy);
			goodStock.get(house).addNbConsumePerDay(nbBuy / (float)nbDays);
			System.out.println("buy "+nbBuy+" "+house.getName());
		}

		prv.addMoney(moneyUsed);
		return moneyUsed;
	}

}
