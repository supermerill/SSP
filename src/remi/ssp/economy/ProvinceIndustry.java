package remi.ssp.economy;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.network.SimpleSerializable;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.LongInterval;

public class ProvinceIndustry implements Job, SimpleSerializable{
	
	public static class ProvinceIndustryFactory{
		public static ProvinceIndustryFactory creator = new ProvinceIndustryFactory();
		protected ProvinceIndustry prvIndus;
		public ProvinceIndustryFactory(){ create(); }
		public ProvinceIndustry create(){ ProvinceIndustry ret = prvIndus; prvIndus =  new ProvinceIndustry(); return ret; }
		public ProvinceIndustryFactory setProvince(Province prv){ prvIndus.setProvince(prv); return this; };
		public ProvinceIndustryFactory setInustry(Industry indus){ 
			prvIndus.setIndustry(indus); 
			prvIndus.need = indus.getMyNeedsFactory().apply(prvIndus); 
			return this; };
	}
	
	Province province;
	Industry industry;
	IndustryNeed need;
	public String getName(){return industry.getName();}
	public String toString(){return industry.toString();}
	
//	int needs;
//	int offer;
//	int price;
	
	//int efficiency; //TODO : number of people that work optimally (hard to boostrap industry)

	
	// used to run
	Object2LongMap<Good> stock = new Object2LongOpenHashMap<Good>(); //raw goods + tools
	long money=0;//bfr
	long rawGoodsCost=0; // rawgoods + depreciation of owned stock
	long previousProduction=0;
	double previousSalary=1;
	
	public Province getProvince() { return province; }
	public void setProvince(Province province) { this.province = province; }
	public Industry getIndustry() { return industry; }
	public void setIndustry(Industry industry) { this.industry = industry; }
	public IndustryNeed getNeed() { return need; }
//	public int getNeeds() { return needs; }
//	public void setNeeds(int needs) { this.needs = needs; }
//	public int getOffer() { return offer; }
//	public void setOffer(int offer) { this.offer = offer; }
//	public int getPrice() { return price; }
//	public void setPrice(int price) { this.price = price; }
//	public int getEfficiency() { return efficiency; }
//	public void setEfficiency(int efficiency) { this.efficiency = efficiency; }
	public long getMoney() { return money; }
	public void addMoney(long moneyAdd) { 
		this.money += moneyAdd; 
		if(money<0){
			System.err.println("Error, now indus has no money");
		}
	}
//	public void setMoney(long money) { this.money = money; }
	public long getRawGoodsCost() { return rawGoodsCost; }
	public void setRawGoodsCost(long rawGoodsCost) { this.rawGoodsCost = rawGoodsCost; }
	public Object2LongMap<Good> getStock() { return stock; }
	public long getPreviousProduction() { return previousProduction; }
	public void setPreviousProduction(long previousProduction) { this.previousProduction = previousProduction; 
	if(previousProduction<0){
		System.err.println("Error, negative production");
	}
	}
	public double getPreviousSalary() { return previousSalary; }
	public void setPreviousSalary(double previousSalary) { this.previousSalary = previousSalary; 
	if(previousSalary<0 || previousSalary>10000000){
		System.err.println("Error, negative previousSalary");
	}
	}
	
	//-----------------------------------------------
	

	// TODO; save this
	double previousConsumptionPD = 0;
	double currentConsumptionPD = 0;
	long currentProvinceStock = 0;
	long currentStock = 0;
	long previousEmployes = 0;
	double previousPrice = 0;
	double previousGain = 0;
	long buyableStock = 0;
	long previousBuyableStock = 0;
	public double getPreviousPrice() { return previousPrice; }
	public void setPreviousPrice(double previousPrice) { this.previousPrice = previousPrice; }
	public double getPreviousGain() { return previousGain; }
	public void setPreviousGain(double previousGain) { this.previousGain = previousGain; }
	public long getBuyableStock() { return buyableStock; }
	public void setBuyableStock(long buyableStock) { this.buyableStock = buyableStock; }
	
	 /**
	  *  -100 : crack; -10: need to fire some people, it's not good; 0: ok; 10: need a bit more people; 100: need many more people, business is flourishing
	  */
	//int industryHealth = 0;

	public long getPreviousEmployes() { return previousEmployes; }
	public long getEmployes() {
		long nbEmp = 0;
		for(Pop pop : province.getPops()){
			nbEmp += pop.getNbMensEmployed(this);
		}
		return nbEmp;
	}
	public void updateEmpoyes() { previousEmployes = getEmployes(); }

