package remi.ssp.technology;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import remi.ssp.CurrentGame;
import remi.ssp.army.DivisionTemplate;
import remi.ssp.army.DivisionUnit;
import remi.ssp.army.EquipmentDevelopped;
import remi.ssp.politic.Carte;
import remi.ssp.politic.Province;

public class NameDescription {
	
	protected String name, description;
	
	protected NameDescription() {}

	public NameDescription(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	

	
	public void load(JsonObject jsonObj){
		name = jsonObj.getString("name");
		description = jsonObj.getString("description");
	}
	
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("name", name);
		jsonOut.add("description", description);
	}

}
