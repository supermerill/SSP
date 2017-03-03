package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.GlobalDefines;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

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
	 * 
	 * <500g/people => die! <1.5kg => reduce productivity <2kg => unhappy
	 */
	public static Object2IntMap<Good> kJoules = new Object2IntOpenHashMap<>();

	// public static FoodNeed me = new FoodNeed();

	public FoodNeed(Pop pop) {
		super(pop);
	}

	
	//TODO: change from vital => crap food, lux => lux food  to vit+n+l => crap food & better if possible
	// why? to be able to let some money slip to other need if enough food stock.
	@Override
	public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		final long nbMensInPop = myPop.getNbAdult() + myPop.getNbChildren() + myPop.getNbElder();

		NeedWish wish = new NeedWish(0, 0, 0);
		
		if (totalMoneyThisTurn <= 0) {
			if(nbMensInPop == 1)
				System.err.println("ERROR no money for pop:" + totalMoneyThisTurn);
			return wish;
		}

		if (nbMensInPop == 0) {
			return wish;
		}

		// check the min price

		// kjoule need to consume
		final long maxKgNeeded = (int) (nbMensInPop * 1.5f * nbDays);
		long kgNeeded = maxKgNeeded;
		log(", \"consume_food_wish\":{ \"maxmoney\":" + totalMoneyThisTurn + ", \"KG_needed\":" + kgNeeded + ", \"to feed nbpop\":" + nbMensInPop);
		// final long maxKgWished = (maxKgNeeded * 4)/3;

		// kjoules in stock, in case of?
		// TODO

		List<Good> lowPriceFood = new ArrayList<>();
		List<Good> normalFood = new ArrayList<>();
		List<Good> luxuryFood = new ArrayList<>();
		Object2LongMap<Good> goodPrice = new Object2LongOpenHashMap<>();
		long myStock = 0;
		for (Entry<Good, ProvinceGoods> entry : goodStock.entrySet()) {
			// logln("there are a stock of
			// "+entry.getValue().getStock()+" of "+entry.getKey().getName()+"
			// @"+prv.x+":"+prv.y);
			Good good = entry.getKey();
			if (kJoules.containsKey(good) && entry.getValue().getStock() > 0) {
				lowPriceFood.add(good);
				// logln(", \"i can buy " + entry.getValue().getStock() + " " +
				// good.getName() + " at "
				// + entry.getValue().getPrice() + "->" +
				// entry.getValue().getPriceBuyFromMarket(nbDays)
				// + "€");
				goodPrice.put(good, entry.getValue().getPriceBuyFromMarket(nbDays));
				if (good.getDesirability() > 2) {
					normalFood.add(good);
				}
				if (good.getDesirability() > 5) {
					luxuryFood.add(good);
				}
				myStock += myPop.getStock().getLong(good);
			}
		}
		
		// NOTE: we don't use the joule value yet, we assume a joule value of
		// 16000 kJ per kilo of food for everything
		// sort by price (lower first)
		lowPriceFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		kgNeeded = Math.max(0, maxKgNeeded);
		for (Good food : lowPriceFood) {
			 log(", \"i wish buy crap " + food.getName() + "\":{\"price\":" +goodPrice.getLong(food));
			if (kgNeeded <= goodStock.get(food).getStock()) {
				 logln(", \"kgNeeded\": "+kgNeeded + ", \"stock\":" + goodStock.get(food).getStock()+"}");
				wish.vitalNeed += kgNeeded * goodPrice.getLong(food);
				kgNeeded = 0;
				break;
			} else {
				 logln(", \"kgNeeded\": "+kgNeeded + ", \"stock\":" + goodStock.get(food).getStock()+", \"not enough\":true}");
				wish.vitalNeed += goodStock.get(food).getStock() * goodPrice.getLong(food);
				kgNeeded -= goodStock.get(food).getStock();
			}
		}
		if (kgNeeded > 0) {
			logln(", \"can't find enough cheap food >< , i miss (kg)\": " + kgNeeded + "}");
			// famine
			wish.normalNeed = 0;
			wish.luxuryNeed = wish.vitalNeed;
			// logln("@" + myPop.getProvince().x + ":" + myPop.getProvince().y +
			// ", food wish: "
			// + wish.vitalNeed + " " + wish.normalNeed + " " +
			// wish.luxuryNeed);
			return wish;
		}

		// sort by price (lower first)
		normalFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));
		kgNeeded = maxKgNeeded /2;
		for (Good food : normalFood) {
			 log(", \"i wish buy " + food.getName() + "\":{\"price\":" +goodPrice.getLong(food)+"}");
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
			 log(", \"i wish buy lux " + food.getName() + "\":{\"price\":" +goodPrice.getLong(food)+"}");
			if (kgNeeded <= goodStock.get(food).getStock()) {
				wish.luxuryNeed += kgNeeded * goodPrice.getLong(food);
				kgNeeded = 0;
				break;
			} else {
				wish.luxuryNeed += goodStock.get(food).getStock() * goodPrice.getLong(food);
				kgNeeded -= goodStock.get(food).getStock();
			}
		}

		//logln(", \"food wish bef\":\"" + wish + "\"");
		// compute extra money needed
		wish.normalNeed = Math.max(wish.normalNeed - wish.vitalNeed, 0);
		wish.luxuryNeed = Math.max(wish.luxuryNeed - (wish.vitalNeed + wish.normalNeed), wish.vitalNeed / 2);
		

		logln(", \"food wish\":\"" + wish + "\"}");

		return wish;
	}

	@Override
	public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		logln(", \"spend_food\":{\"dispo\": \"" + maxMoneyToSpend + "\"");

		// is reduced later if no food are buyed.
		myPop.setFoodEffectiveness(1);

		final long nbMensInPop = myPop.getNbAdult() + (long)(myPop.getNbChildren()*0.7) + myPop.getNbElder();
		final Object2LongMap<Good> currentPopStock = myPop.getStock();
		if (nbMensInPop == 0) {
			return 0;
		}
		// get basic then switch them to more grateful dishs?
		// kjoule need to consume
		// long kJNeeded = nbMensInPop * 24000l * nbDays;
		final long maxKgNeeded = (long) (nbMensInPop * 1.5d * nbDays);
		final long maxKgWish = (long) (nbMensInPop * 2d * nbDays);
		long kgNeeded = maxKgNeeded; // changé si rationnemeent

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
				goodPrice.put(good, entry.getValue().getPriceBuyFromMarket(nbDays));
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
		luxuryBuyableFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1) / o1.getDesirability(), goodPrice.getLong(o2) / o2.getDesirability()));
		allBuyableFood.sort((o1, o2) -> Long.compare(goodPrice.getLong(o1), goodPrice.getLong(o2)));

		nbGoods.defaultReturnValue(-1);
		int nbKiloVital = 0;
		int nbKiloNormal = 0;
		int nbKiloLuxe = 0;

		long nbCoinsVital = maxMoneyToSpend.vitalNeed;
		long nbCoins = nbCoinsVital;
		for (Good food : allBuyableFood) {
			if(kgNeeded <= nbKiloVital) break;
			long quantityWanted = Math.min(goodStock.get(food).getStock(), kgNeeded - nbKiloVital);
			log(", \"goodStock.get(food).getStock()\":" + goodStock.get(food).getStock());
			log(", \"kgNeeded\":" + kgNeeded);
			logln(", \"nbKiloVital\":" + nbKiloVital);
			long totalPrice = quantityWanted * goodPrice.getLong(food);
			logln(", \"try_buy_" + food.getName() + "_price\":" + goodPrice.getLong(food));
			if (totalPrice == 0)
				continue; // no food here
			if (nbCoins <= totalPrice) {
				long quantityPicked = nbCoins / goodPrice.getLong(food);
				nbKiloVital += quantityPicked;
				nbGoods.put(food, quantityPicked);
				logln(", \"choose_crap_" + food.getName() + "\":{\"qt\":" + quantityPicked + ", \"price(c)\":" + goodPrice.getLong(food) + ", \"ipay(c)\": "
						+ nbCoins + "}");
				nbCoins = 0;
				break;
			} else {
				nbCoins -= totalPrice;
				long quantityPicked = quantityWanted;
				nbKiloVital += goodStock.get(food).getStock();
				nbGoods.put(food, quantityPicked);
				logln(", \"chooseAll_crap_" + food.getName() + "\":{\"qt\":" + quantityPicked + ", \"price(c)\":" + goodPrice.getLong(food) + ", \"ipay(c)\": "
						+ nbCoins + "}");
			}
		}

		nbCoinsVital -= nbCoins; // keep the remaining, because shortage.
		long nbCoinsNormal = maxMoneyToSpend.normalNeed + nbCoins;
		nbCoins = nbCoinsNormal;
		for (Good food : normalBuyableFood) {
			if((3 * kgNeeded / 2) <= (nbKiloVital + nbKiloNormal)) break;
			long quantityWanted = Math.min(goodStock.get(food).getStock() - nbGoods.getLong(food), (3 * kgNeeded / 2) - (nbKiloVital + nbKiloNormal));
			log(", \"goodStock.get(food).getStock()\":" + goodStock.get(food).getStock());
			log(", \"(3 * kgNeeded / 2)\":" + (3 * kgNeeded / 2));
			logln(", \" (nbKiloVital + nbKiloNormal)\":" +  (nbKiloVital + nbKiloNormal));
			long totalPrice = quantityWanted * goodPrice.getLong(food);
			if (totalPrice == 0)
				continue; // no food here
			if (nbCoins <= totalPrice) {
				long quantityPicked = nbCoins / goodPrice.getLong(food);
				nbKiloNormal += quantityPicked;
				nbGoods.put(food, quantityPicked + nbGoods.getLong(food));
				// logln("i am going to buy " + quantityPicked + " /
				// "+quantityWanted+ " " + food.getName() + " @ "
				// + goodPrice.getLong(food) + " for " + nbCoins + " (q=" +
				// (nbCoins / goodPrice.getLong(food))
				// + "=" + (nbCoins / (double) goodPrice.getLong(food))+" ,
				// inmap:"+nbGoods.getLong(food));
				logln(", \"choose_" + food.getName() + "\":{\"qt\":" + quantityPicked + ", \"price(c)\":" + goodPrice.getLong(food) + ", \"ipay(c)\": "
						+ nbCoins + "}");
				nbCoins = 0;
				break;
			} else {
				long quantityPicked = quantityWanted;
				nbCoins -= totalPrice;
				nbKiloNormal += quantityPicked;
				nbGoods.put(food, quantityPicked + nbGoods.getLong(food));
				// logln("i am going to buy " + quantityPicked + " /
				// "+quantityWanted + " " + food.getName() + " @ "
				// + goodPrice.getLong(food) + " for " + totalPrice + " (now i
				// hav " + nbCoins + ")"+" , inmap:"+nbGoods.getLong(food));
				logln(", \"chooseAll_" + food.getName() + "\":{\"qt\":" + quantityPicked + ", \"price(c)\":" + goodPrice.getLong(food) + ", \"ipay(c)\": "
						+ nbCoins + "}");
			}
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
			long totalPrice = (goodStock.get(food).getStock() - nbGoods.getLong(food)) * goodPrice.getLong(food);
			if (totalPrice == 0)
				continue; // no food here
			if (nbCoins <= totalPrice) {
				long quantityPicked = nbCoins / goodPrice.getLong(food);
				nbKiloLuxe += quantityPicked;
				nbGoods.put(food, quantityPicked + nbGoods.getLong(food));
				// logln("i am going to buy good " + quantityPicked + " " +
				// food.getName() + " @ "
				// + goodPrice.getLong(food) + " for " + nbCoins+" ,
				// inmap:"+nbGoods.getLong(food));
				logln(", \"choose_good_" + food.getName() + "\":{\"qt\":" + quantityPicked + ", \"price(c)\":" + goodPrice.getLong(food) + ", \"ipay(c)\": "
						+ nbCoins + "}");
				nbCoins = 0;
				break;
			} else {
				long quantityPicked = goodStock.get(food).getStock();
				nbCoins -= totalPrice;
				nbKiloLuxe += quantityPicked;
				nbGoods.put(food, quantityPicked + nbGoods.getLong(food));
				// logln("i am going to buy good " + quantityPicked + " " +
				// food.getName() + " @ "
				// + goodPrice.getLong(food) + " for " + totalPrice+" ,
				// inmap:"+nbGoods.getLong(food));
				logln(", \"chooseAll_good_" + food.getName() + "\":{\"qt\":" + quantityPicked + ", \"price(c)\":" + goodPrice.getLong(food) + ", \"ipay(c)\": "
						+ nbCoins + "}");
			}
		}
