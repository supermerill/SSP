package remi.ssp.politic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import remi.ssp.CurrentGame;
import remi.ssp.army.Battalion;
import remi.ssp.army.DivisionTemplate;
import remi.ssp.army.DivisionUnit;
import remi.ssp.army.EquipmentDevelopped;
import remi.ssp.economy.Good;
import remi.ssp.economy.Needs;

public class Civilisation {

	List<Province> provinces = new ArrayList<>();
	
	 // for army
	List<DivisionTemplate> divisionTemplate = new ArrayList<>();
	List<DivisionUnit> divisions = new ArrayList<>();
	List<Battalion> battalionTemplate = new ArrayList<>();
	int mensInReserve = 0;
	Object2IntMap<EquipmentDevelopped> equipmentReserve = new Object2IntOpenHashMap<>();

	// to compute tech movements. 
	Object2IntMap<Civilisation> lastTradeRouteExchange = new Object2IntOpenHashMap<>(); //TODOSAVE cache value for economy (TODO: reasert)
	
	
	Culture mainCulture;

	public List<Province> getProvinces() { return provinces; }
	public List<DivisionUnit> getDivisions() { return divisions; }
	public List<DivisionTemplate> getDivisionTemplate() { return divisionTemplate; }
	public List<Battalion> getBattalionTemplate() { return battalionTemplate; }
	public int getMensInReserve() { return mensInReserve; }
	public Object2IntMap<EquipmentDevelopped> getEquipmentReserve() { return equipmentReserve; }
	public Object2IntMap<Civilisation> getTradeRouteExchange() { return lastTradeRouteExchange; }
	

	public void load(JsonObject jsonObj, Carte carte){
		
		mensInReserve = jsonObj.getInt("mensRsv");
		
		mainCulture = CurrentGame.cultures.get(jsonObj.get("cultName"));

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
		jsonOut.add("mensRsv", mensInReserve);

		jsonOut.add("cultName", mainCulture.getName());
		
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Province prv : provinces){
			array.add(prv.x);
			array.add(prv.y);
		}
		jsonOut.add("prvs", array);

		array = Json.createArrayBuilder();
		for(Entry<EquipmentDevelopped> prv : equipmentReserve.object2IntEntrySet()){
			JsonObjectBuilder objBuild = Json.createObjectBuilder();
			prv.getKey().save(objBuild);
			objBuild.add("id", prv.getKey().getId());
			objBuild.add("nbStock", prv.getIntValue());
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
