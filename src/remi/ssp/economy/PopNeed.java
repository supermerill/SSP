package remi.ssp.economy;

import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

public abstract class PopNeed extends Needs {

	public static interface PopNeedFactory{ PopNeed create(Pop pop); }

	public static class PopNeedFactoryStorage{
		public static HashMap<String, PopNeedFactory> popNeedList = new HashMap<>();
		public static PopNeedFactory get(String name) { return popNeedList.get(name); }
		public static void put(String name, PopNeedFactory obj) { popNeedList.put(name, obj); }
	}
	public static PopNeed create(String name, Pop pop){
		return PopNeedFactoryStorage.get(name).create(pop);
	}
	
	protected Pop myPop;
	
	public PopNeed(Pop pop){
		myPop = pop;
	}

	public void load(JsonObject jsonObject) {}

	public void save(JsonObjectBuilder objectBuilder) {}

	protected Object2LongMap<Good> goodsNeeded(Province prv, long totalMoneyThisTurn, int nbDays, Collection<Good> possibleGoods, double nbPerDayPerMen) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult() + myPop.getNbChildren() + myPop.getNbElder();

		Object2LongOpenHashMap<Good> wish = new Object2LongOpenHashMap<>();
		
		if (totalMoneyThisTurn <= 0) {
			if(nbMensInPop == 1)
				System.err.println("ERROR no money for pop:" + totalMoneyThisTurn);
			return wish;
		}

		if (nbMensInPop == 0) {
			return wish;
		}
//
//		// all prices = price - min price*
//		// all prices = price / max price
//		// now prices are between 0 and 1
//		// all prices = 1 - price ^2
//		// now prices are the weight used to choose the quantity we want to buy
//
//		long maxPrice = 0;
//		long minPrice = Long.MAX_VALUE;
//		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
//		for (Entry<Good, ProvinceGoods> entry : goodStock.entrySet()) {
//			Good good = entry.getKey();
//			if(possibleGoods.contains(good) && entry.getValue().getStock() > 0) {
//				long price = entry.getValue().getPriceBuyFromMarket(nbDays);
//				maxPrice = Math.max(maxPrice, price);
//				minPrice = Math.min(minPrice, price);
//				goodPrice.put(good, price);
//			}
//		}
//		maxPrice -= minPrice;
//		double totQte = 0;
//		Object2DoubleMap<Good> goodQte = new Object2DoubleOpenHashMap<>();
//		for(it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entry : goodPrice.object2LongEntrySet()){
//			double temp = entry.getLongValue() - minPrice;
//			temp = temp / maxPrice;
//			temp = temp * temp;
//			totQte += temp;
//			goodQte.put(entry.getKey(), temp);
//		}
////		if(totQte == 0){
////			System.err.println("no "+this.name+" possible: ");
////		}
//		for(it.unimi.dsi.fastutil.objects.Object2DoubleMap.Entry<Good> entry : goodQte.object2DoubleEntrySet()){
//			entry.setValue(entry.getDoubleValue() / totQte);
//		}
//		
//		// now, i try to buy enought food (nbMens * nbPerDayPerMen * nbdays) mult by quantity. I stop when i have not enough money
//		long multNbFood = (long) (nbMensInPop * nbPerDayPerMen * nbDays);
//		long moneyNow = totalMoneyThisTurn;
//		List<Good> lstGood= new ArrayList<>(goodQte.keySet());
//		lstGood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
//		for(Good good : lstGood){
//			long price = goodPrice.getLong(good);
//			double qteGood = goodQte.getDouble(good);
//			if(price * qteGood * multNbFood > moneyNow){
//				wish.put(good, moneyNow / price);
//				moneyNow = 0;
//				break;
//			}else{
//				moneyNow -= price * qteGood * multNbFood;
//				wish.put(good, (long)(qteGood * multNbFood));
//			}
//		}
//		
//		//if not enough, we ad a bit of everithing
//		
//		
//		
//		logln(", \"food wish/need\":\"" + wish + "\"}");
		
