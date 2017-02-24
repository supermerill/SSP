package remi.ssp_basegame.economy;

import java.util.Collection;

import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

/**
 * 
 * like maid, barber, gardener ...
 * 
 * note: i count in untooled&unskilled hour/man instead of in kilogramme.
 * 
 * 
 * @author merill
 *
 */
public class PersonalServiceIndustry  extends Industry {

	static protected PersonalServiceIndustry ptr;
	public static void load(){ ptr = new PersonalServiceIndustry(); Industry.put("service", ptr); }
	public static PersonalServiceIndustry get(){ return ptr; }

	private Good tools;

	protected PersonalServiceIndustry() {
		createThis = Good.get("service");
		tools = Good.get("wood_goods");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi)
				.addToolGood(Good.get("wood_goods"), 1);
	}
	
	private BasicIndustryNeeds getNeed(ProvinceIndustry indus){
		return (BasicIndustryNeeds)indus.getNeed();
	}
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		

		long nbMens = 0;
		for(Pop pop : pops){
			//TODO tools
			nbMens += pop.getNbMensEmployed(indus);
			// take into account personal tools ?
		}
		long production = 0;
		for(Pop pop : pops){
			float educCoeff = (0.5f+pop.getEducationMoy()/2);
			if(prv.getIndustry(ptr).getStock().getLong(tools)>nbMens){
				production += durationInDay * 10 * pop.getNbMensEmployed(indus) * educCoeff;
			}else{
				production += 0.25 * durationInDay * 10 * pop.getNbMensEmployed(indus) * educCoeff;
				production += 0.75 * (durationInDay * 10 * pop.getNbMensEmployed(indus) * educCoeff * prv.getIndustry(ptr).getStock().getLong(tools)) / nbMens;
			}
		}

		// produce
		long intproduction = getNeed(indus).useGoodsAndTools(indus, (int)production, durationInDay);
		
		return intproduction;
	}
	
}
