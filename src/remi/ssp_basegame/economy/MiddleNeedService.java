package remi.ssp_basegame.economy;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
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
			System.out.println("ERROR no money for pop:"+totalMoneyThisTurn);
		}
		
		NeedWish wish = new NeedWish(0, 0, 0);
		
		if(nbMensInPop==0){
			return wish;
		}
		
		//i wish i have the money for at least one man-hour of personal service each day
		long stock = goodStock.getStock();
		long price = goodStock.getPriceBuyFromMarket(prv, nbDays);
		
		wish.luxuryNeed += Math.min(stock, nbMensInPop * nbDays) * price;
		
		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		System.out.println("middle class is going to spend max "+maxMoneyToSpend.getMoney()+" to buy services");

		final long nbMensInPop = myPop.getNbAdult();
		final Object2LongMap<Good> currentPopStock = myPop.getStock();
		
		if(nbMensInPop==0){
			return 0;
		}
		
		long moneySpent = super.simpleConsume(nbMensInPop*nbDays, serviceGood, maxMoneyToSpend.luxuryNeed, nbDays);
		System.out.println("middle class haso spend " + moneySpent + " to buy services");
		return moneySpent;
	}

}
