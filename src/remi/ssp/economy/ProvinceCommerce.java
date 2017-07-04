package remi.ssp.economy;

import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.GlobalDefines;
import remi.ssp.economy.Needs.NeedWish;
import remi.ssp.network.SimpleSerializable;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.LongInterval;
import remi.ssp_basegame.economy.BasicIndustryNeeds;
import remi.ssp_basegame.economy.ElevageIndustry;

public class ProvinceCommerce implements Job, SimpleSerializable {

	Province province;

	float efficiency=1.0f;

	// used to run
	Object2LongMap<Good> stock = new Object2LongOpenHashMap<Good>(); // raw goods + tools
	long money;// bfr
	double previousSalary;
	long nbPreviousExchange;
	
	String name;
	
	LandCommerceNeeds myNeed;
	
	

	public String getName() {
		return name;
	}

	public ProvinceCommerce(String name, Province prv) {
		this.name = name;
		myNeed = new LandCommerceNeeds();
		setProvince(prv);
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public long getMoney() {
		return money;
	}

	public void addMoney(long money) {
		this.money += money;
	}

	public void setMoney(long money) {
		this.money = money;
	}

	public Object2LongMap<Good> getStock() {
		return stock;
	}

	public double getPreviousSalary() {
		return previousSalary;
	}

	public void setPreviousSalary(double previousSalary) {
		this.previousSalary = previousSalary;
	}

	public void addToPreviousSalary(long newIncome) {
		this.previousSalary += newIncome;
	}
	
	public LandCommerceNeeds getMyNeed() {
		return myNeed;
	}

	public long getNbPreviousExchange() {
		return nbPreviousExchange;
	}
	public void addNbPreviousExchange(long nbPreviousExchange) {
		this.nbPreviousExchange += nbPreviousExchange;
	}
	public void setNbPreviousExchange(long nbPreviousExchange) {
		this.nbPreviousExchange = nbPreviousExchange;
	}

	/**
	 * 
	 * @param nbMerchants
	 *            all merchant inside this commerce "guild"
	 * @return the capacity in transportUnit (~liter)
	 */
	public long computeCapacity(final long nbMerchants) {
		// get goods
		ArrayList<Good> sortedGoods = new ArrayList<Good>(stock.keySet());
		sortedGoods.sort((o0, o1) -> -Long.compare(o0.commerceCapacityPerMen, o1.commerceCapacityPerMen));
		long capacity = nbMerchants;
		long remainingMechants = nbMerchants;
		for (Good cart : sortedGoods) {
			final long nbGoods = stock.getLong(cart);
			capacity += Math.min(nbGoods, remainingMechants) * cart.commerceCapacityPerMen;
			if (nbGoods >= remainingMechants) {
				remainingMechants = 0;
				break;
			} else {
				remainingMechants -= nbGoods;
			}
			if (cart.commerceCapacityPerMen == 0) {
				break;
			}

		}

		return (long) (capacity * efficiency) + nbMerchants;
	}

	public void load(JsonObject jsonObj, Province prv) {
		this.province = prv;
		efficiency = (float) jsonObj.getJsonNumber("eff").doubleValue();
		money = jsonObj.getJsonNumber("money").longValue();
		previousSalary = jsonObj.getJsonNumber("salary").longValue();
		JsonArray array = jsonObj.getJsonArray("stock");
		stock.clear();
		for (int i = 0; i < array.size(); i++) {
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getJsonNumber("nb").longValue());
		}
	}

