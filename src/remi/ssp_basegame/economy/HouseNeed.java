package remi.ssp_basegame.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.ComparatorValueDesc;


public class HouseNeed extends PopNeed{

	public static List<Good> houses = new ArrayList<>();

	private HouseNeed(Pop pop){super(pop);}
	
	@Override
	public NeedWish moneyNeeded(Province prv, int nbMensInPop, Object2IntMap<Good> currentStock,
			int totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		
		NeedWish wish = new NeedWish(0, 0, 0);
		
		int moneyObj = (int) (totalMoneyThisTurn * 0.25);
		
		//get the prices (1 house = 4 hab, per default)
		int nbHousesNeeded = nbMensInPop/4;
		List<Good> lowPriceHouse = new ArrayList<>();
		Object2IntMap<Good> goodPrice = new Object2IntOpenHashMap<>();
		for(Good house : houses){
			
			nbHousesNeeded -= myPop.getStock().getInt(house);
			goodPrice.put(house, goodStock.get(house).getPriceBuyFromMarket(prv, nbDays));
		}
		
		//vital: at least the worst house for every clodo
		if(nbHousesNeeded > 0){
			lowPriceHouse.sort(new ComparatorValueDesc<>(goodPrice));
			ListIterator<Good> it = lowPriceHouse.listIterator();
			while(it.hasNext()){
				Good house = it.next();
				if(nbHousesNeeded <= currentStock.getInt(house)){
					wish.vitalNeed += nbHousesNeeded * goodPrice.getInt(house);
					nbHousesNeeded = 0;
					break;
				}else{
					wish.vitalNeed += currentStock.getInt(house) * goodPrice.getInt(house);
					nbHousesNeeded -= currentStock.getInt(house);
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
			moneyObj = (int) (wish.vitalNeed * 0.2);
		}
		wish.normalNeed = (int) (moneyObj * 0.5);
		
		
		//luxury: upgrade some house (up to lux). Do not consume too much money
		wish.luxuryNeed = (int) (moneyObj * 0.5);
		
		return wish;
	}

	@Override
	public int spendMoney(Province prv, int nbMensInPop, Object2IntMap<Good> currentStock,
			NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		
		int moneyUsed = 0;
		
		//get the prices (1 house = 4 hab, per default)
		int nbHousesNeeded = nbMensInPop/4;
		List<Good> lowPriceHouse = new ArrayList<>();
		List<Good> normalHouse = new ArrayList<>();
		List<Good> luxuryHouse = new ArrayList<>();
		Object2IntMap<Good> goodPrice = new Object2IntOpenHashMap<>();
		for(Good house : houses){
			
			nbHousesNeeded -= myPop.getStock().getInt(house);
			goodPrice.put(house, goodStock.get(house).getPriceBuyFromMarket(prv, nbDays));
			
			lowPriceHouse.add(house);
			if(house.getDesirability()<20){
				normalHouse.add(house);
			}
			if(house.getDesirability()>20){
				luxuryHouse.add(house);
			}
		}

		int nbCoins = maxMoneyToSpend.vitalNeed;
		Object2IntMap<Good> nbGoods = new Object2IntOpenHashMap<>();
		//first, buy all basic houses.
		if(nbHousesNeeded > 0){
			lowPriceHouse.sort(new ComparatorValueDesc<>(goodPrice));
			ListIterator<Good> it = lowPriceHouse.listIterator();
			while(it.hasNext()){
				Good house = it.next();
				int totalPrice = currentStock.getInt(house) * goodPrice.getInt(house);
				if(nbCoins <= totalPrice){
					int quantityPicked = nbCoins / goodPrice.getInt(house);
					nbGoods.put(house, quantityPicked);
					nbHousesNeeded -= quantityPicked;
					nbCoins = 0 ;
					moneyUsed += nbCoins;
					break;
				}else{
					nbCoins -= totalPrice;
					moneyUsed += totalPrice;
					int quantityPicked = currentStock.getInt(house);
					nbHousesNeeded -= quantityPicked;
					nbGoods.put(house, quantityPicked);
				}
			}
			if(nbHousesNeeded > 0){
				//make homeless die
				int nbPopToDie = 1+ (int)(nbHousesNeeded * 0.0001 * nbDays); // 0.3% per month ->3.6% per year (it's 2% per year in paris 2013 450/22500)
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
		int chunk = nbCoins / 10;
		normalHouse.sort(new ComparatorValueDesc<>(goodPrice));
		ListIterator<Good> it = normalHouse.listIterator();
		//Use this money to buy 10 chunk of random house.
		for(int i=0;i<10 && nbCoins > 0;i++){
			Good house = normalHouse.get(GlobalRandom.aleat.getInt(normalHouse.size(), nbMensInPop));
			int maxStock = currentStock.getInt(house) - nbGoods.getInt(house);
			int price = goodPrice.getInt(house);
			int nbPicked = Math.min(Math.max(1, chunk / price), maxStock);
			if(nbCoins >= nbPicked * price){
				nbCoins -= nbPicked * price;
				moneyUsed += nbPicked * price;
				nbGoods.put(house, nbGoods.get(house) + nbPicked);
				nbHousesNeeded -= nbPicked;
			}
			if(i==8){
				chunk = nbCoins;
			}
		}
		
		//upgrade to luxurious houses, if possible
		nbCoins += maxMoneyToSpend.luxuryNeed;
		chunk = nbCoins / 10;
		luxuryHouse.sort(new ComparatorValueDesc<>(goodPrice));
		it = luxuryHouse.listIterator();
		//Use this money to buy 10 chunk of random house.
		for(int i=0;i<10 && nbCoins > 0;i++){
			Good house = luxuryHouse.get(GlobalRandom.aleat.getInt(luxuryHouse.size(), nbMensInPop));
			int maxStock = currentStock.getInt(house) - nbGoods.getInt(house);
			int price = goodPrice.getInt(house);
			int nbPicked = Math.min(Math.max(1, chunk / price), maxStock);
			if(nbCoins >= nbPicked * price){
				nbCoins -= nbPicked * price;
				moneyUsed += nbPicked * price;
				nbGoods.put(house, nbGoods.get(house) + nbPicked);
				nbHousesNeeded -= nbPicked;
			}
			if(i==8){
				chunk = nbCoins;
			}
		}
		
		// send empty houses to stock
		chunk = Math.max(1, -nbHousesNeeded / 10);
		while(nbHousesNeeded < 0){
			int randomHouseToSell = GlobalRandom.aleat.getInt(lowPriceHouse.size(), nbMensInPop);
			randomHouseToSell += GlobalRandom.aleat.getInt(lowPriceHouse.size(), nbMensInPop);
			randomHouseToSell += GlobalRandom.aleat.getInt(lowPriceHouse.size(), nbMensInPop);
			// randomHouseToSell is now between 0 and 3*lowPriceHouse.size, with a mean at lowPriceHouse.size*1.5
			randomHouseToSell = (int)(Math.abs( (randomHouseToSell - nbMensInPop * 1.5)/1.5));
			Good house = lowPriceHouse.get(Math.min(randomHouseToSell, lowPriceHouse.size()-1));
			//sell it
			//TODO create a renovation industry (sell house to renovation -> then sell house to market)
			int price = goodStock.get(house).getPriceSellToMarket(prv, nbDays);
			nbGoods.put(house, nbGoods.get(house) - chunk);
			nbHousesNeeded += chunk;
			int totalCost = chunk * price;
			moneyUsed -= totalCost;
			prv.addMoney(totalCost);
			nbCoins += totalCost;
		}
		
		//now tranfert the properties
		moneyUsed = 0;
		for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Good> entry : nbGoods.object2IntEntrySet()){
			Good house = entry.getKey();
			int nbBuy = entry.getIntValue();
			goodStock.get(house).stock -= nbBuy;
		}
		
		return moneyUsed;
	}

}
