package remi.ssp_basegame.economy;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.Needs;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Province;

//TODO: a more complex one: raw from the map, and tools from goods available in market & their tools efficiency (and an upgrade of stock tools)

public class BasicIndustryNeeds extends Needs {
	
	Industry indus;
	Object2FloatMap<Good> rawNeeded = new Object2FloatOpenHashMap<>();
	Object2FloatMap<Good> toolsNeeded = new Object2FloatOpenHashMap<>();
	
	public BasicIndustryNeeds(Industry indus){
		this.indus = indus;
	}
	
	public BasicIndustryNeeds addRawGood(Good good, float quantityPerProduct){
		rawNeeded.put(good, quantityPerProduct);
		return this;
	}
	
	public BasicIndustryNeeds addToolGood(Good good, float quantityPerProduct){
		toolsNeeded.put(good, quantityPerProduct);
		return this;
	}


	@Override
	public NeedWish moneyNeeded(Province prv, int lastProductionSamePeriod, Object2IntMap<Good> currentStock, int totalMoneyThisTurn,
			int nbDays) {
		NeedWish wish = new NeedWish(0, 0, 0);
		//vital: raw needed
		//normal: tools (and a bit more raw)
		//luxury : a bit more tools & raw
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			int price = prv.getStock().get(needed.getKey()).getPriceBuyFromMarket(prv, nbDays);
			wish.vitalNeed += (int)(lastProductionSamePeriod * needed.getFloatValue()) - currentStock.getInt(needed.getKey()) * price;
			wish.normalNeed +=  (int)(lastProductionSamePeriod * needed.getFloatValue()*0.3f) - currentStock.getInt(needed.getKey()) * price;
			wish.luxuryNeed +=  (int)(lastProductionSamePeriod * needed.getFloatValue()*0.2f) - currentStock.getInt(needed.getKey()) * price;
		}
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
			int price = prv.getStock().get(needed.getKey()).getPriceBuyFromMarket(prv, nbDays);
			wish.normalNeed +=  (int)(lastProductionSamePeriod * needed.getFloatValue()*0.9f) - currentStock.getInt(needed.getKey()) * price;
			wish.luxuryNeed +=  (int)(lastProductionSamePeriod * needed.getFloatValue()*0.6f) - currentStock.getInt(needed.getKey()) * price;
		}
		return wish;
	}

	/**
	 * Spend money: sped your money for buying goods from market and puttig into our stock. !!!! return the money spend, do not remove it yourself from the IndustryProvince instance !!!
	 * @param prv province (market)
	 * @param lastProductionSamePeriod Number of goods produced the last time (same nbDays as now)
	 * @param currentStock Our stock (put new goods inside that)
	 * @param maxMoneyToSpend  the money we can spend (by category)
	 * @param nbDays period
	 * @return the amount of money that must be removed from the provinceindustry instance
	 */
	@Override
	public int spendMoney(Province prv, int lastProductionSamePeriod, Object2IntMap<Good> currentStock, NeedWish maxMoneyToSpend,
			int nbDays) {
		//TODO: better one.
		final int maxMoney = maxMoneyToSpend.normalNeed + maxMoneyToSpend.vitalNeed + maxMoneyToSpend.luxuryNeed;
		int moneyToSpend = maxMoney;
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			int currentStockNumber = currentStock.getInt(needed.getKey());
			ProvinceGoods market = prv.getStock().get(needed.getKey());
			int quantityBuy = Math.min(market.stock, (int)(lastProductionSamePeriod * needed.getFloatValue()) - currentStockNumber);
			int price = market.getPriceBuyFromMarket(prv, nbDays);
			if(quantityBuy * price > moneyToSpend){
				quantityBuy = moneyToSpend / price;
			}
			//buy
			moneyToSpend  -= quantityBuy * price;
			prv.addMoney(quantityBuy * price);
			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
			market.stock -= quantityBuy;
		}
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
			int currentStockNumber = currentStock.getInt(needed.getKey());
			ProvinceGoods market = prv.getStock().get(needed.getKey());
			int quantityBuy = Math.min(market.stock, (int)(lastProductionSamePeriod * needed.getFloatValue()) - currentStockNumber);
			int price = market.getPriceBuyFromMarket(prv, nbDays);
			if(quantityBuy * price > moneyToSpend){
				quantityBuy = moneyToSpend / price;
			}
			//buy
			moneyToSpend  -= quantityBuy * price;
			prv.addMoney(quantityBuy * price);
			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
			market.stock -= quantityBuy;
		}
		//then buy some more stock
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			int currentStockNumber = currentStock.getInt(needed.getKey());
			ProvinceGoods market = prv.getStock().get(needed.getKey());
			int quantityBuy = Math.min(market.stock, (int)(lastProductionSamePeriod * needed.getFloatValue()*0.5));
			int price = market.getPriceBuyFromMarket(prv, nbDays);
			if(quantityBuy * price > moneyToSpend){
				quantityBuy = moneyToSpend / price;
			}
			//buy
			moneyToSpend  -= quantityBuy * price;
			prv.addMoney(quantityBuy * price);
			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
			market.stock -= quantityBuy;
		}
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
			int currentStockNumber = currentStock.getInt(needed.getKey());
			ProvinceGoods market = prv.getStock().get(needed.getKey());
			int quantityBuy = Math.min(market.stock, (int)(lastProductionSamePeriod * needed.getFloatValue()*0.5));
			int price = market.getPriceBuyFromMarket(prv, nbDays);
			if(quantityBuy * price > moneyToSpend){
				quantityBuy = moneyToSpend / price;
			}
			//buy
			moneyToSpend  -= quantityBuy * price;
			prv.addMoney(quantityBuy * price);
			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
			market.stock -= quantityBuy;
		}
		return maxMoney - moneyToSpend;
	}

}