		for(Good good : possibleGoods){
			wish.put(good, (long) (nbPerDayPerMen * nbDays * nbMensInPop));
		}
		return wish;
	}


	public long simpleBuy(long maxNb, Good good, long moneyToSpend, int nbDays){
		Province prv = myPop.getProvince();
		Object2LongMap<Good> currentStock = myPop.getStock();
		ProvinceGoods market = prv.getStock().get(good);
		logln(", \"simplebuy_"+myPop+"_"+good+"\":{\"stock\":"+market.getStock()+",\"iwant\":"+(maxNb - currentStock.getLong(good))+"}");
		long quantityBuy = Math.min(market.getStock(), maxNb - currentStock.getLong(good));
		long price = market.getPriceBuyFromMarket(nbDays);
		if(quantityBuy * price > moneyToSpend){
			quantityBuy = (moneyToSpend / price);
		}
		//buy
		moneyToSpend  -= quantityBuy * price;
		if(moneyToSpend <0){
		}
		prv.addMoney(quantityBuy * price);
		myPop.addMoney(- price * quantityBuy);
		currentStock.put(good, currentStock.getLong(good) + quantityBuy);
//		market.addNbConsumePerDay(quantityBuy / (float)nbDays);
		market.addStock( -quantityBuy,prv.getStock().get(good).getPriceBuyFromMarket(nbDays));
		
		return quantityBuy * price;
	}

	//TODO: it was crashing here (too muchmoney spend) but i think the problem comes from baseEconomy
	public long simpleConsume(long maxNb, Good good, long moneyToSpend, int nbDays){
		Province prv = myPop.getProvince();
		Object2LongMap<Good> currentStock = myPop.getStock();
		ProvinceGoods market = prv.getStock().get(good);
		logln(", \"SC_"+myPop+"_"+good+"\":{\"stock\":"+market.getStock()+", \"want\":"+(maxNb - currentStock.getLong(good))+"}");
		long quantityBuy = Math.min(market.getStock(), maxNb - currentStock.getLong(good));
		final long price = market.getPriceBuyFromMarket(nbDays);
		final long wtf = quantityBuy * price;
		final boolean wtf2 = (quantityBuy * price > moneyToSpend);
		final long wtf22 = quantityBuy;
		final long wtf23 = moneyToSpend;
		boolean wtf3 = false;
		if(quantityBuy * price > moneyToSpend){
			quantityBuy = (moneyToSpend / price);
			wtf3 = true;
		}
		final long wtf4 = quantityBuy;
		if(quantityBuy > 0){
			//buy
			long moneySpend = quantityBuy * price;
			moneyToSpend  -= moneySpend;
			prv.addMoney(moneySpend);
			myPop.addMoney(- moneySpend);
//			market.addNbConsumePerDay(quantityBuy / (float)nbDays);
			market.addStock( -quantityBuy,prv.getStock().get(good).getPriceBuyFromMarket(nbDays));
		}
		return quantityBuy * price;
	}
	
	protected long consumeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myPop.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
		prv.addMoney(price*quantity);
//		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.getStock().get(good).addStock(-quantity,prv.getStock().get(good).getPriceBuyFromMarket(nbDays));
		myPop.addMoney(-price*quantity);
		return price*quantity;
	}
	
	protected long storeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myPop.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
//		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.addMoney(price*quantity);
		prv.getStock().get(good).addStock(-quantity,prv.getStock().get(good).getPriceBuyFromMarket(nbDays));
		log(",\""+myPop+"_buy_qtt\":"+(price*quantity)+",\""+myPop+"_buy_Mbefore\":"+myPop.getMoney());
		myPop.addMoney(-price*quantity);
		logln(",\""+myPop+"_buy_Mafter\":"+myPop.getMoney());
		myPop.getStock().put(good, quantity + myPop.getStock().getLong(good));
		return price*quantity;
	}
	
}
