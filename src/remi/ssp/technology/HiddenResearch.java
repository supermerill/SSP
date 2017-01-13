package remi.ssp.technology;

import java.util.List;

import remi.ssp.Civilisation;
import remi.ssp.army.EquipmentTemplate;

public class HiddenResearch extends NameDescription implements ResearchImpl {
	
	
	
	public boolean canBeRushed() { return false;};

	@Override
	public int currentNbPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<EquipmentTemplate> equipmentUnlocked() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Idea> ideasUnlocked() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResearched() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int maxNbPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void research(Civilisation civ, int nbResearchPointUsed, int nbDays) {
		// TODO Auto-generated method stub

	}
}
