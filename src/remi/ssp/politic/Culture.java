package remi.ssp.politic;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Culture {
	
	
	
	//TODO tenet, etc
	
	String name;
	
	//caracteristiques: linéaire de -1 000 000 à + 1 000 000
	
	//gros pompage de stellaris
	int esclavage2freedom = 0;
	int spiritual2athee = 0;
	int collectivist2individualist = 0;
	int warrior2peaceful = 0;
	int rascite2tolerant = 0;
	
	//un peu d'autres pour le fun
	int violence2talking = 0;
	int polytheiste2monotheiste = 0;
	int puritain2hippie = 0;
	int vengance2mercy = 0;
	int unrespectful2honor = 0;
	
	
	//croyances spirituelles. <-500 000 => banned, >100 000 => accepted
	int nature = 0;
	int stars = 0;
	int moon = 0;
	int sea = 0;
	int divination = 0;
	int sorcery = 0;
	
	//religion (TODO: extension) (ie, organized beliefs)
	String religionName = "Flying Spaghetti Monster";
	
	
	public String getName(){return name;}

	public void load(JsonObject jsonObject) {
		name = jsonObject.getString("name");
		religionName = jsonObject.getString("religionName");

		esclavage2freedom = jsonObject.getInt("esclavage2freedom");
		spiritual2athee = jsonObject.getInt("spiritual2athee");
		collectivist2individualist = jsonObject.getInt("collectivist2individualist");
		warrior2peaceful = jsonObject.getInt("warrior2peaceful");
		rascite2tolerant = jsonObject.getInt("rascite2tolerant");
		violence2talking = jsonObject.getInt("violence2talking");
		polytheiste2monotheiste = jsonObject.getInt("polytheiste2monotheiste");
		puritain2hippie = jsonObject.getInt("puritain2hippie");
		vengance2mercy = jsonObject.getInt("vengance2mercy");
		unrespectful2honor = jsonObject.getInt("unrespectful2honor");
		nature = jsonObject.getInt("nature");
		stars = jsonObject.getInt("stars");
		moon = jsonObject.getInt("moon");
		sea = jsonObject.getInt("sea");
		divination = jsonObject.getInt("divination");
		sorcery = jsonObject.getInt("sorcery");
		
	}

	public void save(JsonObjectBuilder objectBuilder) {
		objectBuilder.add("name", name);
		objectBuilder.add("religionName", religionName);
		
		objectBuilder.add("esclavage2freedom", esclavage2freedom);
		objectBuilder.add("spiritual2athee", spiritual2athee);
		objectBuilder.add("collectivist2individualist", collectivist2individualist);
		objectBuilder.add("warrior2peaceful", warrior2peaceful);
		objectBuilder.add("rascite2tolerant", rascite2tolerant);
		objectBuilder.add("violence2talking", violence2talking);
		objectBuilder.add("polytheiste2monotheiste", polytheiste2monotheiste);
		objectBuilder.add("puritain2hippie", puritain2hippie);
		objectBuilder.add("vengance2mercy", vengance2mercy);
		objectBuilder.add("unrespectful2honor", unrespectful2honor);
		objectBuilder.add("nature", nature);
		objectBuilder.add("stars", stars);
		objectBuilder.add("moon", moon);
		objectBuilder.add("sea", sea);
		objectBuilder.add("divination", divination);
		objectBuilder.add("sorcery", sorcery);
		
	}

	
	
}
