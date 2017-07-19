package remi.ssp_basegame;

import java.util.List;

import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.algorithmes.Research;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.technology.Idea.ProvinceIdea;
import remi.ssp.technology.Technology;

public class BaseResearch extends Research {

	@Override
	public void research4all(int nbDays) {
		List<Technology> allTechs = Technology.getAll();
		for(Civilisation civ : CurrentGame.get().getCivs()){
			
			//ultra-basic one for directed research
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
					tech.doResearched(civ);
				}else{
					civ.getResearchingTechs().put(tech, sciencePoints);
					civ.setOverflowTechPoint(0);
				}
			}
			
			//totally random one for undirected research

			//choose one in random
			int idxTech = 0;
			Technology techRandom = null;
			int simpleAntiLoop = 100;
			while(civ.getTechs().contains(techRandom) || techRandom.isResearchable() && simpleAntiLoop>0){
				idxTech = GlobalRandom.aleat.getInt(allTechs.size(), civ.getMensInReserve());
				techRandom = allTechs.get(idxTech);
				simpleAntiLoop--;
			}
			if(techRandom != null && (civ.getTechs().contains(techRandom) || techRandom.isResearchable())) techRandom = null;
			
			if(techRandom != null)
			{
				// random check for success
				if(GlobalRandom.aleat.getInt(tech.getCost(), civ.getMensInReserve())<100){
					//success!
					civ.getTechs().add(tech);
					tech.doResearched(civ);
				}
			}

			//TODO: make (also) a propagation algorithm via province ideas or something similar to propagate already researched unresearchable&researchable technologies
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
			for(ProvinceIdea prvI : prv.getIdeas()){
				//all is made inside, to be able to create special propagation things.
				//to create standard propagation routines, please create an other function here.
				prvI.idea.doTurn(prvI, nbDays);
			}
		}
		
	}

}
