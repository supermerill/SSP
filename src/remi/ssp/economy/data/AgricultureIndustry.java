package remi.ssp.economy.data;

import java.util.Collection;

import remi.ssp.Pop;
import remi.ssp.Province;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;

//create food from fields
public class AgricultureIndustry extends Industry {

	static protected AgricultureIndustry ptr;
	public static void load(){ ptr = new AgricultureIndustry(); }
	public static AgricultureIndustry get(){ return ptr; }

	private AgricultureIndustry(){
		myNeeds = null;
		createThis = Good.get("crop");
	}
	
	@Override
	public int produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		
		//TODO: use tools
		//TODO: evolution de la surface agricole prv.surfaceSol*prv.pourcentChamps*prv.champsRendement
		//TODO evolution de la surface cultivable par personne
		int production = 0;
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		for(Pop pop : pops){
			//
			int nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed().getInt(indus));
			production += prv.champsRendement * nbChampsUsed * 4;
			nbChamps -= nbChampsUsed;
			if(nbChamps == 0) break;
		}
		
		
		
		return production * durationInDay;
	}

}
