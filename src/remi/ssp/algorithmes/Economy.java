package remi.ssp.algorithmes;

import remi.ssp.economy.Good;
import remi.ssp.politic.Province;

public abstract class Economy {
	
	static public Economy ptr;
	

	public abstract void sellToMarket(Province prv, Good item, int quantity, int price, int durationInDay);
	
	public abstract void doTurn(Province prv, int durationInDay);

}
