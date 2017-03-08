package remi.ssp_basegame;

import java.util.stream.Stream;

import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.WarUnitManager;
import remi.ssp.army.DivisionUnit;
import remi.ssp.politic.Carte;
import remi.ssp.politic.Province;

public class BaseWarUnitManager extends WarUnitManager {
	
	public static int kmPerHex = 100;

	@Override
	public void doTurn(Carte map, int durationInDay) {
		
		int nbHours = durationInDay*24;
//		Stream<Province> prvStream = map.getAllProvinces().stream();
		Stream<DivisionUnit> duStream = CurrentGame.get().getCivs().stream().flatMap(civ -> civ.getDivisions().stream());
				
		//for each hour
		for(int hour = 0; hour<nbHours; hour++)
		{
			//filter the companies that don't move for now
			duStream = duStream.filter(du->du.getTarget() != null);
			
			//add mouvement point (from speed)
			duStream.forEach(du -> du.addMouvementPoint());
			
			//move all units that have enough mouvementpoint
			//TODO 30km per hex? please
			duStream.forEach(du -> { if(du.getMouvementPoint() > kmPerHex && du.getTarget() != null) moveUnit(du);});
			
		
			//for each battle
				//do turn
		}
		
		
	}
	
	
	/**
	 * Move a division, and return true if an other one is on the same hex
	 * @param unit unit to move
	 * @return true if it's on the same hex as an enemy unit.
	 */
	public boolean moveUnit(DivisionUnit unit){
		
		//

		//if two enemies unit are on the same plot, create a battle or join the existing one
		
		return false;
	}
	

}
