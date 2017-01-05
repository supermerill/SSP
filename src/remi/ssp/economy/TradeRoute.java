package remi.ssp.economy;

import remi.ssp.Province;

/**
 * A trade route between a source province and a destination
 * 
 * 
 * @author Admin
 *
 */
public class TradeRoute {
	Province source;
	Province destination;
	
	boolean isWater = false; //for sea and canals liaisons
	
	//TODO: extension: previous share of market to keep track of market share, and modify it by opinion and profit over time.
	
	
	public Province getOtherEnd(Province me){
		if(source == me) return destination;
		return source;
	}
	
	//note: this is "duplicated" for the two merchant province force
	//note2: must review the numbers before release
	public int getMaxMerchant(){
		if(isWater){
			//sea can handle many boats
			//TODO: use the level of ports in provinces.
			return 0;
		}else{
			//minimum amount of intrepid explorers (high for testing purpose)
			float capacity = 100;
			//route is good
			capacity += source.getRoutes() * 10;
			//rail is excelent
			capacity += Math.min(source.getRail(), 100)*1000;
			//mountain are hard
			capacity *= (1-source.relief);
			return (int)capacity;
		}
	}


	public boolean isBoatTR() { return isWater; }
	
	
	
}
