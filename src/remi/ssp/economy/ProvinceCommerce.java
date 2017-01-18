package remi.ssp.economy;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import remi.ssp.politic.Province;
import remi.ssp.utils.ComparatorValueDesc;

public class ProvinceCommerce implements Job{

	Province province;
	
	float efficiency;
	
	// used to run
	Object2IntMap<Good> stock = new Object2IntOpenHashMap<Good>(); //raw goods + tools
	int money;//bfr
	int previousSalary;
	
	String name;
	public String getName() { return name; }
	public ProvinceCommerce(String name){ this.name = name; }
	
	public Province getProvince() { return province; }
	public void setProvince(Province province) { this.province = province; }
	public int getMoney() { return money; }
	public void addMoney(int money) { this.money += money; }
	public void setMoney(int money) { this.money = money; }
	public Object2IntMap<Good> getStock() { return stock; }
	public int getPreviousSalary() { return previousSalary; }
	public void setPreviousSalary(int previousSalary) { this.previousSalary = previousSalary; }
	public void addToPreviousSalary(int newIncome) { this.previousSalary += newIncome; }
	
	/**
	 * 
	 * @param nbMerchants all  merchant inside this commerce "guild"
	 * @return the capacity in transportUnit (~liter)
	 */
	public int computeCapacity(final int nbMerchants) {
		//get goods
		ArrayList<Good> sortedGoods = new ArrayList<Good>(stock.keySet());
		sortedGoods.sort(new ComparatorValueDesc<>(stock));
		int capacity = nbMerchants;
		int remainingMechants = nbMerchants;
		for(Good cart : sortedGoods){
			final int nbGoods = stock.getInt(cart);
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
		
		return (int)(capacity * efficiency);
	}

	public void load(JsonObject jsonObj, Province prv){
		this.province = prv;
		efficiency = (float)jsonObj.getJsonNumber("eff").doubleValue();
		money = jsonObj.getInt("money");
		previousSalary = jsonObj.getInt("salary");
		JsonArray array = jsonObj.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getInt("nb"));
		}
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("eff", efficiency);
		jsonOut.add("money", money);
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
