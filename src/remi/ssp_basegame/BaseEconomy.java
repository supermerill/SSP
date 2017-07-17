package remi.ssp_basegame;

import static remi.ssp.GlobalDefines.f;
import static remi.ssp.GlobalDefines.fm;
import static remi.ssp.GlobalDefines.i;
import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;
import static remi.ssp.GlobalDefines.plog;
import static remi.ssp.GlobalDefines.plogln;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.longs.Long2LongRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.CurrentGame;
import remi.ssp.GlobalDefines;
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
import remi.ssp.utils.LongInterval;
import remi.ssp.utils.U;

public class BaseEconomy extends Economy {
	
	public float regulateCropPrice= 16; // more = less costly. 8 is ok

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
	public void sellToMarket(Province prv, Good item, int quantity, int price, int nbDays) {
		bfrThisTurn += price * quantity;
	}

	/**
	 * TODO: rework: instead of a province-loop, it should be a: - produce goods
	 * in all province in the world (no order) - move goods via merchants for
	 * all provinces (ordered by the richest/pop to the poorest) - consume goods
	 * in each province (no order) - set prices in each province (no order)
	 */
	public void doTurn(Carte map, int nbDays) {
		// TODO: REMINDER: don't forget to clear the TradeRouteExchange
		// structure from all civ & prv before calling this
		for (Civilisation civ : CurrentGame.get().getCivs()) {
			civ.getTradeRouteExchange().clear();
		}
		for (Province prv : map.getAllProvinces()) {
			prv.getLastTradeRouteExchange().clear();
		}

		final Object2LongMap<Province> prv2Wealth = new Object2LongOpenHashMap<Province>();
		final List<Province> allPrvs = new ArrayList<>();
		for (Province prv : map.getAllProvinces()) {
			// init map to compute prices
				// do not make eco on sea tile and no-pop tiles.
				if (prv.getNbAdult() == 0) {
					continue;
				}
				if (!oldStock.containsKey(prv))
					oldStock.put(prv, new HashMap<>());
				oldStock.get(prv).clear();
				bfrThisTurn = 0;
				prv.resetMoneyComputeNewTurn(nbDays);
				// save previous stock to redo price
				for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
					GoodsProduced gp = new GoodsProduced();
					gp.oldStock = goodStock.getValue().getStock();
					// logln("set stock of
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

		}

		// sort list to have the first one with the biggest value.
		//TODO allow this, disable for clarity for debugging (for now)
		//allPrvs.sort((prv1, prv2) -> -Long.compare(prv2Wealth.getLong(prv1), prv2Wealth.getLong(prv2)));
		for (Province prv : allPrvs) {
			doNavalImportExport(prv, nbDays);
			doLandImportExport(prv, nbDays);
		}
		

		for (Province prv : allPrvs) {
				// produce goods
				logln("\"produce_" + prv + "\":{ \"province\":\"" + prv + "\"");
				produce(prv, nbDays);
				logln("}");
			
		}
		for (Province prv : allPrvs) {
			for (Pop pop : prv.getPops()) {
				logln(", \"money_after_produce_" + pop + "\":{\"money\":" + fm(pop.getMoney()) + ", \"nbAdult:\": " + pop.getNbAdult() + "}");
			}
		}
		for (Province prv : allPrvs) {
			consume(prv, nbDays);
			// popSellThingsIfNeedMoney(prv, nbDays);
			setPrices(prv, nbDays);
			distributeProvinceMoney(prv, nbDays);
			moveWorkers(prv, nbDays); // ie: changejob
			promotePop(prv, nbDays);
			aging(prv, nbDays);

			// stock gaspi
			for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
				goodStock.getValue().setStock((goodStock.getKey().storageLoss(goodStock.getValue().getStock(), nbDays)));
			}
			for(Pop pop : prv.getPops()){
				// stock gaspi
				for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> goodStock : pop.getStock().object2LongEntrySet()) {
					goodStock.setValue((goodStock.getKey().storageLoss(goodStock.getLongValue(), nbDays)));
				}
			}
			for(ProvinceIndustry indus : prv.getIndustries()){
				// stock gaspi
				for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> goodStock : indus.getStock().object2LongEntrySet()) {
					goodStock.setValue((goodStock.getKey().storageLoss(goodStock.getLongValue(), nbDays)));
				}
			}
			// stock gaspi
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Good> goodStock : prv.getLandCommerce().getStock().object2LongEntrySet()) {
				goodStock.setValue((goodStock.getKey().storageLoss(goodStock.getLongValue(), nbDays)));
			}
			
			// useful accumulators for computing prices
			// prv.setMoneyChangePerDay(1 + bfrThisTurn / nbDays);
			long totalMoney = 0;
			double tempCoeff = prv.getMoneyConsolidated() / (1.0 + prv.getMoneyChangePerDayConsolidated()*ProvinceGoods.nbDayMarketBFR);
			plog(",\"province_resume\":{\"money\":" + f(prv.getMoney() / 1000f) + ", \"Mprevious\":" + f(prv.getPreviousMoney() / 1000f));
			plog(", \"Mconsolidated\":" + f(prv.getMoneyConsolidated() / 1000f));
			plog(", \"Mchange_day\": " + f(prv.getMoneyChangePerDay() / 1000f));
			plog(", \"Mchange_day_consoli\": " + f(prv.getMoneyChangePerDayConsolidated() / 1000f));
			plogln(", \"coeff\": " + f(tempCoeff) + ", \"coeffcomput\":" + f(1f / (1 + tempCoeff * tempCoeff)));
			totalMoney += prv.getMoney();
			List<ProvinceIndustry> lstPrvIndus = new ArrayList<>(prv.getIndustries());
			lstPrvIndus.sort((o1,o2) -> o1.getName().compareTo(o2.getName()));
			for (ProvinceIndustry indus : lstPrvIndus) {
				int nbEmp = 0;
				for (Pop pop : prv.getPops())
					nbEmp += pop.getNbMensEmployed(indus);
				plog(", \"" + indus.getIndustry() + "\":{");
				for (int tab = 3; indus.getName().length() < tab * 8 + 2 && tab > 0; tab--) {
					plog("\t");
				}
				plogln("\"money\":" + f(indus.getMoney() / 1000f) + ", \"Emp\":" + nbEmp + ", \"sal\":" + f(indus.getPreviousSalary() / 1000f) + ", \"buy\":"
						+ f(prv.getStock().get(indus.getIndustry().getGood()).getPriceBuyFromMarket(nbDays) / 1000d) + ", \"stpr\":"
						+ f(prv.getStock().get(indus.getIndustry().getGood()).getStockPrice() / 1000d) + ", \"prc\":"
						+ f(prv.getStock().get(indus.getIndustry().getGood()).getPrice() / 1000d) + ", \"sll\":"
						+ f(prv.getStock().get(indus.getIndustry().getGood()).getPriceSellToMarket(nbDays) / 1000f) 
						+ ", \"nbP\":"+ indus.getPreviousProduction() 
						+ ", \"nbP'\":"+ (int)(prv.getStock().get(indus.getIndustry().getGood()).getNbProduceThisPeriod())
						+ ", \"nbC\":"+ (int)(prv.getStock().get(indus.getIndustry().getGood()).getNbConsumeThisPeriod())
						+ ", \"nbRPrd\":"+ (int)(prv.getStock().get(indus.getIndustry().getGood()).getNbProducePerDayConsolidated())
						+ ", \"nbRCsm\":"+ (int)(prv.getStock().get(indus.getIndustry().getGood()).getNbConsumePerDayConsolidated())
						+ ",\"Stk\":" + prv.getStock().get(indus.getIndustry().getGood()).getStock() + "}");
				totalMoney += indus.getMoney();
			}
			{ // land commerce
				ProvinceCommerce indus = prv.getLandCommerce();
				int nbEmp = 0;
				for (Pop pop : prv.getPops())
					nbEmp += pop.getNbMensEmployed(prv.getLandCommerce());
				plog(", \"land commerce\":{");
				for (int tab = 3; "land commerce".length() < tab * 8 + 2 && tab > 0; tab--) {
					plog("\t");
				}
				plogln("\"money\":" + f(indus.getMoney() / 1000f) + ", \"Emp\":" + nbEmp + ", \"sal\":" + f(indus.getPreviousSalary() / 1000f)
				+ ", \"previousNbGoodsExchanges\":"+ indus.getNbPreviousExchange()
				+ ", \"nbStockCart\":"+ indus.getStock().getLong(Good.get("wood_goods"))+  "}");
				totalMoney += indus.getMoney();
			}
			long nbHab = 0;
			long nbAdults = 0;
			for (Pop pop : prv.getPops()) {
				plog(",\t\"Pop_" + pop + "\":{\t\"money\":" + fm(pop.getMoney()) + ",   \t\"nbAdult\":" + pop.getNbAdult() 
				+ ",\"gain\":"+ fm(pop.getGain()) + ",\"spend\":"+ fm(pop.getSpend()) + "");
				plog( ",\"sal\":"+ fm(pop.getGainSalary()) + ",\"inv\":"+ fm(pop.getGainInvest()) + "");
				plog( ",\"chom\":"+ i(pop.getNbMensChomage()));
				pop.resetGain();
				pop.resetSpend();
				long nbFood = pop.getStock().getLong(Good.get("meat"));
				nbFood += pop.getStock().getLong(Good.get("crop"));
				plogln(", \"nbDayFood\":" + nbFood / Math.max(1,pop.getNbAdult()) /*+ ", \"nbFood\":" + nbFood */+ ", \"foodEff\":" + f(pop.getFoodEffectiveness()) + "}");
				totalMoney += pop.getMoney();
				nbHab += pop.getNbChildren()+pop.getNbAdult()+pop.getNbElder();
				nbAdults += pop.getNbAdult();
			}
