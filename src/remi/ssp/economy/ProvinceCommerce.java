package remi.ssp.economy;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import remi.ssp.politic.Province;
import remi.ssp.utils.ComparatorValueDesc;

public class ProvinceCommerce implements Job{

	Province province;
	
	float efficiency;
	
	// used to run
	Object2LongMap<Good> stock = new Object2LongOpenHashMap<Good>(); //raw goods + tools
	long money;//bfr
	long previousSalary;
	
	String name;
	public String getName() { return name; }
	public ProvinceCommerce(String name){ this.name = name; }
	
	public Province getProvince() { return province; }
	public void setProvince(Province province) { this.province = province; }
	public long getMoney() { return money; }
	public void addMoney(long money) { this.money += money; }
	public void setMoney(long money) { this.money = money; }
	public Object2LongMap<Good> getStock() { return stock; }
	public long getPreviousSalary() { return previousSalary; }
	public void setPreviousSalary(long previousSalary) { this.previousSalary = previousSalary; }
	public void addToPreviousSalary(long newIncome) { this.previousSalary += newIncome; }
	
	/**
	 * 
	 * @param nbMerchants all  merchant inside this commerce "guild"
	 * @return the capacity in transportUnit (~liter)
	 */
	public long computeCapacity(final long nbMerchants) {
		//get goods
		ArrayList<Good> sortedGoods = new ArrayList<Good>(stock.keySet());
		sortedGoods.sort((o0,o1) -> - Long.compare(stock.getLong(o0), stock.getLong(o1)));
		long capacity = nbMerchants;
		long remainingMechants = nbMerchants;
		for(Good cart : sortedGoods){
			final long nbGoods = stock.getLong(cart);
			capacity += Math.min(nbGoods, remainingMechants) * cart.commerceCapacityPerMen;
			if(nbGoods >= remainingMechants){
				remainingMechants = 0;
				break;
			}else{
				remainingMechants -= nbGoods;
			}
			if(cart.commerceCapacityPerMen == 0){
				break;
			}
			
		}
		
		return (long)(capacity * efficiency);
	}

	public void load(JsonObject jsonObj, Province prv){
		this.province = prv;
		efficiency = (float)jsonObj.getJsonNumber("eff").doubleValue();
		money = jsonObj.getJsonNumber("money").longValue();
		previousSalary = jsonObj.getJsonNumber("salary").longValue();
		JsonArray array = jsonObj.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getJsonNumber("nb").longValue());
		}
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("eff", efficiency);
		jsonOut.add("money", money);
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
