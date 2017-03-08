package remi.ssp;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	protected Carte map;
	protected List<Civilisation> civs = new ArrayList<>();
	protected Map<String, Culture> cultures = new HashMap<>();

	public Carte getMap(){ return map; }
	public List<Civilisation> getCivs(){ return civs; }
	public Civilisation getCiv(String name){
		for(Civilisation civ : civs){
			if(civ.getName().equals(name)){
				return civ;
			}
		}
		return null;
	}
	public Map<String, Culture> getCults(){ return cultures; }
	public void setMap(Carte createMap) {
		this.map = createMap;
	}
	
}
