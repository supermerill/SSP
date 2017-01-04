package remi.ssp.army;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DivisionUnit {
	DivisionTemplate template;
	List<BattalionUnit> battalions = new ArrayList<>();
	
	//global morale for my army. 1000 = full, 0 = none
	int moralePer1k; //TODO: impact of morale on combat effectiveness

	public int getMorale() { return moralePer1k; }
	public void setMorale(int moralePer1k) { this.moralePer1k = moralePer1k; }
	public DivisionTemplate getTemplate() { return template; }
	public Collection<BattalionUnit> getBattalions() { return battalions; }
	
	
	
}
