package remi.ssp_basegame.economy;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
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
			System.out.println("ERROR no money for pop:"+totalMoneyThisTurn);
		}
		
		NeedWish wish = new NeedWish(0, 0, 0);

		final long nbMensInPop = myPop.getNbAdult();
		
		if(nbMensInPop==0){
			return wish;
		}
		//i need to have at least a secretary
		
		//i wish i have the money for at least one man-hour of personal service each day
		long stock = goodStock.getStock();
		long price = goodStock.getPriceBuyFromMarket(prv, nbDays);
		
		wish.normalNeed += Math.min(stock, nbMensInPop * 10 * nbDays) * price;
		
		
		//but if possible, grab at least 4
		stock -= Math.min(stock, nbMensInPop * 10 * nbDays);
		wish.luxuryNeed += Math.min(stock, nbMensInPop * 40 * nbDays) * price;
		
		
		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		System.out.println("rich class is going to spend max "+maxMoneyToSpend.getMoney()+" to buy services");

		final long nbMensInPop = myPop.getNbAdult();
		if(nbMensInPop==0){
			return 0;
		}
		
		long moneySpent = super.simpleConsume(nbMensInPop*50*nbDays, serviceGood, maxMoneyToSpend.getMoney(), nbDays);
		System.out.println("rich class haso spend " + moneySpent + " to buy services");
		return moneySpent;
	}

}
