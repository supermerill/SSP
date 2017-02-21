package remi.ssp.economy;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

// unlocked by technologies
// then added to your pool of industry.
// provinceIndustry can then be upgraded via research or investment.
// the Industry class contains the logic and provinceIndustry the data (with province and pop).
public abstract class Industry {
	
	protected static HashMap<String, Industry> industryList = new HashMap<>();
	public static Industry get(String name) { return industryList.get(name); }
	public static void put(String name, Industry indus) { industryList.put(name, indus); }
	
	
	protected Good createThis = null; // we produce only one good for now, because it's easier to compute the profitability this way
	protected Function<ProvinceIndustry,IndustryNeed> myNeedsFactory = o->null; // ask for raw goods and tools.
	
	public String getName(){return getClass().getSimpleName();}
	@Override public String toString() { return getName(); }
	
	//produce goods, by consuming needed things ( people are payedd in economy plugin)
	// !!! you must update setRawGoodsCost (to be able compute the marge, the salary and the savings)
	public abstract long produce(ProvinceIndustry indus, Collection<Pop> pop, int durationInDay);
	
	/**
	 * Rediine this method to limit the number of people that can be hire here.
	 * @return number of maximum new workers this industry can accept this period
	 */
	public long needNewWorkers(int nbDays){ return Long.MAX_VALUE; }

	//TODO: canHire? canFire? canReopen?
	//public void recruit(province prv, Int2LongMap poptype2chomeur, int nbDays);
	//TODO: enhance produce to put here the process of thinking to "should i produce, with the current price?" ?
	//TODO: or maybe produce anyway... it's for "graiculture", that the current model of continuous flow is a bit flaw, and oscillate too much.

	public Good getGood() { return createThis; }
	public Function<ProvinceIndustry,IndustryNeed> getMyNeedsFactory() { return myNeedsFactory; }

	
	public long sellProductToMarket(Province prv, long quantity, int nbDays){
		long price = prv.getStock().get(createThis).getPriceSellToMarket(nbDays);
		price *= quantity;
		prv.addMoney(-price);
		prv.getStock().get(createThis).addStock(quantity);
		prv.getIndustry(this).addMoney(price);
		return price;
	}
	
}
