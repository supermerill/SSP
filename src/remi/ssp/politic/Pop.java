package remi.ssp.politic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.CurrentGame;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.Job;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceCommerce;

public class Pop {
	Province prv; //weak?
	Culture culture; //TODO: save/load with index
	int popType;
	public static List<String> popTypeName = new ArrayList<>();
	static{
		//note: the algorithm try to use the getRepartitionRevenu to keep track of ideal pop wealth distribution.
		// each category (poor, middle and rich) should have the same wealth.
		
		// POOR : basic class
		// some can be upped to middle if the global welth of this category is too high vs the middle one
		popTypeName.add("poor");
		// like poors one, but with two time more earnings, can be manager, traders because they have some earnings.
		// some can be downgrade to poor if the wealth of middle+rish is too low.
		// some can be upgraded to rich is wealth of poor+middle is too high.
		popTypeName.add("middle");
		// RICH : possess things inside the province (that can be possessed)
		//		can be downdgraded to middle if not enough things to own, or not wealthy enough vs middle
		popTypeName.add("rich"); 
	}

	public Pop(){}
	public Pop(/*Culture culture,*/ Province prv){ this.prv = prv; }
	

	private long nbAdult=0;
	private long nbChildren=0;
	private long nbElder=0;
	//private int[] nombreHabitantsParAge = new int[100]; // de 0 à 100ans
	long nbMensInArmy=0; //cf job?
	long nbMensChomage=0;
//	int nbMensCommerce=0;//cf job // more = more imports & exports. A part is used as a baseline for shop = efficacity of stock retention (TODO)
	Object2LongMap<Job> nbMensEmployed = new Object2LongOpenHashMap<>();

	// commerce
	ProvinceCommerce commerceLand = new ProvinceCommerce("commLand"); //fake industry to contains commerce data
	ProvinceCommerce commerceSea = new ProvinceCommerce("commSea"); //fake industry to contains commerce data
//	int revenueCommerce = 0; //industry one is stored in factories, military in civilisation.

	long cash = 0; // during a tunr, people receive cash. Then, they use it to buy goods.
	Object2LongMap<Good> stock = new Object2LongOpenHashMap<>();
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
	float repartitionMult = 20; //20 => 80/20
	
	
	public Culture getCulture() { return culture; }
	public Province getProvince() { return prv; }
//	public int getNombreHabitantsParAge(int age) { if(age>=nombreHabitantsParAge.length) return 0; return nombreHabitantsParAge[age]; }
//	public void addHabitants(int age, int nb){ nombreHabitantsParAge[age] += nb; nbMensTotal += nb; }
//	public void addHabitants(int nb){ nbMensTotal += nb; }
	public long getNbMensInArmy() { return nbMensInArmy; }
	public long getNbMensChomage() { return nbMensChomage; }
	public void setNbMensChomage(long nb) { nbMensChomage = nb; }
	public void addNbMensChomage(long nb) { nbMensChomage += nb; }
//	public int getNbMensCommerce() { return nbMensCommerce; }
	public Object2LongMap<Job> getNbMensEmployed() { return nbMensEmployed; }
	public float getEducationMoy() { return educationMoy; }
	public float getSante() { return sante; }
//	public SocialStatus getStatus() { return status; }
	public long getMoney() { return cash; }
	public void addMoney(long moneyAdd) { this.cash += moneyAdd; }
//	public void setMoney(long money) { this.cash = money; }
	public Object2LongMap<Good> getStock() { return stock; }
	public float getRepartitionRevenuMult() { return repartitionMult; }
	public float getRepartitionRevenu(float i){ return 1-(1/(1+(i*repartitionMult))); }
	public ProvinceCommerce getLandCommerce() { return commerceLand; }
	public ProvinceCommerce getSeaCommerce() { return commerceSea; }
	

	public long getNbAdult() { return nbAdult; }
	public void addAdult(final int nb){ 
		System.out.println("remove "+ (-nb)+" adults from "+nbAdult);
		nbAdult += nb; 
		if(nbAdult<0){ 
			System.err.println("Error, too low number of mens in pop: "+nbAdult);
		}
	}
	public long getNbChildren() { return nbChildren; }
	public void addChildren(final int nb){ 
		nbChildren += nb; 
		if(nbChildren<0){
			System.err.println("Error, too low number of children in pop: "+nbChildren);
		}
	}
	public long getNbElder() { return nbElder; }
	public void addElder(final int nb){ 
		nbElder += nb; 
		if(nbElder<0){
			System.err.println("Error, too low number of nbElder in pop: "+nbElder);
		}
	}
	public int getPopType() { return popType; }
	public void setPopType(int popType) { this.popType = popType; }
	//0 for poor, little for middle, many for rich
	public int getCoeffInvestors(){ int type = getPopType(); return type*type*type; }
	
	//public float criminalite=0; //0=> auncun, 1=> anarchie

	public void load(JsonObject jsonObj, Province prv){
		this.prv = prv;
		popType = jsonObj.getJsonNumber("type").intValue();
		nbAdult = jsonObj.getJsonNumber("nbA").longValue();
		nbChildren = jsonObj.getJsonNumber("nbC").longValue();
		nbElder = jsonObj.getJsonNumber("nbE").longValue();
		nbMensInArmy = jsonObj.getJsonNumber("nbArmy").longValue();
		nbMensChomage = jsonObj.getJsonNumber("nbUE").longValue();
//		nbMensCommerce = jsonObj.getLong("nbBI");
//		JsonArray array = jsonObj.getJsonArray("popD");
//		for(int i=0;i<array.size() && i< nombreHabitantsParAge.length;i++){
//			nombreHabitantsParAge[i] = array.getInt(i);
//		}
		JsonArray array = jsonObj.getJsonArray("nbE");
		nbMensEmployed.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			String jobName = object.getString("name");
			Industry jobIndustry = Industry.get(jobName);
			if(jobIndustry != null){
				nbMensEmployed.put(prv.industries.get(jobIndustry), object.getJsonNumber("nb").longValue());
			}else if(jobName.equals("commLand")){
				commerceLand = new ProvinceCommerce("commLand"); //TODO: use moddable factories in all load() method in the project
				commerceLand.load(jsonObj.getJsonObject("trl"), prv);
			}else if(jobName.equals("commSea")){
				commerceSea = new ProvinceCommerce("commSea");
				commerceSea.load(jsonObj.getJsonObject("trs"), prv);
			}
		}
		
		cash = jsonObj.getJsonNumber("cash").longValue();
		
		array = jsonObj.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			JsonObject object = array.getJsonObject(i);
			stock.put(Good.get(object.getString("name")), object.getJsonNumber("nb").longValue());
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
		jsonOut.add("type", popType);
		jsonOut.add("nbA", nbAdult);
		jsonOut.add("nbC", nbChildren);
		jsonOut.add("nbE", nbElder);
		jsonOut.add("nbArmy", nbMensInArmy);
		jsonOut.add("nbUE", nbMensChomage);
//		jsonOut.add("nbBI", nbMensCommerce);
		
//		JsonArrayBuilder array = Json.createArrayBuilder();
//		for(int i=0; i< nombreHabitantsParAge.length;i++){
//			array.add(nombreHabitantsParAge[i]);
//		}
//		jsonOut.add("popD", array);
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Entry<Job> good : nbMensEmployed.object2LongEntrySet()){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getLongValue());
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
		for(Entry<Good> good : stock.object2LongEntrySet()){
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add("name", good.getKey().getName());
			objectBuilder.add("nb", good.getLongValue());
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