//		logResume(nbGoods,"afterinit");

		// if theere are not a global shortage, sort out the possibilities.
		long nbCanEat = (nbKiloVital + nbKiloNormal + nbKiloLuxe);
		long nbShouldBeEat = (nbKiloVital + nbKiloNormal + nbKiloLuxe);
		long nbSurplus = (nbKiloVital + nbKiloNormal + nbKiloLuxe) - kgNeeded;
		logln(", \"nbMens\":" + nbMensInPop + ", \"nbKgVital\":" + nbKiloVital + ", \"nbKiloNormal\":" + nbKiloNormal + ", \"nbKiloLuxe\":" + nbKiloLuxe
				+ ", \"kgFoodNeeded\":" + kgNeeded + ", \"nbSurplus\":" + (nbKiloVital + nbKiloNormal + nbKiloLuxe - kgNeeded));
		// int kgFoodNeeded = (int)(kJNeeded / (float) 16000);

		// logln("nbMens="+nbMensInPop+",
		// nbKgVital="+nbKiloVital+", nbKiloNormal="+nbKiloNormal+",
		// nbKiloLuxe="+nbKiloLuxe+", kgFoodNeeded="+kgNeeded+",
		// nbSurplus="+nbSurplus);
		int newMoney = 0;

		logln(", \"food: i have a surplus of\": " + nbSurplus);
		

		//if stock is low, rationnement
		if (nbSurplus < 0) {
			int nbKiloStock = 0;
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entry : currentPopStock.object2LongEntrySet()) {
				nbKiloStock+=entry.getLongValue();
			}
			if(0.7f * nbKiloStock < nbMensInPop * 1.5 * 200){
				float ratio = 0.7f * nbKiloStock / ( nbMensInPop * 1.5f * 200);
				GlobalDefines.plog(", \"nbKiloStock\":"+nbKiloStock );
				GlobalDefines.plog(", \" ( nbMensInPop * 1.5f * 200)\":"+ ( nbMensInPop * 1.5f * 200));
				GlobalDefines.plog(", \"ratio\":"+ratio );
				//reduce ration from 1.5kg to 1kg
				kgNeeded = (long) (nbMensInPop * nbDays * (0.8f + ratio)); 
				nbSurplus = (nbKiloVital + nbKiloNormal + nbKiloLuxe) - kgNeeded;
				log(", \"rationnement\": true" );
				final double kiloPerDayPerMen = (nbShouldBeEat) / (double) (nbMensInPop * nbDays);
				myPop.setFoodEffectiveness(Math.min(1, (float) (kiloPerDayPerMen - 0.5)));
				logln(", \"changeFoodEffectiveness\": " + myPop.getFoodEffectiveness());
			}
		}
