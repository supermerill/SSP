package remi.ssp.economy;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.GlobalDefines;
import remi.ssp.politic.Province;

public class ProvinceIndustry implements Job{
	
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
	public void setPreviousProduction(long previousProduction) { this.previousProduction = previousProduction; }
	public double getPreviousSalary() { return previousSalary; }
	public void setPreviousSalary(double previousSalary) { this.previousSalary = previousSalary; }
	

	@Override
	public float wantToFire(Province prv, long nbEmployed, int nbDays) {
		ProvinceGoods prvGood = prv.getStock().get(industry.createThis);
		//TODO: logs to understand why agri overproductino isn't fired
//		GlobalDefines.plog(", \"WillfireOverproduction_"+industry.createThis+"_"+nbEmployed+"\":\""+prvGood.getStockConsolidated()+" > "+(prvGood.getNbConsumePerDayConsolidated() * industry.createThis.getOptimalNbDayStock()));
//		GlobalDefines.plogln(" && "+(prvGood.getNbConsumeThisPeriod()+prvGood.getNbConsumePerDayConsolidated()*nbDays)+" < "+prvGood.getNbProduceThisPeriod()+prvGood.getNbProduceThisPeriod()*nbDays+"\"");
		//if too much stock and overproduction
		if(prvGood.getStockConsolidated() > prvGood.getNbConsumePerDayConsolidated() * industry.createThis.getOptimalNbDayStock()
				&& prvGood.getNbConsumeThisPeriod()+prvGood.getNbConsumePerDayConsolidated()*nbDays < prvGood.getNbProduceThisPeriod()+prvGood.getNbProduceThisPeriod()*nbDays){
			//then fire enough to remove overproduction *2 (max 10%)
			double ratioFire = 1 - (double)(prvGood.getNbConsumeThisPeriod()+prvGood.getNbConsumePerDayConsolidated()*nbDays) / (double)(prvGood.getNbProduceThisPeriod()+prvGood.getNbProduceThisPeriod()*nbDays);
			long nbFire = (long) (nbEmployed * ratioFire);
			nbFire = 1 + nbFire*2;
			nbFire = Math.min(nbFire, Math.max(1, nbEmployed/10));
//			GlobalDefines.plogln(", \"fireOverproduction_"+industry.createThis+"_"+nbEmployed+"\":"+nbFire);
			return nbFire;
		}
		return 0;
	}
	
	
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
