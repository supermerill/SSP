package remi.ssp.army;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.politic.Civilisation;
import remi.ssp.technology.NameDescription;

public class DivisionTemplate extends NameDescription{
	
	private final int id;
	public DivisionTemplate(int id){this.id = id; this.name = "id_"+id;}
	public DivisionTemplate(Civilisation others){
		int i=0;
		for(DivisionTemplate other : others.getDivisionTemplate()){
			if(i <= other.id){
				i = other.id+1;
			}
		}
		this.id = i;
	}
	public final int getId(){return id;}
	

	Object2IntMap<Battalion> nbBattalions = new Object2IntOpenHashMap<>();
	
	public Object2IntMap<Battalion> getNbBattalions() {
		return nbBattalions;
	}
	
	// cached value, = all mens in battalions.
	//int nbMens;
	int getMens(){
		int nbMens = 0;
		for(Entry<Battalion> entry : nbBattalions.object2IntEntrySet()){
			nbMens += entry.getIntValue() * (entry.getKey().nbFightingMens + entry.getKey().nbHandlingMens);
		}
		return nbMens;
	}

	int getFightingMens(){
		int nbMens = 0;
		for(Entry<Battalion> entry : nbBattalions.object2IntEntrySet()){
			nbMens += entry.getIntValue() * entry.getKey().nbFightingMens;
		}
		return nbMens;
	}

	public void load(JsonObject jsonObj, Civilisation civ) {
		super.load(jsonObj);
		JsonArray array = jsonObj.getJsonArray("bats");
		nbBattalions.clear();
		for(int i=0;i<array.size();i+=2){
			JsonObject objBat = array.getJsonObject(i);
			Battalion bat = new Battalion(objBat.getInt("id"));
			bat.load(objBat, civ);
			nbBattalions.put(bat, objBat.getInt("nbInDiv"));
		}
	}

	public void save(JsonObjectBuilder jsonOut) {
		super.save(jsonOut);
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Entry<Battalion> bat : nbBattalions.object2IntEntrySet()){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			bat.getKey().save(objBuild);
			objBuild.add("nbInDiv", bat.getIntValue());
			objBuild.add("id", bat.getKey().getId());
			array.add(objBuild);
		}
		jsonOut.add("bats", array);
		
	}
	
}
