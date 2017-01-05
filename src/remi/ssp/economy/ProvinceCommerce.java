package remi.ssp.economy;

import java.util.ArrayList;
import java.util.Comparator;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.Province;
import remi.ssp.utils.ComparatorValueDesc;

public class ProvinceCommerce {

	Province province;
	
	float efficiency;
	
	// used to run
	Object2IntMap<Good> stock = new Object2IntOpenHashMap<Good>(); //raw goods + tools
	int money;//bfr
	int previousSalary;
	
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
}
