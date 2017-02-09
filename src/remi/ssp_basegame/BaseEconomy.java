package remi.ssp_basegame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.Economy;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.economy.Good;
import remi.ssp.economy.Job;
import remi.ssp.economy.Needs;
import remi.ssp.economy.Needs.NeedWish;
import remi.ssp.economy.ProvinceCommerce;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.economy.TradeRoute;
import remi.ssp.politic.Carte;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.U;

public class BaseEconomy extends Economy {

	// cache values
	Map<Province, Map<Good, GoodsProduced>> oldStock = new HashMap<>();
	int bfrThisTurn = 0;

	// for(Province prv: allPrvs){
	public static class GoodsProduced {
		// Good good;
		// Province prv;
		long exportPrice;
		long nbExport;
		long importPrice;
		long nbImport;
		long prodPrice;
		long nbProd;
		long oldStock;
		long oldPrice;

	}

	@Deprecated // (replaced by addmoney() in province)
	public void sellToMarket(Province prv, Good item, int quantity, int price, int durationInDay) {
		bfrThisTurn += price * quantity;
	}

	/**
	 * TODO: rework: instead of a province-loop, it should be a: - produce goods
	 * in all province in the world (no order) - move goods via merchants for
	 * all provinces (ordered by the richest/pop to the poorest) - consume goods
	 * in each province (no order) - set prices in each province (no order)
	 */
	public void doTurn(Carte map, int durationInDay) {
		// TODO: REMINDER: don't forget to clear the TradeRouteExchange
		// structure from all civ & prv before calling this
		for (Civilisation civ : CurrentGame.civs) {
			civ.getTradeRouteExchange().clear();
		}
		for (List<Province> prvs : map.provinces) {
			for (Province prv : prvs) {
				prv.getLastTradeRouteExchange().clear();
			}
		}

		final Object2LongMap<Province> prv2Wealth = new Object2LongOpenHashMap<Province>();
		final List<Province> allPrvs = new ArrayList<>();
		for (List<Province> prvs : map.provinces) {
			// init map to compute prices
			for (Province prv : prvs) {
				for(Pop pop : prv.getPops()){
					System.out.println(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney()/1000+"€ for "+pop.getNbAdult());
				}
				// do not make eco on sea tile and no-pop tiles.
				if (prv.getNbAdult() == 0) {
					continue;
				}
				if (!oldStock.containsKey(prv))
					oldStock.put(prv, new HashMap<>());
				oldStock.get(prv).clear();
				bfrThisTurn = 0;
				prv.resetMoneyComputeNewTurn();
				// save previous stock to redo price
				for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
					GoodsProduced gp = new GoodsProduced();
					gp.oldStock = goodStock.getValue().getStock();
					// System.out.println("set stock of
					// "+goodStock.getKey().getName()+" @
					// "+goodStock.getValue().getStock()+" @"+prv.x+":"+prv.y);
					gp.oldPrice = goodStock.getValue().getPrice();
					// gp.good = goodStock.getKey();
					// gp.prv = prv;
					oldStock.get(prv).put(goodStock.getKey(), gp);
				}

				// init map to compute wealth order
				allPrvs.add(prv);
				long richesse = 0;
				for (Pop pop : prv.getPops()) {
					// TODO: this is not the wealth but the current bank
					// account... change it (when you have time)
					richesse = pop.getMoney();
				}
				prv2Wealth.put(prv, richesse / prv.getNbAdult());

				// produce goods
				produce(prv, durationInDay);
			}
		}
		for (Province prv : allPrvs) {
			for(Pop pop : prv.getPops()){
				System.out.println(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney()/1000+"€ for "+pop.getNbAdult());
			}
		}

