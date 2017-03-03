package remi.ssp;

import java.util.List;
import java.util.Map;

import remi.ssp.politic.Carte;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Culture;

public class CurrentGame {

	protected static CurrentGame me = new CurrentGame();
	
	public static CurrentGame get(){
		return me;
	}
	
	public Carte map;
	public List<Civilisation> civs;
	public Map<String, Culture> cultures;

	Carte getMap(){ return map; }
	List<Civilisation> getCivs(){ return civs; }
	Civilisation getCiv(String name){
		for(Civilisation civ : civs){
			if(civ.getName().equals(name)){
				return civ;
			}
		}
		return null;
	}
	Map<String, Culture> getCults(){ return cultures; }
	
}
