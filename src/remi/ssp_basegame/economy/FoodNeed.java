package remi.ssp_basegame.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.org.apache.bcel.internal.generic.POP;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.ComparatorValueDesc;

//TODO: use the current stock.
// actually, we eat all right away, without stockpiling anything.
public class FoodNeed extends PopNeed {

	final int nbDayStockNeeded = 300;

	/*
	 * 1 gramme de glucide donne 4 kCalories 1 gramme de lipide donne 9
	 * kCalories 1 gramme de protide donne 4 kCalories
	 * 
	 * 1 kilo de nouriture = 16000 kJ grosomodo
	 * 
	 * 1hour of gardening ~= 1500 kJ 1 men need 8000 kJ to live without moving
	 * So 1 average worker need to eat 24000 kJ per day (1,5kg of food)
	 */
	public static Object2IntMap<Good> kJoules = new Object2IntOpenHashMap<>();

	// public static FoodNeed me = new FoodNeed();

	public FoodNeed(Pop pop) {
		super(pop);
	}

	@Override
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult();

		if (totalMoneyThisTurn <= 0) {
			System.out.println("ERROR no money for pop:" + totalMoneyThisTurn);
		}

		NeedWish wish = new NeedWish(0, 0, 0);

		if (nbMensInPop == 0) {
			return wish;
		}

		// check the min price

		// kjoule need to consume
		final long maxKgNeeded = (int) (nbMensInPop * 1.5f * nbDays);
		long kgNeeded = maxKgNeeded;
		System.out.println("algo for consume food, i have " + totalMoneyThisTurn + " and i need " + kgNeeded
				+ " kg of food to feed " + nbMensInPop);

		// kjoules in stock, in case of?
		// TODO

		List<Good> lowPriceFood = new ArrayList<>();
		List<Good> normalFood = new ArrayList<>();
		List<Good> luxuryFood = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		for (Entry<Good, ProvinceGoods> entry : goodStock.entrySet()) {
			// System.out.println("there are a stock of
			// "+entry.getValue().getStock()+" of "+entry.getKey().getName()+"
			// @"+prv.x+":"+prv.y);
			Good good = entry.getKey();
			if (kJoules.containsKey(good) && entry.getValue().getStock() > 0) {
				lowPriceFood.add(good);
				System.out.println("i can buy " + entry.getValue().getStock() + " " + good.getName() + " at "
						+ entry.getValue().getPrice() + "->" + entry.getValue().getPriceBuyFromMarket(prv, nbDays)
						+ "€");
				goodPrice.put(good, entry.getValue().getPriceBuyFromMarket(prv, nbDays));
				if (good.getDesirability() > 2) {
					normalFood.add(good);
				}
				if (good.getDesirability() > 5) {
					luxuryFood.add(good);
				}
			}
		}
		// NOTE: we don't use the joule value yet, we assume a joule value of
		// 16000 kJ per kilo of food for everything
		// sort by price (lower first)
		lowPriceFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		kgNeeded = maxKgNeeded;
		for (Good food : lowPriceFood) {
			System.out.println("i try buy " + food.getName() + " @ " + goodPrice.getLong(food));
			if (kgNeeded <= goodStock.get(food).getStock()) {
				// System.out.println("enough " + food.getName() + " : " +
				// kgNeeded + "/" + goodStock.get(food).getStock());
				wish.vitalNeed += kgNeeded * goodPrice.getLong(food);
				kgNeeded = 0;
				break;
			} else {
				// System.out.println("not enough " + food.getName() + " : " +
				// kgNeeded + "/" + goodStock.get(food).getStock());
				wish.vitalNeed += goodStock.get(food).getStock() * goodPrice.getLong(food);
				kgNeeded -= goodStock.get(food).getStock();
			}
		}
		if (kgNeeded > 0) {
			System.out.println("can't find enough cheap food >< , i miss " + kgNeeded + "kg");
			// famine
			wish.normalNeed = 0;
			wish.luxuryNeed = wish.vitalNeed;
			System.out.println("@" + myPop.getProvince().x + ":" + myPop.getProvince().y + ", food wish: "
					+ wish.vitalNeed + " " + wish.normalNeed + " " + wish.luxuryNeed);
			return wish;
		}

		// sort by price (lower first)
		normalFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		kgNeeded = maxKgNeeded;
		for (Good food : normalFood) {
			if (kgNeeded <= goodStock.get(food).getStock()) {
				wish.normalNeed += kgNeeded * goodPrice.getLong(food);
				kgNeeded = 0;
				break;
			} else {
				wish.normalNeed += goodStock.get(food).getStock() * goodPrice.getLong(food);
				kgNeeded -= goodStock.get(food).getStock();
			}
		}

