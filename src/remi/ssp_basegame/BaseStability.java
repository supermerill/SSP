package remi.ssp_basegame;

import remi.ssp.algorithmes.Stability;
import remi.ssp.politic.Province;

public class BaseStability extends Stability {

	@Override
	public void computeStabilityProvince(Province prv) {
		// TODO Auto-generated method stub
		
		//check % of pop of "not the main culture"
		
		//add modifs:
		
			//add stability if suppression (army)
			//remove stability if no army on me
			//add stability if winning a battle
			//remove stability if loosing a battle
			//remove stability if oppressed (by laws?)
			//remove stability from 'nationalism'
			//add stability if needs are satified / grow is hapenning (better future for children)
			//remove stability if starving
			//remove stability if the economy isn't growing.
			//remove stability from current corruption

	}

	@Override
	public void computeCorruption(Province prv) {
		
		//check distance from palace (via sea/via land)
		//set corruption (X per day of travel)

	}

}