	public void save(JsonObjectBuilder jsonOut) {
		jsonOut.add("eff", efficiency);
		jsonOut.add("money", money);
		jsonOut.add("salary", previousSalary);
		JsonArrayBuilder array = Json.createArrayBuilder();
		for (Entry<Good> good : stock.object2LongEntrySet()) {
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getLongValue());
			array.add(objectBuilder);
		}
		jsonOut.add("stock", array);
	}

	@Override
	public LongInterval needFire(LongInterval toReturn, Province prv, Pop pop, int nbDays) {
		return toReturn==null?new LongInterval(0, 0):toReturn.set(0, pop.getNbMensEmployed(this));
	}

	@Override
	public LongInterval needHire(LongInterval toReturn, Province prv, Pop pop, int nbDays) {
		return toReturn==null?new LongInterval(0, 20):toReturn.set(0, 10+pop.getNbMensEmployed(this)/4);
	}

	// TODO better than "just buy some wood goods"
	public class LandCommerceNeeds extends Needs {
		public Object2FloatMap<Good> toolsNeeded = new Object2FloatOpenHashMap<>();

		public LandCommerceNeeds() {
			toolsNeeded.put(Good.get("wood_goods"),1);
		}

		@Override
		public NeedWish moneyNeeded(Province prv, long totalMoneyThisTurn, int nbDays) {
			Object2LongMap<Good> currentStock = getStock();
			long maxLastProd = 0;
			for (Pop pop : prv.getPops()) {
				maxLastProd += pop.getNbMensEmployed(prv.getLandCommerce());
			}
			NeedWish wish = new NeedWish(0, 0, 0);
			for (it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry<Good> needed : toolsNeeded.object2FloatEntrySet()) {
				long price = prv.getStock().get(needed.getKey()).getPriceBuyFromMarket(nbDays);
				long nbItemCanBuy = Math.max(0,
						Math.min(prv.getStock().get(needed.getKey()).getStock(),
								(long) (2 * maxLastProd * needed.getFloatValue() * 0.9f)
										- currentStock.getLong(needed.getKey())));
				if (nbItemCanBuy < 0)
					nbItemCanBuy = 0;
				wish.normalNeed += nbItemCanBuy * price / 2;
				wish.luxuryNeed += nbItemCanBuy * price / 4;
			}
			if(wish.normalNeed>totalMoneyThisTurn){
				wish.normalNeed = totalMoneyThisTurn;
				wish.luxuryNeed = 0;
			}
			if(wish.luxuryNeed>totalMoneyThisTurn){
				wish.luxuryNeed = totalMoneyThisTurn - wish.normalNeed;
			}
			return wish;
		}

		@Override
		public long spendMoney(Province prv, NeedWish maxMoneyToSpend, int nbDays) {
			Object2LongMap<Good> currentStock = getStock();
			long maxLastProd = 0;
			for (Pop pop : prv.getPops()) {
				maxLastProd += pop.getNbMensEmployed(prv.getLandCommerce());
			}
			long rawGoodCost = 0;
			//TODO: better one.
			Map<Good, ProvinceGoods> goodStock = prv.getStock();
//			logln("0indus "+myIndus.getName()+" has "+myIndus.getMoney()+" : "+maxMoneyToSpend+", lastProd = "+maxLastProd);
			final long maxMoney = maxMoneyToSpend.getMoney();
			long moneyToSpend = maxMoney;
			for(it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry<Good> needed : toolsNeeded.object2FloatEntrySet()){
				long currentStockNumber = currentStock.getLong(needed.getKey());
				ProvinceGoods market = goodStock.get(needed.getKey());
				long quantityBuy = Math.max(0, Math.min(market.getStock(), (int)(maxLastProd * needed.getFloatValue()) - currentStockNumber));
//				logln("2indus "+myIndus.getName()+" want to buy "+quantityBuy+" "+needed.getKey().getName()
//						+"( "+maxLastProd+" * "+needed.getFloatValue()+" - "+currentStockNumber+")");
				long price = market.getPriceBuyFromMarket(nbDays);
				if(quantityBuy * price > moneyToSpend){
//					logln("2indus "+myIndus.getName()+" can't buy more than "+(moneyToSpend/price)+" "+needed.getKey().getName()+" @"+price+"€ ("+moneyToSpend+")");
					quantityBuy = (moneyToSpend / price);
				}
				//buy
				moneyToSpend  -= storeProductFromMarket(needed.getKey(), quantityBuy, nbDays);
//				moneyToSpend  -= quantityBuy * price;
//				if(moneyToSpend <0){
//					System.err.println("error "+moneyToSpend+"<0");
//				}
//				prv.addMoney(quantityBuy * price);
//				logln("2indus "+myIndus.getName()+" buy "+quantityBuy+" "+needed.getKey().getName());
//				if(quantityBuy<0){
//					logln(moneyToSpend+" "+market.getStock()+" , "+( (int)(maxLastProd * needed.getFloatValue()) - currentStockNumber));
//				}
//				market.addNbConsumePerDay(quantityBuy / (float)nbDays);
//				currentStock.put(needed.getKey(),currentStockNumber + quantityBuy);
//				market.addStock( -quantityBuy);
			}
			if (getMoney() < 0)
				System.err.println("Error, in commerce land buy: now has "+getMoney()+"!!");
			
			return maxMoney - moneyToSpend;
		}
		

		protected long storeProductFromMarket(Good good, long quantity, int nbDays){
			Province prv = getProvince();
			long price = prv.getStock().get(good).getPriceBuyFromMarket(nbDays);
			long moneyExch = price*quantity;
			prv.addMoney(moneyExch);
//			prv.getStock().get(good).addNbConsumePerDay(quantity / (float)nbDays);
			prv.getStock().get(good).addStock(-quantity);
			addMoney(-moneyExch);
			getStock().put(good, quantity + getStock().getLong(good));
			return moneyExch;
		}

	}

}
