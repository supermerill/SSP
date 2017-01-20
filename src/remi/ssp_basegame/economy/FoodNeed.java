package remi.ssp_basegame.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.ComparatorValueDesc;

//TODO: use the current stock.
// actually, we eat all right away, without stockpiling anything.
public class FoodNeed extends PopNeed{
	
	/*
	 * 1 gramme de glucide donne 4 kCalories
	 * 1 gramme de lipide donne 9 kCalories
	 * 1 gramme de protide donne 4 kCalories
	 * 
	 * 1 kilo de nouriture = 16000 kJ grosomodo
	 * 
	 * 1hour of gardening ~= 1500 kJ
	 * 1 men need 8000 kJ to live without moving
	 * So 1 average worker need to eat 24000 kJ per day (1,5kg of food) 
	 */
	public static Object2IntMap<Good> kJoules = new Object2IntOpenHashMap<>();

	//public static FoodNeed me = new FoodNeed();

	public FoodNeed(Pop pop){super(pop);}
	
	@Override
	public NeedWish moneyNeeded(Province prv, int nbMensInPop, Object2IntMap<Good> currentPopStock,
			int totalMoneyThisTurn, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();

		NeedWish wish = new NeedWish(0, 0, 0);
		
		//check the min price
		
		//kjoule need to consume
		final long maxKgNeeded = (int)(nbMensInPop * 1.5f * nbDays);
		long kgNeeded = maxKgNeeded;
		System.out.println("algo for consume food, i have "+totalMoneyThisTurn+" and i need "+kgNeeded+" kg of food to feed "+nbMensInPop);
		
		//kjoules in stock, in case of?
		//TODO
		
		
		List<Good> lowPriceFood = new ArrayList<>();
		List<Good> normalFood = new ArrayList<>();
		List<Good> luxuryFood = new ArrayList<>();
		Object2IntMap<Good> goodPrice = new Object2IntOpenHashMap<>();
		for(Entry<Good, ProvinceGoods> entry : goodStock.entrySet()){
			Good good = entry.getKey();
			if(kJoules.containsKey(good) && entry.getValue().stock>0){
				lowPriceFood.add(good);
				System.out.println("i can buy "+entry.getValue().stock+" "+good.getName()+" at "+entry.getValue().price
						+"->"+entry.getValue().getPriceBuyFromMarket(prv, nbDays)+"€");
				goodPrice.put(good, entry.getValue().getPriceBuyFromMarket(prv, nbDays));
				if(good.getDesirability()>2){
					normalFood.add(good);
				}
				if(good.getDesirability()>5){
					luxuryFood.add(good);
				}
			}
		}
		//NOTE: we don't use the joule value yet, we assume a joule value of 16000 kJ per kilo of food for everything
		lowPriceFood.sort(new ComparatorValueDesc<>(goodPrice));
		kgNeeded = maxKgNeeded;
		for(Good food : lowPriceFood){
			System.out.println("i try buy "+food.getName());
			if(kgNeeded <= goodStock.get(food).stock){
				System.out.println("enough "+food.getName()+" : "+kgNeeded+"/"+goodStock.get(food).stock);
				wish.vitalNeed += kgNeeded * goodPrice.getInt(food);
				kgNeeded = 0;
				break;
			}else{
				System.out.println("not enough "+food.getName()+" : "+kgNeeded+"/"+goodStock.get(food).stock);
				wish.vitalNeed += goodStock.get(food).stock * goodPrice.getInt(food);
				kgNeeded -= goodStock.get(food).stock;
			}
		}
		if(kgNeeded > 0){
			System.out.println("can't find enough cheap food >< , i miss "+kgNeeded+"kg");
			//famine
			wish.normalNeed = 0;
			wish.luxuryNeed = wish.vitalNeed;
			System.out.println("@"+myPop.getProvince().x+":"+myPop.getProvince().y+", food wish: "+wish.vitalNeed+" "+wish.normalNeed+" "+wish.luxuryNeed);
			return wish;
		}
		
		normalFood.sort(new ComparatorValueDesc<>(goodPrice));
		kgNeeded = maxKgNeeded;
		for(Good food : normalFood){
			if(kgNeeded <= goodStock.get(food).stock){
				wish.normalNeed += kgNeeded * goodPrice.getInt(food);
				kgNeeded = 0;
				break;
			}else{
				wish.normalNeed += goodStock.get(food).stock * goodPrice.getInt(food);
				kgNeeded -= goodStock.get(food).stock;
			}
		}
		
		luxuryFood.sort(new ComparatorValueDesc<>(goodPrice));
		kgNeeded = maxKgNeeded;
		for(Good food : luxuryFood){
			if(kgNeeded <= goodStock.get(food).stock){
				wish.luxuryNeed += kgNeeded * goodPrice.getInt(food);
				kgNeeded = 0;
				break;
			}else{
				wish.luxuryNeed += goodStock.get(food).stock * goodPrice.getInt(food);
				kgNeeded -= goodStock.get(food).stock;
			}
		}
		
		// compute extra money needed
		wish.normalNeed = Math.max(wish.normalNeed - wish.vitalNeed, 0);
		wish.luxuryNeed = Math.max(wish.luxuryNeed - (wish.vitalNeed +  wish.normalNeed), wish.vitalNeed/2);
		
		System.out.println("@"+myPop.getProvince().x+":"+myPop.getProvince().y+", food wish: "+wish.vitalNeed+" "+wish.normalNeed+" "+wish.luxuryNeed);
		
		return wish;
	}

