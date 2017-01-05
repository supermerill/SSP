package remi.ssp.army;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class DivisionTemplate {

	Object2IntMap<Battalion> nbBattalions = new Object2IntOpenHashMap<>();
	// cached value, = all mens in battalions.
	//int nbMens;
	int getMens(){
		int nbMens = 0;
		for(Entry<Battalion> entry : nbBattalions.object2IntEntrySet()){
			nbMens += entry.getIntValue() * (entry.getKey().nbFightingMens + entry.getKey().nbHandlingMens);
		}
		return nbMens;
	}

	int getFightingMens(){
		int nbMens = 0;
		for(Entry<Battalion> entry : nbBattalions.object2IntEntrySet()){
			nbMens += entry.getIntValue() * entry.getKey().nbFightingMens;
		}
		return nbMens;
	}
	
}
