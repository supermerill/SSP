package remi.ssp.technology;

import java.util.List;

import remi.ssp.army.EquipmentTemplate;
import remi.ssp.politic.Civilisation;

// a research item.
public abstract interface ResearchImpl {

	public String getName();
	public String getDescription();

	/**
	 * Can we use you scientist to research this?
	 * @return
	 */
	public boolean canBeRushed();

	/**
	 * @return
	 */
	public int currentNbPoints();
	
	/**
	 * @return
	 */
	public int maxNbPoints();
	
	/**
	 * Can we see this is the panel?
	 * @return
	 */
	public boolean isVisible();

	/**
	 * It is researched?
	 * @return
	 */
	public boolean isResearched();
	
	/**
	 * Ideas unlocked when this research is done.
	 * @return list of ideas (not null)
	 */
	public List<Idea> ideasUnlocked();
	
	/**
	 * List of (military) equipment unlocked by this research
	 * @return list of equipment (not null)
	 */
	public List<EquipmentTemplate> equipmentUnlocked();
	
	/**
	 * Do some stuff: has appeared magically? is my research points are enough? 
	 * Also, use the tr between civs
	 * @param civ
	 * @param nbDays
	 */
	public abstract void research(Civilisation civ, int nbResearchPointUsed, int nbDays);
	
}
