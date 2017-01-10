package remi.ssp.economy.data;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.Pop;
import remi.ssp.Province;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
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
		
		//first, buy all basic houses.
		if(nbHousesNeeded > 0){
			lowPriceHouse.sort(new ComparatorValueDesc<>(goodPrice));
			ListIterator<Good> it = lowPriceHouse.listIterator();
			for(it.hasNext()){
				Good house : it.next();
				int totalPrice = currentStock.getInt(house) * goodPrice.getInt(house);
				if(nbCoins <= totalPrice){
					int quantityPicked = nbCoins / goodPrice.getInt(house);
					nbKiloVital += quantityPicked;
					nbGoods.put(house, quantityPicked);
					nbCoins = 0 ;
					break;
				}else{
					nbCoins -= totalPrice;
					int quantityPicked = currentStock.getInt(house);
					nbKiloVital += currentStock.getInt(house);
					nbGoods.put(house, quantityPicked);
				}
			}
			while(it.hasNext()){
				Good house = it.next();
				if(nbHousesNeeded <= currentStock.getInt(house)){
					if(maxMoneyToSpend <=)
					maxMoneyToSpend -= nbHousesNeeded * goodPrice.getInt(house);
					nbHousesNeeded = 0;
					break;
				}else{
					maxMoneyToSpend -= currentStock.getInt(house) * goodPrice.getInt(house);
					nbHousesNeeded -= currentStock.getInt(house);
					it.remove();
					normalHouse.remove(house);
				}
			}
			if(nbHousesNeeded > 0){
				//manque => certain peuvent mourir de froid et/ou maladie
				wish.normalNeed = 0;
				wish.luxuryNeed = wish.vitalNeed;
				return wish;
			}
		}
		
		//then, upgrade random houses to better ones (max normal)
		

		//then, upgrade random houses to better ones (lux only)
		
		
		return moneyUsed;
	}

}
