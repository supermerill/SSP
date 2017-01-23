package remi.ssp.economy;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Province;

public class ProvinceGoods {
//	
//	protected Province prv;
//	protected Good good;
	
	protected int stock=0;
	protected int price=0;
	protected float nbConsumePerDay=0; // BFR, we need to keep this as stock, so choose the price accordingly
	
	//TODO "demande" pour permettre aux pop et à l'etat de demander aux marchants d'apporter des trucs
	
	
	
	
	public ProvinceGoods() { super(); }
	public int getStock() { return stock; }
	public int getPrice() { return price; }
	public float getNbConsumePerDay() { return nbConsumePerDay; }
	public void setPrice(int price) { this.price = price; }
	public void setStock(int stock) { this.stock = stock; }
	public void setNbConsumePerDay(float nbConsumePerDay) { this.nbConsumePerDay = nbConsumePerDay; }
//	public void addPrice(int price) { this.price += price; }
	public void addStock(int stock) { this.stock += stock; }
	public void addNbConsumePerDay(float nbConsumePerDay) { this.nbConsumePerDay += nbConsumePerDay; }
	
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
		float coeff = prv.getMoney() / (durationInDay*(1+prv.getMoneyChangePerDay()));
		coeff = ( 0.2f / (1+ coeff*coeff) );
		return (int)( price * (1-coeff) );
	}
	
	public int getPriceBuyFromMarket(Province prv, int durationInDay){
		float coeff = prv.getMoney() / (durationInDay*(1+prv.getMoneyChangePerDay()));
		coeff = ( 0.2f / (1+ coeff*coeff) );
		return (int)( price * (1+coeff) );
	}
	
	public void load(JsonObject jsonObj){
		stock = jsonObj.getInt("stock");
		price = jsonObj.getInt("price");
		nbConsumePerDay = jsonObj.getInt("nb");
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("stock", stock);
		jsonOut.add("price", price);
		jsonOut.add("nb", nbConsumePerDay);
	}
	
}
