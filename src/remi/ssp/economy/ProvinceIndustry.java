package remi.ssp.economy;

import java.util.HashMap;
import java.util.Map;

import remi.ssp.Province;

public class ProvinceIndustry {
	
	Province province;
	Industry industry;
	
//	int needs;
//	int offer;
//	int price;
	
	//int efficiency; //TODO : number of people that work optimally (hard to boostrap industry)

	
	// used to run
	Map<Good, Integer> stock=new HashMap<Good, Integer>(); //raw goods + tools
	int money;//bfr
	int rawGoodsCost; // rawgoods + depreciation of owned stock
	int previousProduction;
	int previousSalary;
	
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
	public Map<Good, Integer> getStock() { return stock; }
	public int getPreviousProduction() { return previousProduction; }
	public void setPreviousProduction(int previousProduction) { this.previousProduction = previousProduction; }
	public int getPreviousSalary() { return previousSalary; }
	public void setPreviousSalary(int previousSalary) { this.previousSalary = previousSalary; }
	
	
}