//		logResume(nbGoods,"afterrat");
		
		
		if (nbSurplus < 0) {

			// try to eat the stock
			

			// from the less degradable to the most one
//			allBuyableFood.sort((food1, food2) -> -Float.compare(food1.getStorageKeepPerYear(), food2.getStorageKeepPerYear()));
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> entry : currentPopStock.object2LongEntrySet()) {
				Good food = entry.getKey();
				long currentStock = entry.getLongValue();
				logln(", \"Stock of " + food + "\" : " + currentStock);
				if (currentStock > 0) {
					if (currentStock > -nbSurplus) {
						currentPopStock.put(food, currentStock + nbSurplus);
						kgNeeded += nbSurplus;
						nbShouldBeEat -= nbSurplus;
						logln(", \"Use stock of " + food + "\" : " + nbSurplus);
						nbSurplus = 0;
					} else {
						currentPopStock.put(food, 0);
						nbSurplus += currentStock;
						kgNeeded -= currentStock;
						nbShouldBeEat += currentStock;
						logln(", \"Use All stock of " + food + "\" : " + currentStock);
					}
				}
			}
			
			

			if (nbSurplus < 0) {

				// TODO: add a bit of random
				// TODO: give some from others to vital, if possible?

				// TODO: do not kill people before 1 turn, to let time to
				// distribute urgent supplys from govrt stock.
				// TODO: add "steal" (people which steal from market)
				// TODO: add a factor from unnocupied landfarm to let people
				// plant their own crop for their own food, whitout taxes
				// because they are starving.
				// maybe it should work with food industry to coordinate their
				// things.

				// TODO: kill children & old people first because they are weaks
				// famine! (old code, placeholder that can work, somehow (but
				// it's not efficient)
				// 1.5kg par personne par jour is optimal, less than 0.5 and
				// people began to die.
				final double kiloPerDayPerMen = (nbShouldBeEat) / (double) (nbMensInPop * nbDays);
				if(kiloPerDayPerMen <0){
					System.err.println("error in computation for kiloPerDayPerMen");
				}
				
				// int nbPopNoFood = (int) (nbSurplus / (-0.5f * nbDays));
				// int nbPopToDie = (int) Math.min(Integer.MAX_VALUE,
				// Math.min(nbMensInPop, (int) (nbPopNoFood * 0.5)));
				if (kiloPerDayPerMen < 0.5) {
					long nbPopNoFood = (long) ((0.5-kiloPerDayPerMen) * nbMensInPop); 
					GlobalDefines.plogln(", \"famine!\":\" (" + nbPopNoFood + " may die because " + (nbKiloVital + nbKiloNormal + nbKiloLuxe) + "<=" + kgNeeded +") we are " 
					+ nbMensInPop + " , we need " + (-nbSurplus) + " kg more of food (kiloPerDayPerMen="+GlobalDefines.f(kiloPerDayPerMen)+"\"");

					// utilisation loi de poisson avec 30 days half-life.
//					long nbPopToDie = (long) (nbPopNoFood * Math.exp(-0.69 * nbDays / 30d));
//					log(", \"poisson\":" + GlobalDefines.f(Math.exp(-0.69 * nbDays / 30d)));
					long nbPopToDie = GlobalRandom.aleat.poissonLaw(Math.min(Integer.MAX_VALUE,(int)nbMensInPop), (int)(nbPopNoFood/2), nbDays / 30d);
					log(", \"nbPopToDie(esp)\":" + nbPopToDie);

					// create some variance from my ass
//					nbPopToDie = nbPopToDie / 2 + GlobalRandom.aleat.getInt((int) (nbPopToDie), (int) (nbSurplus));

					nbPopToDie = (long) Math.min(Integer.MAX_VALUE, Math.min(nbMensInPop, nbPopToDie));
					log(", \"nbPopToDie(with some rand)\":" + nbPopToDie);

					// for (int i = 0; i < -prv.rationReserve / 2; i++) {
					// while (nbPopToDie > 0 && nbMensInPop > 0) {
					// la moitié des habitant non nouris meurent, les plus
					// TODO: les esclave en premier
					// jeunes d'abord (loi normale entre 0 et 14
					// old or young?
					// TODO: remove child first, elder second
					// }

					
					//TODO: take into account the ratio elder/other and children/adult and not assume it's 50/50 and 33/66
					long nbElderDie = myPop.getNbElder()/2;
					nbElderDie = Math.min(myPop.getNbElder(), GlobalRandom.aleat.normalLaw(Math.min(nbElderDie, nbPopToDie/3)));
					long nbChildDie = myPop.getNbChildren()/2;
					nbChildDie = Math.min(myPop.getNbChildren(), GlobalRandom.aleat.normalLaw(Math.min(nbChildDie, ((nbPopToDie-nbElderDie)*5)/6)));
					long nbAdultDie = nbPopToDie - nbElderDie - nbChildDie;
					if(nbAdultDie>myPop.getNbAdult()){
						//too much dies, be extrem!
						nbElderDie = myPop.getNbElder();
						nbChildDie = Math.min(myPop.getNbChildren(), nbPopToDie-nbElderDie);
						nbAdultDie = nbPopToDie - nbElderDie - nbChildDie;
						if(nbAdultDie>myPop.getNbAdult()){
							System.err.println("Error, food straving more people than present!");
						}
					}
					

					myPop.addChildren(-nbChildDie);
					myPop.addAdult(-nbAdultDie);
					myPop.addElder(-nbElderDie);
					GlobalDefines.plog(",\"nbChildDie\":"+nbChildDie+",\"nbAdultDie\":"+nbAdultDie+",\"nbElderDie\":"+nbElderDie);
					GlobalDefines.plogln(", \"now\": \"" + nbMensInPop + " (" + myPop.getNbAdult() + ") mens\"");
					myPop.setFoodEffectiveness(0);
				} else {
					// reduce effectiveness
					logln(", \"can eat kg/menday\": " + kiloPerDayPerMen+", \""+kgNeeded
							+" ("+nbShouldBeEat+") / "+(double) (nbMensInPop * nbDays)+"\":true");
					myPop.setFoodEffectiveness(Math.min(1, (float) (kiloPerDayPerMen - 0.5)));
					logln(", \"changeFoodEffectiveness\": " + myPop.getFoodEffectiveness());
				}

			}else{
				final double kiloPerDayPerMen = (nbShouldBeEat) / (double) (nbMensInPop * nbDays);
				myPop.setFoodEffectiveness(Math.min(1, (float) (kiloPerDayPerMen - 0.5)));
			}
		} else {
			// TODO: add a bit of random
			// TODO: this algo is WRONG
			logln(", \"TRY TO BUY EXTRA SPICEY MEAT\":true");
//			logResume(nbGoods,"spicey");

			// grab the money back -> to redo
//			if (nbSurplus >= nbKiloVital) {
//				nbSurplus -= nbKiloVital;
//				newMoney += nbCoinsVital;
//				logln(", \"grabmoneyVital\":" + nbCoinsVital);
//				nbKiloVital = 0;
//				// nbCoinsVital = 0; //not up to date now
//				// grab also from normal
//				if (nbSurplus >= nbKiloNormal) {
//					nbSurplus -= nbKiloNormal;
//					newMoney += nbCoinsNormal;
//					logln(", \"grabmoneyNormal\":" + nbCoinsVital);
//					nbKiloNormal = 0;
//					// nbCoinsNormal = 0;
//
//					for (Good food : luxuryBuyableFood) {
////						logResume(nbGoods,"luxf");
//						long nb = nbGoods.getLong(food);
//						if (nb == -1)
//							System.err.println("Error in food needs");
//						if (nb == 0)
//							continue; // no food here
//						if (goodPrice.getLong(food) == 0) {
//							System.err.println("Error in food needs: no price for " + food.getName());
//							continue;
//						} // no price here ?
//						if (nbSurplus <= nb) {
//							newMoney -= nbSurplus * goodPrice.getLong(food);
//							logln(", \"spendmoneyLux "+food+"\":" + (nbSurplus * goodPrice.getLong(food)));
//							nbGoods.put(food, nb - nbSurplus);
//							nbSurplus = 0;
//							logln(", \"finally, iwon't going to buy more than " + (nb - nbSurplus) + " of " + food + "\":true ");
//							break;
//						} else {
//							newMoney -= nb * goodPrice.getLong(food);
//							logln(", \"spendmoneyLux2 "+food+"\":" + (nb * goodPrice.getLong(food)));
//							nbGoods.put(food, 0);
//							nbSurplus -= nb;
//							logln(", \"finally, iwon't going to buy " + food + "\":true");
//						}
//					}
//				} else {
////					logResume(nbGoods,"normalf");
//					for (Good food : normalBuyableFood) {
//						long nb = nbGoods.getLong(food);
//						if (nb == -1)
//							System.err.println("Error in food needs");
//						if (nb == 0)
//							continue; // no food here
//						if (goodPrice.getLong(food) == 0) {
//							System.err.println("Error in food needs: no price for " + food.getName());
//							continue;
//						}
//						if (nbSurplus <= nb) {
//							newMoney -= nbSurplus * goodPrice.getLong(food);
//							logln(", \"spendmoneyN "+food+"\":" + (nbSurplus * goodPrice.getLong(food)));
//							nbGoods.put(food, nb - nbSurplus);
//							nbSurplus = 0;
//							logln(", \"finally, iwon't going to buy more than " + (nb - nbSurplus) + " of " + food + "\":true ");
//							break;
//						} else {
//							newMoney -= nb * goodPrice.getLong(food);
//							logln(", \"spendmoneyN2 "+food+"\":" + (nb * goodPrice.getLong(food)));
//							nbGoods.put(food, 0);
//							nbSurplus -= nb;
//							logln(", \"finally, iwon't going to buy " + food + "\":true ");
//						}
//					}
//				}
//
//			} else {
////				logResume(nbGoods,"lowp");
//				for (Good food : lowPriceFood) {
//					final long nb = nbGoods.getLong(food);
//					if (nb == -1)
//						System.err.println("Error in food needs");
//					if (nb == 0)
//						continue; // no food here
//					if (goodPrice.getLong(food) == 0) {
//						System.err.println("Error in food needs: no price for " + food.getName());
//						continue;
//					}
//					if (nbSurplus <= nb) {
//						newMoney -= nbSurplus * goodPrice.getLong(food);
//						logln(", \"spendmoneyV "+food+"\":" + (nbSurplus * goodPrice.getLong(food)));
//						nbGoods.put(food, nb - nbSurplus);
//						nbSurplus = 0;
//						logln(", \"finally, iwon't going to buy " + food + "\":true");
//						break;
//					} else {
//						newMoney -= nb * goodPrice.getLong(food);
//						logln(", \"spendmoneyV2 "+food+"\":" + (nb * goodPrice.getLong(food)));
//						nbGoods.put(food, 0);
//						nbSurplus -= nb;
//						logln(", \"finally, iwon't going to buy more than " + (nb - nbSurplus) + " of " + food + "\":true ");
//					}
//				}
//				//
//				// //spend money for costly vital food
//				// for(Good food : lowPriceFood){
//				// nbGoods.put(food, 0);
//				// }
//				// int nbKiloVitalObj = nbKiloVital - vitalSurplus;
//				// nbKiloVital = 0;
//				// for(int i=lowPriceFood.size()-1;i>=0;i--){
//				// Good food = lowPriceFood.get(i);
//				// int totalPrice = goodStock.get(food).getStock() *
//				// goodPrice.getInt(food);
//				// if(nbCoins <= totalPrice){
//				// int quantityPicked = nbCoins / goodPrice.getInt(food);
//				// nbKiloVital += quantityPicked;
//				// nbGoods.put(food, quantityPicked);
//				// nbCoins = 0 ;
//				// break;
//				// }else{
//				// nbCoins -= totalPrice;
//				// int quantityPicked = goodStock.get(food).getStock();
//				// nbKiloVital += goodStock.get(food).getStock();
//				// nbGoods.put(food, quantityPicked);
//				// }
//				// }
//			}
//			logResume(nbGoods,"befSpend");

			// Use this money to buy 10 chunk of random food.
			long moneyToSpend = newMoney / 10;
			if (!allBuyableFood.isEmpty()) {
				for (int i = 0; i < 10; i++) {
					Good food = allBuyableFood.get(GlobalRandom.aleat.getInt(allBuyableFood.size(), (int) (nbMensInPop % Integer.MAX_VALUE)));
					long maxStock = goodStock.get(food).getStock() - nbGoods.getLong(food);
					long price = goodPrice.getLong(food);
					if (maxStock == 0)
						continue; // no food here
					if (price == 0) {
						System.err.println("Error in food needs: no price for " + food.getName());
						continue;
					}
					long nbPicked = Math.min(Math.max(moneyToSpend,0) / price, maxStock);
					newMoney -= nbPicked * price;
					nbGoods.put(food, nbGoods.get(food) + nbPicked);
					logln(", \"instead i will eat "+food+"\":{\"nb\":"+nbPicked+", \"price\":" + price + ",\"tot\":"+nbPicked * price
							+", \"money_reste\":"+newMoney+"}");
				}
			}
			
//			logResume(nbGoods,"end");
		}

		// now eat these items (from the more tasty to the least one)
		allBuyableFood.sort((food1, food2) -> -Integer.compare(food1.getDesirability(), food2.getDesirability()));
		long kiloToEat = kgNeeded * 2; // max eating: 2 time more Kj than needed
		logln(", \"kgNeedToEat\":" + kgNeeded + ",\"maxKgNeeded\":" + maxKgNeeded);
		long moneyUsed = 0;
		for (Good food : allBuyableFood) {
			// it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Good> entry :
			// nbGoods.object2IntEntrySet());
			// Good food = entry.getKey();
			long nbFood = Math.min(kiloToEat, nbGoods.getLong(food));
			// logln(nbGoods.getLong(food));
			double cost = goodPrice.getLong(food) * nbFood;
			if (maxMoneyToSpend.getMoney() < moneyUsed) {
				cost = Math.max(0, maxMoneyToSpend.getMoney() - moneyUsed);
				nbFood = (long)(cost / goodPrice.getLong(food));
				if(cost < nbFood*goodPrice.getLong(food)){
					
				}
			}
			moneyUsed += cost;
			// goodStock.get(food).addStock( -nbFood);
			// goodStock.get(food).addNbConsumePerDay(nbFood / (float)nbDays);
			// prv.addMoney(cost);
			// myPop.addMoney(-cost);
			logln(", \"i buy & eat " + food.getName() + "\":{\"price\": " + goodPrice.getLong(food) + ", \"qtt\":" + nbFood + "\"needKg:\":" + kiloToEat
					+ ", \"total_cost\":" + cost + ", \"now i have spent\":" + moneyUsed + " }");
			super.consumeProductFromMarket(food, nbFood, nbDays);
			kiloToEat -= nbFood;
		}

		// money left? maybe store some grain (1 year?)
		if (moneyUsed < maxMoneyToSpend.getMoney()) {
			long leftover = maxMoneyToSpend.getMoney() - moneyUsed;
			logln(", \"i have some leftover to build some reserves: \":" + leftover);

			long reserveNeeded = (nbMensInPop * 365 * 3) / 2;
			for (Good food : allBuyableFood) {
				reserveNeeded -= currentPopStock.getLong(food);
			}
			
			//TODO: if stock >300days, choose food in random instead to be rational

			if (reserveNeeded > 0) {
				// from the less degradable to the most one (and do not pick all
				// of it)
				allBuyableFood.sort((food1, food2) -> Float.compare(food1.getStorageKeepPerYear(), food2.getStorageKeepPerYear()));
				for (Good food : allBuyableFood) {
					long totalPrice = goodStock.get(food).getStock() * goodPrice.getLong(food) / 5;
					long quantityPicked = 0;
					if (totalPrice == 0)
						continue; // no food here
					if (leftover <= totalPrice) {
						moneyUsed += leftover;
						quantityPicked = leftover / goodPrice.getLong(food);
						logln(", \"i am going to buy RESERVE good \":{\"qtt\":" + quantityPicked + ",\"name\":\"" + food.getName() + "\", \"price\":"
								+ goodPrice.getLong(food) + ", \"totPrice\":" + nbCoins + ", \"now i have spent\":" + moneyUsed + "}");
						leftover = 0;
					} else {
						moneyUsed += totalPrice;
						quantityPicked = goodStock.get(food).getStock() / 5;
						logln(", \"i am going to buy ALL RESERVE good \":{\"qtt\":" + quantityPicked + ",\"name\":\"" + food.getName() + "\", \"price\":"
								+ goodPrice.getLong(food) + ", \"totPrice\":" + totalPrice + ", \"now i have spent\":" + moneyUsed + "}");
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
					if (leftover <= 0)
						break;
				}
			}

		}

		// TODO: reduce happiness if less than 2kg per people /day.

		// note: this has already triggered, cf the logs down here
		if (moneyUsed > maxMoneyToSpend.getMoney()) {
			System.err.println("error in food wish, i spend " + moneyUsed + " instead of " + maxMoneyToSpend.getMoney());
		}
		/*
		 * RICH "spend_food": { "dispo": 36450: 0: 18225,
		 * "try_buy_rare_meat_price": 405, "choose_crap_rare_meat": { "qt": 90,
		 * "price(c)": 405, "ipay(c)": 36450}, "choose_meat": { "qt": 0,
		 * "price(c)": 623, "ipay(c)": 0}, "choose_good_rare_meat": { "qt": 92,
		 * "price(c)": 405, "ipay(c)": 1445}, "nbMens": 6, "nbKgVital": 90,
		 * "nbKiloNormal": 0, "nbKiloLuxe": 92, "kgFoodNeeded": 90, "nbSurplus":
		 * 92, "food: i have a surplus of": 92, "TRY TO BUY EXTRA SPICEY MEAT":
		 * true, "finally, iwon't going to buy more than 182 of rare_meat":
		 * true, "kgNeedToEat": 90, "maxKgNeeded": 90, "i buy & eat rare_meat":
		 * { "price": 405, "qtt": 92 "needKg:": 180, "total_cost": 37260,
		 * "now i have spent": 37260}, "i buy & eat meat": { "price": 623,
		 * "qtt": 10 "needKg:": 88, "total_cost": 6230, "now i have spent":
		 * 43490}, "i buy & eat crop": { "price": 428, "qtt": 32 "needKg:": 78,
		 * "total_cost": 13696, "now i have spent": 57186}
		 * 
		 * error in food wish, i spend 57186 instead of 54675 },
		 */

		logln("}");
		return moneyUsed;
	}
	
	private void logResume(Object2LongMap<?> nbGoods, String id){
		GlobalDefines.plog(",\""+id+" resume\":{");boolean tempcoma = false;;
		for(it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<?> toeat : nbGoods.object2LongEntrySet()){
			GlobalDefines.plog((tempcoma?",":"")+" \""+toeat.getKey()+"\":"+toeat.getLongValue());
			tempcoma= true;
		}
		GlobalDefines.plogln("}");
	}

}
