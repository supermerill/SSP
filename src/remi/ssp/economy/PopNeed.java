package remi.ssp.economy;

import java.util.HashMap;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
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


	public long simpleBuy(long maxNb, Good good, long moneyToSpend, int nbDays){
		Province prv = myPop.getProvince();
		Object2LongMap<Good> currentStock = myPop.getStock();
		ProvinceGoods market = prv.getStock().get(good);
		System.out.println("there are "+market.getStock()+" available, and i want buy "+(maxNb - currentStock.getLong(good)));
		long quantityBuy = Math.min(market.getStock(), maxNb - currentStock.getLong(good));
		long price = market.getPriceBuyFromMarket(prv, nbDays);
		if(quantityBuy * price > moneyToSpend){
			quantityBuy = (moneyToSpend / price);
		}
		//buy
		moneyToSpend  -= quantityBuy * price;
		if(moneyToSpend <0){
		}
		prv.addMoney(quantityBuy * price);
		myPop.addMoney(- price * quantityBuy);
		market.addNbConsumePerDay(quantityBuy / (float)nbDays);
		currentStock.put(good, currentStock.getLong(good) + quantityBuy);
		market.addStock( -quantityBuy);
		
		return quantityBuy * price;
	}

	public long simpleConsume(long maxNb, Good good, long moneyToSpend, int nbDays){
		Province prv = myPop.getProvince();
		Object2LongMap<Good> currentStock = myPop.getStock();
		ProvinceGoods market = prv.getStock().get(good);
		System.out.println("there are "+market.getStock()+" available, and i want buy "+(maxNb - currentStock.getLong(good)));
		long quantityBuy = Math.min(market.getStock(), maxNb - currentStock.getLong(good));
		long price = market.getPriceBuyFromMarket(prv, nbDays);
		if(quantityBuy * price > moneyToSpend){
			quantityBuy = (moneyToSpend / price);
		}
		//buy
		moneyToSpend  -= quantityBuy * price;
		if(moneyToSpend <0){
		}
		prv.addMoney(quantityBuy * price);
		myPop.addMoney(- price * quantityBuy);
		market.addNbConsumePerDay(quantityBuy / (float)nbDays);
		market.addStock( -quantityBuy);
		
		return quantityBuy * price;
	}
	
	protected void consumeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myPop.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(prv, nbDays);
		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.addMoney(price*quantity);
		prv.getStock().get(good).addStock(-quantity);
		myPop.addMoney(-price*quantity);
	}
	
	protected void storeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myPop.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(prv, nbDays);
		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.addMoney(price*quantity);
		prv.getStock().get(good).addStock(-quantity);
		myPop.addMoney(-price*quantity);
		myPop.getStock().put(good, quantity + myPop.getStock().getLong(good));
	}
	
}
