package remi.ssp.economy;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

import remi.ssp.GlobalDefines;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.LongInterval;

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
	 * Redefine this method to limit the number of people that can be hire here.
	 * This implementation does nothing interesting.
	 * @param pop 
	 * @return number of maximum new workers this industry can accept this period
	 * @Deprecated not used yet (i think)
	 */
	public LongInterval needHire(LongInterval toReturn, ProvinceIndustry provinceIndustry, Pop pop, int nbDays)
	{ if(toReturn==null)toReturn=new LongInterval(0, 0); return toReturn.set(DEF_INTER); }
	

	/**
	 * Redefine this method to set a minimum number of people that should be fired here.
	 * This base implementation will fire a minimum number of people if it detect an overproduction.
	 * @param provinceIndustry 
	 * @return number of minimum workers this industry should fire
	 */
	public LongInterval needFire(LongInterval toReturn, ProvinceIndustry indus, Pop pop, int nbDays)
	{
		if(toReturn==null)toReturn=new LongInterval(0, 0);
		toReturn.set(DEF_INTER); 
		Province prv = indus.getProvince();
		long nbEmployed = pop.getNbMensEmployed(indus);
		ProvinceGoods prvGood = prv.getStock().get(createThis);
		//TODO: logs to understand why agri overproductino isn't fired
		GlobalDefines.log(", \"WillfireOverproduction_"+createThis+"_"+nbEmployed+"\":\""+prvGood.getStockConsolidated()
		+" > "+(prvGood.getNbConsumePerDayConsolidated() * createThis.getOptimalNbDayStock()));
		GlobalDefines.logln(" && "+(prvGood.getNbConsumeThisPeriod()+prvGood.getNbConsumePerDayConsolidated()*nbDays)+" < "+prvGood.getNbProduceThisPeriod()+prvGood.getNbProduceThisPeriod()*nbDays+"\"");
		//if too much stock and overproduction
		if(prvGood.getStockConsolidated() > prvGood.getNbConsumePerDayConsolidated() * createThis.getOptimalNbDayStock()
				&& prvGood.getNbConsumeThisPeriod()+prvGood.getNbConsumePerDayConsolidated()*nbDays < prvGood.getNbProduceThisPeriod()+prvGood.getNbProduceThisPeriod()*nbDays){
			//then fire enough to remove overproduction *2 (max 10%)
			double ratioFire = 1 - (double)(prvGood.getNbConsumeThisPeriod()+prvGood.getNbConsumePerDayConsolidated()*nbDays) / (double)(prvGood.getNbProduceThisPeriod()+prvGood.getNbProduceThisPeriod()*nbDays);
			long nbFire = (long) (nbEmployed * ratioFire);
			nbFire = 1 + nbFire*2;
			nbFire = Math.min(nbFire, Math.max(1, nbEmployed/10));
			GlobalDefines.logln(", \"fireOverproduction_"+createThis+"_"+nbEmployed+"\":"+nbFire);
			return toReturn.set(nbFire, Long.MAX_VALUE);
		}
		return toReturn;
	}

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
	
	
	protected static final LongInterval DEF_INTER = new LongInterval(0, Long.MAX_VALUE);
}
