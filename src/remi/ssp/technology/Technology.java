package remi.ssp.technology;

import java.util.List;

import remi.ssp.army.EquipmentTemplate;
import remi.ssp.politic.Civilisation;

public class Technology extends NameDescription{

	protected final boolean isVisible = false;
	protected final boolean isResearchable = false;
	protected final int researchCost = 1000000;

	protected float traderouteMult = 1;
	protected float scientistMult = 1;

	protected List<Idea> ideas;
	protected List<EquipmentTemplate> equipments;
	//TODO other unlocks? like government options, 

	/**
	 * Can we use you scientist to research this?
	 * @return
	 */
	public boolean isResearchable(){return isResearchable;}
	
	/**
	 * @return
	 */
	public int getCost(){return researchCost;}
	
	/**
	 * Can we see this is the panel?
	 * @return
	 */
	public boolean isVisible(){ return isVisible; }
	
	/**
	 * Ideas unlocked when this research is done.
	 * @return list of ideas (not null)
	 */
	public List<Idea> ideasUnlocked(){ return ideas; }
	
	/**
	 * List of (military) equipment unlocked by this research
	 * @return list of equipment (not null)
	 */
	public List<EquipmentTemplate> equipmentUnlocked(){ return equipments; }
	
	
	/**
	 * If something is needed to be done when researched
	 */
	public void researched(Civilisation civ) {
		// TODO Auto-generated method stub
		
	}
	
//	/**
//	 * Do some stuff: has appeared magically? is my research points are enough? 
//	 * Also, use the tr between civs
//	 * @param civ
//	 * @param nbDays
//	 * @return unused research points
//	 */
//	public int research(Civilisation civ, int nbResearchPointUsed, int nbDays){
//		//TODO
//		
//		//research
//		// get research from tr (not yet, TODO)
//		//get research from ideas (not yet, TODO) (if an ieda spread to me without its research being researched)
//		//for each civ
//			//has they researched me?
//				//transfert some research
//		
//		//add research points
//		
//		
//		return 0;
//	}

	
}
