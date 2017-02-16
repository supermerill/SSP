package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.f;
import static remi.ssp.GlobalDefines.logln;

import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

public class MiddleNeedService extends PopNeed {

	// public static FoodNeed me = new FoodNeed();

	private Good serviceGood;

	public MiddleNeedService(Pop pop) {
		super(pop);
		serviceGood = Good.get("service");
	}

	@Override
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		ProvinceGoods goodStock = prv.getStock().get(serviceGood);

		final long nbMensInPop = myPop.getNbAdult();

		if(totalMoneyThisTurn<=0){
			System.err.println("ERROR no money for pop:"+totalMoneyThisTurn);
		}
		
		NeedWish wish = new NeedWish(0, 0, 0);
		
		if(nbMensInPop==0){
			return wish;
		}
		
		//i wish i have the money for at least one man-hour of personal service each day
		long stock = goodStock.getStock();
		long price = goodStock.getPriceBuyFromMarket(nbDays);
		
		wish.luxuryNeed += Math.min(stock, nbMensInPop * nbDays * (myPop.getPopType()+1)) * price;
		
		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		logln(", \"spend_max_services\": "+f(maxMoneyToSpend.getMoney()/1000f));

		final long nbMensInPop = myPop.getNbAdult();
//		final Object2LongMap<Good> currentPopStock = myPop.getStock();
		
		if(nbMensInPop==0){
			return 0;
		}
		
		long moneySpent = super.simpleConsume(nbMensInPop*nbDays, serviceGood, maxMoneyToSpend.luxuryNeed, nbDays);
		logln(", \"spend_services\": "+f(moneySpent/1000f));
		return moneySpent;
	}

}
