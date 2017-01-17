package remi.ssp.army;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Civilisation;

//an equipment created by a country from a researched one.
public class EquipmentDevelopped extends EquipmentTemplate {

	private final int id; //to be retreived, an id is unique for a player
	EquipmentTemplate baseline;

	public EquipmentDevelopped(int id){ this.id = id; }
	public EquipmentDevelopped(Civilisation others){
		int i=0;
		for(EquipmentDevelopped other : others.getEquipmentReserve().keySet()){
			if(i <= other.id){
				i = other.id+1;
			}
		}
		this.id = i;
	}
	
	public final int getId(){ return id; }

	public void load(JsonObject jsonValue) {
		try{
			baseline = EquipmentTemplate.get(jsonValue.getString("nameTlp"));
		}catch(Exception e){
			baseline = new EquipmentTemplate();
			e.printStackTrace();
		}
	}

	public void save(JsonObjectBuilder objBuild) {
		objBuild.add("nameTlp", baseline.getName());
	}
	
	//it's extended so to also have his own stats
	
}
