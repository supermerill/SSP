package remi.ssp.government;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import remi.ssp.politic.Civilisation;

public class Government {
	
	Civilisation myCiv; // its a 1-1 relation
	Map<LawType, Law> currentLaws = new HashMap<>();
	List<Law> availableLaws = new ArrayList<>();

	
	//todo: save/load
}
