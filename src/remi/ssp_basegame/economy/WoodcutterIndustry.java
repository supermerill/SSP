package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

//create lumber from forest (and grow the forest)
public class WoodcutterIndustry extends Industry {

	static protected WoodcutterIndustry ptr;

	public static void load() {
		ptr = new WoodcutterIndustry();
		Industry.put("wood", ptr);
	}

	public static WoodcutterIndustry get() {
		return ptr;
	}

	protected WoodcutterIndustry() {
		createThis = Good.get("wood");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi).addToolGood(Good.get("wood_goods"), 1);
	}

	private BasicIndustryNeeds getNeed(ProvinceIndustry indus) {
		return (BasicIndustryNeeds) indus.getNeed();
	}

	// pin maritime: (recolte en 50ans) 4,8 m3/ha/an en 1960 à 15 m3/ha/an en 2015

	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay, long wish) {
		Province prv = indus.getProvince();

		// TODO: use tools
		long nbTree = (long) ((prv.pourcentForet * prv.surface * 100000l)); // 1 tree per 10 m²

		// multiplicate
		// add 5% every year
		float percentMore = 0;
		float percentGrow = durationInDay * 0.05f / 360;
		float percentLost = durationInDay * 0.2f / 360; // can't lost more than 20% of friche every year
		if (prv.pourcentForet * percentGrow > prv.pourcentFriche * percentLost) {
			// no place for more
			percentMore = prv.pourcentFriche * percentLost;
		} else {
			percentMore = prv.pourcentForet * percentGrow;
		}
		prv.pourcentForet += percentMore;
		prv.pourcentFriche -= percentMore;

		// how much lumber per man?
		float productivity = 10 * durationInDay; // base: 10kg per day (will be much better with tools)

		long nbMens = 0;
		for (Pop pop : pops) {
			// TODO tools
			nbMens += pop.getNbMensEmployed(indus);
			// don't take into account personal tools
		}
		long nbWoodCut = (long) (productivity * nbMens);

		// pin maritime: 1,2m² per tree, 450/650 kg/m²
		// a tree is 1000 kg (seems good)
		// if cut toomuch, reduction from concurence
		if (nbWoodCut > nbTree * 100) {
			nbWoodCut = (long) ((nbWoodCut * nbTree * 100.0) / nbWoodCut);
		}

		// remove forest
		long nbTreeCut = 1 + nbWoodCut / 1000;
		float percentMove = prv.pourcentForet * (float) nbTreeCut / (float) nbTree;
		prv.pourcentForet -= percentMove;
		prv.pourcentFriche += percentMove;

		// produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (int) nbWoodCut, durationInDay);

		//store prod
		indus.getStock().put(getGood(),indus.getStock().getLong(getGood())+intproduction);
		return intproduction;
	}

	@Override
	public long getMenWish(ProvinceIndustry indus, double currentConsumptionPD) {
		return (long) (currentConsumptionPD/10);
	}

}
