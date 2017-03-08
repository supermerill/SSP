package remi.ssp.army;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Plot;
import remi.ssp.technology.NameDescription;

public class DivisionUnit extends NameDescription {
	DivisionTemplate template;
	List<BattalionUnit> battalions = new ArrayList<>(4);
	Plot position;
	List<Plot> target = new ArrayList<>(0);
	
	//global morale for my army. 1000 = full, 0 = none
	int moralePer1k; //TODO: impact of morale on combat effectiveness
	int mouvementPoints;
	
	public DivisionUnit(){}



	public void load(JsonObject jsonObject, Civilisation civ) {
		super.load(jsonObject);
		moralePer1k = jsonObject.getInt("morale");
		int id2find = jsonObject.getInt("divId");
		for(DivisionTemplate template : civ.getDivisionTemplate()){
			if(template.getId() == id2find){
				this.template = template;
				break;
			}
		}
		JsonArray arraybat = jsonObject.getJsonArray("bats");
		for(int i=0; i<arraybat.size(); i++){
			BattalionUnit batu = new BattalionUnit();
			batu.load(arraybat.getJsonObject(i), civ);
			battalions.add(batu);
		}
	}
	public void save(JsonObjectBuilder objBuild) {
		super.save(objBuild);
		objBuild.add("morale", moralePer1k);
		objBuild.add("divId", template.getId());
		JsonArrayBuilder arrayBatu = Json.createArrayBuilder();
		for(BattalionUnit batu : battalions){
			JsonObjectBuilder objBatu = Json.createObjectBuilder();
			batu.save(objBatu);
			arrayBatu.add(objBatu);
		}
		objBuild.add("bats", arrayBatu);
	}
	
	public int getMorale() { return moralePer1k; }
	public void setMorale(int moralePer1k) { this.moralePer1k = moralePer1k; }
	public DivisionTemplate getTemplate() { return template; }
	public Collection<BattalionUnit> getBattalions() { return battalions; }
	
	
	
	public Plot getPosition() {
		return position;
	}

	public void setPosition(Plot position) {
		this.position = position;
	}
	

	public List<Plot> getTarget() {
		return target;
	}

	public void addTarget(Plot target) {
		this.target.add(target);
	}

	public int getMoralePer1k() {
		return moralePer1k;
	}

	public void setMoralePer1k(int moralePer1k) {
		this.moralePer1k = moralePer1k;
	}

	public void setTemplate(DivisionTemplate template) {
		this.template = template;
	}

	public void setBattalions(List<BattalionUnit> battalions) {
		this.battalions = battalions;
	}

	public void addMouvementPoint() {
		//get speed
		int speed = 4; //(km/h)
		mouvementPoints += speed;
	}
	public int getMouvementPoint() {
		return mouvementPoints;
	}
	
	
	
}