		// sort by price (lower first)
		luxuryFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		kgNeeded = maxKgNeeded;
		for (Good food : luxuryFood) {
			if (kgNeeded <= goodStock.get(food).getStock()) {
				wish.luxuryNeed += kgNeeded * goodPrice.getLong(food);
				kgNeeded = 0;
				break;
			} else {
				wish.luxuryNeed += goodStock.get(food).getStock() * goodPrice.getLong(food);
				kgNeeded -= goodStock.get(food).getStock();
			}
		}

		// compute extra money needed
		wish.normalNeed = Math.max(wish.normalNeed - wish.vitalNeed, 0);
		wish.luxuryNeed = Math.max(wish.luxuryNeed - (wish.vitalNeed + wish.normalNeed), wish.vitalNeed / 2);

		System.out.println("@" + myPop.getProvince().x + ":" + myPop.getProvince().y + ", food wish: " + wish.vitalNeed
				+ " " + wish.normalNeed + " " + wish.luxuryNeed);

		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		System.out.println("@" + myPop.getProvince().x + ":" + myPop.getProvince().y + ", to buy food, i have  "
				+ maxMoneyToSpend.vitalNeed + " " + maxMoneyToSpend.normalNeed + " " + maxMoneyToSpend.luxuryNeed);

		final long nbMensInPop = myPop.getNbAdult();
		final Object2LongMap<Good> currentPopStock = myPop.getStock();
		if (nbMensInPop == 0) {
			return 0;
		}
		// get basic then switch them to more grateful dishs?
		// kjoule need to consume
		// long kJNeeded = nbMensInPop * 24000l * nbDays;
		final long maxKgNeeded = (int) (nbMensInPop * 1.5f * nbDays);
		long kgNeeded = maxKgNeeded;

		// kjoules in stock, in case of?
		// TODO

