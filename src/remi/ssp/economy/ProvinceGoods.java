package remi.ssp.economy;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.network.SimpleSerializable;
import remi.ssp.politic.Province;

public class ProvinceGoods implements SimpleSerializable {
//	
	protected Province prv;
	protected Good good;
	
	protected long stock=0;
	protected long price=10;
	protected long stockPrice=10;
	protected float nbConsumeThisPeriod=0; // BFR, we need to keep this as stock, so choose the price accordingly
	protected float nbConsumePerDayConsolidated=0;
	protected float nbProduceThisPeriod=0;
	protected float nbProducePerDayConsolidated=0;
	protected float stockConsolidated=0;
	
	protected long previousPrice=0;
	
	//TODO "demande" pour permettre aux pop et à l'etat de demander aux marchants d'apporter des trucs
	
	
	public ProvinceGoods(Good good, Province prv) { super(); this.good = good; this.prv = prv; price = 10; stock = 0;}
	public long getStock() { return stock; }
	public long getPrice() { return price; }
	public long getStockPrice() { return stockPrice; }
	public float getNbConsumeThisPeriod() { return nbConsumeThisPeriod; }
	public float getNbProduceThisPeriod() { return nbProduceThisPeriod; }
	public void setPrice(long price) { this.price = price; 
		if(price < 0){
			System.err.println("Error, price is <0 !!");
		}
	}
	public void setStock(long stock) { this.stock = stock; 
	if(stock<0){
		System.err.println("error, you set a too low amount for stock of "+good);
	}
	}
	public void initNewPrice(long newPrice) {
		stockPrice = price;
		previousPrice = price;
	}
	public void setNbConsumeThisPeriod(float nbConsume) { this.nbConsumeThisPeriod = nbConsume; }
	public float getNbConsumePerDayConsolidated() { return nbConsumePerDayConsolidated; }
	public void setNbProduceThisPeriod(float nbProduce) { this.nbProduceThisPeriod = nbProduce; }
	public float getNbProducePerDayConsolidated() { return nbProducePerDayConsolidated; }
	public float getStockConsolidated() { return stockConsolidated; }
	public final static int NBDAYS_CONSOLIDATED = 60;
	public void updateNbConsumeConsolidated(int nbDays) { //should be called 1 time per tick
//		int nbTicks = Math.max(1, (int) good.getOptimalNbDayStock()*2);
		int nbTicks = NBDAYS_CONSOLIDATED;
		nbDays = Math.min(nbDays, nbTicks/2);
		this.nbConsumePerDayConsolidated = 
				(this.nbConsumePerDayConsolidated*(nbTicks-nbDays)/(float)(nbTicks)) 
				+ (nbConsumeThisPeriod/(float)(nbTicks)); 
		this.nbProducePerDayConsolidated = 
				(this.nbProducePerDayConsolidated*(nbTicks-nbDays)/(float)(nbTicks)) 
				+ (nbProduceThisPeriod/(float)(nbTicks));
		//stock (not /days)
		this.stockConsolidated = (this.stockConsolidated*(NBDAYS_CONSOLIDATED-nbDays)/(float)(NBDAYS_CONSOLIDATED)) + (stock*(nbDays)/(float)(NBDAYS_CONSOLIDATED)); 
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
	
	
	public static float nbDayMarketBFR = 5f;
	
	public long getPriceSellToMarket(int durationInDay){
		double coeff = Math.max(0.1,prv.getMoneyConsolidated()) / ((1.0+prv.getMoneyChangePerDayConsolidated()*nbDayMarketBFR));
		coeff = ( 1f / (1+ coeff*coeff) );
		
//		logln("price to sell me :"+( price * (1-coeff) )+" / "+price);
		//if stock is high and i don't have money, lower this even more
//		if(prv.getMoney()<0 && stock > 100*getMoneyChangePerDayConsolidated){
//			logln("too much stock for ??, stop buy it! "+(long)( price * (1-coeff) ) + " => " + (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoney())) ));
//			return (long)( price * (1-coeff) * (0.1+900/(1000-prv.getPreviousMoneyconsolidated())) );
//		}else
		long buyPrice = (long)( price * (1-coeff/2) );
		
		
		//decrease even more the if the good has too many stock
		if(this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays < this.stockConsolidated){
//			logln("too much stock, reduce buy price more quickly to prevent banckrupcy of marketplace, and reduce offer");
			
			if((this.nbConsumePerDayConsolidated - this.nbProducePerDayConsolidated) > 0){
				//TODO: take into consideration the trend of consume - produced with a real algorithm and not this crap of wtf algo
//				buyPrice = (long)( price * (0.5 - (coeff/2) 
//						+ (1/((1/Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated))
//						*(1+this.nbProducePerDayConsolidated)/(1+this.nbConsumePerDayConsolidated)))/2 ) );
//				GlobalDefines.plogln(", \"ratioStock\": "+(Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated)));
//				GlobalDefines.plogln(", \"ratioProd\": "+((1+this.nbProducePerDayConsolidated)/(1+this.nbConsumePerDayConsolidated)));
//				GlobalDefines.plogln(", \"ratioFusion\": "+((1/((1/Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated))
//						*(1+this.nbProducePerDayConsolidated)/(1+this.nbConsumePerDayConsolidated)))));
				// reduce the reduction if the big stock is shrinking
				buyPrice *= (1/((((1/Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated))-1)
						*(1+this.nbProducePerDayConsolidated)/(1+this.nbConsumePerDayConsolidated))+1)) ;
			}else
				buyPrice *= Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated);
//				buyPrice = (long)( price * (0.5 - (coeff/2) + Math.sqrt((this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays) / this.stockConsolidated)/2 ) );
		}
		
		
		if(prv.getPreviousMoney()<0){
			//buyPrice = 0; //can't buy, faillite // can't do that, money can't circulate if true.
			double ratio = Math.max(1, -prv.getPreviousMoney()) / (double)(1.0 + prv.getMoneyChangePerDayConsolidated()*nbDayMarketBFR);
			if(ratio>0.7){
				buyPrice *= 0.3;
			}else{
				buyPrice *= 1-ratio;
			}
		}
			
		
		if(buyPrice> price){
			System.err.println("error in price sell calculation");
		}
		return buyPrice;
	}

	//TODO: use the stock value to compute the price to sell, to avoid selling at loss most of the time.
	public long getPriceBuyFromMarket(int durationInDay){
		double coeff = Math.max(0.1,prv.getMoneyConsolidated()) / ((1.0+prv.getMoneyChangePerDayConsolidated()*nbDayMarketBFR));
		coeff = ( 1f / (1+ coeff*coeff) );
//		logln("price to buy me :"+( price * (1+coeff) )+" / "+price+"    ("+prv.getMoney()+"/"
//		+(durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated()))+" = "+(prv.getMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated())))
//				+" => "+coeff);
		
//		long buyPrice = (long)( price * (1+5*coeff) );
		//don't inflate, as it create a vicious cycle (more pricey -> need more meny -> more pricey)
		long buyPrice = (long)( price * (1+coeff*0) );
//		if(coeff>0.7){
//			buyPrice = (long)( price * (0.3+coeff) );
//		}
		if(prv.getMoneyConsolidated()<0){
//			//TODO more linear one
//			buyPrice = (long)( buyPrice * 2 );
//			if(coeff>0.9) buyPrice = (long)( buyPrice * 2 );
//			if(coeff>0.9) buyPrice = (long)( buyPrice * 2 );
//			System.err.println("!!!<0!! coeff:"+coeff);
			coeff = Math.max(0.1,-prv.getMoneyConsolidated()) / ((1.0+prv.getMoneyChangePerDayConsolidated()*nbDayMarketBFR));
			if(coeff>1.5){
				buyPrice *= 1.5;
			}else{
				buyPrice = (long)( price * (1+coeff/3) );
			}
		}
		//don't sell at loss, if possible
		if(buyPrice<stockPrice){
			//follow the lower price more if too much stock.
			if(this.nbConsumePerDayConsolidated * 2 * good.optimalStockNbDays < this.stockConsolidated){
				buyPrice = (buyPrice+stockPrice)/2;
			}else{
				buyPrice = (buyPrice+stockPrice*9)/10;
			}
		}
		if(price <0){
			System.err.println("Error in compute buyfrommarket price: coeff:"+coeff+", price:"+price);
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
		nbProducePerDayConsolidated = (float) jsonObj.getJsonNumber("nbProdPeriod").doubleValue();
		stockConsolidated = (float) jsonObj.getJsonNumber("stockPeriod").doubleValue();
	}
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("stock", stock);
		jsonOut.add("price", price);
		jsonOut.add("stockPrice", stockPrice);
		jsonOut.add("nbConso", nbConsumeThisPeriod);
		jsonOut.add("nbConsoPeriod", nbConsumePerDayConsolidated);
		jsonOut.add("nbProd", nbProduceThisPeriod);
		jsonOut.add("nbProdPeriod", nbProducePerDayConsolidated);
		jsonOut.add("stockPeriod", stockConsolidated);
	}
	
}
