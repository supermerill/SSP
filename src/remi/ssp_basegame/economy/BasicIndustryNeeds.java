package remi.ssp_basegame.economy;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.IndustryNeed;
import remi.ssp.economy.Needs.NeedWish;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Province;

//TODO: a more complex one: raw from the map, and tools from goods available in market & their tools efficiency (and an upgrade of stock tools)

public class BasicIndustryNeeds extends IndustryNeed {
	
	Object2FloatMap<Good> rawNeeded = new Object2FloatOpenHashMap<>();
	Object2FloatMap<Good> toolsNeeded = new Object2FloatOpenHashMap<>();
	
	public BasicIndustryNeeds(ProvinceIndustry indus){
		super(indus);
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
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		NeedWish wish = new NeedWish(0, 0, 0);
		Object2LongMap<Good> currentStock = myIndus.getStock();
		long maxLastProd = Math.max(1, myIndus.getPreviousProduction());
		//vital: raw needed
		//normal: tools (and a bit more raw)
		//luxury : a bit more tools & raw
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			long price = prv.getStock().get(needed.getKey()).getPriceBuyFromMarket(nbDays);
			long nbItemCanBuy = Math.max(0, Math.min(prv.getStock().get(needed.getKey()).getStock(), 
					(long)(2 * maxLastProd * needed.getFloatValue()) - currentStock.getLong(needed.getKey())));
			if(nbItemCanBuy<0) nbItemCanBuy = 0;
			wish.vitalNeed += nbItemCanBuy * price/2;
			wish.normalNeed +=  nbItemCanBuy * price /4;
			wish.luxuryNeed +=  nbItemCanBuy * price /4;
		}
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
			long price = prv.getStock().get(needed.getKey()).getPriceBuyFromMarket(nbDays);
			long nbItemCanBuy = Math.max(0, Math.min(prv.getStock().get(needed.getKey()).getStock(), 
					(long)(2*maxLastProd * needed.getFloatValue()*0.9f) - currentStock.getLong(needed.getKey())));
			if(nbItemCanBuy<0) nbItemCanBuy = 0;
			wish.normalNeed +=  nbItemCanBuy * price /2;
			wish.luxuryNeed +=  nbItemCanBuy * price /4;
		}
//		logln("2indus "+myIndus.getName()+" need "+wish);
		return wish;
	}

	/**
	 * Spend money: sped your money for buying goods from market and putting into our stock. 
	 * !!!! return the money spend, but do not forget to remove it yourself from the IndustryProvince instance !!! 
	 * Use the helper methods to avoid breaking the program
	 * @param prv province (market)
	 * @param nbEmployes number of workers
	 * @param currentStock Our stock (put new goods inside that)
	 * @param maxMoneyToSpend  the money we can spend (by category)
	 * @param nbDays period
	 * @return the amount of money that must be removed from the provinceindustry instance
	 */
	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		Object2LongMap<Good> currentStock = myIndus.getStock();
		long maxLastProd = Math.max(1, myIndus.getPreviousProduction());
		long rawGoodCost = 0;
		//TODO: better one.
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
//		logln("0indus "+myIndus.getName()+" has "+myIndus.getMoney()+" : "+maxMoneyToSpend+", lastProd = "+maxLastProd);
		final long maxMoney = maxMoneyToSpend.getMoney();
		long moneyToSpend = maxMoney;
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			long currentStockNumber = currentStock.getLong(needed.getKey());
			ProvinceGoods market = goodStock.get(needed.getKey());
			long quantityBuy = Math.max(0, Math.min(market.getStock(), (int)(maxLastProd * needed.getFloatValue()) - currentStockNumber));
//			logln("1indus "+myIndus.getName()+" want to buy "+quantityBuy+" "+needed.getKey().getName()
//					+"( "+maxLastProd+" * "+needed.getFloatValue()+" - "+currentStockNumber+")");
			long price = market.getPriceBuyFromMarket(nbDays);
			if(quantityBuy * price > moneyToSpend){
//				logln("1indus "+myIndus.getName()+" can't buy more than "+(moneyToSpend/price)+" "+needed.getKey().getName()+" @"+price+"€ ("+moneyToSpend+")");
				quantityBuy = (long)(moneyToSpend / price);
			}
			//buy
			moneyToSpend  -= super.storeProductFromMarket(needed.getKey(), quantityBuy, nbDays);
//			moneyToSpend  -= quantityBuy * price;
//			prv.addMoney(quantityBuy * price);
//			logln("1indus "+myIndus.getName()+" buy "+quantityBuy+" "+needed.getKey().getName());
//			market.addNbConsumePerDay(quantityBuy / (float)nbDays);
//			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
//			market.addStock( -quantityBuy);
			
			//update rawgoodCost
			rawGoodCost += price * needed.getFloatValue();
		}
		myIndus.setRawGoodsCost(rawGoodCost);
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
			long currentStockNumber = currentStock.getLong(needed.getKey());
			ProvinceGoods market = goodStock.get(needed.getKey());
			long quantityBuy = Math.max(0, Math.min(market.getStock(), (int)(maxLastProd * needed.getFloatValue()) - currentStockNumber));
