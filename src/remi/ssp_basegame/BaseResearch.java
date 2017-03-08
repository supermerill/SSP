package remi.ssp_basegame;

import java.util.List;

import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.Research;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.technology.Technology;

public class BaseResearch extends Research {

	@Override
	public void research4all(int nbDays) {
		for(Civilisation civ : CurrentGame.get().getCivs()){
			
			//ultra-basic one
			Technology tech = civ.getCurrentResearch();
			if(tech != null){
				int sciencePoints = civ.getOverflowTechPoint();
				sciencePoints += getResearchPoints(civ, nbDays);
				sciencePoints += civ.getResearchingTechs().getInt(tech);
				//int overflow = tech.research(civ, getResearchPoints(civ, nbDays) + , nbDays);
				if(sciencePoints >= tech.getCost()){
					//researched!
					civ.getResearchingTechs().remove(tech);
					civ.getTechs().add(tech);
					civ.setOverflowTechPoint(sciencePoints - tech.getCost());
					tech.researched(civ);
				}else{
					civ.getResearchingTechs().put(tech, sciencePoints);
					civ.setOverflowTechPoint(0);
				}
			}
		}
	}

	@Override
	public int getResearchPoints(Civilisation civ, int nbDays) {

		//basic: get research points from rich pop
		long nbRechPoint = 0;
		//get nb of rich
		for(Province prv : civ.getProvinces()){
			for( Pop pop : prv.getPops()){
				nbRechPoint += 5 /* 100/20 */ * pop.getEducationMoy() * pop.getNbAdult();
			}
		}
		nbRechPoint = Math.max(1, nbRechPoint / 100);
		return (int) nbRechPoint * nbDays;
	}

	@Override
	public void ideaPropagation(int nbDays) {
		
		//for all province
		for(Province prv : CurrentGame.get().getMap().getAllProvinces()){
				//for each trading province (nearby+sea?)
					//compute a coeff (from culture, and amount of goods)
					//check ideas that you don't have
						//add coeff from idea compatibility (researched tech, ...)
						//tick a chance to spread
						//if success, add this idea to the prv.
		}
		
	}

}
