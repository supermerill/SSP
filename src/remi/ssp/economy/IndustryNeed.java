package remi.ssp.economy;

import java.util.HashMap;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import remi.ssp.economy.PopNeed.PopNeedFactoryStorage;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

/**
 * A Pop want:
 * Food
 * House
 * Goods
 * 
 * An industry may want:
 * Tools
 * Factory
 * Boats
 * 
 * 
 * @author Admin
 *
 */
public abstract class IndustryNeed extends Needs{

	public static interface IndustryNeedFactory{ IndustryNeed create(ProvinceIndustry pop); }

	public static class IndustryNeedsFactoryStorage{
		public static HashMap<String, IndustryNeedFactory> indusNeedList = new HashMap<>();
		public static IndustryNeedFactory get(String name) { return indusNeedList.get(name); }
		public static void put(String name, IndustryNeedFactory obj) { indusNeedList.put(name, obj); }
	}
	public static IndustryNeed create(String name, ProvinceIndustry prvIndus){
		return IndustryNeedsFactoryStorage.get(name).create(prvIndus);
	}
	
	protected ProvinceIndustry myIndus;
	
	public IndustryNeed(ProvinceIndustry prvIndus){
		myIndus = prvIndus;
	}
	
	/**
	 * !! unprotected !! be sure the quantity and money are available before calling.
	 * @param prv
	 * @param good
	 * @param indus
	 * @param quantity
	 * @param nbDays
	 * @return money used
	 */
	protected long storeProductFromMarket(Good good, long quantity, int nbDays){
		Province prv = myIndus.getProvince();
		long price = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
		long moneyExch = price*quantity;
		prv.addMoney(moneyExch);
//		prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
		prv.getStock().get(good).addStock(-quantity);
		myIndus.addMoney(-moneyExch);
		myIndus.getStock().put(good, quantity + myIndus.getStock().getLong(good));
		return moneyExch;
	}
}