	/**
	 * TODO: redo it with industry's stock
	 * Compute the consumption of produced goods. Call this after a produce().
	 * @param durationInDay used as duration since last update (to see the evolve of stock)
	 * @param newProvinceStock new stock in the province for my produced good at this instant.
	 * @param nbProduced i use this to do setPreviousProduction with it
	 */
	public void updateStock(int durationInDay, long newProvinceStock, long nbProduced){
		setPreviousProduction(nbProduced);
		buyableStock += nbProduced;
		if(buyableStock!=getStock().getLong(industry.createThis) && !industry.createThis.getName().contains("wood_go")){
			System.err.println("Error, different number of thing");
		}
//		long previousProvinceStock = currentProvinceStock;
//		currentProvinceStock = newProvinceStock;
//		double newConsumptionPD = getPreviousProduction() + (previousProvinceStock - currentProvinceStock) / (double) durationInDay;

		long previousStock = currentStock;
		currentStock = buyableStock;
		double newConsumptionPD = getPreviousProduction() + (previousStock - currentStock) / (double) durationInDay;
		
		previousConsumptionPD = currentConsumptionPD;
		currentConsumptionPD = newConsumptionPD;
	}
	
	/**
	 * @param durationInDay used as duration need for stock.
	 * @return estimation of max number of goods to produce to optimize the production.
	 */
	public long getMaxToCreateAndUpdate(int durationInDay) {
		long maxToCreate = Long.MAX_VALUE;
		// check how many consumed since last time
		if (currentProvinceStock > 0) {
			if (currentConsumptionPD > previousConsumptionPD) {
				maxToCreate = (long) ((currentConsumptionPD * 2 - previousConsumptionPD) * durationInDay);
			} else {
				// limit to not create too much if consumptino is low
				if (currentProvinceStock > currentConsumptionPD * durationInDay * 3) {
					maxToCreate = (long) (currentConsumptionPD * (currentConsumptionPD * durationInDay * 3) / currentProvinceStock);
				} else {
					maxToCreate = (long) (currentConsumptionPD * durationInDay);
				}
			}
		}
		return maxToCreate;
	}

	@Override
	public LongInterval needFire(LongInterval toReturn, Province prv, Pop pop, int nbDays) {
		return industry.needFire(toReturn, this, pop, nbDays);
	}

	@Override
	public LongInterval needHire(LongInterval toReturn, Province prv, Pop pop, int nbDays) {
		return industry.needHire(toReturn, this, pop, nbDays);
	}
	
	/**
	 * 
	 * @param wish
	 * @return price per element
	 */
	public double getPrice(long wish) {
		
		// --- compute fixed cost: salary
		
		//check salary
		double salaryNeeded = getPreviousSalary();
		
		//do we have more or less salarymens?
//		long empoyesDiff = getPreviousEmployes() - getEmployes();
		
		//do we want more or less ?
		//if stock is growing -> less (-price)
		//if stock is depleting -> more (+price)
		
		//check how many mens we want
		long currentMens = this.getEmployes();
		// if we don't produce enough, ask for more money
		// if we overproduce, ask for less money.
		double ratio = (wish/ (double)previousProduction);
		salaryNeeded *= ratio;
		
		long fixedPrice = (long) (currentMens * salaryNeeded / wish);
		
		//if consumption is bigger than production -> increase price
		//if consumption is lower than production -> lower price
		
		// --- compute variable cost: goods
		
		long variablePrice = this.rawGoodsCost;

		if(fixedPrice<variablePrice*0.25){
			//set a minimum for the salary
			fixedPrice = variablePrice / 4;
		}
//		if(variablePrice<fixedPrice*0.2){
//			//set a minimum for the tools & raw goods
//			variablePrice = fixedPrice / 5;
//		}
		
		return fixedPrice + variablePrice;
	}
	
	public void buyGood(Good good, long nbBuy, long totalPrice) {
		addMoney(totalPrice);
		previousGain = totalPrice;
		long nbGoods = getStock().getLong(good);
		System.out.print(",\"buy"+good+"\":{\"nb\":"+nbBuy+",\"bstok\":"+buyableStock+",\"realstock\":"+nbGoods);
		getStock().put(good, nbGoods-nbBuy);
		if(getStock().getLong(good)<0){
			System.err.println("Error, negative number of thing");
		}
		buyableStock -= nbBuy;
		System.out.println("}");
		if(buyableStock>getStock().getLong(good)){
			System.err.println("Error, negative number of thing");
		}
	}
	
	//--------------------------------------------------

	
	
	public void load(JsonObject jsonObj){
		money = jsonObj.getJsonNumber("money").longValue();
		rawGoodsCost = jsonObj.getJsonNumber("cost").longValue();
		previousProduction = jsonObj.getJsonNumber("prod").longValue();
		previousSalary = jsonObj.getJsonNumber("salary").longValue();
		JsonArray array = jsonObj.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getJsonNumber("nb").longValue());
		}
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("money", money);
		jsonOut.add("cost", rawGoodsCost);
		jsonOut.add("prod", previousProduction);
		jsonOut.add("salary", previousSalary);
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Entry<Good> good : stock.object2LongEntrySet()){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getLongValue());
			array.add(objectBuilder);
		}
		jsonOut.add("stock", array);
	}
}
