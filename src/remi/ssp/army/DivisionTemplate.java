package remi.ssp.army;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DivisionTemplate {

	Map<Battalion, Integer> nbBattalions = new HashMap<>();
	// cached value, = all mens in battalions.
	//int nbMens;
	int getMens(){
		int nbMens = 0;
		for(Entry<Battalion, Integer> entry : nbBattalions.entrySet()){
			nbMens += entry.getValue() * (entry.getKey().nbFightingMens + entry.getKey().nbHandlingMens);
		}
		return nbMens;
	}

	int getFightingMens(){
		int nbMens = 0;
		for(Entry<Battalion, Integer> entry : nbBattalions.entrySet()){
			nbMens += entry.getValue() * entry.getKey().nbFightingMens;
		}
		return nbMens;
	}
	
}
