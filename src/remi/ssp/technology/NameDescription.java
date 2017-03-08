package remi.ssp.technology;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.utils.U;

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
		U.addStrOrNull(jsonOut,"name", name);
		U.addStrOrNull(jsonOut,"description", description);
	}

}