		// sort list to have the first one with the biggest value.
		allPrvs.sort((prv1, prv2) -> -Long.compare(prv2Wealth.getLong(prv1), prv2Wealth.getLong(prv2)));
		for (Province prv : allPrvs) {
			doNavalImportExport(prv, durationInDay);
			doLandImportExport(prv, durationInDay);
		}
		for (Province prv : allPrvs) {
			consume(prv, durationInDay);
			// popSellThingsIfNeedMoney(prv, durationInDay);
			setPrices(prv, durationInDay);
			moveWorkers(prv, durationInDay); // ie: changejob

			// stock gaspi
			for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
				goodStock.getValue()
						.setStock((goodStock.getKey().storageLoss(goodStock.getValue().getStock(), durationInDay)));
			}
			// useful accumulators for computing prices
//			prv.setMoneyChangePerDay(1 + bfrThisTurn / durationInDay);
			long totalMoney = 0;
			System.out.println("PROVINCE money : "+prv.getMoney()/1000+"€ / "+prv.getMoneyChangePerDay()/1000+" ~ "+prv.getMoneyChangePerDayConsolidated()/1000);
			totalMoney += prv.getMoney();
			for(ProvinceIndustry indus : prv.getIndustries()){
				int nbEmp = 0; for(Pop pop : prv.getPops()) nbEmp += pop.getNbMensEmployed().getLong(indus);
				System.out.println(indus.getIndustry()+" has "+indus.getMoney()/1000+"€ for "+nbEmp);
				totalMoney += indus.getMoney();
			}
			for(Pop pop : prv.getPops()){
				System.out.println(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney()/1000+"€ for "+pop.getNbAdult());
				totalMoney += pop.getMoney();
			}
			System.out.println("TOTAL : "+totalMoney);
		}
		// TODO
		// for(Province prv: allPrvs){
		// emigration(prv, durationInDay);
		// }
	}

	@Deprecated
	private void doTurn(Province prv, int durationInDay) {
		oldStock.get(prv).clear();
		bfrThisTurn = 0;
		// save previous stock to redo price
		for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
			GoodsProduced gp = new GoodsProduced();
			gp.oldStock = goodStock.getValue().getStock();
			gp.oldPrice = goodStock.getValue().getPrice();
			// gp.good = goodStock.getKey();
			// gp.prv = prv;
			oldStock.get(prv).put(goodStock.getKey(), gp);
		}

		produce(prv, durationInDay);
		doNavalImportExport(prv, durationInDay);
		doLandImportExport(prv, durationInDay);
		consume(prv, durationInDay);
		setPrices(prv, durationInDay);
		moveWorkers(prv, durationInDay);
		// popSellThingsIfNeedMoney(prv, durationInDay);

		// stock gaspi
		for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
			goodStock.getValue()
					.setStock((goodStock.getKey().storageLoss(goodStock.getValue().getStock(), durationInDay)));
		}

	}

	private void doNavalImportExport(Province prv, int durationInDay) {
		// check if this province can make naval commerce
		// TODO buy a boat with pop money if none are present, same with
		// commerce guys.
		// TODO like land, but with boats instead of cart (1cart=1men but
		// 1boat=many mens)
	}

	private void doLandImportExport(Province prv, int durationInDay) {
		// create the list of imported & exported goods, sorted by benefice.
		// List<TradeTry> listOfTrades = new ArrayList<>();

		final Object2LongMap<TradeRoute> trAlreadyTaken = new Object2LongOpenHashMap<>();
		for (TradeRoute tr : prv.getTradeRoute()) {
			trAlreadyTaken.put(tr, 0);
		}

		// for each pop TODO: richer pop first
		for (Pop pop : prv.getPops()) {
			final List<TradeRoute> listOfTrades = new ArrayList<>();

			// check our transport capacity (from number of merchant,
			// efficiency, and merchant tools
			final ProvinceCommerce data = pop.getLandCommerce();
			final long transportCapa = data.computeCapacity(pop.getNbMensEmployed().getLong(pop.getLandCommerce()));
			final float capaPerMen = transportCapa / (float) pop.getNbMensEmployed().getLong(pop.getLandCommerce());

			// init : give all pop money to merchant (loan)
			data.setPreviousSalary(0);
			final long loan = pop.getMoney();
			data.setMoney(data.getMoney() + loan);

			// importedGoods
			final Object2DoubleMap<TradeRoute> maxProfitability = new Object2DoubleOpenHashMap<>();
			final Map<TradeRoute, List<TradeTry>> maxProfitabilityTR = new HashMap<>();
			// for each nearby province
			int totalCapacityTR = 0;
			double bestProfitability = 0;
			for (TradeRoute tr : prv.getTradeRoute()) {
				if (tr.isBoatTR())
					continue;
				double bestProfitabilityTR = 0;
				Province otherOne = tr.getOtherEnd(prv);
				maxProfitabilityTR.put(tr, new ArrayList<TradeTry>());
				// look to each goods i can buy
				for (Entry<Good, ProvinceGoods> otherGoods : otherOne.getStock().entrySet()) {
					Good good = otherGoods.getKey();
					// look at how much it cost here (double if not present
					// ever)
					long hisBuyCost = otherGoods.getValue().getPriceBuyFromMarket(otherOne, durationInDay);
					long hisSellCost = otherGoods.getValue().getPriceSellToMarket(otherOne, durationInDay);
					long myBuyCost = hisSellCost;
					long mySellCost = hisBuyCost;
					ProvinceGoods myGoods = prv.getStock().get(good);
					if (myGoods == null || myGoods.getStock() == 0) {
						if (myGoods == null) {
							prv.getStock().put(good, new ProvinceGoods(good));
						}
						myBuyCost *= 2;
						// can't move naval stuff by land
						// & if prv is not coastal i don't want it anyway.
						if (good.isNaval()) {
							myBuyCost = 0;
							mySellCost = Integer.MAX_VALUE;
						}
					} else {
						myBuyCost = prv.getStock().get(good).getPriceBuyFromMarket(prv, durationInDay);
						mySellCost = prv.getStock().get(good).getPriceSellToMarket(prv, durationInDay);
					}
					// if profitable, add ; profitablity = gain in money per
					// merchant
					// note: we choose the kilogram (liter) as the base unit for
					// transortation
					// so it's "base import per kilo"
					double diffImport = (1000 * (myBuyCost - hisSellCost) / (double) good.getWeight());
					double diffExport = (1000 * (hisBuyCost - mySellCost) / (double) good.getWeight());
					if (diffImport > diffExport) { // IMPORT
						if (diffImport <= 0)
							continue; // no way

						if (bestProfitability < diffImport)
							bestProfitability = diffImport;
						if (bestProfitabilityTR < diffImport)
							bestProfitabilityTR = diffImport;
						maxProfitabilityTR.get(tr)
								.add(new TradeTry(good, otherOne, prv, myBuyCost, hisSellCost, diffImport));
					} else { // EXPORT
						if (diffExport <= 0)
							continue; // no way

						if (bestProfitability < diffExport)
							bestProfitability = diffExport;
						if (bestProfitabilityTR < diffExport)
							bestProfitabilityTR = diffExport;
						maxProfitabilityTR.get(tr)
								.add(new TradeTry(good, prv, otherOne, hisBuyCost, mySellCost, diffExport));
					}

				}
				maxProfitability.put(tr, bestProfitabilityTR);
				totalCapacityTR += tr.getMaxMerchant();
				listOfTrades.add(tr);
				maxProfitabilityTR.get(tr).sort((arg0,arg1) -> Long.compare(arg1.buyerPrice - arg1.sellerPrice, arg0.buyerPrice - arg0.sellerPrice));
			}

			// sort
			listOfTrades.sort(new Comparator<TradeRoute>() {
				@Override
				public int compare(TradeRoute arg0, TradeRoute arg1) {
					return Double.compare(maxProfitability.getDouble(arg1), maxProfitability.getDouble(arg0));
				}
			});

			// trade route numbers: from profitability(route,good),
			// capacity(route) and number of merchant.

			// algo1:
			// 70% don't look at profitability (or sightly): T(r,g) = (min(1,
			// 10*profitablity/bestProfitability)) * N(m) / N(r)
			// 30% want the most profitable route (fill, with the leftover if
			// anny)
			// for each trade route
			// do trade : buy & sell (benef or deficit are for the pop money)
			// use tools

			// careless merchants
			final long nbMerchantDontCare = (long) (pop.getNbAdult() * 0.7f);
			long merchantToFill = pop.getNbAdult();
			for (TradeRoute tr : listOfTrades) {
				Province otherOne = tr.getOtherEnd(prv);
				long maxMerchantForThisTR = tr.getMaxMerchant() - trAlreadyTaken.getLong(tr);
				if (maxMerchantForThisTR > 0) {
					// get the number of merchant we have here.
					final long trMerchantUsed = (long) Math.min(maxMerchantForThisTR,
							nbMerchantDontCare * tr.getMaxMerchant() / (float) totalCapacityTR);
					merchantToFill -= trMerchantUsed;
					long availableMerchantSlot = trMerchantUsed;
					trAlreadyTaken.put(tr, trAlreadyTaken.getLong(tr) + trMerchantUsed);
					// get the best trade here
					List<TradeTry> bestGoods = maxProfitabilityTR.get(tr);
					long maxCapa = 0;
					for (TradeTry tradeOffer : bestGoods) {
						maxCapa += tradeOffer.from.getStock().get(tradeOffer.good).getStock();
					}
					// TODO: 30/70 repartiion between avid and careless, like
					// for the TR
					for (TradeTry tradeOffer : bestGoods) {
						// get the valued exchange (and can't grab more than
						// half the stock, because it would be harsh)
						long stock = tradeOffer.from.getStock().get(tradeOffer.good).getStock() / 2;
						float capPercent = stock / (float)maxCapa;
						long nbGoodsToChange = (long) (capPercent * capaPerMen * trMerchantUsed);
						if (nbGoodsToChange > stock) {
							// max it
							nbGoodsToChange = stock;
							// other merchants do nothing, they lost the
							// contract!
						}
						availableMerchantSlot -= (long) (nbGoodsToChange / (capPercent * capaPerMen));
						// change stock
						tradeOffer.from.getStock().get(tradeOffer.good).addStock(-nbGoodsToChange);
						tradeOffer.to.getStock().get(tradeOffer.good).addStock(nbGoodsToChange);
						tradeOffer.from.addMoney(nbGoodsToChange * tradeOffer.sellerPrice); // he
																															// sell,
																															// we
																															// (the
																															// merchant)
																															// give
																															// him
																															// money
						tradeOffer.from.addMoney( - nbGoodsToChange * tradeOffer.buyerPrice); // he
																														// buy,
																														// he
																														// give
																														// us
																														// (merchant)
																														// money
						data.addMoney(nbGoodsToChange * (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
						// data.addToPreviousSalary(nbGoodsToChange *
						// (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
						U.add(tradeOffer.from.getLastTradeRouteExchange(), tradeOffer.to, trMerchantUsed);
						U.add(tradeOffer.to.getLastTradeRouteExchange(), tradeOffer.from, trMerchantUsed);
						if (tradeOffer.from.getOwner() != tradeOffer.to.getOwner()) {
							Civilisation civFrom = tradeOffer.from.getOwner();
							Civilisation civTo = tradeOffer.to.getOwner();
							U.add(civTo.getTradeRouteExchange(), civFrom, trMerchantUsed);
							U.add(civFrom.getTradeRouteExchange(), civTo, trMerchantUsed);
						}

						if (availableMerchantSlot < 0)
							break;
					}
					if (availableMerchantSlot < 0)
						System.err.println("Error, phantom merchants");
					// Remaining merchants are loosers, or they try their chance
					// at an other place
					merchantToFill += availableMerchantSlot;
				}
			}

			// avid merchants
			for (TradeRoute tr : listOfTrades) {
				Province otherOne = tr.getOtherEnd(prv);
				long maxMerchantForThisTR = tr.getMaxMerchant() - trAlreadyTaken.getLong(tr);
				if (maxMerchantForThisTR > 0) {
					final long trMerchantUsed = (long) Math.min(maxMerchantForThisTR, merchantToFill);
					merchantToFill -= trMerchantUsed;
					long availableMerchantSlot = trMerchantUsed;
					trAlreadyTaken.put(tr, trAlreadyTaken.getLong(tr) + trMerchantUsed);
					// get the best trade here
					List<TradeTry> bestGoods = maxProfitabilityTR.get(tr);

					for (TradeTry tradeOffer : bestGoods) {
						// get the valued exchange (and can't grab more than
						// half the stock, because it would be harsh)
						long stock = tradeOffer.from.getStock().get(tradeOffer.good).getStock() / 2;
						long nbGoodsToChange = Math.min(stock, (int) (availableMerchantSlot * capaPerMen));
						availableMerchantSlot -= (long) (nbGoodsToChange / (capaPerMen));
						// change stock
						tradeOffer.from.getStock().get(tradeOffer.good).addStock(-nbGoodsToChange);
						tradeOffer.to.getStock().get(tradeOffer.good).addStock(nbGoodsToChange);
						tradeOffer.from.addMoney( nbGoodsToChange * tradeOffer.sellerPrice); // he
																															// sell,
																															// we
																															// (the
																															// merchant)
																															// give
																															// him
																															// money
						tradeOffer.from.addMoney( - nbGoodsToChange * tradeOffer.buyerPrice); // he
																														// buy,
																														// he
																														// give
																														// us
																														// (merchant)
																														// money
						data.addMoney(nbGoodsToChange * (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
						// data.addToPreviousSalary(nbGoodsToChange *
						// (tradeOffer.buyerPrice - tradeOffer.sellerPrice));
						U.add(tradeOffer.from.getLastTradeRouteExchange(), tradeOffer.to, trMerchantUsed);
						U.add(tradeOffer.to.getLastTradeRouteExchange(), tradeOffer.from, trMerchantUsed);
						if (tradeOffer.from.getOwner() != tradeOffer.to.getOwner()) {
							Civilisation civFrom = tradeOffer.from.getOwner();
							Civilisation civTo = tradeOffer.to.getOwner();
							U.add(civTo.getTradeRouteExchange(), civFrom, trMerchantUsed);
							U.add(civFrom.getTradeRouteExchange(), civTo, trMerchantUsed);
						}

						if (availableMerchantSlot < 0)
							break;
					}
					if (availableMerchantSlot < 0)
						System.err.println("Error, phantom merchants");
					// Remaining merchants are loosers, or they try their chance
					// at an other place
					merchantToFill += availableMerchantSlot;
				}
			}

			// if leftover, it's a loss for them.

			// rembourse the loan
			data.setMoney(data.getMoney() - loan);

			// for all our carts, destroy x% of them

			// then use merchant needs with merchant money to buy merchant tools
			// TODO (and do it on consume phase plz)
			// get the number of merchant without carts
			// get the best carts for them

			// or check our worst carts
			// get amount of good carts in stocks
			// iterate: replace best cart for worst cart
			// selling : half value because used, and you put only half of the
			// used carts into the market because some are broken.

			// salary : give 50% money to pop, to smooth things
			//TODO: use gross marge/2 to compute salary, and give the excedent cash to investors
			//data.addToPreviousSalary(data.getMoney() / 2);
			pop.addMoney(-data.getMoney() / 2);
			data.setMoney(0);

		}
	}

	protected static class TradeTry {
		double beneficePerKilo;
		long buyerPrice;
		long sellerPrice;
		Province from;
		Province to;
		Good good;

		public TradeTry(Good good, Province from, Province to, long buyerPrice, long sellerPrice,
				double beneficePerKilo) {
			super();
			this.good = good;
			this.from = from;
			this.to = to;
			this.buyerPrice = buyerPrice;
			this.sellerPrice = sellerPrice;
			this.beneficePerKilo = beneficePerKilo;
		}

	}

	private void produce(Province prv, int durationInDay) {
		// produce
		for (ProvinceIndustry indus : prv.getIndustries()) {

			System.out.println(indus.getIndustry().getName() + " want to produce " + indus.getIndustry().getGood()
					+ " with " + indus.getMoney() + "€");
			// compute marge
			// ProvinceGoods goodPrice=
			// prv.getStock().get(indus.getIndustry().getGood());
			ProvinceGoods prvGood = prv.getStock().get(indus.getIndustry().getGood());
			long goodPrice = prvGood.getPriceSellToMarket(prv, durationInDay);
			long marge = goodPrice - indus.getRawGoodsCost();
			if (marge <= 0 && prvGood.getPrice() != 0) {
				if(prvGood.getStock()==0){
					// it seems the market choose a too low price
					// negociate with fake demand (maybe he doesn't know our product can be desired)
					prvGood.setNbConsumePerDay(10+prvGood.getNbConsumePerDay());
				}
				prvGood.setNbConsumePerDay(1+prvGood.getNbConsumePerDay());
				
				System.out.println(" but the price ("+prvGood.getPrice()+") is null or not enough marge ("+marge+", "+indus.getRawGoodsCost()+" )");
				// chomage technique
				long nbOwner = 0;
				long nbEmployes = 0;
				for (Pop pop : prv.getPops()) {
					if(pop.getCoeffInvestors() > 0){
						nbOwner += pop.getNbAdult() * pop.getCoeffInvestors();
					}
					nbEmployes+=pop.getNbMensEmployed().getLong(indus);
				}
				//maybe pay some people even when chomage technique?
				long moneySalary = (long) Math.min(indus.getMoney() /10, (indus.getPreviousSalary()* durationInDay *nbEmployes )/30);
				indus.setPreviousSalary( (30*moneySalary) / (double)(nbEmployes * durationInDay));
				// repay investors
				long moneyInvestors = indus.getMoney() /2;
				for (Pop pop : prv.getPops()) {
					if(pop.getCoeffInvestors() > 0){
						//if owner
						long money = (long) (moneyInvestors * pop.getNbAdult() * pop.getCoeffInvestors() / (float) nbOwner);
						indus.addMoney(-money);
						System.out.print(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney());
						pop.addMoney(money);
						System.out.println(" and rich pop gain "+money+ ", and now has "+pop.getMoney());
					}
					//salary
					long employesHere = pop.getNbMensEmployed().getLong(indus);
					long money = (long) (moneySalary * employesHere / (float) nbEmployes);
					indus.addMoney(-money);
					System.out.print(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney());
					pop.addMoney(money);
					System.out.println(" and gain "+money+ ", and now has "+pop.getMoney());
				}
				
			} else {
				// produce & pay people

				// produce
				// here  it  should  use  our  industry  money
				long quantity = indus.getIndustry().produce(indus, prv.getPops(), durationInDay); 

				indus.setPreviousProduction(quantity);
				
				// get employes
				long nbEmployes = 0;
				for (Pop pop : prv.getPops()) {
					nbEmployes += pop.getNbMensEmployed().getLong(indus);
				}
				
				if (quantity == 0) {
					System.out.println(" but can't produce for a reason");
					//check if we need investment
					
					continue;
				}
				if(nbEmployes<=0){
					System.err.println("Error, industry "+indus.getName()+" has produced "+quantity+" things with "+nbEmployes+" inside!!!");
				}
				
				System.out.println(" and with " + nbEmployes + " mens has produce " + quantity+ " "+indus.getIndustry().getGood() + " and now has " + indus.getMoney() + "$");
				// choose first price from raw cost + honest salary
				if (prvGood.getPrice() == 0) {
					// get mean salary
					long mean = 0;
					long nbEmployedOthers = 0;
					for (Pop pop : prv.getPops()) {
						for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus2 : pop.getNbMensEmployed()
								.object2LongEntrySet()) {
							if (indus2.getKey().getPreviousSalary() > 0) {
								mean += indus2.getLongValue() * indus2.getKey().getPreviousSalary();
								nbEmployedOthers += indus2.getLongValue();
							}
						}
					}
					if (nbEmployedOthers != 0) {
						mean /= nbEmployedOthers;
						if (mean == 0)
							mean = 1;
						prvGood.setPrice(indus.getRawGoodsCost() + (long) ((mean * nbEmployes) / quantity));
					} else {
						prvGood.setPrice(10);
					}
					goodPrice = prvGood.getPriceSellToMarket(prv, durationInDay);
					marge = goodPrice - indus.getRawGoodsCost();
				}
				// sell to market
				

				NeedWish wish = indus.getNeed().moneyNeeded(prv, indus.getMoney(), durationInDay);
				long investNeeded = wish.getMoney();

				// give money to workers
				// TODO : change share (salary vs marge (profit+investment))
				// from civilization vars
				// => if low marge, low insvestment => decline. If high marge =>
				// increase of inequality (or not), unhappiness of poor people
				// compute how many we need to keep going (increase prod)
				
				//TODO redo
				long moneyToKeep = indus.getRawGoodsCost() * quantity;
				moneyToKeep += investNeeded;
				float ratio = (indus.getMoney() / (float)moneyToKeep);
				System.out.println("ratio="+ratio);
				ratio = 1 - (1 / (1 + ratio * ratio)); // asymptote en 1, if
														// indus.getMoney() ==
														// moneyToKeep then
														// ratio = 0.5
				// /2 for investment & investors
				System.out.println("ratio="+ratio);
				final long moneySalary = (long) (marge * quantity * ratio) / 2;
				System.out.println(", indus.getMoney()="+indus.getMoney()+", moneyToKeep="+moneyToKeep+", goodPrice="+goodPrice+", rawcost="+indus.getRawGoodsCost()+", marge="+marge+",quantity="+quantity+",ratio="+ratio+", moneyToKeep="+moneyToKeep+", moneySalary="+moneySalary);
				System.out.println(" give " + moneySalary + " / " + indus.getMoney() + " (moneyToKeep=" + moneyToKeep
						+ ", " + marge + ", " + quantity + ")");
				// give money to investors
				long moneyInvestors = 0;
				long moneyBonus = (indus.getMoney() - moneySalary) - moneyToKeep;
				if (moneyBonus > 0) {
					//try to keep 3 month of salary in reserve
					System.out.println("moneyBonus(slow)! "+moneyBonus);
					ratio = (moneyBonus / (float)moneyToKeep);
					System.out.println("ratio(slow)! "+ratio);
					ratio = 0.2f - (0.2f / (1 + ratio * ratio));
					System.out.println("ratio(slow)! "+ratio);
					moneyInvestors = (long) (moneyBonus * (1-ratio));
					if(moneyBonus > moneySalary*3){
						System.out.println("moneybonus: all possible! ");
						moneyInvestors = moneyBonus -(long)(moneySalary*3);
//						System.out.println("moneyBonus2! "+moneyBonus);
//						ratio = (moneyBonus / moneyToKeep);
//						System.out.println("ratio2! "+ratio);
//						ratio = 1f - (1f / (1 + ratio * ratio));
//						System.out.println("ratio2! "+ratio);
//						moneyToGive += (moneyBonus * ratio);
					}
				}
				System.out.println("moneyInvestors = "+moneyInvestors);
				// safeguard
				if (moneyInvestors + moneySalary > indus.getMoney())
					System.err.println("error in money distribution algorithm");
				

				// distribute money
				indus.setPreviousSalary( (30*moneySalary) / (double)(nbEmployes * durationInDay));
				System.out.println(" give " + moneySalary + " / " + indus.getMoney() + " (bonus=" + moneyBonus + ", "
						+ ratio + ")");
				long nbOwner = prv.getPops().stream()
						.filter(pop -> pop.getCoeffInvestors()>0)
						.mapToLong(pop -> pop.getNbAdult() * pop.getCoeffInvestors())
						.sum();
				long nbOwnerVerif = 0;
				for (Pop pop : prv.getPops()) {
					if(pop.getCoeffInvestors() > 0){
						nbOwnerVerif += pop.getNbAdult() * pop.getCoeffInvestors();
					}
				}
				if(nbOwner != nbOwnerVerif) System.err.println("stream error : "+nbOwner +" != "+nbOwnerVerif);
				for (Pop pop : prv.getPops()) {
					if(pop.getCoeffInvestors() > 0){
						//if owner
						long money = (long) (moneyInvestors * pop.getNbAdult() * pop.getCoeffInvestors() / (float) nbOwner);
						indus.addMoney(-money);
						System.out.print(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney());
						pop.addMoney(money);
						System.out.println(" and rich pop gain "+money+ ", and now has "+pop.getMoney());
					}
					//salary
					long employesHere = pop.getNbMensEmployed().getLong(indus);
					long money = (long) (moneySalary * employesHere / (float) nbEmployes);
					indus.addMoney(-money);
					System.out.print(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney());
					pop.addMoney(money);
					System.out.println(" and gain "+money+ ", and now has "+pop.getMoney());
				}
				
				if (indus.getMoney() < 0)
					System.err.println("Error, in industral" + indus.getName() + "@" + prv.x + ":" + prv.y
							+ " money salary : negative value " + indus.getMoney());

				System.out.println("now indus "+indus+" has "+indus.getMoney()+"€");
				// we will use our money for our need in the consume phase (or
				// for selling)
				
			}
		}
		for(Pop pop : prv.getPops()){
			System.out.println(Pop.popTypeName.get(pop.getPopType())+" pop has "+pop.getMoney()/1000+"€ for "+pop.getNbAdult());
		}
	}

	// private void doImport(Province prv, int durationInDay) {
	// //look at each nearby province
	// //TODO: search lower price before
	// for(Province near : prv.proche){
	// //TODO: check country, legal issues, customs (intra, inter), etc
	// for(Entry<Good, ProvinceGoods> goodsStock : prv.getStock().entrySet()){
	// //can we import yours?
	// if(near.getStock().get(goodsStock.getKey()).stock >
	// goodsStock.getValue().stock){
	// int opti = (near.getStock().get(goodsStock.getKey()).stock +
	// goodsStock.getValue().stock) /2;
	// //TODO merchant: efficiency, customs, revenue, repartition inside pops
	// //TODO: road network efficiency
	// int nbMerchant = 0;
	// int nbPop = 0;
	// for(Pop pop: prv.getPops()){ nbMerchant += pop.getNbMensCommerce(); nbPop
	// +=pop.getNbAdult();}
	// //efficiency: TODO (like how many a men can transport? with goods? erf..
	// float efficiency = nbMerchant/(float)nbPop;
	// int goodsMovement = (opti - goodsStock.getValue().stock);
	// goodsMovement *= efficiency;
	// //money: for now, they do this gratis (and it's almost the same anyway
	// because it's import, they consume it)
	//// for(Pop pop: prv.getPops()){
	//// int popGoodsMouvement = (goodsMovement * pop.getNbMensCommerce()) /
	// nbMerchant;
	//// //pop.addMoney(popGoodsMouvement*(Math.abs(goodsStock.getValue().price
	// - );
	//// }
	//
	// //compute new price
	//
	// //move goods
	// goodsStock.getValue().stock += goodsMovement;
	// near.getStock().get(goodsStock.getKey()).stock -= goodsMovement;
	//
	// }
	// }
	// }
	// //TODO: maritime routes
	// }

	private void consume(Province prv, int durationInDay) {
		// TODO SELL THINGS IF IN NEED OF MONEY (and it's possible)

		// particuliers
		// note: it's a discrimination for the pop that arrive in last.
		Iterator<Pop> itPop = prv.getPops().iterator();
		while (itPop.hasNext()) {
			Pop pop = itPop.next();
			// on scinde en 3
			// les riches achetent en premier
			System.out.println(Pop.popTypeName.get(pop.getPopType())+" POP CONSUME");
			popBuy(pop, pop.getNbAdult(), pop.getMoney(), durationInDay);

			// TODO: if a pop is died, remove it
			if (pop.getNbAdult() == 0) {
				itPop.remove();
				System.out.println("A "+Pop.popTypeName.get(pop.getPopType())+" POP IS NOW EXTINCT @" + prv.x + ":" + prv.y);
			}
		}

		// industry
		List<ProvinceIndustry> randomList = new ArrayList<>(prv.getIndustries());
		GlobalRandom.aleat.shuffle(randomList);
		for (ProvinceIndustry indus : randomList) {
			Needs need = indus.getNeed();
			// get employes
			long nbEmployes = 0;
			for (Pop pop : prv.getPops()) {
				nbEmployes += pop.getNbMensEmployed().getLong(indus);
			}
			NeedWish wish = need.moneyNeeded(prv, indus.getMoney(), durationInDay);
			long moneyLeft = indus.getMoney();
			if (wish.vitalNeed > moneyLeft) {
				wish.vitalNeed = moneyLeft;
				wish.normalNeed = 0;
				wish.luxuryNeed = 0;
			}
			moneyLeft -= wish.vitalNeed;
			if (wish.normalNeed > moneyLeft) {
				wish.normalNeed = moneyLeft;
				wish.luxuryNeed = 0;
			}
			moneyLeft -= wish.normalNeed;
			if (wish.luxuryNeed > moneyLeft) {
				wish.luxuryNeed = moneyLeft;
			}
			long moneySpent = need.spendMoney(prv, wish, durationInDay);
			//indus.addMoney(-moneySpent);
			if (indus.getMoney() < 0)
				System.err.println("Error, in industral money buy: "+indus+" now has "+indus.getMoney()+"€");
		}

	}

	private void popBuy(Pop pop, long nb, final long money, int durationInDay) {
		Map<Needs, NeedWish> moneyNeeded = new HashMap<>();
		NeedWish wantedMoney = new NeedWish(0, 0, 0);
		for (Needs need : pop.getPopNeeds()) {
			NeedWish moneyNeeds = need.moneyNeeded(pop.getProvince(), money, durationInDay);
			wantedMoney.add(moneyNeeds);
			moneyNeeded.put(need, moneyNeeds);
		}

		long totalSpend = 0;
		for (Needs need : pop.getPopNeeds()) {
			NeedWish wish = moneyNeeded.get(need);
			totalSpend += wish.getMoney();
		}
		System.out.println("pop want to spend "+wantedMoney.getMoney()+"=="+totalSpend+" / "+money);

		if (money < wantedMoney.vitalNeed) {
			// shortage! easy decision
			float coeff = money / (float) wantedMoney.vitalNeed;
			for (Needs need : pop.getPopNeeds()) {
				NeedWish wish = moneyNeeded.get(need);
				wish.luxuryNeed = 0;
				wish.normalNeed = 0;
				wish.vitalNeed *= coeff;
			}
			System.out.println("shortage");
		} else {
			final long useableMoney = money - wantedMoney.vitalNeed;
			System.out.println("pop vital: "+wantedMoney.vitalNeed+" / "+money+", reste " +useableMoney);

			// look if we can fulfill the normal need
			long luxuryMoney = useableMoney;
			long aeffNormal =  0;
			if (useableMoney < wantedMoney.normalNeed) {
				// keep a little bit for luxury
				float coeff = useableMoney * 0.9f / (float) wantedMoney.normalNeed;
				for (Needs need : pop.getPopNeeds()) {
					NeedWish wish = moneyNeeded.get(need);
					wish.normalNeed = (long) (wish.normalNeed * coeff);
					luxuryMoney -= wish.normalNeed;
					aeffNormal += wish.normalNeed;
				}
			} else {
				luxuryMoney -= wantedMoney.normalNeed;
				aeffNormal = wantedMoney.normalNeed;
			}
			if (luxuryMoney < 0)
				System.err.println("Error, in luxury money assignment");

			System.out.println("pop normal: "+wantedMoney.normalNeed+" => "+aeffNormal+" / "+useableMoney+", reste " +luxuryMoney);
			// try to fulfill the luxury need
			long leftoverMoney = luxuryMoney;
			long aeff =  0;
			if (luxuryMoney > 0) {
				float coeff = Math.min(1, luxuryMoney / (float) wantedMoney.luxuryNeed);
				System.out.println("luxuryMoney:"+luxuryMoney+", wantedMoney.luxuryNeed="+wantedMoney.luxuryNeed+", coeff="+coeff);
				for (Needs need : pop.getPopNeeds()) {
					NeedWish wish = moneyNeeded.get(need);
					System.out.println(need+" wish "+wish+", leftoverMoney:"+leftoverMoney+" - "+wish.luxuryNeed * coeff);
					wish.luxuryNeed = (long) (wish.luxuryNeed * coeff);
					leftoverMoney -= wish.luxuryNeed;
					if(leftoverMoney<0){
						wish.luxuryNeed += leftoverMoney;
						leftoverMoney = 0;
					}
				}

				// add the leftover to normal and luxury need
				float coeffLeftoverNormal = 0.2f * leftoverMoney / (float) wantedMoney.normalNeed;
				float coeffLeftoverLuxury = 0.8f * leftoverMoney / (float) wantedMoney.luxuryNeed;
				long moneyNotSpent = leftoverMoney;
				for (Needs need : pop.getPopNeeds()) {
					NeedWish wish = moneyNeeded.get(need);
					long temp = (long) (wish.normalNeed * coeffLeftoverNormal);
					wish.normalNeed += temp;
					moneyNotSpent -= temp;
					temp = (long) (wish.normalNeed * coeffLeftoverLuxury);
					wish.luxuryNeed += temp;
					aeff += wish.luxuryNeed;
					moneyNotSpent -= temp;
					if(moneyNotSpent<0){
						wish.luxuryNeed += moneyNotSpent;
						moneyNotSpent = 0;
					}
				}
				if (moneyNotSpent < 0)
					System.err.println("Error, in leftover money assignment : pop has now "+moneyNotSpent+"€");
				// maybe some centimes are left
			}
			System.out.println("pop luxury: "+wantedMoney.luxuryNeed+"=>"+aeff+" / "+(useableMoney-aeffNormal));
		}
		 totalSpend = 0;
		for (Needs need : pop.getPopNeeds()) {
			NeedWish wish = moneyNeeded.get(need);
			totalSpend += wish.getMoney();
		}
		System.out.println("pop are going to spend "+totalSpend+" / "+money);

		for (Needs need : pop.getPopNeeds()) {
			NeedWish wish = moneyNeeded.get(need);
			long moneySpent = need.spendMoney(pop.getProvince(), wish, durationInDay);
			System.out.println("pop spend "+moneySpent+" / "+money+" for "+need.getName());
			
			if (pop.getMoney() < 0){
				System.err.println("ERROR: the pop has spent more than available into "+need.getName()+".spendMoney : "+moneySpent+" / "+money + "(wishLimit="+wish.getMoney()+")");
			}
		}
		if (pop.getMoney() < 0)
			System.err.println("Error, in pop money buy");
	}

	// private void doExport(Province prv, int durationInDay) {
	// // TODO Auto-generated method stub
	// }

	private void setPrices(Province prv, int durationInDay) {

		// Object2IntMap<Good> mimumStock = new Object2IntOpenHashMap<>();
		// Object2IntMap<Good> optimalStock = new Object2IntOpenHashMap<>();
		// for(Pop pop : prv.getPops()){
		// for(Needs need : pop.getPopNeeds()){
		// need.getMinimumStockPerPop()
		// }
		// }

		// TODO: compute the need when there are no stock ?

		for (Entry<Good, GoodsProduced> entry : oldStock.get(prv).entrySet()) {
			GoodsProduced gp = entry.getValue();
			long newPrice = 0;
			if (gp.oldStock > 0) {
				newPrice += gp.exportPrice * (long) gp.nbExport;
				newPrice += gp.importPrice * (long) gp.nbImport;
				newPrice += gp.prodPrice * (long) gp.nbProd;
				newPrice += gp.oldPrice * (long) gp.oldStock;
				// if( prv.getStock().get(entry.getKey()).getNbConsumePerDay()
				// >0)
				// System.out.println("oldStock = "+gp.oldStock+",
				// oldprice*oldstock = "+gp.oldPrice * (long) gp.oldStock+" / "+
				// (1 + gp.nbExport + gp.nbImport + gp.nbProd + gp.oldStock));
				newPrice /= (1 + gp.nbExport + gp.nbImport + gp.nbProd + gp.oldStock);
			} else {
				// no previous, so can't infer on this.
				newPrice = gp.oldPrice;
			}
			newPrice = Math.max(10, newPrice);

			// test for shortage/overproduction
			ProvinceGoods prvGood = prv.getStock().get(entry.getKey());
			prvGood.updateNbConsumeConsolidated(1+(int)entry.getKey().getOptimalNbDayStock());
			long newStock = prvGood.getStock();
			// max 50% (asymptote)
			// 2 * need for a step is the objective.
			// 5 * need => -22%
			// 3 * need => -10%
			// 1.5 * need => +7%
			// 1 * need => +16%

			double ratio = (1 + newStock) / (0.9 + entry.getKey().getOptimalNbDayStock() * prvGood.getNbConsumePerDayConsolidated()
					+ (prvGood.getNbConsumePerDayConsolidated() * durationInDay));
			// if( prvGood.getNbConsumePerDay() >0)
			// System.out.print(newPrice+" * "+(0.5 + (1 / (1 +
			// ratio)))+"="+(newPrice * (0.5 + (1 / (1 + ratio)))) );
			double tempPrice = (newPrice * (0.5 + (1 / (1 + ratio))));
			long saveNP=newPrice;
			if(tempPrice > newPrice){
				newPrice = Math.max(newPrice+1, (long)(newPrice + (tempPrice - newPrice) * entry.getKey().getVolatility()));
			}else if(tempPrice < newPrice){
				//max to protect against high volatility that can put us lower than 0
				newPrice = Math.max(1, Math.min(saveNP-1,(long)(saveNP + (tempPrice - saveNP) * entry.getKey().getVolatility())));
			}
			
			// if( prvGood.getNbConsumePerDay() >0)
			// System.out.println(" = "+newPrice);

			// System.out.println( "ratio="+(1+newStock)+" / (
			// (0.9+"+prvGood.getNbConsumePerDay() +")*"+durationInDay * 2.0+")
			// = "+ratio);
			// System.out.println( "newPrice * (0.5+(1/(1+ratio)))="+newPrice);
			// if( prvGood.getNbConsumePerDay() >0)
			System.out.println("new price for " + entry.getKey().getName() + ":  " + entry.getValue().oldPrice + " => "
					+ newPrice + ", (" + gp.exportPrice + ", " + gp.importPrice + ", " + gp.prodPrice + ", "
					+ gp.oldPrice + ", " 
					+ prvGood.getNbConsumePerDay()*durationInDay + " / " + prvGood.getStock() + " each day, " + ratio
					+ ", " + (0.5 + (1 / (1 + ratio))) + ")"+" opti:"+prv.getNbAdult()*entry.getKey().getOptimalNbDayStock()
					+", buy="+prvGood.getPriceBuyFromMarket(prv, durationInDay)+", raw="+prvGood.getPrice()+", sell="+prvGood.getPriceSellToMarket(prv, durationInDay)
					+", coeff="+(prv.getPreviousMoney() / (durationInDay*(1.0+prv.getMoneyChangePerDayConsolidated()/4))));

			if (newPrice <= 0)
				System.err.println("Error, new price is null:" + newPrice + " for " + entry.getKey().getName() + " @"
						+ prv.x + ":" + prv.y + " " + ratio);
			prv.getStock().get(entry.getKey()).setPrice((int) newPrice);
			// "clean" for next loop (we want to keep 2 time more than we need
			// in stock, and this method add a bit of inertia)
			if (prv.getStock().get(entry.getKey()).getStock() > prvGood.getNbConsumePerDay() / 2) {
				prv.getStock().get(entry.getKey()).setNbConsumePerDay(prvGood.getNbConsumePerDay() / 2);
			} else {
				// don't touch it if there are not enough stock, to keep it
				// increasing price
			}
		}

	}

	private void moveWorkers(Province prv, int durationInDay) {

		// note: as we used the "job" abstraction, it count commerce and
		// industry on an equal foot. TODO: Do we do the same for the army?
		for (Pop pop : prv.getPops()) {
			// sort job by best salary
			List<Job> bestJob = new ArrayList<>(pop.getNbMensEmployed().keySet());
			bestJob.sort((i1, i2) -> -Double.compare(i1.getPreviousSalary(), i2.getPreviousSalary()));

			// compute mean revenu
			long mean = 0;
			float nbEmployed = 0;
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus : pop.getNbMensEmployed()
					.object2LongEntrySet()) { 
				//TODO multiply by an "efficiency" from education or other things.
				mean += indus.getLongValue() * indus.getKey().getPreviousSalary();
				nbEmployed += indus.getLongValue();
			}
			mean /= pop.getNbAdult();
			System.out.println("Mean salary: "+mean);

			// 1 : put some men from inefficient industry into chomage.
			// asymptote 1/(98x+1), => f(1) = 0.01 (1% turnover per month), so
			// f(0.5) => ~0.02
			// and f(0.1) => 0.085 (if the salaries earn only 10% of the mean
			// salary, 8.5% of them will leave each month)
			// lim(f, oo)=0 f(100%)=1% f(50%)=2% f(25%)=4% f(10%)=9% f(0)=100%
			// .. too steep
			// new func: (2^(-5.64x))/2 => f(1)=1%, f(0.5)=7%, f(25%)=19%
			// f(10%)=34% f(0)=50% .. not steep enough
			// new func: 1/(98x²+1) => f(1)=1% f(0.5)=3.8% f(25%)=12.3
			// f(10%)=33.5 f(0)=50% .. not a good form (bell shape)
			// new func: 1/(70*(x+0.17)²) f(1)=1% f(0.5)=3.1% f(25%)=8%
			// f(10%)=19.6 f(0)=49% .. seems fine

			// for each indus, remove employes with this asymptote.
			// if an industry has less than 10 mens (and in bad shape), remove
			// all.
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus : pop.getNbMensEmployed()
					.object2LongEntrySet()) {
				float result = (float)indus.getKey().getPreviousSalary() / (float) mean + 0.17f;
				result *= result;
				result *= 70;
				result = 1 / result;
				result *= indus.getLongValue();
				// don't fire more people than are employed inside the company
				long nbFired = Math.min(indus.getLongValue(), (int) result);
//				System.out.println(indus.getKey().getName()+" fire: "+nbFired+"=="+result+" mens : "+(indus.getKey().getPreviousSalary() / (float) mean + 0.17f));
				
				pop.addNbMensChomage(nbFired);
//				System.out.print(indus.getLongValue()+" - "+nbFired);
				indus.setValue(indus.getLongValue() - nbFired);
//				System.out.println(" = "+indus.getLongValue());
			}

			// then, for each job (more prosperous can grab more mens, as they
			// have a share on the bigger pool).

			for (Job indus : bestJob) {
				long nbMensEmployed = pop.getNbMensEmployed().getLong(indus);
				// compute the % of mens employed inside vs all employed.
				float ratioEmployed = nbMensEmployed / (float) nbEmployed;
				// TODO (later) compute the factor from edu and adu needed then
				// passed in a f(eduratio, %popemployed) => ratioEduHire (you
				// can't hire dumb people if all smart one are taken)
				// you should checj each indus to see how many smarts people are
				// taken, and how many you can grab now.
				float ratioEduHire = 0.8f;
				// grab ratioEduHire * employedratio * min(nbemployes/2,
				// nbchomage/2)
				long nbHire = 1 + (int) (Math.min(2 + nbMensEmployed / 2,
						ratioEmployed * ratioEduHire * pop.getNbMensChomage() / 2));
				// System.out.println("ratioEmployed="+ratioEmployed+",
				// ratioEduHire"+ratioEduHire+" menavailable="+Math.min(2 +
				// nbMensEmployed / 2, pop.getNbMensChomage() / 2));
				nbHire = Math.min(nbHire, pop.getNbMensChomage()); // safeguard
																	// for the
																	// +1

				pop.addNbMensChomage(-nbHire);
				pop.getNbMensEmployed().put(indus, nbMensEmployed + nbHire);
				
				//make it come with investment
				long investment = (long)(nbHire*pop.getMoney()/(float)pop.getNbAdult());
				pop.addMoney(-investment);
				indus.addMoney(investment);
				
				System.out.println("i try to hire " + nbHire + " in " + indus.getName() + ", ("
						+ indus.getPreviousSalary() + "), now " + pop.getNbMensEmployed().getLong(indus));
			}

			// TODO: army can also grab unemployed

		}

	}

}
