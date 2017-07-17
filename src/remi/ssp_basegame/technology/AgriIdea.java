package remi.ssp_basegame.technology;

import remi.ssp.technology.Idea;

public class AgriIdea extends Idea{
	
	private float increaseRendementPerField;
	AgriIdea(String name, float increaseRendementPerField){
		super(name);
		this.increaseRendementPerField = increaseRendementPerField;
	}

	public void propagateIdeaToPop(ProvinceIdea prvI, int nbDays){
		simplePropagateIdeaToPop(prvI, nbDays);
	}

	public void oneTimeEffect(ProvinceIdea prvI, boolean adopted){
		if(adopted) prvI.prv.champsRendement += increaseRendementPerField;
		else prvI.prv.champsRendement -= increaseRendementPerField;
	}
	public void manyTimeEffect(ProvinceIdea prvI){}

}
