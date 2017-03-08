package remi.ssp.algorithmes;

import remi.ssp.politic.Carte;

public abstract class WarUnitManager {
	
	static public WarUnitManager ptr;

	public abstract void doTurn(Carte map, int durationInDay);
	
}