	@Override
	public int spendMoney(Province prv, int nbMensInPop, Object2IntMap<Good> currentPopStock,
			NeedWish maxMoneyToSpend, int nbDays) {
		Map<Good, ProvinceGoods> goodStock = prv.getStock();
		System.out.println("@"+myPop.getProvince().x+":"+myPop.getProvince().y+", to buy food, i have  "+maxMoneyToSpend.vitalNeed+" "+maxMoneyToSpend.normalNeed+" "+maxMoneyToSpend.luxuryNeed);
		// get basic then switch them to more grateful dishs?
		//kjoule need to consume
//				long kJNeeded = nbMensInPop * 24000l * nbDays;
				int kgNeeded = (int)(nbMensInPop * 1.5f * nbDays);
				
				//kjoules in stock, in case of?
				//TODO
				
				List<Good> lowPriceFood = new ArrayList<>();
				List<Good> normalFood = new ArrayList<>();
				List<Good> luxuryFood = new ArrayList<>();
				List<Good> allFood = new ArrayList<>();
				Object2IntMap<Good> goodPrice = new Object2IntOpenHashMap<>();
				Object2IntMap<Good> nbGoods = new Object2IntOpenHashMap<>();
				for(Entry<Good, ProvinceGoods> entry : goodStock.entrySet()){
					Good good = entry.getKey();
					if(kJoules.containsKey(good)){
						goodPrice.put(good, entry.getValue().getPriceBuyFromMarket(prv, nbDays));
						if(good.getDesirability()>5){
							luxuryFood.add(good);
						}else if(good.getDesirability()>2){
							normalFood.add(good);
						}else{
							lowPriceFood.add(good);
						}
						allFood.add(good);
						nbGoods.put(good, 0);
					}
				}
				//NOTE: we don't use the joule value yet, we assume a joule value of 16000 kJ per kilo of food for everything
				lowPriceFood.sort(new ComparatorValueDesc<>(goodPrice));
				normalFood.sort(new ComparatorValueDesc<>(goodPrice));
				luxuryFood.sort(new ComparatorValueDesc<>(goodPrice));


				nbGoods.defaultReturnValue(-1);
				int nbKiloVital = 0;
				int nbKiloNormal = 0;
				int nbKiloLuxe = 0;

				int nbCoinsVital = maxMoneyToSpend.vitalNeed;
				int nbCoins = nbCoinsVital;
				for(Good food : lowPriceFood){
					int totalPrice = goodStock.get(food).stock * goodPrice.getInt(food);
					if(totalPrice == 0) continue; //no food here
					if(nbCoins <= totalPrice){
						int quantityPicked = nbCoins / goodPrice.getInt(food);
						nbKiloVital += quantityPicked;
						nbGoods.put(food, quantityPicked);
						nbCoins = 0 ;
						break;
					}else{
						nbCoins -= totalPrice;
						int quantityPicked = goodStock.get(food).stock;
						nbKiloVital += goodStock.get(food).stock;
						nbGoods.put(food, quantityPicked);
					}
				}

				nbCoinsVital -= nbCoins; //keep the remaining, because shortage.
				int nbCoinsNormal = maxMoneyToSpend.normalNeed + nbCoins;
				nbCoins = nbCoinsNormal;
				for(Good food : normalFood){
					int totalPrice = goodStock.get(food).stock * goodPrice.getInt(food);
					if(totalPrice == 0) continue; //no food here
					if(nbCoins <= totalPrice){
						int quantityPicked = nbCoins / goodPrice.getInt(food);
						nbKiloNormal += quantityPicked;
						nbGoods.put(food, quantityPicked);
						nbCoins = 0 ;
						break;
					}else{
						int quantityPicked = goodStock.get(food).stock;
						nbCoins -= totalPrice;
						nbKiloNormal += quantityPicked;
						nbGoods.put(food, quantityPicked);
					}
				}

				nbCoinsNormal -= nbCoins; //keep the remaining, because shortage.
				int nbCoinsluxe = maxMoneyToSpend.luxuryNeed + nbCoins; //keep the remaining, because shortage.
				nbCoins = nbCoinsluxe;
				for(Good food : luxuryFood){
					int totalPrice = goodStock.get(food).stock * goodPrice.getInt(food);
					if(totalPrice == 0) continue; //no food here
					if(nbCoins <= totalPrice){
						int quantityPicked = nbCoins / goodPrice.getInt(food);
						nbKiloLuxe += quantityPicked;
						nbGoods.put(food, quantityPicked);
						nbCoins = 0 ;
						break;
					}else{
						int quantityPicked = goodStock.get(food).stock;
						nbCoins -= totalPrice;
						nbKiloLuxe += quantityPicked;
						nbGoods.put(food, quantityPicked);
					}
				}
				
				//if theere are not a global shortage, sort out the possibilities.
				int nbSurplus = (nbKiloVital + nbKiloNormal + nbKiloLuxe) - kgNeeded;
				System.out.println("nbMens="+nbMensInPop+", nbKgVital="+nbKiloVital+", nbKiloNormal="+nbKiloNormal+", nbKiloLuxe="+nbKiloLuxe+", kgFoodNeeded="+kgNeeded+", nbSurplus="+(nbKiloVital + nbKiloNormal + nbKiloLuxe- kgNeeded));
				if(nbCoins<=0 || nbSurplus<0){

//					int kgFoodNeeded = (int)(kJNeeded / (float) 16000);
					
					
					//System.out.println("nbMens="+nbMensInPop+", nbKgVital="+nbKiloVital+", nbKiloNormal="+nbKiloNormal+", nbKiloLuxe="+nbKiloLuxe+", kgFoodNeeded="+kgNeeded+", nbSurplus="+nbSurplus);
					int newMoney = 0;
					
					if(nbSurplus < 0){
						//TODO: add a bit of random
						//TODO: give some from others to vital, if possible?
						
						//TODO: kill children & old people because they are hungry
						// famine! (old code, placeholder that can work, somehow (but it's not efficient)
						int nbPopNoFood = (int)(nbSurplus / (-1.5f * nbDays)); //1.5kg par personne par jour
						int nbPopToDie = Math.min(nbMensInPop,(int)(nbPopNoFood * 0.5));
						if(nbPopToDie>0){
							System.out.println("famine! ("+nbPopNoFood+"+"+(nbKiloVital + nbKiloNormal + nbKiloLuxe)+"<="+kgNeeded+" => "+prv.rationReserve+")"+nbMensInPop+" => ");
//							for (int i = 0; i < -prv.rationReserve / 2; i++) {
							System.out.print(nbPopToDie+" can die / "+nbMensInPop+" ("+(int)(1+nbPopNoFood * 0.5)+"/"+myPop.getNbMens()+") @"+myPop.getProvince().x+":"+myPop.getProvince().y);
							while(nbPopToDie > 0 && nbMensInPop > 0){
								// la moitié des habitant non nouris meurent, les plus
								//TODO: les esclave en premier
								// jeunes d'abord (loi normale entre 0 et 14
								//old or young?
								if(GlobalRandom.aleat.getInt(2, nbPopToDie) == 0){
									//young
									int age = (GlobalRandom.aleat.getInt(7, nbPopToDie) + GlobalRandom.aleat.getInt(7, nbPopToDie));
									if(myPop.getNombreHabitantsParAge(age)>0){
										myPop.removeHabitants(age, 1);
										nbPopToDie--;
										nbMensInPop--;
									}
								}else{
									//random
									int age = GlobalRandom.aleat.getInt(100, nbPopToDie);
									if(myPop.getNombreHabitantsParAge(age)>0){
										myPop.removeHabitants(age, 1);
										nbPopToDie--;
										nbMensInPop--;
									}
								}
								// TODO: ne pas impacter les elites? => remonter leur %age
							}
							System.out.println(", now "+nbMensInPop+" ("+myPop.getNbMens()+") mens");
						}
						
					}else{
						//TODO: add a bit of random

						//grab the money back
						if(nbSurplus >= nbKiloVital){
							nbSurplus -= nbKiloVital;
							newMoney += nbCoinsVital;
							nbKiloVital = 0;
//							nbCoinsVital = 0; //not up to date now
							//grab also from normal
							if(nbSurplus >= nbKiloNormal){
								nbSurplus -= nbKiloNormal;
								newMoney += nbCoinsNormal;
								nbKiloNormal = 0;
//								nbCoinsNormal = 0;
								
								for(Good food : luxuryFood){
									int nb = nbGoods.getInt(food);
									if(nb == -1) System.err.println("Error in food needs");
									if(nb == 0) continue; //no food here
									if(goodPrice.getInt(food) == 0){System.err.println("Error in food needs: no price"); continue;} //no price here ?
									if(nbSurplus <= nb){
										newMoney += nbSurplus * goodPrice.getInt(food);
										nbGoods.put(food, nb - nbSurplus);
										nbSurplus = 0;
										break;
									}else{
										newMoney += nb * goodPrice.getInt(food);
										nbGoods.put(food, 0);
										nbSurplus -= nb;
									}
								}
							}else{
								for(Good food : normalFood){
									int nb = nbGoods.getInt(food);
									if(nb == -1) System.err.println("Error in food needs");
									if(nb == 0) continue; //no food here
									if(goodPrice.getInt(food) == 0){System.err.println("Error in food needs: no price"); continue;}
									if(nbSurplus <= nb){
										newMoney += nbSurplus * goodPrice.getInt(food);
										nbGoods.put(food, nb - nbSurplus);
										nbSurplus = 0;
										break;
									}else{
										newMoney += nb * goodPrice.getInt(food);
										nbGoods.put(food, 0);
										nbSurplus -= nb;
									}
								}
							}
							
						}else{
							for(Good food : lowPriceFood){
								int nb = nbGoods.getInt(food);
								if(nb == -1) System.err.println("Error in food needs");
								if(nb == 0) continue; //no food here
								if(goodPrice.getInt(food) == 0){System.err.println("Error in food needs: no price"); continue;}
								if(nbSurplus <= nb){
									newMoney += nbSurplus * goodPrice.getInt(food);
									nbGoods.put(food, nb - nbSurplus);
									nbSurplus = 0;
									break;
								}else{
									newMoney += nb * goodPrice.getInt(food);
									nbGoods.put(food, 0);
									nbSurplus -= nb;
								}
							}
//							
//							//spend money for costly vital food
//							for(Good food : lowPriceFood){
//								nbGoods.put(food, 0);
//							}
//							int nbKiloVitalObj = nbKiloVital - vitalSurplus;
//							nbKiloVital = 0;
//							for(int i=lowPriceFood.size()-1;i>=0;i--){
//								Good food = lowPriceFood.get(i);
//								int totalPrice = goodStock.get(food).stock * goodPrice.getInt(food);
//								if(nbCoins <= totalPrice){
//									int quantityPicked = nbCoins / goodPrice.getInt(food);
//									nbKiloVital += quantityPicked;
//									nbGoods.put(food, quantityPicked);
//									nbCoins = 0 ;
//									break;
//								}else{
//									nbCoins -= totalPrice;
//									int quantityPicked = goodStock.get(food).stock;
//									nbKiloVital += goodStock.get(food).stock;
//									nbGoods.put(food, quantityPicked);
//								}
//							}
						}
					}
					
					//Use this money to buy 10 chunk of random food.
					int moneyToSpend = newMoney / 10;
					for(int i=0;i<10;i++){
						Good food = allFood.get(GlobalRandom.aleat.getInt(allFood.size(), nbMensInPop));
						int maxStock = goodStock.get(food).stock - nbGoods.getInt(food);
						int price = goodPrice.getInt(food);
						if(maxStock == 0) continue; //no food here
						if(price == 0){System.err.println("Error in food needs: no price"); continue;}
						int nbPicked = Math.min(moneyToSpend / price, maxStock);
						newMoney -= nbPicked * price;
						nbGoods.put(food, nbGoods.get(food) + nbPicked);
					}
					
					
				}
				
				//now eat these items
				int moneyUsed = 0;
				for(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Good> entry : nbGoods.object2IntEntrySet()){
					Good food = entry.getKey();
					int nbFood = entry.getIntValue();
					goodStock.get(food).stock -= nbFood;
					goodStock.get(food).nbConsumePerDay += 1 + (nbFood/nbDays);
					int cost = goodPrice.getInt(food) * nbFood;
					prv.addMoney(cost);
					moneyUsed += cost;
				}
		
		
		return moneyUsed;
	}

}
