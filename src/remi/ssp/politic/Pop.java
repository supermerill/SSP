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
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.CurrentGame;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.Job;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceCommerce;

public class Pop {
	Culture culture; //TODO: save/load with index
	Province prv; //weak?

	public Pop(){}
	public Pop(/*Culture culture,*/ Province prv){ this.prv = prv; }
	
	private int nbMensTotal=0;
	private int[] nombreHabitantsParAge = new int[100]; // de 0 à 100ans
	int nbMensInArmy=0; //cf job?
	int nbMensChomage=0;
//	int nbMensCommerce=0;//cf job // more = more imports & exports. A part is used as a baseline for shop = efficacity of stock retention (TODO)
	Object2IntMap<Job> nbMensEmployed = new Object2IntOpenHashMap<>();

	// commerce
	ProvinceCommerce commerceLand = new ProvinceCommerce("commLand"); //fake industry to contains commerce data
	ProvinceCommerce commerceSea = new ProvinceCommerce("commSea"); //fake industry to contains commerce data
//	int revenueCommerce = 0; //industry one is stored in factories, military in civilisation.

	int cash = 0; // during a tunr, people receive cash. Then, they use it to buy goods.
	Object2IntMap<Good> stock = new Object2IntOpenHashMap<>();
	List<PopNeed> myNeeds = new ArrayList<>();
//	SortedSet<Needs> popNeeds = new TreeSet<>();
	public Collection<PopNeed> getPopNeeds() { return myNeeds; }
	
	public float educationMoy=0; //0 ignare, 1 érudit
	public float sante=0.5f;//0 épidémie foudroyante, 1 santé parfaite.
	
	//pas d'eslave en v1 (et c'est dommage)
//	public SocialStatus status;
//	public float pourcentEsclave=0; ///primaire
//	public float pourcentPaysan=0; ///primaire (sauf mines)	
//	public float pourcentOuvrier=0; //et artisan, secondaire (+mines)
//	public float pourcentTertiaire=0;
//	public float pourcentElite=0; //les riches, nobles, clergé, intellectuels
	 // pour y = 1-(1/(1+(x*repartitionMult))), x est la fraction la plus riche de la pop. 
	//y est la fraction de la richesse possédé. 
	//Ne fonctionne pas pour un grand x
	float repartitionMult = 5; //5 => 80/20
	
	
	public Culture getCulture() { return culture; }
	public Province getProvince() { return prv; }
	public int getNombreHabitantsParAge(int age) { if(age>=nombreHabitantsParAge.length) return 0; return nombreHabitantsParAge[age]; }
	public void addHabitants(int age, int nb){ nombreHabitantsParAge[age] += nb; nbMensTotal += nb; }
	public void removeHabitants(int age, int nb){ nombreHabitantsParAge[age] -= nb; nbMensTotal -= nb; }
	public int getNbMensInArmy() { return nbMensInArmy; }
	public int getNbMensChomage() { return nbMensChomage; }
	public void setNbMensChomage(int nb) { nbMensChomage = nb; }
	public void addNbMensChomage(int nb) { nbMensChomage += nb; }
//	public int getNbMensCommerce() { return nbMensCommerce; }
	public Object2IntMap<Job> getNbMensEmployed() { return nbMensEmployed; }
	public float getEducationMoy() { return educationMoy; }
	public float getSante() { return sante; }
//	public SocialStatus getStatus() { return status; }
	public int getNbMens() { return nbMensTotal; }
	public int getMoney() { return cash; }
	public void addMoney(int money) { this.cash += money; }
	public void setMoney(int money) { this.cash = money; }
	public Object2IntMap<Good> getStock() { return stock; }
	public float getRepartitionRevenuMult() { return repartitionMult; }
	public float getRepartitionRevenu(int i){ return 1-(1/(1+(i*repartitionMult))); }
	public ProvinceCommerce getLandCommerce() { return commerceLand; }
	public ProvinceCommerce getSeaCommerce() { return commerceSea; }
	
	
	//public float criminalite=0; //0=> auncun, 1=> anarchie

	public void load(JsonObject jsonObj, Province prv){
		this.prv = prv;
		nbMensTotal = jsonObj.getInt("nb");
		nbMensInArmy = jsonObj.getInt("nbArmy");
		nbMensChomage = jsonObj.getInt("nbUE");
//		nbMensCommerce = jsonObj.getInt("nbBI");
		JsonArray array = jsonObj.getJsonArray("popD");
		for(int i=0;i<array.size() && i< nombreHabitantsParAge.length;i++){
			nombreHabitantsParAge[i] = array.getInt(i);
		}
		array = jsonObj.getJsonArray("nbE");
		nbMensEmployed.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			String jobName = object.getString("name");
			Industry jobIndustry = Industry.get(jobName);
			if(jobIndustry != null){
				nbMensEmployed.put(prv.industries.get(jobIndustry), object.getInt("nb"));
			}else if(jobName.equals("commLand")){
				commerceLand = new ProvinceCommerce("commLand"); //TODO: use moddable factories in all load() method in the project
				commerceLand.load(jsonObj.getJsonObject("trl"), prv);
			}else if(jobName.equals("commSea")){
				commerceSea = new ProvinceCommerce("commSea");
				commerceSea.load(jsonObj.getJsonObject("trs"), prv);
			}
		}
		
		cash = jsonObj.getInt("cash");
		
		array = jsonObj.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getInt("nb"));
		}
		array = jsonObj.getJsonArray("needs");
		myNeeds.clear();
		for(int i=0;i<array.size();i++){
			PopNeed need = PopNeed.create(array.getJsonObject(i).getString("name"), this);
			need.load(array.getJsonObject(i));
			myNeeds.add(need);
		}
		educationMoy = (float) jsonObj.getJsonNumber("edu").doubleValue();
		sante = (float) jsonObj.getJsonNumber("pv").doubleValue();
		
		culture = CurrentGame.cultures.get(jsonObj.get("cultName"));
		
	}
	
	public void save(JsonObjectBuilder jsonOut){
		jsonOut.add("nb", nbMensTotal);
		jsonOut.add("nbArmy", nbMensInArmy);
		jsonOut.add("nbUE", nbMensChomage);
//		jsonOut.add("nbBI", nbMensCommerce);
		
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(int i=0; i< nombreHabitantsParAge.length;i++){
			array.add(nombreHabitantsParAge[i]);
		}
		jsonOut.add("popD", array);
		array = Json.createArrayBuilder();
		for(Entry<Job> good : nbMensEmployed.object2IntEntrySet()){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getIntValue());
			array.add(objectBuilder);
		}
		jsonOut.add("nbE", array);
		
		JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		commerceLand.save(jsonObj);
		jsonOut.add("trl", jsonObj);
		jsonObj = Json.createObjectBuilder();
		commerceSea.save(jsonObj);
		jsonOut.add("trs", jsonObj);
		jsonOut.add("cash", cash);

		array = Json.createArrayBuilder();
		for(Entry<Good> good : stock.object2IntEntrySet()){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getIntValue());
			array.add(objectBuilder);
		}
		jsonOut.add("stock", array);
		
		jsonOut.add("needs", array);
		for(int i=0;i<myNeeds.size();i++){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			myNeeds.get(i).save(objectBuilder);
			objectBuilder.add("name", myNeeds.get(i).getName());
			array.add(objectBuilder);
		}
		jsonOut.add("edu", educationMoy);
		jsonOut.add("pv", sante);

		jsonOut.add("cultName", culture.getName());
	}
	
}
