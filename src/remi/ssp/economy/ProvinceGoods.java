package remi.ssp.economy;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Province;

public class ProvinceGoods {
//	
	protected Province prv;
	protected Good good;
	
	protected long stock=0;
	protected long price=10;
	protected long stockPrice=10;
	protected float nbConsumeThisPeriod=0; // BFR, we need to keep this as stock, so choose the price accordingly
	protected float nbConsumePerDayConsolidated=0;
	protected float nbProduceThisPeriod=0;
	protected float stockConsolidated=0;
	
	protected long previousPrice=0;
	
	//TODO "demande" pour permettre aux pop et à l'etat de demander aux marchants d'apporter des trucs
	
	
	public ProvinceGoods(Good good, Province prv) { super(); this.good = good; this.prv = prv; price = 10; stock = 0;}
	public long getStock() { return stock; }
	public long getPrice() { return price; }
	public long getStockPrice() { return stockPrice; }
	public float getNbConsumeThisPeriod() { return nbConsumeThisPeriod; }
	public float getNbProduceThisPeriod() { return nbProduceThisPeriod; }
	public void setPrice(long price) { this.price = price; }
	public void setStock(long stock) { this.stock = stock; 
	if(stock<0){
		System.err.println("error, you set a too low amount for stock of "+good);
	}
	}
	public void setNbConsumeThisPeriod(float nbConsumePerDay) { this.nbConsumeThisPeriod = nbConsumePerDay; }
	public float getNbConsumePerDayConsolidated() { return nbConsumePerDayConsolidated; }
	public float getStockConsolidated() { return stockConsolidated; }
	public void updateNbConsumeConsolidated(int nbDays) { //should be called 1 time per tick
		int nbTicks = Math.max(1, (int) good.getOptimalNbDayStock()*2);
		nbDays = Math.min(nbDays, nbTicks/2);
		this.nbConsumePerDayConsolidated = 
				(this.nbConsumePerDayConsolidated*(nbTicks-nbDays)/(float)(nbTicks)) 
				+ (nbConsumeThisPeriod/(float)(nbTicks+nbDays)); 
		//stock vary much more quickly, and is not dependent on "days", as it's an instant measure
		this.stockConsolidated = (this.stockConsolidated*(5)/(float)(6)) + stock/(float)(6); 
	}
//	public void addPrice(int price) { this.price += price; }
	public void addStock(long stock) {
		if(stock>0){
			long price = getPriceSellToMarket(1);
			this.stockPrice = (1+ this.stockPrice * this.stock + stock*price) / (1+this.stock + stock);
			nbProduceThisPeriod += stock;
		}else{
			addNbConsumeThisPeriod(-stock);
		}
		this.stock += stock;
		if(this.stock<0){
			System.err.println("error, you pick too much "+good);
		}
	}
	private void addNbConsumeThisPeriod(float nbConsume) { this.nbConsumeThisPeriod += nbConsume; }
	
	
	static float coeffMarketBFR = 0.5f;
	
	public long getPriceSellToMarket(int durationInDay){
		double coeff = Math.max(0.1,prv.getMoneyConsolidated()) / ((1.0+prv.getMoneyChangePerDayConsolidated()*coeffMarketBFR));
		coeff = ( 1f / (1+ coeff*coeff) );
//		logln("price to sell me :"+( price * (1-coeff) )+" / "+price);
		//if stock is high and i don't have money, lower this even more
//		if(prv.getMoney()<0 && stock > 100*getMoneyChangePerDayConsolidated){
//			logln("too much stock for ??, stop buy it! "+(long)( price * (1-coeff) ) + " => " + (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoney())) ));
//			return (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoneyconsolidated())) );
//		}else
		long buyPrice = (long)( price * (1-coeff) );
		//decrease even more the if the good has too many stock
		if(this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays < this.stockConsolidated){
//			logln("too much stock, reduce buy price more quickly to prevent banckrupcy of marketplace, and reduce offer");
			buyPrice = (long)( buyPrice * Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated) );
		}
		
		if(prv.getPreviousMoney()<0){
			//buyPrice = 0; //can't buy, faillite // can't do that, money can't circulate if true.
			buyPrice *= 0.3;
		}
			
		return buyPrice;
	}

	//TODO: use the stock value to compute the price to sell, to avoid selling at loss most of the time.
	public long getPriceBuyFromMarket(int durationInDay){
		double coeff = prv.getMoneyConsolidated() / ((1.0+prv.getMoneyChangePerDayConsolidated()*coeffMarketBFR));
		coeff = ( 1f / (1+ coeff*coeff) );
//		logln("price to buy me :"+( price * (1+coeff) )+" / "+price+"    ("+prv.getMoney()+"/"
//		+(durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated()))+" = "+(prv.getMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated())))
//				+" => "+coeff);
		
//		long buyPrice = (long)( price * (1+5*coeff) );
		//don't inflate, as it create a vicious cycle (more pricey -> need more meny -> more pricey)
		long buyPrice = (long)( price * (1+coeff*0) );
		if(coeff>0.7){
			buyPrice = (long)( price * (0.3+coeff) );
		}
		if(prv.getMoneyConsolidated()<0){
//			//TODO more linear one
//			buyPrice = (long)( buyPrice * 2 );
//			if(coeff>0.9) buyPrice = (long)( buyPrice * 2 );
			buyPrice = (long)( price * (1+coeff) );
		}
		if(buyPrice<stockPrice){
			//follow the lower price more if too much stock.
			if(this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays < this.stockConsolidated){
				buyPrice = (buyPrice+stockPrice)/2;
			}else{
				buyPrice = (buyPrice+stockPrice*9)/10;
			}
		}
		return buyPrice;
	}

	public void load(JsonObject jsonObj){
		stock = jsonObj.getJsonNumber("stock").longValue();
		price = jsonObj.getJsonNumber("price").longValue();
		stockPrice = jsonObj.getJsonNumber("stockPrice").longValue();
		nbConsumeThisPeriod = (float) jsonObj.getJsonNumber("nbConso").doubleValue();
		nbConsumePerDayConsolidated = (float) jsonObj.getJsonNumber("nbConsoPeriod").doubleValue();
		nbProduceThisPeriod = (float) jsonObj.getJsonNumber("nbProd").doubleValue();
		stockConsolidated = (float) jsonObj.getJsonNumber("stockPeriod").doubleValue();
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("stock", stock);
		jsonOut.add("price", price);
		jsonOut.add("stockPrice", stockPrice);
		jsonOut.add("nbConso", nbConsumeThisPeriod);
		jsonOut.add("nbConsoPeriod", nbConsumePerDayConsolidated);
		jsonOut.add("nbProd", nbProduceThisPeriod);
		jsonOut.add("stockPeriod", stockConsolidated);
	}
	
}
