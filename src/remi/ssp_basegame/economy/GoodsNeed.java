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
public class GoodsNeed extends PopNeed{

	public static List<Good> goods = new ArrayList<>();

	public GoodsNeed(Pop pop){super(pop);}

	@Override
	public Object2LongMap<Good> goodsNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		return goodsNeeded(prv, totalMoneyThisTurn/2, nbDays, goods, 2);
	}
	
	@Override
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult();
		NeedWish wish = new NeedWish(0, 0, 0);
		
		long moneyObj = (long) (totalMoneyThisTurn * 0.25);
		
		//get the prices
		long nbGoodsNeededNormal = nbMensInPop * myPop.getCoeffInvestors();
		List<Good> lowPriceGoods = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for(Good good : goods){
			lowPriceGoods.add(good);
			nbGoodsNeededNormal -= myPop.getStock().getLong(good);
			goodPrice.put(good, goodStock.get(good).getPriceBuyFromMarket(nbDays));
			logln(", \"price of "+good+" is\":"+goodPrice.getLong(good));
		}
		
		//normal: at least 1 kilo of goods per person
		if(nbGoodsNeededNormal > 0){
			lowPriceGoods.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
			ListIterator<Good> it = lowPriceGoods.listIterator();
			while(it.hasNext()){
				Good house = it.next();
				if(nbGoodsNeededNormal <= goodStock.get(house).getStock()){
					wish.normalNeed += nbGoodsNeededNormal * goodPrice.getLong(house);
					nbGoodsNeededNormal = 0;
					break;
				}else{
					wish.normalNeed += goodStock.get(house).getStock() * goodPrice.getLong(house);
					nbGoodsNeededNormal -= goodStock.get(house).getStock();
					it.remove();
					logln(", \"i can buy\":"+goodStock.get(house).getStock());
				}
			}
			if(nbGoodsNeededNormal > 0){
				wish.normalNeed = 0;
				wish.luxuryNeed = wish.vitalNeed;
				logln(", \"--- goods need? not enough ----\":\""+wish+"\"");
				return wish;
			}
		}
		
		//lux: 
		moneyObj -= wish.normalNeed;
		wish.luxuryNeed = (long) Math.min(moneyObj * 0.5, wish.normalNeed*2);

		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult();
		final Object2LongMap<Good> currentPopStock = myPop.getStock();
		long moneyUsed = 0;
		
		//get the prices
		long nbGoodsNeededNormal = nbMensInPop * myPop.getCoeffInvestors();
		long nbGoodsNeededLux = nbGoodsNeededNormal + nbMensInPop * myPop.getCoeffInvestors() * myPop.getCoeffInvestors();
		List<Good> priceGoods = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for(Good good : goods){
			priceGoods.add(good);
			nbGoodsNeededNormal -= myPop.getStock().getLong(good);
			nbGoodsNeededLux -= myPop.getStock().getLong(good);
			goodPrice.put(good, goodStock.get(good).getPriceBuyFromMarket(nbDays));
			logln(", \"price of "+good+" is\":"+goodPrice.getLong(good));
		}

		long nbCoins = maxMoneyToSpend.vitalNeed + maxMoneyToSpend.normalNeed;
		Object2LongMap<Good> nbGoods = new Object2LongOpenHashMap<>();
		
		//try to grab at least our normal need
		if(nbGoodsNeededNormal > 0){
			priceGoods.sort((o0,o1) -> Long.compare(goodPrice.getLong(o0), goodPrice.getLong(o1)));
			for(Good good : priceGoods){
				long totalPrice = Math.min(goodStock.get(good).getStock(), nbGoodsNeededNormal)  * goodPrice.getLong(good);
				if(totalPrice == 0) continue; //no house here
				if(nbCoins <= totalPrice){
					long quantityPicked = nbCoins / goodPrice.getLong(good);
					nbGoods.put(good, quantityPicked);
					nbGoodsNeededNormal -= quantityPicked;
					nbCoins = 0 ;
					moneyUsed += nbCoins;
					break;
				}else{
					nbCoins -= totalPrice;
					moneyUsed += totalPrice;
					long quantityPicked = goodStock.get(good).getStock();
					nbGoodsNeededNormal -= quantityPicked;
					nbGoods.put(good, quantityPicked);
				}
				if(nbGoodsNeededNormal == 0) break;
			}
			
			
			
			priceGoods.sort((o0,o1) ->Long.compare( o1.getDesirability(),  o0.getDesirability()));
			//TODO: maybe random instead of best?
			for(Good good : priceGoods){
				long totalPrice = Math.min(goodStock.get(good).getStock(), nbGoodsNeededLux)  * goodPrice.getLong(good);
				if(totalPrice == 0) continue; //no house here
				if(nbCoins <= totalPrice){
					long quantityPicked = nbCoins / goodPrice.getLong(good);
					nbGoods.put(good, quantityPicked);
					nbGoodsNeededLux -= quantityPicked;
					nbCoins = 0 ;
					moneyUsed += nbCoins;
					break;
				}else{
					nbCoins -= totalPrice;
					moneyUsed += totalPrice;
					long quantityPicked = goodStock.get(good).getStock();
					nbGoodsNeededLux -= quantityPicked;
					nbGoods.put(good, quantityPicked);
				}
				if(nbGoodsNeededLux == 0) break;
			}
			
		}
		
		return moneyUsed;
	}

}
