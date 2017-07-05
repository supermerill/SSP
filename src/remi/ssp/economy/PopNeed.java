package remi.ssp.economy;

import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;

import java.util.HashMap;

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
		market.addStock( -quantityBuy);
		
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
		if(quantityBuy * price > moneyToSpend){
			quantityBuy = (moneyToSpend / price);
		}
		if(quantityBuy > 0){
			//buy
			long moneySpend = quantityBuy * price;
			moneyToSpend  -= moneySpend;
			prv.addMoney(moneySpend);
			myPop.addMoney(- moneySpend);
//			market.addNbConsumePerDay(quantityBuy / (float)nbDays);
			market.addStock( -quantityBuy);
		}
		return quantityBuy * price;
	}
	
	protected long consumeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myPop.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
		prv.addMoney(price*quantity);
//		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.getStock().get(good).addStock(-quantity);
		myPop.addMoney(-price*quantity);
		return price*quantity;
	}
	
	protected long storeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myPop.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
//		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.addMoney(price*quantity);
		prv.getStock().get(good).addStock(-quantity);
		log(",\""+myPop+"_buy_qtt\":"+(price*quantity)+",\""+myPop+"_buy_Mbefore\":"+myPop.getMoney());
		myPop.addMoney(-price*quantity);
		logln(",\""+myPop+"_buy_Mafter\":"+myPop.getMoney());
		myPop.getStock().put(good, quantity + myPop.getStock().getLong(good));
		return price*quantity;
	}
	
}
