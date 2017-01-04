package remi.ssp.economy;

import remi.ssp.Province;

public class ProvinceGoods {
//	
//	protected Province prv;
//	protected Good good;
	
	public int stock;
	public int price;
	public int nbConsumePerDay; // BFR, we need to keep this as stock, so choose the price accordingly
	
	
	
	
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
	
}
