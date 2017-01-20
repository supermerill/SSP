package remi.ssp.economy;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.politic.Province;

public class ProvinceIndustry implements Job{
	
	public static class ProvinceIndustryFactory{
		public static ProvinceIndustryFactory creator = new ProvinceIndustryFactory();
		protected ProvinceIndustry prvIndus = new ProvinceIndustry();
		public ProvinceIndustry create(){ ProvinceIndustry ret = prvIndus; prvIndus =  new ProvinceIndustry(); return ret; }
		public ProvinceIndustryFactory setProvince(Province prv){ prvIndus.setProvince(prv); return this; };
		public ProvinceIndustryFactory setInustry(Industry prv){ prvIndus.setIndustry(prv); return this; };
	}
	
	Province province;
	Industry industry;
	public String getName(){return industry.getName();}
	
//	int needs;
//	int offer;
//	int price;
	
	//int efficiency; //TODO : number of people that work optimally (hard to boostrap industry)

	
	// used to run
	Object2IntMap<Good> stock = new Object2IntOpenHashMap<Good>(); //raw goods + tools
	int money=0;//bfr
	int rawGoodsCost=1; // rawgoods + depreciation of owned stock
	int previousProduction=0;
	int previousSalary=1;
	
	public Province getProvince() { return province; }
	public void setProvince(Province province) { this.province = province; }
	public Industry getIndustry() { return industry; }
	public void setIndustry(Industry industry) { this.industry = industry; }
//	public int getNeeds() { return needs; }
//	public void setNeeds(int needs) { this.needs = needs; }
//	public int getOffer() { return offer; }
//	public void setOffer(int offer) { this.offer = offer; }
//	public int getPrice() { return price; }
//	public void setPrice(int price) { this.price = price; }
//	public int getEfficiency() { return efficiency; }
//	public void setEfficiency(int efficiency) { this.efficiency = efficiency; }
	public int getMoney() { return money; }
	public void addMoney(int money) { this.money += money; }
	public void setMoney(int money) { this.money = money; }
	public int getRawGoodsCost() { return rawGoodsCost; }
	public void setRawGoodsCost(int rawGoodsCost) { this.rawGoodsCost = rawGoodsCost; }
	public Object2IntMap<Good> getStock() { return stock; }
	public int getPreviousProduction() { return previousProduction; }
	public void setPreviousProduction(int previousProduction) { this.previousProduction = previousProduction; }
	public int getPreviousSalary() { return previousSalary; }
	public void setPreviousSalary(int previousSalary) { this.previousSalary = previousSalary; }
	
	
	public void load(JsonObject jsonObj){
		money = jsonObj.getInt("money");
		rawGoodsCost = jsonObj.getInt("cost");
		previousProduction = jsonObj.getInt("prod");
		previousSalary = jsonObj.getInt("salary");
		JsonArray array = jsonObj.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getInt("nb"));
		}
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("money", money);
		jsonOut.add("cost", rawGoodsCost);
		jsonOut.add("prod", previousProduction);
		jsonOut.add("salary", previousSalary);
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Entry<Good> good : stock.object2IntEntrySet()){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getIntValue());
			array.add(objectBuilder);
		}
		jsonOut.add("stock", array);
	}
}