//			plog("\"TOTAL_money\": " + totalMoney);
			plog("\"TOTAL_POP\": " + nbHab +", \"nbAdults\":"+nbAdults+", \"prvTotalMoney\":"+fm(totalMoney));
			plogln("}");
		}
		

		for (Province prv : allPrvs) {
			for(Entry<Good, ProvinceGoods> entry : prv.getStock().entrySet()){
				entry.getValue().setNbConsumeThisPeriod(0);
				entry.getValue().setNbProduceThisPeriod(0);
			}
		}
		// TODO
		// for(Province prv: allPrvs){
		// emigration(prv, nbDays);
		// }
	}

	@Deprecated
	private void doTurn(Province prv, int nbDays) {
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

		produce(prv, nbDays);
		doNavalImportExport(prv, nbDays);
		doLandImportExport(prv, nbDays);
		consume(prv, nbDays);
		setPrices(prv, nbDays);
		moveWorkers(prv, nbDays);
		// popSellThingsIfNeedMoney(prv, nbDays);

		// stock gaspi
		for (Entry<Good, ProvinceGoods> goodStock : prv.getStock().entrySet()) {
			goodStock.getValue().setStock((goodStock.getKey().storageLoss(goodStock.getValue().getStock(), nbDays)));
		}

	}

	private void doNavalImportExport(Province prv, int nbDays) {
		// check if this province can make naval commerce
		// TODO buy a boat with pop money if none are present, same with
		// commerce guys.
		// TODO like land, but with boats instead of cart (1cart=1men but
		// 1boat=many mens)
	}

	private void doLandImportExport(Province prv, int nbDays) {
		// create the list of imported & exported goods, sorted by benefice.
		// List<TradeTry> listOfTrades = new ArrayList<>();
		plogln(",\"doLandImportExport\":{\"prv\":\""+prv.toString()+"\"");

		final Object2LongMap<TradeRoute> trAlreadyTaken = new Object2LongOpenHashMap<>();
		for (TradeRoute tr : prv.getTradeRoute()) {
			trAlreadyTaken.put(tr, 0);
		}
		prv.getLandCommerce().setNbPreviousExchange(0);

		// for each pop TODO: richer pop first
		//TODO: not for each pop now, HAVE TO REMOVE THIS LOOP
//		for (Pop pop : prv.getPops()) {
//			logln(",\"pop"+Pop.popTypeName.get(pop.getPopType())+"\":{\"pop\":\""+pop+"\"");
			final List<TradeRoute> listOfTrades = new ArrayList<>();

			// check our transport capacity (from number of merchant,
			// efficiency, and merchant tools
			long nbMerchants = 0;
			for (Pop pop : prv.getPops()) {
				nbMerchants += pop.getNbMensEmployed(prv.getLandCommerce());
			}
			plog(",\"nbMerchants\":"+nbMerchants);
			if(nbMerchants==0){
				log("}");
				return;
			}
			final ProvinceCommerce data = prv.getLandCommerce();
			final long transportCapa = data.computeCapacity(nbMerchants);
			plog(",\"transportCapa\":"+transportCapa);
			final float capaPerMen = transportCapa / (float) nbMerchants;

			// init : give all pop money to merchant (loan)
			data.setPreviousSalary(0);
			long loan = 0;
			for (Pop pop : prv.getPops()) {loan+=pop.getMoney();}
			logln(",\"loan\":"+loan);
			data.setMoney(data.getMoney() + loan);

			// importedGoods
			final Object2DoubleMap<TradeRoute> maxProfitability = new Object2DoubleOpenHashMap<>();
			final Map<TradeRoute, List<TradeTry>> maxProfitabilityTR = new HashMap<>();
			// for each nearby province
			int totalCapacityTR = 0;
			double bestProfitability = 0;
			for (TradeRoute tr : prv.getTradeRoute()) {
				logln(",\"tr\":{\"dest\":\""+tr.getOtherEnd(prv)+"\"");
				if (tr.isBoatTR())
					continue;
				double bestProfitabilityTR = 0;
				Province otherOne = tr.getOtherEnd(prv);
				maxProfitabilityTR.put(tr, new ArrayList<TradeTry>());
				// look to each goods i can buy
				for (Entry<Good, ProvinceGoods> otherGoods : otherOne.getStock().entrySet()) {
					logln(",\"good\":{\"name\":\""+otherGoods.getKey()+"\"");
					Good good = otherGoods.getKey();
					log(",\"stock\":"+otherGoods.getValue().getStock());
					if(otherGoods.getValue().getStock() == 0){
						log("}");
						continue;
					}
					// look at how much it cost here (double if not present
					// ever)
					long hisBuyCost = otherGoods.getValue().getPriceBuyFromMarket(nbDays);
					long hisSellCost = otherGoods.getValue().getPriceSellToMarket(nbDays);
					long myBuyCost = hisSellCost;
					long mySellCost = hisBuyCost;
					ProvinceGoods myGoods = prv.getStock().get(good);
					if (myGoods == null || myGoods.getStock() == 0) {
						if (myGoods == null) {
							prv.getStock().put(good, new ProvinceGoods(good, prv));
						}
						myBuyCost *= 2;
						// can't move naval stuff by land
						// & if prv is not coastal i don't want it anyway.
						if (good.isNaval()) {
							myBuyCost = 0;
							mySellCost = Integer.MAX_VALUE;
						}
					} else {
						myBuyCost = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
						mySellCost = prv.getStock().get(good).getPriceSellToMarket(nbDays);
					}
					// if profitable, add ; profitablity = gain in money per
					// merchant
					// note: we choose the kilogram (liter) as the base unit for
					// transortation
					// so it's "base import per kilo"
					double diffImport = (1000 * (myBuyCost - hisSellCost) / (double) good.getWeight());
					double diffExport = (1000 * (hisBuyCost - mySellCost) / (double) good.getWeight());
					if (diffImport > diffExport) { // IMPORT
						if (diffImport <= 0){
							log("}");
							continue; // no way
						}

						if (bestProfitability < diffImport)
							bestProfitability = diffImport;
						if (bestProfitabilityTR < diffImport)
							bestProfitabilityTR = diffImport;
						maxProfitabilityTR.get(tr).add(new TradeTry(good, otherOne, prv, myBuyCost, hisSellCost, diffImport));
					} else { // EXPORT
						if (diffExport <= 0){
							log("}");
							continue; // no way
						}
						if (bestProfitability < diffExport)
							bestProfitability = diffExport;
						if (bestProfitabilityTR < diffExport)
							bestProfitabilityTR = diffExport;
						maxProfitabilityTR.get(tr).add(new TradeTry(good, prv, otherOne, hisBuyCost, mySellCost, diffExport));
					}

					log(",\"diffImport\":"+diffImport);
					log(",\"diffExport\":"+diffExport);
					logln("}");
				}
				maxProfitability.put(tr, bestProfitabilityTR);
				totalCapacityTR += tr.getMaxMerchant();
				listOfTrades.add(tr);
				maxProfitabilityTR.get(tr).sort((arg0, arg1) -> Long.compare(arg1.buyerPrice - arg1.sellerPrice, arg0.buyerPrice - arg0.sellerPrice));
				logln("}");
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
			long merchantToFill = nbMerchants;//pop.getNbMensEmployed(prv.getLandCommerce());
//			for (Pop pop : prv.getPops()) {merchantToFill+=pop.getNbMensEmployed(prv.getLandCommerce());}
			final long nbMerchantDontCare = (long) ( merchantToFill * 0.7f);
			for (TradeRoute tr : listOfTrades) {
				Province otherOne = tr.getOtherEnd(prv);
				long maxMerchantForThisTR = tr.getMaxMerchant() - trAlreadyTaken.getLong(tr);
				if (maxMerchantForThisTR > 0) {
					// get the number of merchant we have here.
					final long trMerchantUsed = (long) Math.min(maxMerchantForThisTR, nbMerchantDontCare * tr.getMaxMerchant() / (float) totalCapacityTR);
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
						float capPercent = stock / (float) maxCapa;
//						float capPercent = 0.5f;
						long nbGoodsToChange = (long) (capPercent * capaPerMen * trMerchantUsed);
						if (nbGoodsToChange > stock) {
							// max it
							nbGoodsToChange = stock;
							// other merchants do nothing, they lost the
							// contract!
						}
//						if(tradeOffer.to.getMoney()<nbGoodsToChange*tradeOffer.buyerPrice){
//							nbGoodsToChange = Math.max(0, tradeOffer.to.getMoney()) / tradeOffer.buyerPrice;
//						}
						if(tradeOffer.to.getMoneyChangePerDay()/10<nbGoodsToChange*tradeOffer.buyerPrice){
							nbGoodsToChange = Math.max(0, tradeOffer.to.getMoneyChangePerDay()/10) / tradeOffer.buyerPrice;
						}
						availableMerchantSlot -= (long) (nbGoodsToChange / (capaPerMen));
						log(",\"exchange "+tradeOffer.good+"\":{\"nbGoods\":"+nbGoodsToChange);
						log(",\"stock\":"+stock);
						log(",\"maxCapa\":"+maxCapa);
						log(",\"trMerchantUsed\":"+trMerchantUsed);
						log(",\"capaPerMen\":"+capaPerMen);
						log(",\"capPercent\":"+capPercent);
						log(",\"availableMerchantSlot\":"+availableMerchantSlot);
						log(",\"from\":\""+tradeOffer.from);
						log("\",\"sell@\":"+tradeOffer.sellerPrice);
						log(",\"moneyF@\":"+tradeOffer.from.getMoney());
						log(",\"to\":\""+tradeOffer.to);
						log("\",\"buy@\":"+tradeOffer.buyerPrice);
						log(",\"moneyT@\":"+tradeOffer.to.getMoney());
						log(",\"benef/kilo\":"+tradeOffer.beneficePerKilo);
						logln("}");
						data.addNbPreviousExchange(nbGoodsToChange);
						// change stock
						tradeOffer.from.getStock().get(tradeOffer.good).addStock(-nbGoodsToChange);
						tradeOffer.to.getStock().get(tradeOffer.good).addStock(nbGoodsToChange);
						tradeOffer.from.addMoney(nbGoodsToChange * tradeOffer.sellerPrice); 
						tradeOffer.to.addMoney(-nbGoodsToChange * tradeOffer.buyerPrice);
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
						long nbGoodsToChange = Math.min(stock, (long) (availableMerchantSlot * capaPerMen));
						if(tradeOffer.to.getMoney()<nbGoodsToChange*tradeOffer.buyerPrice){
							nbGoodsToChange =  Math.max(0, tradeOffer.to.getMoney()) / tradeOffer.buyerPrice;
						}
						availableMerchantSlot -= (long) (nbGoodsToChange / (capaPerMen));
						log(",\"AVIDexchange "+tradeOffer.good+"\":{\"nbGoods\":"+nbGoodsToChange);
						log(",\"stock\":"+stock);
						log(",\"maxStock\":"+tradeOffer.from.getStock().get(tradeOffer.good).getStock());
						log(",\"maxCapa\":"+(long) (availableMerchantSlot * capaPerMen));
						log(",\"from\":\""+tradeOffer.from);
						log("\",\"capaPerMen\":"+capaPerMen);
						log(",\"sell@\":"+tradeOffer.sellerPrice);
						log("\",\"moneyF@\":"+tradeOffer.from.getMoney());
						log(",\"to\":\""+tradeOffer.to);
						log("\",\"buy@\":"+tradeOffer.buyerPrice);
						log("\",\"moneyT@\":"+tradeOffer.to.getMoney());
						log(",\"benef/kilo\":"+tradeOffer.beneficePerKilo);
						logln("}");
						data.addNbPreviousExchange(nbGoodsToChange);
						// change stock
						tradeOffer.from.getStock().get(tradeOffer.good).addStock(-nbGoodsToChange);
						tradeOffer.to.getStock().get(tradeOffer.good).addStock(nbGoodsToChange);
						// he sell, we (the merchant) give him money
						tradeOffer.from.addMoney(nbGoodsToChange * tradeOffer.sellerPrice); 
						// he buy, he give us (merchant) money
						tradeOffer.to.addMoney(-nbGoodsToChange * tradeOffer.buyerPrice); 
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
						System.err.println("Error, phantom merchants(2)");
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
			// TODO: use gross marge/2 to compute salary, and give the excedent
			// cash to investors
			// data.addToPreviousSalary(data.getMoney() / 2);
			long moneySalary = data.getMoney() / 2;
			for(Pop pop : prv.getPops()){
				pop.addMoney(moneySalary/prv.getPops().size());
			}
			data.setPreviousSalary(moneySalary / (double)nbMerchants);
			logln(",\"moneyGivenToPop\":"+fm((moneySalary / (double)nbMerchants)));
			data.setMoney(data.getMoney() - moneySalary);
			plogln("}");
//		}
//		logln("}");
	}

	protected static class TradeTry {
		double beneficePerKilo;
		long buyerPrice;
		long sellerPrice;
		Province from;
		Province to;
		Good good;

		public TradeTry(Good good, Province from, Province to, long buyerPrice, long sellerPrice, double beneficePerKilo) {
			super();
			this.good = good;
			this.from = from;
			this.to = to;
			this.buyerPrice = buyerPrice;
			this.sellerPrice = sellerPrice;
			this.beneficePerKilo = beneficePerKilo;
		}

	}

	public float ratioSalaryInvest = 0.5f;
	private void produce(Province prv, int nbDays) {
		// produce
		for (final ProvinceIndustry indus : prv.getIndustries()) {

			logln(", \"prvIndus_" + indus + "\":{\"indus\":\"" + indus.getIndustry().getName() + "\",\"goodProduced\":\"" + indus.getIndustry().getGood()
					+ "\", \"money\":" + fm(indus.getMoney()) + "");
			// compute marge
			// ProvinceGoods goodPrice=
			// prv.getStock().get(indus.getIndustry().getGood());
			ProvinceGoods prvGood = prv.getStock().get(indus.getIndustry().getGood());
			long goodPrice = prvGood.getPriceSellToMarket(nbDays);
			 // can't sell for less than 1 cent
			long marge = goodPrice - (indus.getRawGoodsCost() + 10);
			
			//check if price can't be reset at a real high price.
			if (marge <= 0 && prvGood.getPrice() != 0 && prvGood.getStock() == 0 && prvGood.getNbProducePerDayConsolidated() < 1) {
				long newPrice = (long) ((prvGood.getPrice() * indus.getRawGoodsCost() * 1.1) / (double)goodPrice);
				logln(",\"production_problem\":\" not enough marge (" + marge + ", "
						+ indus.getRawGoodsCost() + " ), price increased from "+fm(prvGood.getPrice())+" to "+fm(newPrice)+"\"");
				prvGood.setPrice(Math.min(newPrice, prvGood.getPrice()*2)); 
				goodPrice = prvGood.getPriceSellToMarket(nbDays);
				marge = goodPrice - (indus.getRawGoodsCost() + 10);
			}
			//check if price is high enough to warrant a production run
			if (marge <= 0 && prvGood.getPrice() != 0) {
				if (prvGood.getStock() == 0) {
					// it seems the market choose a too low price
					// negociate with fake demand (maybe he doesn't know our
					// product can be desired)
					prvGood.setNbConsumeThisPeriod(10 + prvGood.getNbConsumeThisPeriod());
				}
				prvGood.setNbConsumeThisPeriod(1 + prvGood.getNbConsumeThisPeriod());

//				logln(",\"production_problem\":\"prvGood.getStock()=(" + prvGood.getStock() + ", prvGood.getNbProducePerDayConsolidated()=" + prvGood.getNbProducePerDayConsolidated()+"\"");
				logln(",\"production_problem\":\"(" + prvGood.getPriceSellToMarket(nbDays) + ") is null or not enough marge (" + marge + ", "
						+ indus.getRawGoodsCost() + " )\"");
				// chomage technique
				industryPayNoProduction(prv, indus, nbDays);

			} else {

				// get employes
				long nbEmployes = 0;
				for (Pop pop : prv.getPops()) {
					nbEmployes += pop.getNbMensEmployed(indus);
					log(", \"nbEmployed_" + pop + "\":" + pop.getNbMensEmployed(indus));
				}
				log(", \"nbEmployed\":" + nbEmployes);
				// choose first price from raw cost + honest salary
				if (prvGood.getPrice() == 0) {
					//note: this is dead code, as a good can't have a price below 1~5
					// get mean salary
					long mean = 0;
					long nbEmployedOthers = 0;
					for (Pop pop : prv.getPops()) {
						for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus2 : pop.getNbMensEmployed().object2LongEntrySet()) {
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
						prvGood.setPrice(indus.getRawGoodsCost() + (long) ((mean * nbEmployes)));
					} else {
						prvGood.setPrice(10);
					}
					goodPrice = prvGood.getPriceSellToMarket(nbDays);
					marge = goodPrice - indus.getRawGoodsCost();
				}

				// produce & pay people

				// produce
				// here it should use our industry money
				final long quantity = indus.getIndustry().produce(indus, prv.getPops(), nbDays);

				indus.setPreviousProduction(quantity);

				if (quantity == 0) {
					logln(", \"production_problem\":\"zero production\"");
					// check if we need investment
					industryPayNoProduction(prv, indus, nbDays);
					logln("}");
					continue;
				}
				if (nbEmployes <= 0) {
					System.err.println("Error, industry " + indus.getName() + " has produced " + quantity + " things with " + nbEmployes + " inside!!!");
				}
				
				//if new product (from a long time), set a new price
				//completement fuck�, mais c'est overrite par qqchose d'autre (setprice?)
//				if(prvGood.getNbConsumeThisPeriod()==0 &&prvGood.getNbConsumePerDayConsolidated()<20 && prvGood.getStock()<1){
//					// get mean salary
//					long mean = 0;
//					long nbEmployedOthers = 0;
//					for (Pop pop : prv.getPops()) {
//						for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus2 : pop.getNbMensEmployed().object2LongEntrySet()) {
//							if (indus2.getKey().getPreviousSalary() > 0) {
//								mean += indus2.getLongValue() * indus2.getKey().getPreviousSalary();
//								nbEmployedOthers += indus2.getLongValue();
//							}
//						}
//					}
//					mean = (long) (mean / nbEmployedOthers);
//					//TODO indus.getIndustry().estimateRawGoodCost(quantity)
//					long newPrice = mean * nbEmployes / quantity; //+ indus.getIndustry().estimateRawGoodCost(quantity);
//					newPrice = (long) (newPrice * 3);
//					if(newPrice>10){
//						plog(", \""+indus.getIndustry().getGood()+"  old_price\": " + fm(prvGood.getPrice()) + "");
//						plogln(", \"create_newPrice\": " + fm(newPrice) + "");
//						prvGood.setPrice(newPrice);
//					}
//				}

				// sell to market
				final long totalGain = indus.getIndustry().sellProductToMarket(prv, quantity, nbDays);
				logln(", \"sell_gain\": " + fm(totalGain) + "");
				logln(", \""+indus+" produce_nb\":" + quantity + ", \"money_after_prod\":" + fm(indus.getMoney()));

				NeedWish wish = indus.getNeed().moneyNeeded(prv, indus.getMoney(), nbDays);
				final long investNeeded = wish.getMoney();

				// give money to workers
				// TODO : change share (salary vs marge (profit+investment))
				// from civilization vars
				// => if low marge, low insvestment => decline. If high marge =>
				// increase of inequality (or not), unhappiness of poor people
				// compute how many we need to keep going (increase prod)

				// TODO redo
				long moneyToProduce = (long) (indus.getRawGoodsCost() * 1.2) + 10000;
				
				float ratio = (indus.getMoney() / (float) moneyToProduce);
				// logln("ratio="+ratio);
				ratio = 1 - (1 / (1 + ratio * ratio)); // asymptote en 1, if
														// indus.getMoney() ==
														// moneyToKeep then
														// ratio = 0.5
				// /2 for investment & investors
				// logln("ratio="+ratio);
				//i removed the ratio for now because is has caused a big starvation in poor people (salary /2 in 1 tick)
//				long moneySalary = (long) (marge * quantity /* ratio*/) / 2;
				long moneySalary = indus.getMoney() - moneyToProduce;
				moneySalary -= Math.min(moneySalary/2, investNeeded);
				long moneyBonus = (long) (moneySalary * (1-ratioSalaryInvest));
				moneySalary = moneySalary - moneyBonus;
				// logln(", indus.getMoney()="+indus.getMoney()+",
				// moneyToKeep="+moneyToKeep+", goodPrice="+goodPrice+",
				// rawcost="+indus.getRawGoodsCost()+",
				// marge="+marge+",quantity="+quantity+",ratio="+ratio+",
				// moneyToKeep="+moneyToKeep+", moneySalary="+moneySalary);
				// logln(" give " + moneySalary + " / " + indus.getMoney() + "
				// (moneyToKeep=" + moneyToKeep
				// + ", " + marge + ", " + quantity + ")");
				log(", \"marge\":" + fm(marge));
				log(", \"money_salary\":" + fm(moneySalary));
				// give money to investors
				long moneyInvestors = 0;//indus.getMoney() - moneySalary - moneyToProduce - investNeeded;
//				if(moneyBonus < (indus.getMoney() - moneySalary)/3){
//					moneyBonus = (indus.getMoney() - moneySalary)/3;
//				}
				if (moneyBonus > 0) {
					// try to keep 3 month of salary in reserve
					// logln("moneyBonus(slow)! "+moneyBonus);
					ratio = (moneyBonus / (float) moneyToProduce + investNeeded);
					// logln("ratio(slow)! "+ratio);
					ratio = 0.2f - (0.2f / (1 + ratio * ratio));
					// logln("ratio(slow)! "+ratio);
					moneyInvestors = (long) (moneyBonus * (1 - ratio));
					if (moneyBonus > moneySalary * 3) {
						// logln("moneybonus: all possible! ");//(but with 3
						// ticks of salaries in reserve ? (
						// "+moneySalary*3+")");
						moneyInvestors = moneyBonus - (long) (moneySalary * 3 / (float) nbDays);
						// logln("moneyBonus2! "+moneyBonus);
						// ratio = (moneyBonus / moneyToKeep);
						// logln("ratio2! "+ratio);
						// ratio = 1f - (1f / (1 + ratio * ratio));
						// logln("ratio2! "+ratio);
						// moneyToGive += (moneyBonus * ratio);
					}
				}
				//Not enough invest money, grab the corrected version from the other pc?
				log(", \"money_investors\":" + fm(moneyInvestors));
				logln(", \"money_kept\":" + fm((indus.getMoney() - moneyInvestors - moneySalary)));
				// safeguard
				if (moneyInvestors + moneySalary > indus.getMoney())
					System.err.println("error in money distribution algorithm");
				
				//check for salary problem.
				if(moneyInvestors > moneySalary * 5 && totalGain>0){
					//give all marge to salary
					long newMoneySalary = Math.min(1+moneyInvestors/5, marge * quantity);
					moneyInvestors -= newMoneySalary + moneySalary;
					moneySalary = newMoneySalary;
				}

				// distribute money
				// TODO: do not reduce the salary that quick. not much than 1%
				// per day
				indus.setPreviousSalary((moneySalary) / (double) (nbEmployes));
				logln(", \"individual_salary\": " + (moneySalary / (double) (nbEmployes)));
				long nbOwner = prv.getPops().stream().filter(pop -> pop.getCoeffInvestors() > 0).mapToLong(pop -> pop.getNbAdult() * pop.getCoeffInvestors())
						.sum();
				long nbOwnerVerif = 0;
				for (Pop pop : prv.getPops()) {
					if (pop.getCoeffInvestors() > 0) {
						nbOwnerVerif += pop.getNbAdult() * pop.getCoeffInvestors();
					}
				}
				if (nbOwner != nbOwnerVerif)
					System.err.println("stream error : " + nbOwner + " != " + nbOwnerVerif);
				// logln(", \"give\": {\"nbPops\":"+prv.getPops());
				for (Pop pop : prv.getPops()) {
					log(", \"give_" + pop + "\":{\"money_before\":" + f(pop.getMoney() / 1000f));
					if (pop.getCoeffInvestors() > 0) {
						// if owner
						long money = (long) (moneyInvestors * pop.getNbAdult() * pop.getCoeffInvestors()) /nbOwner;
						indus.addMoney(-money);
						// log(Pop.popTypeName.get(pop.getPopType())+" pop has
						// "+pop.getMoney());
						pop.addMoney(money, Pop.MONEY_INVEST);
						// logln(" and rich pop gain "+money+ ", and now has
						// "+pop.getMoney());
						log(", \"money_investors\": " + f(money / 1000f));
					}
					// salary
					long employesHere = pop.getNbMensEmployed(indus);
					long money = (long) (moneySalary * employesHere / (float) nbEmployes);
					indus.addMoney(-money);
					pop.addMoney(money, Pop.MONEY_SALARY);
					log(", \"money_salary\": " + f(money / 1000f));
					logln(", \"money_after\": " + f(pop.getMoney() / 1000f) + "}");
				}
				// logln("}")

				if (indus.getMoney() < 0)
					System.err.println(
							"Error, in industral" + indus.getName() + "@" + prv.x + ":" + prv.y + " money salary : negative value " + indus.getMoney());

				log(", \"indus_money_after\": " + f(indus.getMoney() / 1000f) + " ");
				// we will use our money for our need in the consume phase (or
				// for selling)

			}
			logln("}");
		}
	}

	// private void doImport(Province prv, int nbDays) {
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

	private void industryPayNoProduction(Province prv, ProvinceIndustry indus, int nbDays) {

		indus.setPreviousProduction(0);

		long nbOwner = 0;
		long nbEmployes = 0;
		for (Pop pop : prv.getPops()) {
			if (pop.getCoeffInvestors() > 0) {
				nbOwner += pop.getNbAdult() * pop.getCoeffInvestors();
			}
			nbEmployes += pop.getNbMensEmployed(indus);
		}
		if (nbEmployes == 0) {
			indus.setPreviousSalary(0);
			return;
		}
		// maybe pay some people even when chomage technique?
		long moneySalary = (long) Math.min(indus.getMoney() / 10, (indus.getPreviousSalary() * nbDays * nbEmployes) / nbDays);
		indus.setPreviousSalary((moneySalary) / (double) (nbEmployes * nbDays));
		log(", \"no_prod_salary\": " + indus.getPreviousSalary());
		// repay investors
		long moneyInvestors = indus.getMoney() / 2;
		for (Pop pop : prv.getPops()) {
			log(", \"" + pop + "_money\":{\"money_before\":" + fm(pop.getMoney()));
			if (pop.getCoeffInvestors() > 0) {
				// if owner
				long money = (long) (moneyInvestors * pop.getNbAdult() * pop.getCoeffInvestors() / (float) nbOwner);
				indus.addMoney(-money);
				pop.addMoney(money, Pop.MONEY_INVEST);
				log(", \"invest_gain\":" + money + ", \"money_after_invest\":" + pop.getMoney());
			}
			// salary
			long employesHere = pop.getNbMensEmployed(indus);
			long money = (long) (moneySalary * employesHere / (float) nbEmployes);
			indus.addMoney(-money);
			pop.addMoney(money, Pop.MONEY_SALARY);
			log(", \"salary\":" + money + ", \"money_after\":" + pop.getMoney() + "}");
		}
	}

	private void consume(Province prv, int nbDays) {
		// TODO SELL THINGS IF IN NEED OF MONEY (and it's possible)
		log(", \"consume_" + prv + "\":{\"nbPop\":" + prv.getPops().size());
		// particuliers
		// note: it's a discrimination for the pop that arrive in last.
		Iterator<Pop> itPop = prv.getPops().iterator();
		while (itPop.hasNext()) {
			Pop pop = itPop.next();
			if(pop.getNbAdult()+pop.getNbChildren()+pop.getNbElder() == 0){
				continue;
			}
			// on scinde en 3
			// les riches achetent en premier
			log(", \"consume_" + pop + "\":{\"money\":" + fm(pop.getMoney()));
			popBuy(pop, pop.getNbAdult(), pop.getMoney(), nbDays);

			// TODO: if a pop is died, remove it
			if (pop.getNbAdult() == 0) {
//				itPop.remove();
				log(", \"POP IS NOW EXTINCT@" + prv.x + ":" + prv.y + "\":true");
			}
			//land
			prv.getLandCommerce().getMyNeed().spendMoney(prv, prv.getLandCommerce().getMyNeed().moneyNeeded(prv, prv.getLandCommerce().getMoney(), nbDays), nbDays);
			log("}");
		}

		// industry
		List<ProvinceIndustry> randomList = new ArrayList<>(prv.getIndustries());
		GlobalRandom.aleat.shuffle(randomList);
		for (ProvinceIndustry indus : randomList) {
			log(", \"consume_" + indus + "\":{\"money\":" + f(indus.getMoney() / 1000f));
			Needs need = indus.getNeed();
			// get employes
			long nbEmployes = 0;
			for (Pop pop : prv.getPops()) {
				nbEmployes += pop.getNbMensEmployed(indus);
			}
			NeedWish wish = need.moneyNeeded(prv, indus.getMoney(), nbDays);
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
			long moneySpent = need.spendMoney(prv, wish, nbDays);
			// indus.addMoney(-moneySpent);
			if (indus.getMoney() < 0)
				System.err.println("Error, in industral money buy: " + indus + " now has " + indus.getMoney() + "€");
			log("}");
		}
		logln("}");
	}

	private void popBuy(Pop pop, long nb, final long money, int nbDays) {
		logln(", \"hasMoney(0)\":"+pop.getMoney());
		Map<Needs, NeedWish> moneyNeeded = new HashMap<>();
		NeedWish wantedMoney = new NeedWish(0, 0, 0);
		for (Needs need : pop.getPopNeeds()) {
			NeedWish moneyNeeds = need.moneyNeeded(pop.getProvince(), money, nbDays);
			wantedMoney.add(moneyNeeds);
			moneyNeeded.put(need, moneyNeeds);
		}
		logln(", \"hasMoney(1)\":"+pop.getMoney());

		long totalSpend = 0;
		for (Needs need : pop.getPopNeeds()) {
			NeedWish wish = moneyNeeded.get(need);
			totalSpend += wish.getMoney();
			if(wish.getMoney()<0){
				System.err.println("Error, negative wish");
			}
		}
		log(", \"want_spend\":" + fm(wantedMoney.getMoney()) + ", \"totalSpend\":" + fm(totalSpend));

		logln(", \"hasMoney(2)\":"+pop.getMoney());
		logln(", \"hasMoney(3)\":"+money);
		if (money < wantedMoney.vitalNeed && wantedMoney.vitalNeed > 0) {
			// shortage! easy decision
//			float coeff = money / (float) wantedMoney.vitalNeed; //don't do that, i will create some rounding pb
			for (Needs need : pop.getPopNeeds()) {
				NeedWish wish = moneyNeeded.get(need);
				wish.luxuryNeed = 0;
				wish.normalNeed = 0;
				logln(", \"bef has vitalneed\":"+wish.vitalNeed);
				wish.vitalNeed = (long)((double)wish.vitalNeed * (double)money / (double)wantedMoney.vitalNeed);
				logln(", \"now has vitalneed\":"+wish.vitalNeed);
				if(wish.getMoney()<0){
					System.err.println("Error, negative wish");
				}
			}
			wantedMoney.luxuryNeed=0;
			wantedMoney.normalNeed=0;
			wantedMoney.vitalNeed=money;
			log(", \"shortage\":true");
		} else {
			final long useableMoney = money - wantedMoney.vitalNeed;
			logln(", \"vital\":" + fm(wantedMoney.vitalNeed) + ", \"money_novital\":" + fm(useableMoney));

			// look if we can fulfill the normal need
			long luxuryMoney = useableMoney;
			long aeffNormal = 0;
			if (useableMoney < wantedMoney.normalNeed && wantedMoney.normalNeed>0) {
				// keep a little bit for luxury
//				float coeff = useableMoney * 0.9f / (float) wantedMoney.normalNeed; //don't do that, i will create some rounding pb
				for (Needs need : pop.getPopNeeds()) {
					NeedWish wish = moneyNeeded.get(need);
					if(((double)wish.normalNeed * (double)useableMoney * 9.0 / ((double)wantedMoney.normalNeed*10.0))<0){
						System.err.println("Error, negative wish");
					}
					wish.normalNeed = (long) ((double)wish.normalNeed *(double) useableMoney * 9.0 / ((double)wantedMoney.normalNeed*10.0));
					luxuryMoney -= wish.normalNeed;
					aeffNormal += wish.normalNeed;
					if(wish.getMoney()<0){
						System.err.println("Error, negative wish");
					}
				}
				wantedMoney.normalNeed = aeffNormal;
			} else {
				luxuryMoney -= wantedMoney.normalNeed;
				aeffNormal = wantedMoney.normalNeed;
			}
			if (luxuryMoney < 0)
				System.err.println("Error, in luxury money assignment");

			logln(", \"hasMoney(4)\":"+pop.getMoney());
			logln(" ,\"normal\":" + fm(wantedMoney.normalNeed) + " , \"=>\": " + fm(aeffNormal) + " ,\"money_forlux\": " + fm(luxuryMoney));
			// try to fulfill the luxury need
			long leftoverMoney = luxuryMoney;
			long aeff = 0;
			if (luxuryMoney > 0) {
//				float coeff = Math.min(1, luxuryMoney / (float) wantedMoney.luxuryNeed);  //don't do that, i will create some rounding pb
				// logln(", \"wantLux\":"+wantedMoney.luxuryNeed+",
				// \"luxcoeff="+coeff);
				for (Needs need : pop.getPopNeeds()) {
					NeedWish wish = moneyNeeded.get(need);
					logln(", \"wishLux_" + need.getName() + "\":\"" + wish + "\", \"leftoverMoney_after" + need.getName() + "\":" + fm(leftoverMoney));
					wish.luxuryNeed = (long) ((double)wish.luxuryNeed * (double)luxuryMoney / Math.max(1, Math.max(wantedMoney.luxuryNeed, luxuryMoney)));
					leftoverMoney -= wish.luxuryNeed;
					if (leftoverMoney < 0) {
						wish.luxuryNeed += leftoverMoney;
						leftoverMoney = 0;
					}
					if(wish.getMoney()<0){
						System.err.println("Error, negative wish");
					}
				}

				logln(", \"hasMoney(5)\":"+pop.getMoney());
				// add the leftover to normal and luxury need
				float coeffLeftoverNormal = ( 0.2f * leftoverMoney) / (float) (1+wantedMoney.normalNeed);
				float coeffLeftoverLuxury = ( 0.8f * leftoverMoney) / (float) (1+wantedMoney.luxuryNeed);
				long moneyNotSpent = leftoverMoney;
				for (Needs need : pop.getPopNeeds()) {
					NeedWish wish = moneyNeeded.get(need);
					logln(", \"getwish\":\""+wish+"\"");
					long temp = (long) (wish.normalNeed * coeffLeftoverNormal);
					wish.normalNeed += temp;
					moneyNotSpent -= temp;
					temp = (long) (wish.normalNeed * coeffLeftoverLuxury);
					wish.luxuryNeed += temp;
					aeff += wish.luxuryNeed;
					moneyNotSpent -= temp;
					if (moneyNotSpent < 0) {
						wish.luxuryNeed += moneyNotSpent;
						moneyNotSpent = 0;
					}
					if(wish.getMoney()<0){
						System.err.println("Error, negative wish");
					}
					logln(", \"set lux "+need+" wish\":\""+wish+"\"");
				}
				logln(", \"hasMoney(6)\":"+pop.getMoney());
				if (moneyNotSpent < 0)
					System.err.println("Error, in leftover money assignment : pop has now " + fm(moneyNotSpent) + "€");
				// maybe some centimes are left
			}
			logln(", \"pop luxury\": " + fm(wantedMoney.luxuryNeed));// +"\"=>"+aeff+"
																		// /

			logln(", \"hasMoney(7)\":"+pop.getMoney());											// "+(useableMoney-aeffNormal));
		}
		logln(", \"hasMoney(8)\":"+pop.getMoney());
		totalSpend = 0;
		for (Needs need : pop.getPopNeeds()) {
			NeedWish wish = moneyNeeded.get(need);
			totalSpend += wish.getMoney();
			if(wish.getMoney()<0){
				System.err.println("Error, negative wish");
			}
			logln(", \"iagts on "+need.getClass().getSimpleName()+"\":\"" + wish+"\"");
		}
		logln(", \"isgoingtospend\":" + fm(totalSpend)+", \"on money\":"+fm(pop.getMoney()));

		for (Needs need : pop.getPopNeeds()) {
			NeedWish wish = moneyNeeded.get(need);
			long moneySpent = need.spendMoney(pop.getProvince(), wish, nbDays);
			logln(", \"pop_spend_" + need.getName() + "\":{\"spent\":" + fm(moneySpent) + ",\"money\":" + fm(money) + ",\"for\":\"" + need.getName()
					+ "\", \"now\": " + fm(pop.getMoney()) + "}");

			if (pop.getMoney() < 0) {
				System.err.println("ERROR: the pop has spent more than available into " + need.getName() + ".spendMoney : " + moneySpent + " / " + money
						+ "(wishLimit=" + wish.getMoney() + ")");
			}
		}
		if (pop.getMoney() < 0)
			System.err.println("Error, in pop money buy");

	}

	// private void doExport(Province prv, int nbDays) {
	// // TODO Auto-generated method stub
	// }
	static final double PRICE_CHANGE_PERIOD = 30;

	private void setPrices(Province prv, int nbDays) {
		logln(", \"set_price\":{\"nbDays\":" + nbDays);

		// Object2IntMap<Good> mimumStock = new Object2IntOpenHashMap<>();
		// Object2IntMap<Good> optimalStock = new Object2IntOpenHashMap<>();
		// for(Pop pop : prv.getPops()){
		// for(Needs need : pop.getPopNeeds()){
		// need.getMinimumStockPerPop()
		// }
		// }

		// TODO: compute the need when there are no stock ?
		
		//if not enough money inside this prv, reduce all prices a little
		long totalMoney = 0;
		totalMoney += prv.getMoney();
		for (ProvinceIndustry indus : prv.getIndustries()) {
			totalMoney += indus.getMoney();
		}
		for (Pop pop : prv.getPops()) {
			totalMoney += pop.getMoney();
		}
		totalMoney += prv.getLandCommerce().getMoney();
		totalMoney += prv.getSeaCommerce().getMoney();

		final long BFRDayNeeded = 5;
		
		//infaltion/deflation vs "money" (need X days/ticks for money to make a circle)
//		if(totalMoney < prv.getMoneyChangePerDayConsolidated() * ratioDefaltion ){
		if(totalMoney < prv.getMoneyChangePerDay() * BFRDayNeeded ){
			//be sure we don't overshoot
			if(totalMoney<prv.getMoneyChangePerDay()*BFRDayNeeded){
				//reduce all price by 1% per year if two time not enough money
				
				double ratioBetter = prv.getMoneyChangePerDayConsolidated() * BFRDayNeeded;
				ratioBetter -= totalMoney;
				ratioBetter = 1 + ratioBetter / totalMoney;
				ratioBetter = Math.min(ratioBetter, 1000);
				ratioBetter = ratioBetter * ratioBetter;
				//ratioBetter = 1/ ratioBetter;
				
//				double ratio = 1- Math.max(0.01,totalMoney/((double)prv.getMoneyChangePerDayConsolidated() * 20));
				plog(", \"deflation\":{\"prv\":\""+prv+"\", \"period\":"+nbDays+/*", \"ratio\":"+ratio+*/",\"yearly\":"+f( 10*ratioBetter)+", \"ratioBetter\":"+ratioBetter);
				double reduction = (1- (0.1*ratioBetter * nbDays / 360.0));
				for (Entry<Good, GoodsProduced> entry : oldStock.get(prv).entrySet()) {
//				for (Entry<Good, ProvinceGoods> entry :prv.getStock().entrySet()) {
					//if(prv.getStock().get(entry.getKey()).getNbConsumePerDayConsolidated()==0) continue;
					long computePrice = entry.getValue().oldPrice;
					computePrice = (long)(computePrice * reduction)  - GlobalRandom.aleat.getLeftover( ((long)(computePrice))-(double)(computePrice * reduction));
					logln(",\"reducePrice(notenoughmoney)\":{\"g\":\""+entry.getKey()+"\", \"oldPrice\":"+entry.getValue().oldPrice+", \"reduction\":"+(entry.getValue().oldPrice-computePrice)+", \"newPrice\":"+computePrice+"}");
					entry.getValue().oldPrice = (computePrice);
				}
				plogln("}");
			}else{
				plogln(", \"no deflation\":\"totmoney="+fm(totalMoney)+" > "+fm(prv.getMoneyChangePerDay())+"=changeD\"");
			}
		}else{
			//be sure we don't overshoot
			if(totalMoney>prv.getMoneyChangePerDay()*BFRDayNeeded){
				

				double ratioBetter = totalMoney;
				ratioBetter -= prv.getMoneyChangePerDayConsolidated() * BFRDayNeeded;
				ratioBetter = 1 + ratioBetter / (1+prv.getMoneyChangePerDayConsolidated() * BFRDayNeeded);
				ratioBetter = Math.min(ratioBetter, 1000);
				ratioBetter = ratioBetter * ratioBetter;
				//ratioBetter = 1/ ratioBetter;
				
//				double ratio = Math.min(0.99,1-(prv.getMoneyChangePerDayConsolidated() * 20/(double)totalMoney));
				plog(", \"inflation\":{\"prv\":\""+prv+"\", \"period\":"+nbDays+/*", \"ratio\":"+ratio+*/",\"yearly\":"+f(10*ratioBetter)+", \"ratioBetter\":"+ratioBetter);
				double increase = (1+ (0.1*ratioBetter * nbDays / 360.0));
				for (Entry<Good, GoodsProduced> entry : oldStock.get(prv).entrySet()) {
					//if(prv.getStock().get(entry.getKey()).getNbConsumePerDayConsolidated()==0) continue;
//					for (Entry<Good, ProvinceGoods> entry :prv.getStock().entrySet()) {
					long computePrice = entry.getValue().oldPrice;
					computePrice = (long)(computePrice * increase)  + 0;
					int left = GlobalRandom.aleat.getLeftover( ((double)(computePrice * increase)) - ((long)(computePrice)));
					computePrice += left;
					logln(",\"increasePrice(toomuchmoney)\":{\"g\":\""+entry.getKey()+", \"oldPrice\":"+entry.getValue().oldPrice
							+"\", \" increase\":"+(computePrice-entry.getValue().oldPrice)+", \"newPrice\":"+computePrice+"}");
					entry.getValue().oldPrice = (computePrice);
				}
				plogln("}");
			}else{
				plogln(", \"no inflation\":\"totmoney="+fm(totalMoney)+" < "+fm(prv.getMoneyChangePerDay())+"=changeD\"");
			}
		}
		

		for (Entry<Good, GoodsProduced> entry : oldStock.get(prv).entrySet()) {
			GoodsProduced gp = entry.getValue();
			logln(", \"set_price_" + entry.getKey() + "\":{\"oldPrice\":" + gp.oldPrice);
			long newPrice = 0;
//			if (gp.oldStock > 0) {
//				newPrice += gp.exportPrice * (long) gp.nbExport;
//				newPrice += gp.importPrice * (long) gp.nbImport;
//				newPrice += gp.prodPrice * (long) gp.nbProd;
//				newPrice += gp.oldPrice * (long) gp.oldStock;
//				// if( prv.getStock().get(entry.getKey()).getNbConsumePerDay()
//				// >0)
//				// logln("oldStock = "+gp.oldStock+",
//				// oldprice*oldstock = "+gp.oldPrice * (long) gp.oldStock+" / "+
//				// (1 + gp.nbExport + gp.nbImport + gp.nbProd + gp.oldStock));
//				newPrice /= (1 + gp.nbExport + gp.nbImport + gp.nbProd + gp.oldStock);
//			} else {
				// no previous, so can't infer on this.
				newPrice = gp.oldPrice;
//			}
			newPrice = Math.max(20, newPrice);
			

			// test for shortage/overproduction
			Good good = entry.getKey();
			ProvinceGoods prvGood = prv.getStock().get(good);
			prvGood.updateNbConsumeConsolidated(nbDays);
			
			if(prvGood.getNbConsumeThisPeriod() == 0 && prvGood.getStock() == 0 && prvGood.getNbProduceThisPeriod() == 0){
				//it can have a good boost on his price, i think.
				newPrice += 100; // 10 cent more! i'm crazy!!
				if(newPrice > 10000){
					newPrice = 10000;
				}
				prvGood.initNewPrice(newPrice);
			}
			
			//check if shortage are coming
//			if(prvGood.getNbProduceThisPeriodConsolidated() > prvGood.getNbConsumePerDayConsolidated() 
//					&& prvGood.getNbProduceThisPeriodConsolidated() > 1 && prvGood.getNbConsumePerDayConsolidated() > 2){
//				final double nbDaysOfStock = prvGood.getStock() / (prvGood.getNbProduceThisPeriodConsolidated()-prvGood.getNbConsumePerDayConsolidated());
//				if(nbDaysOfStock < good.getOptimalNbDayStock()*2){
//					double ratioIncreaseMore = 1 - (nbDaysOfStock/(good.getOptimalNbDayStock()*2));
//					ratioIncreaseMore = ratioIncreaseMore * ratioIncreaseMore;
//					newPrice += newPrice*ratioIncreaseMore;
//					logln(", \"QuickPriceIncrease\":"+(1+ratioIncreaseMore));
//				}
//			}
			
			//TODO: change this to take into account the speed at which the stock will be depleted (input vs output) 
			// if input << output and less than 30 days of stock, please put the price higher quickly!!
			
			// max 50% (asymptote)
			// 2 * need for a step is the objective.
			// 5 * need => -22%
			// 3 * need => -10%
			// 1.5 * need => +7%
			// 1 * need => +16%

			// double ratio = (1 + newStock) / (0.9 +
			// prv.getNbMens()*entry.getKey().getOptimalStockPerMen() +
			// (prvGood.getNbConsumePerDay() * durationInDay));
			// stock / (conso*2 + wanted stock) ? i added +conso*nbday*2, in
			// case of nbDays is > otimalstocknbDays
			final double ratio = (1 + 3 * prvGood.getStockConsolidated() + prvGood.getStock())
					/ ((1 + good.getOptimalNbDayStock() * prvGood.getNbConsumePerDayConsolidated() * 2 + prvGood.getNbConsumePerDayConsolidated() * nbDays * 2) * 4);
			
			
			
			// if( prvGood.getNbConsumePerDay() >0)
			log(", \"ratio\":" + f(ratio) + ", \"newprice\":\"" + f(newPrice) + " * " + f(0.5 + (1 / (1 + ratio))) + "="
					+ f(newPrice * (0.5 + (1 / (1 + ratio)))) + "\"");
			final double computedPrice = (newPrice * (0.5 + (1 / (1 + ratio))));
			final long updatedOldPrice = newPrice;
			if (computedPrice > updatedOldPrice) {
				// log("[ max from "+(saveNP+1)+" and " +(long)(saveNP +
				// (tempPrice - saveNP) * ((good.getVolatility() * nbDays /
				// PRICE_CHANGE_PERIOD)))+"] ");

				// double increase = (tempPrice - saveNP);
				double increaseMult = (computedPrice / updatedOldPrice) - 1;

				increaseMult *= (good.getVolatility() * nbDays / PRICE_CHANGE_PERIOD);
				
				//don't increase that much if it's in outproduciton
				if(prvGood.getNbConsumePerDayConsolidated()*(double)nbDays + prvGood.getNbConsumeThisPeriod() < prvGood.getNbProducePerDayConsolidated() + prvGood.getNbProduceThisPeriod()*(double)nbDays){
					double factor = (1+prvGood.getNbConsumePerDayConsolidated() + prvGood.getNbConsumeThisPeriod()*(double)nbDays)/(1+prvGood.getNbProducePerDayConsolidated() + prvGood.getNbProduceThisPeriod()*(double)nbDays);
					factor = Math.max(0, factor-0.5f); //don't increase the price if nbProd > 2*nbConsume
					increaseMult *= factor;
				}
				
				increaseMult += 1;

				newPrice = Math.max(updatedOldPrice + 1, (long) ((increaseMult) * updatedOldPrice));
				logln(", \"calculus\":\"increaseMult="+increaseMult+", updatedOldPrice="+updatedOldPrice+" [ max from " + f(updatedOldPrice + 1) + " and " + newPrice + "(" + f(computedPrice / updatedOldPrice) + " -> " + f((computedPrice / updatedOldPrice) - 1)
						+ " * " + f(good.getVolatility() * nbDays / PRICE_CHANGE_PERIOD) + " = " + f(increaseMult - 1) + " -> " + f(increaseMult) + ")] \"");
				
				// try to go back to the market price quickly if needed.
				if (prvGood.getStock() == 0 && newPrice < prvGood.getPriceBuyFromMarket(nbDays)) {
					logln(", \"quick increase due to no stocks\":"+prvGood.getPriceBuyFromMarket(nbDays));
					newPrice += prvGood.getPriceBuyFromMarket(nbDays);
					newPrice /= 2;
				}
				
				//edge case if underflow.
				if(newPrice-updatedOldPrice<3 && ratio>1.1){
					logln(", \"prevent underflow\":"+updatedOldPrice);
					newPrice = updatedOldPrice+3;
				}
				
				//don't increase if consumption is lower than production
				if(prvGood.getNbConsumeThisPeriod()<prvGood.getNbProduceThisPeriod()){
					logln(", \"price stagnate due to low consumptino\":"+updatedOldPrice*1.05);
					newPrice = (long) Math.min(newPrice, updatedOldPrice*1.05);
				}

				GlobalDefines.logFlush();
				if (newPrice <= 0)
					System.err.println("Error, new price is null:" + newPrice + " for " + entry.getKey().getName() + " @" + prv.x + ":" + prv.y + " " + ratio);

				// newPrice = Math.max(saveNP+1, (long)(saveNP + ((tempPrice -
				// saveNP) * ((good.getVolatility() * nbDays) /
				// PRICE_CHANGE_PERIOD))));
			} else if (computedPrice < updatedOldPrice) {
				// log("[ max from "+1+" and from min from " +(saveNP-1)+" and
				// "+(long)(saveNP + ((tempPrice - saveNP) *
				// ((good.getVolatility() * nbDays / PRICE_CHANGE_PERIOD))))+"]
				// ");
				// max to protect against high volatility that can put us lower
				// than 0
				// newPrice = Math.max(1, Math.min(saveNP-1,(long)(saveNP +
				// (tempPrice - saveNP) * ((good.getVolatility() * nbDays) /
				// PRICE_CHANGE_PERIOD))));

				// double decrease = (tempPrice - saveNP);
				double decreaseMult = computedPrice / updatedOldPrice;
				double increaseMult = (1 / decreaseMult) - 1; // =
																// saveNP/decrease
				increaseMult *= (good.getVolatility() * nbDays / PRICE_CHANGE_PERIOD);

				//don't descrease that much if it's in shortage of production
				if(prvGood.getNbConsumePerDayConsolidated() + prvGood.getNbConsumeThisPeriod()*(double)nbDays > prvGood.getNbProducePerDayConsolidated() + prvGood.getNbProduceThisPeriod()*(double)nbDays){
					double factor = (1+prvGood.getNbProducePerDayConsolidated() + prvGood.getNbProduceThisPeriod()*(double)nbDays)/(1+prvGood.getNbConsumePerDayConsolidated() + prvGood.getNbConsumeThisPeriod()*(double)nbDays);
					factor = Math.max(0, factor-0.5f); //don't decrease the price if nbConsume > 2*nbProd
					increaseMult *= factor;
				}
				
				increaseMult += 1;
				decreaseMult = 1 / increaseMult;

				newPrice = Math.max(1, Math.min(updatedOldPrice - 1, (long) (updatedOldPrice * (decreaseMult))));
				logln(", \"calculus2\":\"[ max from " + 1 + " and from min from " + (updatedOldPrice - 1) + " and " + newPrice + "(" + "(" + f(computedPrice / updatedOldPrice)
						+ " => " + f((updatedOldPrice / computedPrice)) + " -> " + f((updatedOldPrice / computedPrice) - 1) + " * "
						+ f(good.getVolatility() * nbDays / PRICE_CHANGE_PERIOD) + " = " + f(increaseMult - 1) + " -> " + f(increaseMult) + "=>"
						+ f(decreaseMult) + ")]\" ");

			}

			// try to reduce the hysteresis
			// compare the price vs the price for the previous year.

			// if( prvGood.getNbConsumePerDay() >0)
			// logln(" = "+newPrice);

			// logln( "ratio":"+(1+newStock)+" / (
			// (0.9+"+prvGood.getNbConsumePerDay() +")*"+nbDays * 2.0+")
			// = "+ratio);
			// logln( "newPrice * (0.5+(1/(1+ratio)))":"+newPrice);
			// if( prvGood.getNbConsumePerDay() >0)
			double coeffAeff = prv.getPreviousMoney() / ((1.0 + prv.getMoneyChangePerDayConsolidated()*5));
			logln(", \"new_price_" + entry.getKey().getName() + "\":  \"" + entry.getValue().oldPrice + " => " + newPrice + ", "
					+ i(prvGood.getNbConsumeThisPeriod() * nbDays) + "(" + i(prvGood.getNbConsumePerDayConsolidated() * entry.getKey().getOptimalNbDayStock())
					+ ") / " + prvGood.getStock() + "(" + i(prvGood.getStockConsolidated()) + ") each day, " + f(ratio) + ", " + f(0.5 + (1 / (1 + ratio)))
					+ ")" + " opti:" + i(prv.getNbAdult() * entry.getKey().getOptimalNbDayStock()) + ", buy=" + prvGood.getPriceBuyFromMarket(nbDays) + ", raw="
					+ prvGood.getPrice() + ", sell=" + prvGood.getPriceSellToMarket(nbDays) + ", coeff1=" + f(coeffAeff) + ", coeff2="
					+ f(1f / (1 + coeffAeff * coeffAeff)) + ", stocktoomuch:"
					+ (prvGood.getNbConsumePerDayConsolidated() * 2 * good.getOptimalNbDayStock() < prvGood.getStockConsolidated()) + ", stockratio:"
					+ f((prvGood.getNbConsumePerDayConsolidated() * 2.0 * good.getOptimalNbDayStock()) / (double)prvGood.getStockConsolidated()) + "\"}");
			if (newPrice <= 0)
				System.err.println("Error, new price is null:" + newPrice + " for " + entry.getKey().getName() + " @" + prv.x + ":" + prv.y + " " + ratio);
			
			//do not go higher than 2 000 000 /kg
			prv.getStock().get(entry.getKey()).setPrice(Math.min(newPrice, Integer.MAX_VALUE));
			// "clean" for next loop (we want to keep 2 time more than we need
			// in stock, and this method add a bit of inertia) => replaced by
			// "consolidated"
			// if (prv.getStock().get(entry.getKey()).getStock() >
			// prvGood.getNbConsumeThisPeriod() / 2) {
			// prv.getStock().get(entry.getKey()).setNbConsumeThisPeriod(prvGood.getNbConsumeThisPeriod()
			// / 2);
			// } else {
			// // don't touch it if there are not enough stock, to keep it
			// // increasing price
			// }
		}
		
		//set price of crop
		if(regulateCropPrice>0){
			ProvinceGoods crop = prv.getStock().get(Good.get("crop"));
			logln(", \"crop.getPrice()\":" + f(crop.getPrice()) + ", \"total\":" + f(crop.getPrice() * regulateCropPrice * prv.getNbAdult())
				+ ", \"totalMoney\":" + f(totalMoney)+ ", \"ratio\":" + f(totalMoney/ (float)(crop.getPrice() * regulateCropPrice * prv.getNbAdult())));
			//reduce the price if a too low amount of people can afford it /not enough money in circulation
			if(totalMoney < crop.getPrice() * regulateCropPrice * prv.getNbAdult() && crop.getPrice() >100 ){
				float ratio = totalMoney/ (float)(crop.getPrice() * regulateCropPrice * prv.getNbAdult());
				//ratio = ratio * ratio;
				log(", \"drop_crop_price\":" + f(ratio) + ", \"oldprice\":" + fm(crop.getPrice()));
				crop.setPrice((long) (crop.getPrice()*ratio));

				logln( ", \"newprice\":" + fm(crop.getPrice()));
			}
//			crop.setPrice(1000);
		}

		logln("}");
	}

	private void distributeProvinceMoney(Province prv, int nbDays) {

		if (prv.getMoney() > 0) {

			//TODO: change when time is available
			double coeff = prv.getPreviousMoney() / ((1.0 + prv.getMoneyChangePerDayConsolidated()*nbDays));
			coeff = (1f / (1 + coeff * coeff));
			// distribute half of money if coeff is at 0 (max coefffis 1);
			double percentDistribute = 0.25 * (1 - coeff);// * (1-coeff);
			long moneyInvestors = (long) (prv.getMoney() * percentDistribute + prv.getMoney() * 0.15f);

			long nbOwner = prv.getPops().stream().filter(pop -> pop.getCoeffInvestors() > 0).mapToLong(pop -> pop.getNbAdult() * pop.getCoeffInvestors()).sum();
			for (Pop pop : prv.getPops()) {
				if (pop.getCoeffInvestors() > 0) {
					// if owner
					long money = (long) (moneyInvestors * pop.getNbAdult() * pop.getCoeffInvestors() / (float) nbOwner);
					prv.addMoney(-money);
					log(", \"MoneyFromShopIn_" + prv + "_to_" + pop + "\":{\"prev_money\": \"" + pop.getMoney() / 1000 + " (1-" + f(coeff) + ") * "
							+ (prv.getMoney()) + " = " + moneyInvestors + "\"");
					pop.addMoney(money, Pop.MONEY_INVEST);
					logln(", \"investor_gain\":" + fm(money) + ", \"moneyAfter\":\"" + fm(pop.getMoney()) + " (1-" + f(coeff) + ") * " + (prv.getMoney())
							+ " = " + moneyInvestors + "\"}");
				}
			}
		}
	}

	//cache value, work because  algo is not executed in // please move it into threadlocal if calling this funct in //
	private LongInterval cacheInter = new LongInterval(0,0);
	private void moveWorkers(Province prv, int nbDays) {
		logln(", \"movePop\":{\"nb\":" + prv.getPops().size());
		Long2LongRBTreeMap mapMedian = new Long2LongRBTreeMap();

		Object2LongMap<Job> nbMensWorking = new Object2LongOpenHashMap<Job>();
		for (Pop pop : prv.getPops()) {
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus : pop.getNbMensEmployed().object2LongEntrySet()) {
				nbMensWorking.put(indus.getKey(), indus.getLongValue() + nbMensWorking.getLong(indus.getKey()));
			}
		}
		HashSet<Job> closed = new HashSet<>();
		for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> job : nbMensWorking.object2LongEntrySet()) {
			if (job.getLongValue() == 0) {
				closed.add(job.getKey());
			}
		}
		
		//compute min salary : buy enough crop for 1 individual
		long minsalary = (long) (prv.getStock().get(Good.get("crop")).getPriceBuyFromMarket(0) * 1.5f);

		// note: as we used the "job" abstraction, it count commerce and
		// industry on an equal foot. TODO: Do we do the same for the army?
		List<Pop> pops = new ArrayList<>(prv.getPops());
		pops.sort((m, n) -> -Integer.compare(m.getCoeffInvestors(), n.getCoeffInvestors()));
		for (Pop pop : pops) {
			if(pop.getNbAdult()==0) continue;
			// sort job by best salary
			List<Job> bestJob = new ArrayList<>(pop.getNbMensEmployed().keySet());
			bestJob.sort((i1, i2) -> -Double.compare(i1.getPreviousSalary(), i2.getPreviousSalary()));

			// compute mean revenu
			long mean = 0;
			mapMedian.clear();
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus : pop.getNbMensEmployed().object2LongEntrySet()) {
				// TODO multiply by an "efficiency" from education or other
				// things.
				mean += indus.getLongValue() * indus.getKey().getPreviousSalary();
				mapMedian.put(indus.getLongValue(), (long) indus.getKey().getPreviousSalary());
			}
			mean /= pop.getNbAdult();
			logln(", \"fire " + pop + "\":{\"Mean salary:\": " + f(mean / 1000f)+", \"gain/nbAdult\":"+f(pop.getGain()/(double)pop.getNbAdult()));

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
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus : pop.getNbMensEmployed().object2LongEntrySet()) {
				if (indus.getLongValue() > 0) {
					logln(", \"" + indus.getKey().getName() + " fire\":{ \"nbEmpl\":" + indus.getLongValue());
					float result = (float) indus.getKey().getPreviousSalary() / (float) mean + 0.17f;
					result *= result;
					result *= 70;
					result = 1 / result;
					result *= indus.getLongValue() * nbDays / 30f;
					if (result > 0 && result < 1)
						result = 1;
					if (indus.getKey().getPreviousSalary()*10 < minsalary) {
						logln(", \"no salary, fire all " + indus.getKey() + "! salary =\":" + indus.getKey().getPreviousSalary());
						result = indus.getLongValue();
						closed.add(indus.getKey());
					}else if (indus.getKey().getPreviousSalary() < minsalary) {
						if(indus.getLongValue() < 10){
							logln(", \"fire all " + indus.getKey() + "! salary =\":" + indus.getKey().getPreviousSalary());
							result = indus.getLongValue();
							closed.add(indus.getKey());
						}else if(indus.getLongValue() < 100){

							logln(", \"fire almost all " + indus.getKey() + "! salary =\":" + indus.getKey().getPreviousSalary());
							// if not enough, fire all
							result = indus.getLongValue()*0.6f;
						}else{

							logln(", \"fire many " + indus.getKey() + "! salary =\":" + indus.getKey().getPreviousSalary());
							// if not enough, fire all
							result = (float) (indus.getLongValue()*0.4f);
						}
					}
//					result = Math.max(result, indus.getKey().wantToFire(prv, indus.getLongValue(), nbDays));
					
//					result = indus.getKey().needFire(cacheInter, prv, pop, nbDays).minmax((long)result);
					indus.getKey().needFire(cacheInter, prv, pop, nbDays);
					log(", \"min\":" +cacheInter.min+",\"max\":" +cacheInter.max+", \"val\":"+result);
					result = cacheInter.minmax((long)result);
					logln(", \"result\":" +result);
					
					// don't fire more people than are employed inside the
					// company
					long nbFired = Math.min(indus.getLongValue() / 5, (long) result);
					if (closed.contains(indus.getKey())) {
						nbFired = Math.min(indus.getLongValue(), (long) result);
					}

					logln(", \"nbFired\":" + nbFired + ",\"from\":" + result + ",\"ratio_from_salary\":"
							+ (indus.getKey().getPreviousSalary() / (float) mean + 0.17f) + ",\"denum\":"
							+ (70 * ((float) indus.getKey().getPreviousSalary() / (float) mean + 0.17f)
									* ((float) indus.getKey().getPreviousSalary() / (float) mean + 0.17f))
							+ ",\"ratiofinal\":"
							+ 1 / (70 * ((float) indus.getKey().getPreviousSalary() / (float) mean + 0.17f)
									* ((float) indus.getKey().getPreviousSalary() / (float) mean + 0.17f))
							+ ",\"mult\":" + (indus.getLongValue() * nbDays / 30f) + "}");
					pop.addNbMensChomage(nbFired);
					// log(indus.getLongValue()+" - "+nbFired);
					// indus.setValue(indus.getLongValue() - nbFired);
					pop.addNbMensEmployed(indus.getKey(), -nbFired);
					nbMensWorking.put(indus.getKey(), nbMensWorking.getLong(indus.getKey()) - nbFired);
					// logln(" = "+indus.getLongValue());
				}else{
					closed.add(indus.getKey());
					logln(", \"already closed\":\"" + indus.getKey() + "\"");
				}

			}
			logln(", \"NBChomeurs (temp)\":" + pop.getNbMensChomage() + "}");
		}

		for (Pop pop : pops) {
			if(pop.getNbAdult()==0) continue;
			// sort job by best salary (but put closed one first)
			List<Job> bestJob = new ArrayList<>(pop.getNbMensEmployed().keySet());
			bestJob.sort((i1, i2) -> -Double.compare(pop.getNbMensEmployed(i1)==0?1000000:i1.getPreviousSalary(), pop.getNbMensEmployed(i2)==0?1000000:i2.getPreviousSalary()));
			long mean = 0;
			mapMedian.clear();
			float nbEmployed = 0;
			for (it.unimi.dsi.fastutil.objects.Object2LongMap.Entry<Job> indus : pop.getNbMensEmployed().object2LongEntrySet()) {
				// TODO multiply by an "efficiency" from education or other
				// things.
				mean += indus.getLongValue() * indus.getKey().getPreviousSalary();
				nbEmployed += indus.getLongValue();
			}
			mean /= nbEmployed;
			logln(", \"hire " + pop + "\":{\"Mean salary:\": " + f(mean / 1000f));

			// then, for each job (more prosperous can grab more mens, as they
			// have a share on the bigger pool).
			for (Job indus : bestJob) {
				long nbMensEmployedHere = pop.getNbMensEmployed(indus);
				log(", \"indus " + indus + "\":{\"nbMensEmployed\":" + nbMensEmployedHere + "");

				// can i open a new industry?
				if (nbMensWorking.getLong(indus) <= 0) {
					// can't open if i'm a newb
					if (pop.getCoeffInvestors() == 0) {
						logln(", \"can't open\":true}");
						continue;
					}
					// 1/2 life: 60days to recreate this activity
					// double limit= Math.pow(0.5, nbDays/60f);
					// reduce it for now, for testing purpose
					double limit = Math.pow(0.5, nbDays / 10f/*20f/* 60f */);
					float aleat = GlobalRandom.aleat.getInt(10000, (int) mean) / 10000f;
					if (limit > aleat) {
						logln(", \"try to open\":false}");
						continue;
					}
					logln(", \"try to open\":true, \"limit\":" + limit + ", \"aleat\":" + aleat + ",\"try to restart\":true");
				}

				// compute the % of mens employed inside vs all employed. edit: counter-productive
//				float ratioEmployed = nbMensEmployedHere / (float) nbEmployed;
				// TODO (later) compute the factor from edu and adu needed then
				// passed in a f(eduratio, %popemployed) => ratioEduHire (you
				// can't hire dumb people if all smart one are taken)
				// you should checj each indus to see how many smarts people are
				// taken, and how many you can grab now.
				float ratioEduHire = 0.8f;
				// grab ratioEduHire * employedratio * min(nbemployes/2,
				// nbchomage/2)
				// do not hire more than 100% per 6 month
				long nbHire = 2 + (long) (Math.min(1 + (nbMensEmployedHere*nbDays) / 180, /*ratioEmployed **/ ratioEduHire * pop.getNbMensChomage() / 2));
				logln(", \"i fist try to hire\":" + nbHire+", \"from\":"+pop.getNbMensChomage()/2);
				// logln("ratioEmployed="+ratioEmployed+",
				// ratioEduHire"+ratioEduHire+" menavailable="+Math.min(2 +
				// nbMensEmployed / 2, pop.getNbMensChomage() / 2));
				
				//more recruit if good salary
				if(indus.getPreviousSalary() > mean){
					double ratio = (double)indus.getPreviousSalary() / mean;
					nbHire =  (long) (Math.min(nbHire * ratio, pop.getNbMensChomage() *0.5));
					logln(", \"i then try to hire more\":" + nbHire+", \"ratio\":"+ratio);
				}
				if(indus.getPreviousSalary() > mean * 3){
					double ratio = (double)indus.getPreviousSalary() / mean;
					nbHire =  (long) (Math.min(nbHire * ratio * 0.5, pop.getNbMensChomage() *0.85));
					logln(", \"i will try to hire\":" + nbHire+", \"ratio\":"+ratio/2);
				}

				if (closed.contains(indus) && nbMensWorking.getLong(indus) == 0) {
					// if re-open, grab "many" people to jumpstart (closed
					// previously and now open)
					// but not too many (100 max)
					nbHire = (long) (Math.min(100, /*ratioEmployed **/ 3+ratioEduHire * pop.getNbMensChomage() / 20));
				}

				nbHire = indus.needHire(cacheInter, prv, pop, nbDays).minmax(nbHire);

				nbHire = Math.min(nbHire, pop.getNbMensChomage() - 1); // safeguard

				pop.addNbMensChomage(-nbHire);
				// pop.getNbMensEmployed().put(indus, nbMensEmployed + nbHire);
				pop.addNbMensEmployed(indus, nbHire);
				nbMensWorking.put(indus, nbMensWorking.getLong(indus) + nbHire);

				// make it come with investment
				if (pop.getCoeffInvestors() > 0 && nbHire>0) {
					long investment = (long) (nbHire * pop.getMoney() / (float) pop.getNbAdult());
//					if(closed.contains(indus) && nbMensWorking.getLong(indus) == 0){
//						investment += pop.getMoney()*0.1*pop.getCoeffInvestors()*pop.getCoeffInvestors();
//						investment = Math.min((long) (pop.getMoney()*0.2*pop.getCoeffInvestors()*pop.getCoeffInvestors()), investment);
//					}
					logln(",\"INVEST "+indus+" \":"+fm(investment));
					pop.addMoney(-investment, Pop.MONEY_INVEST);
					indus.addMoney(investment);
				}

				// if(nbHire>0)
				logln(", \"i try to hire\":" + nbHire + ", \"prevSal\":" + indus.getPreviousSalary() + ", \"now has\":" + pop.getNbMensEmployed(indus) + "}");
			}
			logln(", \"NBChomeurs\":" + pop.getNbMensChomage() + ", \"nbArmy\":" + pop.getNbMensInArmy() + ", \"nbJobs\": "
					+ pop.getNbMensEmployed().values().stream().mapToLong(o -> o).sum() + ", \"tot\":"
					+ (pop.getNbMensChomage() + pop.getNbMensInArmy() + pop.getNbMensEmployed().values().stream().mapToLong(o -> o).sum()) + "}");

			// TODO: army can also grab unemployed

		}
		log("}");

	}

	private void promotePop(Province prv, int nbDays) {

		logln(", \"demote/promote\":{\"pop\":" + prv.getPops().size());
		// TODO: do this by culture type, (if != pop)

		ArrayList<Pop> orderedPop = new ArrayList<>(prv.getPops());
		orderedPop.sort((o1, o2) -> Integer.compare(o1.getPopType(), o2.getPopType()));

		// demote
		for (int i = orderedPop.size() - 1; i > 0; i--) {
			Pop myPop = orderedPop.get(i);
			Pop underPop = orderedPop.get(i - 1);
			// plogln(",
			// \"demote_"+myPop+"\":{\"underPop.getGain()\":"+underPop.getGain()+",\"myPop.getGain()\":"+myPop.getGain()+"}");
			if (underPop.getGain() < myPop.getGain()) {
				double mySalary = myPop.getGain() / (double) Math.max(myPop.getNbAdult(),1);
				double underSalary = underPop.getGain() / (double) Math.max(underPop.getNbAdult(),1);
				double moves = (myPop.getNbAdult() * mySalary) - (underPop.getNbAdult() * underSalary);
				moves = moves / (mySalary + underSalary);
				if (moves > (myPop.getNbAdult() * nbDays) / 300.0 && moves > 2) {
					moves = Math.max(1, (myPop.getNbAdult() * nbDays) / 300.0);
				}
				if (moves >= 1 && myPop.getNbAdult() < 3) {
					moves = 0;
				}
				log(", \"demote " + myPop + "\":{\"nb\":" + (long) moves + ",\"myPop(before)\":" + myPop.getNbAdult() + ",\"underPop(before)\":"
						+ underPop.getNbAdult());
				myPop.addAdult(-(long) moves);
				underPop.addAdult((long) moves);

				logln(",\"myPop(after)\":" + myPop.getNbAdult() + ",\"underPop(after)\":" + underPop.getNbAdult() + "}");
			}
		}

		// promote
		for (int i = 0; i < orderedPop.size() - 1; i++) {
			Pop myPop = orderedPop.get(i);
			Pop unperPop = orderedPop.get(i + 1);
			if (unperPop.getGain() < myPop.getGain()) {
				double mySalary = myPop.getGain() / (double) Math.max(myPop.getNbAdult(),1);
				double underSalary = unperPop.getGain() / (double) Math.max(unperPop.getNbAdult(),1);
				double moves = (myPop.getNbAdult() * mySalary) - (unperPop.getNbAdult() * underSalary);
				moves = moves / (mySalary + underSalary);
				if (moves > (myPop.getNbAdult() * nbDays) / 300.0 && moves > 2) {
					moves = Math.max(1, (myPop.getNbAdult() * nbDays) / 300.0);
				}
				if (moves >= 1 && myPop.getNbAdult() < 3) {
					moves = 0;
				}
				log(", \"promote " + myPop + "\":{\"nb\":" + (long) moves + ",\"myPop(before)\":" + myPop.getNbAdult() + ",\"unperPop(before)\":"
						+ unperPop.getNbAdult());
				myPop.addAdult(-(long) moves);
				unperPop.addAdult((long) moves);

				logln(",\"myPop(after)\":" + myPop.getNbAdult() + ",\"unperPop(after)\":" + unperPop.getNbAdult() + "}");
			}
		}

		logln("}");

	}

	// TODO put this in an other place
	private void aging(final Province prv, int nbDays) {
		nbDays *= 10;
		logln(", \"aging\":{\"nbPop\":" + prv.getPops().size());
		for (Pop pop : prv.getPops()) {
			logln(", \"aging " + pop + "\":{\"nb\":" + (pop.getNbAdult() + pop.getNbChildren() + pop.getNbElder()) + ", \"nbAdults\":" + pop.getNbAdult());

			final double ratioDay = nbDays / 360.0;

			// TODO: disease (kill)
			
			//TODO: compute the natality from something?

			// create child
			// each woman has a child every ~5 years and has 10% chance of dying
			// with it., and the baby also 
			//1/5year is too low, go to 1 every 3 years ; 1/3 => nbChild == nbAdult ; 1/5 => nbchild * 2 = nbAdult
			final double nbBabies = GlobalRandom.aleat.normalLaw(ratioDay * pop.getNbAdult() / (2*5))*1;
			log(", \"nbChild\":" + pop.getNbChildren());
			pop.addChildren((long) nbBabies);
			final int motherDie = GlobalRandom.aleat.normalLaw(nbBabies / 10.0);
			pop.addAdult(-motherDie);
//			logln(",\"pop.getNbAdult() / 2\":" + (pop.getNbAdult() / 2) + ",\"nbBabies *= nbDays\":" + ((pop.getNbAdult() / 2) * nbDays)
//					+ ", \"nbBabies /= 360 * 5\":" + (((pop.getNbAdult() / 2) * nbDays) / (360 * 5)));
			logln(",\"babies\":" + nbBabies + ",\"motherDie\":" + motherDie + ", \"now\":" + pop.getNbChildren());

			// promote child [0-15[
			final float nbYearChild = prv.getOwner().getMinAgeWork();
			final int nbNewAdults = GlobalRandom.aleat.normalLaw(pop.getNbChildren() * ratioDay / nbYearChild);
			log(", \"nbAdult\":" + pop.getNbAdult());
			pop.addChildren(-nbNewAdults);
			pop.addAdult(nbNewAdults);
			logln(",\"new adults\":" + nbNewAdults + ", \"esp\":" + (pop.getNbChildren() * ratioDay / nbYearChild) + ", \"now\":" + pop.getNbAdult());

			// promote adult [15-60[ TODO: maybe make it variable or removable?
			final float nbYearWork = prv.getOwner().getAgeRetraite() - prv.getOwner().getMinAgeWork();
			final int nbOlds = GlobalRandom.aleat.normalLaw(pop.getNbAdult() * ratioDay / nbYearWork);
			log(", \"nbElder\":" + pop.getNbElder());
			pop.addAdult(-nbOlds);
			pop.addElder(nbOlds);
			logln(",\"new elders\":" + nbOlds + ", \"now\":" + pop.getNbElder());

			// natural death [60-80+[
			final float nbYearDieAtWork = Math.max(5, 80 - prv.getOwner().getMinAgeWork());
			long youngAgeDie = (long)Math.min(pop.getNbAdult(), GlobalRandom.aleat.normalLaw(pop.getNbAdult() * ratioDay / nbYearDieAtWork));
			youngAgeDie /= 2;
			pop.addAdult(-youngAgeDie);
			final float nbYearRetraite = Math.max(5, 80 - prv.getOwner().getAgeRetraite());
			final long oldAgeDie = Math.min(pop.getNbElder(),GlobalRandom.aleat.normalLaw(pop.getNbElder() / nbYearRetraite));
			pop.addElder(-oldAgeDie);
			logln(",\"adult die\":" + youngAgeDie + ",\"elder die\":" + oldAgeDie + ", \"now\":" + pop.getNbElder());

			log("}");
		}
		logln("}");
	}
}