//			logln("2indus "+myIndus.getName()+" want to buy "+quantityBuy+" "+needed.getKey().getName()
//					+"( "+maxLastProd+" * "+needed.getFloatValue()+" - "+currentStockNumber+")");
			long price = market.getPriceBuyFromMarket(nbDays);
			if(quantityBuy * price > moneyToSpend){
//				logln("2indus "+myIndus.getName()+" can't buy more than "+(moneyToSpend/price)+" "+needed.getKey().getName()+" @"+price+"€ ("+moneyToSpend+")");
				quantityBuy = (moneyToSpend / price);
			}
			//buy
			moneyToSpend  -= super.storeProductFromMarket(needed.getKey(), quantityBuy, nbDays);
//			moneyToSpend  -= quantityBuy * price;
//			if(moneyToSpend <0){
//				System.err.println("error "+moneyToSpend+"<0");
//			}
//			prv.addMoney(quantityBuy * price);
//			logln("2indus "+myIndus.getName()+" buy "+quantityBuy+" "+needed.getKey().getName());
//			if(quantityBuy<0){
//				logln(moneyToSpend+" "+market.getStock()+" , "+( (int)(maxLastProd * needed.getFloatValue()) - currentStockNumber));
//			}
//			market.addNbConsumePerDay(quantityBuy / (float)nbDays);
//			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
//			market.addStock( -quantityBuy);
		}
		//then buy some more stock (for double prod)
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
//			long currentStockNumber = currentStock.getLong(needed.getKey());
			ProvinceGoods market = goodStock.get(needed.getKey());
			long quantityBuy = Math.max(0, Math.min(market.getStock(), (int)(maxLastProd * needed.getFloatValue())));
//			logln("3indus "+myIndus.getName()+" want to buy "+quantityBuy+" "+needed.getKey().getName()
//					+"( "+maxLastProd+" * "+needed.getFloatValue()+")");
			long price = market.getPriceBuyFromMarket(nbDays);
			if(quantityBuy * price > moneyToSpend){
//				logln("3indus "+myIndus.getName()+" can't buy more than "+(moneyToSpend/price)+" "+needed.getKey().getName()+" @"+price+"€ ("+moneyToSpend+")");
				quantityBuy = (long)(moneyToSpend / price);
			}
			//buy
			moneyToSpend  -= super.storeProductFromMarket(needed.getKey(), quantityBuy, nbDays);
//			moneyToSpend  -= quantityBuy * price;
//			prv.addMoney(quantityBuy * price);
//			logln("3indus "+myIndus.getName()+" buy "+quantityBuy+" "+needed.getKey().getName());
//			market.addNbConsumePerDay(quantityBuy / (float)nbDays);
//			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
//			market.addStock( -quantityBuy);
		}
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
//			long currentStockNumber = currentStock.getLong(needed.getKey());
			ProvinceGoods market = goodStock.get(needed.getKey());
			long quantityBuy = Math.max(0, Math.min(market.getStock(), (int)(maxLastProd * needed.getFloatValue()*0.5)));
			long price = market.getPriceBuyFromMarket(nbDays);
			if(quantityBuy * price > moneyToSpend){
//				logln("4indus "+myIndus.getName()+" can't buy more than "+(moneyToSpend/price)+" "+needed.getKey().getName()+" @"+price+"€ ("+moneyToSpend+")");
				quantityBuy = (long)(moneyToSpend / price);
			}
			//buy
			moneyToSpend  -= super.storeProductFromMarket(needed.getKey(), quantityBuy, nbDays);
//			moneyToSpend  -= quantityBuy * price;
//			prv.addMoney(quantityBuy * price);
//			logln("4indus "+myIndus.getName()+" buy "+quantityBuy+" "+needed.getKey().getName());
//			market.addNbConsumePerDay(quantityBuy / (float)nbDays);
//			currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
//			market.addStock( -quantityBuy);
		}
		

		if (myIndus.getMoney() < 0)
			System.err.println("Error, in industral money buy: "+myIndus+" now has "+myIndus.getMoney()+"€");
		
		return maxMoney - moneyToSpend;
	}

	
	public long useGoodsAndTools(ProvinceIndustry stock, long quantity, int nbDays){
		
		long realProd = quantity;
		//restrict production to the max available from raw materials
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			realProd = Math.min(realProd, (int)(stock.getStock().getLong(needed.getKey())/needed.getFloatValue()));
		}
		//remove used raw material
		for(Entry<Good> needed : rawNeeded.object2FloatEntrySet()){
			stock.getStock().put(needed.getKey(), stock.getStock().getLong(needed.getKey()) - (long)(needed.getFloatValue()* realProd));
		}

		// break some tools (0.5% of tools break per use per day)
		for(Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
			stock.getStock().put(needed.getKey(), (long)(
					Math.min(stock.getStock().getLong(needed.getKey()), realProd) * 0.005f * nbDays * needed.getFloatValue())  );
		}
		
		return realProd;
	}
	
	
	
}