		List<Good> lowPriceFood = new ArrayList<>();
		List<Good> normalBuyableFood = new ArrayList<>();
		List<Good> luxuryBuyableFood = new ArrayList<>();
		List<Good> allBuyableFood = new ArrayList<>();
		long storedFood = 0;
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		Object2LongMap<Good> nbGoods = new Object2LongOpenHashMap<>();
		for (Entry<Good, ProvinceGoods> entry : goodStock.entrySet()) {
			Good good = entry.getKey();
			if (kJoules.containsKey(good) && entry.getValue().getStock() > 0) {
				goodPrice.put(good, entry.getValue().getPriceBuyFromMarket(prv, nbDays));
				if (good.getDesirability() > 5) {
					luxuryBuyableFood.add(good);
				} else if (good.getDesirability() > 2) {
					normalBuyableFood.add(good);
				} else {
					lowPriceFood.add(good);
				}
				allBuyableFood.add(good);
				nbGoods.put(good, 0);
				storedFood += currentPopStock.getLong(good);
			}
		}
		// NOTE: we don't use the joule value yet, we assume a joule value of
		// 16000 kJ per kilo of food for everything
		// sort by price (lower first)
		lowPriceFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		normalBuyableFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		luxuryBuyableFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1) / o1.getDesirability(),
				goodPrice.getLong(o2) / o2.getDesirability()));
		allBuyableFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));

		nbGoods.defaultReturnValue(-1);
		int nbKiloVital = 0;
		int nbKiloNormal = 0;
		int nbKiloLuxe = 0;

		long nbCoinsVital = maxMoneyToSpend.vitalNeed;
		long nbCoins = nbCoinsVital;
		for (Good food : allBuyableFood) {
			long quantityWanted = Math.min(goodStock.get(food).getStock(), kgNeeded - nbKiloVital);
			long totalPrice = quantityWanted * goodPrice.getLong(food);
			System.out.println("i try buy " + food.getName() + " @ " + goodPrice.getLong(food));
			if (totalPrice == 0)
				continue; // no food here
			if (nbCoins <= totalPrice) {
				long quantityPicked = nbCoins / goodPrice.getLong(food);
				nbKiloVital += quantityPicked;
				nbGoods.put(food, quantityPicked);
				System.out.println("i am going to buy crap " + quantityPicked + " " + food.getName() + " @ "
						+ goodPrice.getLong(food) + " for " + nbCoins+" , inmap:"+nbGoods.getLong(food));
				nbCoins = 0;
				break;
			} else {
				nbCoins -= totalPrice;
				long quantityPicked = quantityWanted;
				nbKiloVital += goodStock.get(food).getStock();
				nbGoods.put(food, quantityPicked);
				System.out.println("i am going to buy crap " + quantityPicked + " " + food.getName() + " @ "
						+ goodPrice.getLong(food) + " for " + totalPrice+" , inmap:"+nbGoods.getLong(food));
			}
		}

		nbCoinsVital -= nbCoins; // keep the remaining, because shortage.
		long nbCoinsNormal = maxMoneyToSpend.normalNeed + nbCoins;
		nbCoins = nbCoinsNormal;
		for (Good food : normalBuyableFood) {
			long quantityWanted = Math.min(goodStock.get(food).getStock() - nbGoods.getLong(food),
					(3 * kgNeeded / 2) - (nbKiloVital + nbKiloNormal));
			long totalPrice = quantityWanted * goodPrice.getLong(food);
			if (totalPrice == 0)
				continue; // no food here
			if (nbCoins <= totalPrice) {
				long quantityPicked = nbCoins / goodPrice.getLong(food);
				nbKiloNormal += quantityPicked;
				nbGoods.put(food, quantityPicked+nbGoods.getLong(food));
				System.out.println("i am going to buy " + quantityPicked + " / "+quantityWanted+ " " + food.getName() + " @ "
						+ goodPrice.getLong(food) + " for " + nbCoins + " (q=" + (nbCoins / goodPrice.getLong(food))
						+ "=" + (nbCoins / (double) goodPrice.getLong(food))+" , inmap:"+nbGoods.getLong(food));
				nbCoins = 0;
				break;
			} else {
				long quantityPicked = quantityWanted;
				nbCoins -= totalPrice;
				nbKiloNormal += quantityPicked;
				nbGoods.put(food, quantityPicked+nbGoods.getLong(food));
				System.out.println("i am going to buy " + quantityPicked  + " / "+quantityWanted + " " + food.getName() + " @ "
						+ goodPrice.getLong(food) + " for " + totalPrice + " (now i hav " + nbCoins + ")"+" , inmap:"+nbGoods.getLong(food));
			}
		}
		

		for(it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entr : nbGoods.object2LongEntrySet()){
			System.out.println("entry "+entr.getKey()+" => "+entr.getLongValue());
		}

		nbCoinsNormal -= nbCoins; // keep the remaining, because shortage.
		long nbCoinsluxe = maxMoneyToSpend.luxuryNeed + nbCoins; // keep the
																	// remaining,
																	// because
																	// shortage.
		// check if i have enough food stored before buying expensive one
		if (storedFood < maxKgNeeded * nbDayStockNeeded) { // need at least
															// nbDayStockNeeded
															// days of foods
			nbCoinsluxe = (nbCoinsluxe * storedFood) / (maxKgNeeded * nbDayStockNeeded + 1);
		}

		nbCoins = nbCoinsluxe;
		for (Good food : luxuryBuyableFood) {
			long totalPrice = (goodStock.get(food).getStock()-nbGoods.getLong(food)) * goodPrice.getLong(food);
			if (totalPrice == 0)
				continue; // no food here
			if (nbCoins <= totalPrice) {
				long quantityPicked = nbCoins / goodPrice.getLong(food);
				nbKiloLuxe += quantityPicked;
				nbGoods.put(food, quantityPicked+nbGoods.getLong(food));
				System.out.println("i am going to buy good " + quantityPicked + " " + food.getName() + " @ "
						+ goodPrice.getLong(food) + " for " + nbCoins+" , inmap:"+nbGoods.getLong(food));
				nbCoins = 0;
				break;
			} else {
				long quantityPicked = goodStock.get(food).getStock();
				nbCoins -= totalPrice;
				nbKiloLuxe += quantityPicked;
				nbGoods.put(food, quantityPicked+nbGoods.getLong(food));
				System.out.println("i am going to buy good " + quantityPicked + " " + food.getName() + " @ "
						+ goodPrice.getLong(food) + " for " + totalPrice+" , inmap:"+nbGoods.getLong(food));
			}
		}

		// if theere are not a global shortage, sort out the possibilities.
		long nbSurplus = (nbKiloVital + nbKiloNormal + nbKiloLuxe) - kgNeeded;
		System.out.println("nbMens=" + nbMensInPop + ", nbKgVital=" + nbKiloVital + ", nbKiloNormal=" + nbKiloNormal
				+ ", nbKiloLuxe=" + nbKiloLuxe + ", kgFoodNeeded=" + kgNeeded + ", nbSurplus="
				+ (nbKiloVital + nbKiloNormal + nbKiloLuxe - kgNeeded));
		// int kgFoodNeeded = (int)(kJNeeded / (float) 16000);

		// System.out.println("nbMens="+nbMensInPop+",
		// nbKgVital="+nbKiloVital+", nbKiloNormal="+nbKiloNormal+",
		// nbKiloLuxe="+nbKiloLuxe+", kgFoodNeeded="+kgNeeded+",
		// nbSurplus="+nbSurplus);
		int newMoney = 0;
		

		for(it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entr : nbGoods.object2LongEntrySet()){
			System.out.println("entry "+entr.getKey()+" => "+entr.getLongValue());
		}

		System.out.println("food: i have a surplus of "+nbSurplus);
		if (nbSurplus < 0) {

			// try to eat the stock

			// from the less degradable to the most one
			allBuyableFood.sort(
					(food1, food2) -> -Float.compare(food1.getStorageKeepPerYear(), food2.getStorageKeepPerYear()));
			for (Entry<Good, Long> entry : currentPopStock.entrySet()) {
				Good food = entry.getKey();
				long currentStock = entry.getValue();
				System.out.println("Stock of " + food + " : " + currentStock);
				if (currentStock > 0) {
					if (currentStock > -nbSurplus) {
						currentPopStock.put(food, currentStock + nbSurplus);
						kgNeeded -= nbSurplus;
						System.out.println("Use stock of " + food + " to eat " + nbSurplus);
						nbSurplus = 0;
					} else {
						currentPopStock.put(food, 0);
						nbSurplus += currentStock;
						kgNeeded -= currentStock;
						System.out.println("Use all stock of " + food + " to eat (" + currentStock + ")");
					}
				}
			}

			// TODO: add a bit of random
			// TODO: give some from others to vital, if possible?
			
			//TODO: do not kill people before 1 turn, to let time to distribute urgent supplys from govrt stock.
			// TODO: add "steal" (people which steal from market)
			// TODO: add a factor from unnocupied landfarm to let people plant their own crop for their own food, whitout taxes because they are starving.
			// maybe it should work with food industry to coordinate their things.
			
			// TODO: kill children & old people first because they are weaks
			// famine! (old code, placeholder that can work, somehow (but
			// it's not efficient)
			// 1.5kg par personne par jour
			int nbPopNoFood = (int) (nbSurplus / (-1.5f * nbDays)); 
			int nbPopToDie = (int) Math.min(Integer.MAX_VALUE, Math.min(nbMensInPop, (int) (nbPopNoFood * 0.5)));
			if (nbPopToDie > 0) {
				System.out.println("famine! (" + nbPopNoFood + "+" + (nbKiloVital + nbKiloNormal + nbKiloLuxe) + "<="
						+ kgNeeded + " => " + prv.rationReserve + ")" + nbMensInPop + " , we need "+(-nbSurplus)+ " kg of food");
				// for (int i = 0; i < -prv.rationReserve / 2; i++) {
				System.out.print(nbPopToDie + " can die / " + nbMensInPop + " (" + (int) (1 + nbPopNoFood * 0.5) + "/"
						+ myPop.getNbAdult() + ") @" + myPop.getProvince().x + ":" + myPop.getProvince().y);
				// while (nbPopToDie > 0 && nbMensInPop > 0) {
				// la moitié des habitant non nouris meurent, les plus
				// TODO: les esclave en premier
				// jeunes d'abord (loi normale entre 0 et 14
				// old or young?
				// TODO: remove child first, elder second
				// }
				myPop.addAdult(-nbPopToDie);
				System.out.println(", now " + nbMensInPop + " (" + myPop.getNbAdult() + ") mens");
			}

		} else {
			// TODO: add a bit of random
			// TODO: this algo is WRONG
			System.out.println("TRY TO BUY EXTRA SPICEY MEAT");

			// grab the money back
			if (nbSurplus >= nbKiloVital) {
				nbSurplus -= nbKiloVital;
				newMoney += nbCoinsVital;
				nbKiloVital = 0;
				// nbCoinsVital = 0; //not up to date now
				// grab also from normal
				if (nbSurplus >= nbKiloNormal) {
					nbSurplus -= nbKiloNormal;
					newMoney += nbCoinsNormal;
					nbKiloNormal = 0;
					// nbCoinsNormal = 0;

					for (Good food : luxuryBuyableFood) {
						long nb = nbGoods.getLong(food);
						if (nb == -1)
							System.err.println("Error in food needs");
						if (nb == 0)
							continue; // no food here
						if (goodPrice.getLong(food) == 0) {
							System.err.println("Error in food needs: no price for " + food.getName());
							continue;
						} // no price here ?
						if (nbSurplus <= nb) {
							newMoney += nbSurplus * goodPrice.getLong(food);
							nbGoods.put(food, nb - nbSurplus);
							nbSurplus = 0;
							System.out.println("finally, iwon't going to buy more than "+(nb-nbSurplus)+" of "+food+" , inmap:"+nbGoods.getLong(food));
							break;
						} else {
							newMoney += nb * goodPrice.getLong(food);
							nbGoods.put(food, 0);
							nbSurplus -= nb;
							System.out.println("finally, iwon't going to buy "+food+" , inmap:"+nbGoods.getLong(food));
						}
					}
				} else {
					for (Good food : normalBuyableFood) {
						long nb = nbGoods.getLong(food);
						if (nb == -1)
							System.err.println("Error in food needs");
						if (nb == 0)
							continue; // no food here
						if (goodPrice.getLong(food) == 0) {
							System.err.println("Error in food needs: no price for " + food.getName());
							continue;
						}
						if (nbSurplus <= nb) {
							newMoney += nbSurplus * goodPrice.getLong(food);
							nbGoods.put(food, nb - nbSurplus);
							nbSurplus = 0;
							System.out.println("finally, iwon't going to buy more than "+(nb-nbSurplus)+" of "+food+" , inmap:"+nbGoods.getLong(food));
							break;
						} else {
							newMoney += nb * goodPrice.getLong(food);
							nbGoods.put(food, 0);
							nbSurplus -= nb;
							System.out.println("finally, iwon't going to buy "+food+" , inmap:"+nbGoods.getLong(food));
						}
					}
				}

			} else {
				for (Good food : lowPriceFood) {
					long nb = nbGoods.getLong(food);
					if (nb == -1)
						System.err.println("Error in food needs");
					if (nb == 0)
						continue; // no food here
					if (goodPrice.getLong(food) == 0) {
						System.err.println("Error in food needs: no price for " + food.getName());
						continue;
					}
					if (nbSurplus <= nb) {
						newMoney += nbSurplus * goodPrice.getLong(food);
						nbGoods.put(food, nb - nbSurplus);
						nbSurplus = 0;
						System.out.println("finally, iwon't going to buy "+food+" , inmap:"+nbGoods.getLong(food));
						break;
					} else {
						newMoney += nb * goodPrice.getLong(food);
						nbGoods.put(food, 0);
						nbSurplus -= nb;
						System.out.println("finally, iwon't going to buy more than "+(nb-nbSurplus)+" of "+food+" , inmap:"+nbGoods.getLong(food));
					}
				}
				//
				// //spend money for costly vital food
				// for(Good food : lowPriceFood){
				// nbGoods.put(food, 0);
				// }
				// int nbKiloVitalObj = nbKiloVital - vitalSurplus;
				// nbKiloVital = 0;
				// for(int i=lowPriceFood.size()-1;i>=0;i--){
				// Good food = lowPriceFood.get(i);
				// int totalPrice = goodStock.get(food).getStock() *
				// goodPrice.getInt(food);
				// if(nbCoins <= totalPrice){
				// int quantityPicked = nbCoins / goodPrice.getInt(food);
				// nbKiloVital += quantityPicked;
				// nbGoods.put(food, quantityPicked);
				// nbCoins = 0 ;
				// break;
				// }else{
				// nbCoins -= totalPrice;
				// int quantityPicked = goodStock.get(food).getStock();
				// nbKiloVital += goodStock.get(food).getStock();
				// nbGoods.put(food, quantityPicked);
				// }
				// }
			}

			// Use this money to buy 10 chunk of random food.
			long moneyToSpend = newMoney / 10;
			if (!allBuyableFood.isEmpty()) {
				for (int i = 0; i < 10; i++) {
					Good food = allBuyableFood
							.get(GlobalRandom.aleat.getInt(allBuyableFood.size(), (int) (nbMensInPop % Integer.MAX_VALUE)));
					long maxStock = goodStock.get(food).getStock() - nbGoods.getLong(food);
					long price = goodPrice.getLong(food);
					if (maxStock == 0)
						continue; // no food here
					if (price == 0) {
						System.err.println("Error in food needs: no price for " + food.getName());
						continue;
					}
					long nbPicked = Math.min(moneyToSpend / price, maxStock);
					newMoney -= nbPicked * price;
					nbGoods.put(food, nbGoods.get(food) + nbPicked);
				}
			}
		}
		for(it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entr : nbGoods.object2LongEntrySet()){
			System.out.println("entry "+entr.getKey()+" => "+entr.getLongValue());
		}

		// now eat these items (from the more tasty to the least one)
		allBuyableFood.sort((food1, food2) -> -Integer.compare(food1.getDesirability(), food2.getDesirability()));
		long kiloToEat = kgNeeded * 2; // max eating: 2 time more Kj than needed
		System.out.println(kgNeeded+" / "+maxKgNeeded);
		long moneyUsed = 0;
		for (Good food : allBuyableFood) {
			// it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Good> entry :
			// nbGoods.object2IntEntrySet());
			// Good food = entry.getKey();
			long nbFood = Math.min(kiloToEat, nbGoods.getLong(food));
			System.out.println(nbGoods.getLong(food));
			long cost = goodPrice.getLong(food) * nbFood;
			if (maxMoneyToSpend.getMoney() < moneyUsed) {
				cost = maxMoneyToSpend.getMoney() - moneyUsed;
				nbFood = cost / goodPrice.getLong(food);
			}
			moneyUsed += cost;
			// goodStock.get(food).addStock( -nbFood);
			// goodStock.get(food).addNbConsumePerDay(nbFood / (float)nbDays);
			// prv.addMoney(cost);
			// myPop.addMoney(-cost);
			super.consumeProductFromMarket(food, nbFood, nbDays);
			System.out.println("i buy & eat " + food.getName() + " @ " + goodPrice.getLong(food) + " for an amount of "
					+ nbFood + "/"+kiloToEat+" (total cost=" + cost + ", now i have spent " + moneyUsed + " )");
			kiloToEat -= nbFood;
		}

		// money left? maybe store some grain (1 year?)
		if (moneyUsed < maxMoneyToSpend.getMoney()) {
			long leftover = maxMoneyToSpend.getMoney() - moneyUsed;
			System.out.println("i have some leftover to build some reserves: "+leftover);

			long reserveNeeded = (nbMensInPop * 365 * 3) / 2;
			for (Good food : allBuyableFood) {
				reserveNeeded -= currentPopStock.getLong(food);
			}

			if (reserveNeeded > 0) {
				// from the less degradable to the most one (and do not pick all
				// of it)
				allBuyableFood.sort(
						(food1, food2) -> Float.compare(food1.getStorageKeepPerYear(), food2.getStorageKeepPerYear()));
				for (Good food : allBuyableFood) {
					long totalPrice = goodStock.get(food).getStock() * goodPrice.getLong(food) / 5;
					long quantityPicked = 0;
					if (totalPrice == 0)
						continue; // no food here
					if (leftover <= totalPrice) {
						moneyUsed += leftover;
						quantityPicked = leftover / goodPrice.getLong(food);
						System.out.println("i am going to buy RESERVE good " + quantityPicked + " " + food.getName()
								+ " @ " + goodPrice.getLong(food) + " for " + nbCoins + ", now i have spent "
								+ moneyUsed);
						leftover = 0;
					} else {
						moneyUsed += totalPrice;
						quantityPicked = goodStock.get(food).getStock() / 5;
						System.out.println("i am going to buy ALL RESERVE good " + quantityPicked + " " + food.getName()
								+ " @ " + goodPrice.getLong(food) + " for " + totalPrice + ", now i have spent "
								+ moneyUsed);
						leftover -= totalPrice;
					}
					super.storeProductFromMarket(food, quantityPicked, nbDays);
					// goodStock.get(food).addStock( -quantityPicked);
					// goodStock.get(food).addNbConsumePerDay(quantityPicked /
					// (float)nbDays);
					// long cost = goodPrice.getLong(food) * quantityPicked;
					// moneyUsed += cost;
					// leftover -= cost;
					// prv.addMoney(cost);
					// myPop.addMoney(-cost);
					// currentPopStock.put(food, quantityPicked);
					if(leftover <= 0) break;
				}
			}

		}

		if (moneyUsed > maxMoneyToSpend.getMoney()) {
			System.err
					.println("error in food wish, i spend " + moneyUsed + " instead of " + maxMoneyToSpend.getMoney());
		}

		return moneyUsed;
	}

}
