package remi.ssp.economy;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Province;

public class ProvinceGoods {
//	
//	protected Province prv;
	protected Good good;
	
	protected long stock=0;
	protected long price=10;
	protected float nbConsumePerDay=0; // BFR, we need to keep this as stock, so choose the price accordingly
	protected float nbConsumePerDayConsolidated=0;
	protected float stockConsolidated=0;
	
	protected long previousPrice=0;
	
	//TODO "demande" pour permettre aux pop et à l'etat de demander aux marchants d'apporter des trucs
	
	
	
	
	public ProvinceGoods(Good good) { super(); this.good = good; price = 10; stock = 0;}
	public long getStock() { return stock; }
	public long getPrice() { return price; }
	public float getNbConsumePerDay() { return nbConsumePerDay; }
	public void setPrice(long price) { this.price = price; }
	public void setStock(long stock) { this.stock = stock; }
	public void setNbConsumePerDay(float nbConsumePerDay) { this.nbConsumePerDay = nbConsumePerDay; }
	public float getNbConsumePerDayConsolidated() { return nbConsumePerDayConsolidated; }
	public float getStockConsolidated() { return stockConsolidated; }
	public void updateNbConsumeConsolidated(int nbDays) { //should be called 1 time per tick
		int nbTicks = Math.max(1, (int) good.getOptimalNbDayStock()*10);
		nbDays = Math.min(nbDays, nbTicks/2);
		this.nbConsumePerDayConsolidated = (this.nbConsumePerDayConsolidated*(nbTicks-nbDays)/(float)(nbTicks)) + nbConsumePerDay/(float)(nbTicks); 
		this.stockConsolidated = (this.stockConsolidated*(nbTicks-nbDays)/(float)(nbTicks)) + stock/(float)(nbTicks); 
	}
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
		double coeff = Math.max(0.1,prv.getPreviousMoney()) / ((1.0+prv.getMoneyChangePerDayConsolidated()));
		coeff = ( 1f / (1+ coeff*coeff) );
//		System.out.println("price to sell me :"+( price * (1-coeff) )+" / "+price);
		//if stock is high and i don't have money, lower this even more
//		if(prv.getMoney()<0 && stock > 100*getMoneyChangePerDayConsolidated){
//			System.out.println("too much stock for ??, stop buy it! "+(long)( price * (1-coeff) ) + " => " + (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoney())) ));
//			return (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoneyconsolidated())) );
//		}else
		long buyPrice = (long)( price * (1-coeff) );
		//decrease even more the if the good has too many stock
		if(this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays < this.stockConsolidated){
			System.out.println("too much stock, reduce buy price more quickly to prevent banckrupcy of marketplace");
			buyPrice = (long)( buyPrice * ((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated) );
		}
		
		if(prv.getPreviousMoney()<0){
			buyPrice = 0; //can't buy
		}
			
		return buyPrice;
	}
	
	public long getPriceBuyFromMarket(Province prv, int durationInDay){
		double coeff = prv.getPreviousMoney() / ((1.0+prv.getMoneyChangePerDayConsolidated()));
		coeff = ( 1f / (1+ coeff*coeff) );
//		System.out.println("price to buy me :"+( price * (1+coeff) )+" / "+price+"    ("+prv.getMoney()+"/"
//		+(durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated()))+" = "+(prv.getMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated())))
//				+" => "+coeff);
		long buyPrice = (long)( price * (1+5*coeff) );
		if(prv.getPreviousMoney()<0 || coeff > 0.7){
			buyPrice = (long)( buyPrice * 2 );
			if(coeff>0.9) buyPrice = (long)( buyPrice * 2 );
		}
		return buyPrice;
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
