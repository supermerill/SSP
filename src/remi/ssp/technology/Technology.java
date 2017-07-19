package remi.ssp.technology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.army.EquipmentTemplate;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Province;
import remi.ssp.technology.Idea.ProvinceIdea;

public class Technology extends NameDescription{
	
	public static class TechnologyFactory{
		Technology template;
		public static TechnologyFactory create(String name){ 
			TechnologyFactory ret = new TechnologyFactory();
			ret.template = new Technology(name);
			return ret;
		}
		public TechnologyFactory setVisible(boolean visible){ template.isVisible = visible; return this; }
		public TechnologyFactory setReserchable(boolean isResearchable){ template.isResearchable = isResearchable; return this; }
		public TechnologyFactory setScienceCost(int researchCost){ template.researchCost = researchCost; return this; }
		public TechnologyFactory addIdea(Idea idea){ template.ideas.add(idea); return this; }
		public TechnologyFactory addPrerequisite(Technology tech){ template.prerequisite.add(tech); return this; }
		public Technology create(){ Technology ret = template; template = null; register(ret); return ret; }
	}

	private static Map<String, Technology> techStore = new HashMap<>();
	public static boolean register(Technology tech){
		if(!techStore.containsKey(tech.name)){
			techStore.put(tech.name, tech);
			return true;
		}else return false;
	}
	public static Technology get(String techName){
		return techStore.get(techName);
	}
	public static List<Technology> getAll() {
		return new ArrayList<>(techStore.values());
	}

	protected boolean isVisible = false;
	protected boolean isResearchable = false;
	protected int researchCost = 1000000;

	protected float traderouteMult = 1;
	protected float scientistMult = 1;

	protected List<Technology> prerequisite;
	protected List<Idea> ideas;
	protected List<EquipmentTemplate> equipments;
	//TODO other unlocks? like government options,

	public Technology(String name) {
		super.name = name;
	}

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
	public void doResearched(Civilisation civ) {
		//set the idea center at a random province.
		for(Idea idea : ideas){
			int nbprv = civ.getProvinces().size();
			if(nbprv<=0)break;
			int idxPrv = GlobalRandom.aleat.getInt(nbprv, nbprv+civ.getMensInReserve());
			Province prv = civ.getProvinces().get(idxPrv);
			ProvinceIdea pi = new ProvinceIdea(prv, idea);
			prv.getIdeas().add(pi);
		}
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
