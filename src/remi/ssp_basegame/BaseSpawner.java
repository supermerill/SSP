package remi.ssp_basegame;

import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.Spawner;
import remi.ssp.map.AnneauCarte;

public class BaseSpawner extends Spawner {

	

	/**
	 * populate CurrentGame.map (provinces +plots)
	 * 
	 */
	public void createMap(){
		CurrentGame.map = new AnneauCarte().createMap(30, 30);
	}
	
	/**
	 * use CurrentGame.map to populate CurrentGame.civs
	 * 
	 */
	public void createCivs(){
		
	}

	/**
	 * use CurrentGame.map and populate CurrentGame.civs to add pop on provinces
	 * 
	 */
	public void createPop(){
		
	}
}
