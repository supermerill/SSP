package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.f;
import static remi.ssp.GlobalDefines.logln;

import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

public class RichNeedService extends PopNeed {

	// public static FoodNeed me = new FoodNeed();

	private Good serviceGood;

	public RichNeedService(Pop pop) {
		super(pop);
		serviceGood = Good.get("service");
	}

	@Override
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		ProvinceGoods goodStock = prv.getStock().get(serviceGood);

		if(totalMoneyThisTurn<=0){
			System.err.println("ERROR no money for pop:"+totalMoneyThisTurn);
		}
		
		NeedWish wish = new NeedWish(0, 0, 0);

		final long nbMensInPop = myPop.getNbAdult();
		
		if(nbMensInPop==0){
			return wish;
		}
		//i need to have at least a secretary
		
		//i wish i have the money for at least one maid
		long stock = goodStock.getStock();
		long price = goodStock.getPriceBuyFromMarket(nbDays);
		
		wish.normalNeed += Math.min(stock, nbMensInPop * 10 * nbDays) * price;
		
		
		//but if possible, grab at least 4
		stock -= Math.min(stock, nbMensInPop * 10 * nbDays);
		wish.luxuryNeed += Math.min((long)(totalMoneyThisTurn*0.8), Math.min(stock, nbMensInPop * 100 * nbDays) * price);
		
		if(wish.normalNeed<0 || wish.luxuryNeed<0){
			System.err.println("Error, negative wish");
		}
		
		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		logln(", \"rich_class_spend_max_services\": "+f(maxMoneyToSpend.getMoney()/1000f));

		final long nbMensInPop = myPop.getNbAdult();
		if(nbMensInPop==0){
			return 0;
		}
		
		long moneySpent = super.simpleConsume(nbMensInPop*50*nbDays, serviceGood, maxMoneyToSpend.getMoney(), nbDays);
		logln(", \"rich_class_spend_services\": "+f(moneySpent/1000f));
		return moneySpent;
	}

}
