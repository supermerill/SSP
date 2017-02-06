package remi.ssp.technology;

import remi.ssp.politic.Province;

/**
 * @author meril_000
 * 
 * An idea is an improvement than can be learn by the -population- / province. When learned it  give bonuses
 * 
 * 
 */
public abstract class Idea extends NameDescription {

	/**
	 * Propage idea to near territory, increase idea in territory.
	 * If the threshold is passed, it should register his effects inside the prv.
	 * @param prv
	 * @param nbDays
	 */
	public abstract void doTurn(Province prv, int nbDays);
	
	
}
