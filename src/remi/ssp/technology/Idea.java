package remi.ssp.technology;

import java.util.HashMap;
import java.util.Map;

import remi.ssp.politic.Province;
import remi.ssp.technology.Idea.ProvinceIdea;

/**
 * @author meril_000
 * 
 * An idea is an improvement than can be learn by the -population- / province. When learned it  give bonuses
 * 
 * This instance is a pattern that can be present in multiple provinces.
 * 
 */
public abstract class Idea {
	
	private static Map<String, Idea> ideaStore = new HashMap<>();
	public static boolean register(Idea idea){
		if(!ideaStore.containsKey(idea.name)){
			ideaStore.put(idea.name, idea);
			return true;
		}else return false;
	}
	public static Idea get(String ideaName){
		return ideaStore.get(ideaName);
	}
	
	
	
	public static class ProvinceIdea{
		public Idea idea;
		public Province prv;
		//how many % of pop know this idea?
		public float development = 0;
		public boolean adopted = false;
		
	}
	
	protected final String name;

	
	public Idea(String name){
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this == obj || (obj instanceof Idea && ((Idea)obj).name.equals(name));
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	/**
	 * Propagate idea to near territory, increase idea in territory.
	 * If the threshold is passed, it should register his effects inside the prv.
	 * @param prv
	 * @param nbDays
	 */
	public void doTurn(ProvinceIdea prvI, int nbDays){
		if(prvI.development<1){
			boolean onetime = !prvI.adopted;
			propagateIdeaToPop(prvI, nbDays);
			if(onetime && prvI.adopted){
				oneTimeEffect(prvI, true);
			}
			if(!onetime && !prvI.adopted){
				oneTimeEffect(prvI, false);
			}
		}
		for(Province prv2 : prvI.prv.proche){
			if(!prv2.getIdeas().contains(this)){
				propagateToOtherPrv(prvI, prv2, nbDays);
			}
			//TODO: propagate also via maritime trade
		}
		if(prvI.adopted){
			manyTimeEffect(prvI);
		}
	}


	/**
	 * change adopted from false to true to trigger the oneTimeEffect. To the opposite to trigger the reverse-oneTimeEffect.
	 */
	public void propagateIdeaToPop(ProvinceIdea prvI, int nbDays){
	}
	
	/**
	 * 
	 * @param prvOri
	 * @param prvDest province where this Idea isn't registered yet.
	 * @param nbDays
	 */
	public void propagateToOtherPrv(ProvinceIdea prvOri, Province prvDest, int nbDays){
	}

	public void oneTimeEffect(ProvinceIdea prv, boolean adopted){}
	public void manyTimeEffect(ProvinceIdea prv){}
	
	protected void simplePropagateIdeaToPop(ProvinceIdea prvI, int nbDays){
		double oneInd = 1.0 / prvI.prv.getNbAdult();
		prvI.development += oneInd*nbDays + (prvI.development*nbDays) / 30;
		prvI.development = Math.min(1, prvI.development);
	}
	
}
