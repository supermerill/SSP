package remi.ssp.politic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.CurrentGame;
import remi.ssp.army.Battalion;
import remi.ssp.army.DivisionTemplate;
import remi.ssp.army.DivisionUnit;
import remi.ssp.army.EquipmentDevelopped;
import remi.ssp.network.SimpleSerializable;
import remi.ssp.technology.NameDescription;
import remi.ssp.technology.Technology;
import remi.ssp.utils.U;

public class Civilisation extends NameDescription implements SimpleSerializable{

	List<Province> provinces = new ArrayList<>();
	
	 // for army
	List<DivisionTemplate> divisionTemplate = new ArrayList<>();
	List<DivisionUnit> divisions = new ArrayList<>();
	List<Battalion> battalionTemplate = new ArrayList<>();
	int mensInReserve = 0;
	Object2LongMap<EquipmentDevelopped> equipmentReserve = new Object2LongOpenHashMap<>();

	// to compute tech movements. 
	Object2LongMap<Civilisation> lastTradeRouteExchange = new Object2LongOpenHashMap<>(); //TODOSAVE cache value for economy (TODO: reasert)
	
	
	Culture mainCulture = new Culture();
	
	//TODO put this into a "edict class"
	public int getMinAgeWork(){ return 10; }
	public int getAgeRetraite(){ return 60;}
	

	public List<Province> getProvinces() { return provinces; }
	public List<DivisionUnit> getDivisions() { return divisions; }
	public DivisionUnit getDivision(String name){for(DivisionUnit d : getDivisions()) if(d.getName().equals(name)) return d; return null;}
	public List<DivisionTemplate> getDivisionTemplate() { return divisionTemplate; }
	public List<Battalion> getBattalionTemplate() { return battalionTemplate; }
	public int getMensInReserve() { return mensInReserve; }
	public void addMensInReserve(int modif) { mensInReserve+=modif; }
	public Object2LongMap<EquipmentDevelopped> getEquipmentReserve() { return equipmentReserve; }
	public Object2LongMap<Civilisation> getTradeRouteExchange() { return lastTradeRouteExchange; }
	
	//TODO: serialization
	List<Technology> techs = new ArrayList<>();
	Object2IntMap<Technology> researchingTechs = new Object2IntOpenHashMap<Technology>(1);
	Technology selectedResearch = null;
	int overflowTechPoint = 0;
	
	public Object2IntMap<Technology> getResearchingTechs() {
		return researchingTechs;
	}
	public Technology getSelectedResearch() {
		return selectedResearch;
	}
	public void setSelectedResearch(Technology selectedResearch) {
		this.selectedResearch = selectedResearch;
	}
	public int getOverflowTechPoint() {
		return overflowTechPoint;
	}
	public void setOverflowTechPoint(int overflowTechPoint) {
		this.overflowTechPoint = overflowTechPoint;
	}
	public Collection<Technology> getTechs() {
		return techs;
	}
	public Technology getCurrentResearch() {
		return selectedResearch;
	}
	public void setName(String name) {
		super.name = name;
	}
	
	public void load(JsonObject jsonObj, Carte carte){
		super.load(jsonObj);
		
		mensInReserve = jsonObj.getInt("mensRsv");
		
		mainCulture = CurrentGame.get().getCults().get(jsonObj.get("cultName"));

		JsonArray array = jsonObj.getJsonArray("prvs");
		provinces.clear();
		for(int i=0;i<array.size();i+=2){
			provinces.add(carte.provinces.get(array.getInt(i)).get(array.getInt(i+1)));
		}

		array = jsonObj.getJsonArray("equip");
		equipmentReserve.clear();
		for(int i=0;i<array.size();i+=2){
			JsonObject objEquip = array.getJsonObject(i);
			EquipmentDevelopped equip = new EquipmentDevelopped(objEquip.getInt("id"));
			equip.load(objEquip);
			equipmentReserve.put(equip, objEquip.getInt("nbStock"));
		}
		
		array = jsonObj.getJsonArray("divstlp");
		divisionTemplate.clear();
		for(int i=0;i<array.size();i+=2){
			JsonObject objDiv = array.getJsonObject(i);
			DivisionTemplate divt = new DivisionTemplate(objDiv.getInt("id"));
			divt.load(objDiv, this);
			divisionTemplate.add(divt);
		}
		
		array = jsonObj.getJsonArray("divs");
		divisions.clear();
		for(int i=0;i<array.size();i+=2){
			DivisionUnit div = new DivisionUnit();
			div.load(array.getJsonObject(i), this);
			divisions.add(div);
		}
	}
	
	public void save(JsonObjectBuilder jsonOut){
		super.save(jsonOut);
		
		jsonOut.add("mensRsv", mensInReserve);

		U.addStrOrNull(jsonOut,"cultName", mainCulture.getName());
		
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Province prv : provinces){
			array.add(prv.x);
			array.add(prv.y);
		}
		jsonOut.add("prvs", array);

		array = Json.createArrayBuilder();
		for(Entry<EquipmentDevelopped> prv : equipmentReserve.object2LongEntrySet()){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			prv.getKey().save(objBuild);
			objBuild.add("id", prv.getKey().getId());
			objBuild.add("nbStock", prv.getLongValue());
			array.add(objBuild);
		}
		jsonOut.add("prvs", array);

		array = Json.createArrayBuilder();
		for(DivisionTemplate divt : divisionTemplate){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			divt.save(objBuild);
			objBuild.add("id", divt.getId());
			array.add(objBuild);
		}
		jsonOut.add("divstlp", array);
		
		array = Json.createArrayBuilder();
		for(DivisionUnit div : divisions){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			div.save(objBuild);
			array.add(objBuild);
		}
		jsonOut.add("divs", array);
		
		
	}
	
}
