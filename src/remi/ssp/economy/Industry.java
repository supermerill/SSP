package remi.ssp.economy;

import java.util.Collection;
import java.util.HashMap;

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
	protected Needs myNeeds = null; // ask for raw goods and tools.
	
	public String getName(){return getClass().getName();}
	@Override public String toString() { return getName(); }
	
	//produce goods, by consuming needed things ( people are payedd in economy plugin)
	// !!! you muist update setRawGoodsCost
	public abstract long produce(ProvinceIndustry indus, Collection<Pop> pop, int durationInDay);


	public Good getGood() { return createThis; }
	public Needs getMyNeeds() { return myNeeds; }
	
	//one by economy
	//	public abstract int setPrice(Province prv); // from offre/demande avec le stock disponible

	//done by economy
	//	public abstract void checkImport(Province prv); // si moins cher à coté, importer pour augmenter le stock

	//done by needs
	//	public abstract void consume(Province prv, Object2IntMap<Pop> alreadyUsed); // le habitants peuvent dépenser de l'argnet pour acheter ces biens (selon le fric qu'il leur reste

	
	protected void sellProductToMarket(Province prv, long quantity, int nbDays){
		long price = prv.getStock().get(createThis).getPriceSellToMarket(prv, nbDays);
		
		prv.addMoney(-price*quantity);
		prv.getStock().get(createThis).addStock(quantity);
		prv.getIndustry(this).addMoney(price*quantity);
		
	}
	
}
