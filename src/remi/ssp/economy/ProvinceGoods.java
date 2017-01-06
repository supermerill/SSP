package remi.ssp.economy;

import remi.ssp.Province;

public class ProvinceGoods {
//	
//	protected Province prv;
//	protected Good good;
	
	public int stock=0;
	public int price=0;
	public int nbConsumePerDay=0; // BFR, we need to keep this as stock, so choose the price accordingly
	
	//TODO "demande" pour permettre aux pop et à l'etat de demander aux marchants d'apporter des trucs
	
	
	public ProvinceGoods() {
		super();
	}
//	public ProvinceGoods(Province prv, Good good, int stock, int price) {
//		super();
//		this.prv = prv;
//		this.good = good;
//		this.stock = stock;
//		this.price = price;
//	}
	public ProvinceGoods(ProvinceGoods value) {
		super();
//		this.prv = value.prv;
//		this.good = value.good;
		this.stock = value.stock;
		this.price = value.price;
	}
	
	public int getPriceSellToMarket(Province prv, int durationInDay){
		float coeff = prv.getMoney() / (durationInDay*prv.getMoneyChangePerDay());
		coeff = ( 0.2f / (1+ coeff*coeff) );
		return (int)( price * (1-coeff) );
	}
	
	public int getPriceBuyFromMarket(Province prv, int durationInDay){
		float coeff = prv.getMoney() / (durationInDay*prv.getMoneyChangePerDay());
		coeff = ( 0.2f / (1+ coeff*coeff) );
		return (int)( price * (1+coeff) );
	}
	
}
