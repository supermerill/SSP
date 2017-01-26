package remi.ssp.economy;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Province;

public class ProvinceGoods {
//	
//	protected Province prv;
//	protected Good good;
	
	protected long stock=0;
	protected long price=10;
	protected float nbConsumePerDay=0; // BFR, we need to keep this as stock, so choose the price accordingly

	protected long previousPrice=0;
	
	//TODO "demande" pour permettre aux pop et à l'etat de demander aux marchants d'apporter des trucs
	
	
	
	
	public ProvinceGoods() { super(); price = 10; stock = 0;}
	public long getStock() { return stock; }
	public long getPrice() { return price; }
	public float getNbConsumePerDay() { return nbConsumePerDay; }
	public void setPrice(long price) { this.price = price; }
	public void setStock(long stock) { this.stock = stock; }
	public void setNbConsumePerDay(float nbConsumePerDay) { this.nbConsumePerDay = nbConsumePerDay; }
//	public void addPrice(int price) { this.price += price; }
	public void addStock(long stock) { this.stock += stock; }
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
	
	public long getPriceSellToMarket(Province prv, int durationInDay){
		double coeff = prv.getPreviousMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDay()/4));
		coeff = ( 0.5f / (1+ coeff*coeff) );
//		System.out.println("price to sell me :"+( price * (1-coeff) )+" / "+price);
		//if stock is high and i don't have money, lower this even more
		if(prv.getMoney()<0 && stock > 100*nbConsumePerDay){
			return (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoney())) );
		}else
			return (long)( price * (1-coeff) );
	}
	
	public long getPriceBuyFromMarket(Province prv, int durationInDay){
		double coeff = prv.getPreviousMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDay()/4));
		coeff = ( 0.2f / (1+ coeff*coeff) );
//		System.out.println("price to buy me :"+( price * (1+coeff) )+" / "+price+"    ("+prv.getMoney()+"/"
//		+(durationInDay*(1.0+prv.getMoneyChangePerDay()))+" = "+(prv.getMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDay())))
//				+" => "+coeff);
		return (long)( price * (1+coeff) );
	}
	
	public void load(JsonObject jsonObj){
		stock = jsonObj.getInt("stock");
		price = jsonObj.getJsonNumber("price").longValue();
		nbConsumePerDay = jsonObj.getInt("nb");
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("stock", stock);
		jsonOut.add("price", price);
		jsonOut.add("nb", nbConsumePerDay);
	}
	
}
