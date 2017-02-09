package remi.ssp.algorithmes;

import remi.ssp.politic.Civilisation;

public abstract class Research {
	
	static public Research ptr;

	//pass on all civs and add research points for all techs that can receive one.
	public abstract void research4all(int nbDays);
	
	// get the research points you can alocate yourself to one tech.
	public abstract int getResearchPoints(Civilisation civ, int nbDays);
	
	//pass on all province and check if a nearby idea propagate to it (via trade or something else)
	public abstract void ideaPropagation(int nbDays);


}
