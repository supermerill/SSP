package remi.ssp_basegame.economy;

import static remi.ssp.GlobalDefines.logln;

import java.util.Collection;

import remi.ssp.GlobalDefines;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.LongInterval;

public class SubsistanceIndustry extends Industry {

	static protected SubsistanceIndustry ptr;
	public static void load(){ ptr = new SubsistanceIndustry();  Industry.put("subsistance", ptr);}
	public static SubsistanceIndustry get(){ return ptr; }
	
	private final Good crop;
	private final Industry agriIndus;
	
	private SubsistanceIndustry(){
		createThis = Good.get("crop");
		myNeedsFactory = pi -> new BasicIndustryNeeds(pi);
		crop = Good.get("crop");
		agriIndus = Industry.get("agriculture");
	}
	
	@Override
	public long produce(ProvinceIndustry indus, Collection<Pop> pops, int durationInDay) {
		Province prv = indus.getProvince();
		
		long production = 0;
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		
		//remove nbChamps used by agriculture
		for(Pop pop : pops){
			nbChamps -= Math.min(nbChamps, pop.getNbMensEmployed(indus.getProvince().getIndustry(agriIndus)));
		}
		
		
		GlobalDefines.plogln(",\"subsist nbChamps\":"+nbChamps+", \"pourcentChamps\":"+prv.pourcentChamps);
		for(Pop pop : pops){
			//
			long nbChampsUsed = Math.min(nbChamps, pop.getNbMensEmployed(indus));
			long popProduction = (long)( prv.champsRendement * nbChampsUsed * 4 * durationInDay * (0.5+pop.getFoodEffectiveness()/2)); //Two times les efficient
			GlobalDefines.plogln(", \"nbChampsUsed_"+pop+"\":"+nbChampsUsed+", \"getFoodEffectiveness"+(0.5+pop.getFoodEffectiveness()/2)+", \"prod_"+pop+"\":"+(prv.champsRendement * nbChampsUsed * 4 * durationInDay * (0.5+pop.getFoodEffectiveness()/2)));
			nbChamps -= nbChampsUsed;
			
			GlobalDefines.plogln(", \"stock from "+pop.getStock().get(crop)+" to\":"+(popProduction + pop.getStock().get(crop)));
			pop.getStock().put(crop, popProduction + pop.getStock().get(crop));
			
			production+= popProduction;
			if(nbChamps == 0) break;
		}
	
		//do not do that, economyplugin will call it after caling this
		//super.sellProductToMarket(prv, intproduction, durationInDay);
		
		
		//never produce, only put it into our stock
		
		return 0;
	}
	
	@Override
	public LongInterval needFire(LongInterval toReturn, ProvinceIndustry indus, Pop currentPop, int nbDays) {
		Province prv = indus.getProvince();
		
		//don't grab more than nbfieldDispo/2
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		long nbEmpl = 1;
		long nbChom = 0;
		//remove nbChamps used by agriculture
		for(Pop pop : prv.getPops()){
			long nbEmplAgri = pop.getNbMensEmployed(indus.getProvince().getIndustry(agriIndus));
			nbChamps -= Math.min(nbChamps, nbEmplAgri);
			nbEmpl += pop.getNbMensEmployed(indus);
		}

		long nbEmplThisPop = currentPop.getNbMensEmployed(indus);
		long nbToFire = (Math.max(0, nbEmpl - nbChamps/2) * nbEmplThisPop) / nbEmpl;
		GlobalDefines.plogln(",\"Fcheck "+currentPop+" fields used\": \""+nbEmpl+"/"+(nbChamps/2)+" => "+((nbEmpl - nbChamps/2) )+" for pop "+nbEmplThisPop+"/"+nbEmpl+" ==> "+nbToFire+"\"");
		
		//and do not get more than nbChomage/2
		nbToFire = Math.max(nbToFire, (nbEmplThisPop - currentPop.getNbMensChomage()) /2);
		GlobalDefines.plogln(",\"Fcheck "+currentPop+" chomeurs used\": \""+nbEmplThisPop+"/"+(currentPop.getNbMensChomage())+" => "+((nbEmplThisPop - currentPop.getNbMensChomage()) /2)+" ==> "+ ((nbEmplThisPop - currentPop.getNbMensChomage()) /2)+"\"");
		
		
		return super.needFire(toReturn, indus, currentPop, nbDays).set(nbToFire, nbToFire);
	}
	
	@Override
	public LongInterval needHire(LongInterval toReturn, ProvinceIndustry indus, Pop currentPop, int nbDays) {
		Province prv = indus.getProvince();
		
		//hire more pop if enough field & enough chomeurs
		int nbChamps = (int) ( (prv.pourcentChamps * prv.surface) * 10); // 10 hectare per argi
		long nbEmpl = 1;
		long nbChom = 0;
		for(Pop pop : prv.getPops()){
			long nbEmplAgri = pop.getNbMensEmployed(indus.getProvince().getIndustry(agriIndus));
			nbChamps -= Math.min(nbChamps, nbEmplAgri);
			nbEmpl += pop.getNbMensEmployed(indus);
		}

		long nbEmplThisPop = currentPop.getNbMensEmployed(indus);
		long nbToHire = Math.min(Math.max(0, (nbChamps/2 - nbEmpl)* nbEmplThisPop) / nbEmpl, Math.max(0, (currentPop.getNbMensChomage() - nbEmplThisPop) /2));
		
		GlobalDefines.plogln(",\"Hcheck "+currentPop+" fields used\": \""+nbEmpl+"/"+(nbChamps/2)+" => "+ ((nbChamps/2 - nbEmpl))+" for pop "+nbEmplThisPop+"/"+nbEmpl+" ==> "+((((nbChamps/2 - nbEmpl))* nbEmplThisPop) / nbEmpl)+"\"");
		GlobalDefines.plogln(",\"Hcheck "+currentPop+" chomeurs used\": \""+nbEmplThisPop+"/"+(currentPop.getNbMensChomage())+" => "+((currentPop.getNbMensChomage() - nbEmplThisPop) /2)+"\"");
		
		return super.needHire(toReturn, indus, currentPop, nbDays).setMin(nbToHire);
	}

}
