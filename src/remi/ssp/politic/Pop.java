package remi.ssp.politic;

import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import remi.ssp.GlobalDefines;
import remi.ssp.algorithmes.GlobalRandom;
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
	public float foodEffectiveness = 1; // 0->1 : 0 if almost no food, 1 if full food needs is ok.
	
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
	
	
	@Override
	public String toString() {
		return popTypeName.get(popType)+"_"+culture+"@"+prv.x+":"+prv.y;
	}
	
	public Culture getCulture() { return culture; }
	public Province getProvince() { return prv; }
//	public int getNombreHabitantsParAge(int age) { if(age>=nombreHabitantsParAge.length) return 0; return nombreHabitantsParAge[age]; }
//	public void addHabitants(int age, int nb){ nombreHabitantsParAge[age] += nb; nbMensTotal += nb; }
//	public void addHabitants(int nb){ nbMensTotal += nb; }
	public long getNbMensInArmy() { return nbMensInArmy; }
	public void addNbMensInArmy(long nb) { nbMensInArmy+=nb; }
	public long getNbMensChomage() { return nbMensChomage; }
	public void setNbMensChomage(long nb) { nbMensChomage = nb; }
	public void addNbMensChomage(long nb) { nbMensChomage += nb; }
//	public int getNbMensCommerce() { return nbMensCommerce; }
	@Deprecated public Object2LongMap<Job> getNbMensEmployed() { return nbMensEmployed; }
	public long getNbMensEmployed(Job job) { return nbMensEmployed.getLong(job); }
	public void addNbMensEmployed(Job job, long nbHire) { nbMensEmployed.put(job, nbMensEmployed.get(job) + nbHire); 
		if(nbMensEmployed.get(job)<0){
			System.err.println("Error, now indus "+job+" has "+nbMensEmployed.get(job)+" worker from "+this);
		}
	}
	public float getEducationMoy() { return educationMoy; }
	public float getSante() { return sante; }
//	public SocialStatus getStatus() { return status; }
	public long getMoney() { return cash; }
	protected long gain=0;
	public long getGain() { return gain; }
	public void resetGain() { this.gain = 0; }
	public void addMoney(long moneyAdd) { 
		this.cash += moneyAdd; 
		if(moneyAdd>0) gain += moneyAdd;
		if(cash<0){
			System.err.println("Error, now pop has no money");
		}
	}
//	public void setMoney(long money) { this.cash = money; }
	public Object2LongMap<Good> getStock() { return stock; }
	public float getRepartitionRevenuMult() { return repartitionMult; }
	public float getRepartitionRevenu(float i){ return 1-(1/(1+(i*repartitionMult))); }
	public ProvinceCommerce getLandCommerce() { return commerceLand; }
	public ProvinceCommerce getSeaCommerce() { return commerceSea; }
	

	public long getNbAdult() { return nbAdult; }
	public void addAdult(final long nb){ 
//		logln("remove "+ (-nb)+" adults from "+nbAdult+", actually "+nbAdult+" = "+(nbMensChomage+nbMensInArmy+nbMensEmployed.values().stream().mapToLong(val -> val).sum()));
		nbAdult += nb; 
		if(nbAdult<0){ 
			System.err.println("Error, too low number of mens in pop: "+nbAdult);
		}
		if(nb>0){
			addNbMensChomage(nb);
//			logln("add "+nb+" chomeurs");
		}
		if(nb<0){
			logln(", \"nbRemoved\":"+(-nb));
			//remove from chomage or job
			long nbRemoved = Math.min(getNbMensChomage(), -nb);
			setNbMensChomage(getNbMensChomage() - nbRemoved );
			logln(", \"nbRemoved chomage\":"+nbRemoved);
//			logln("remove "+nbRemoved+" from chomage");
			ArrayList<Job> jobs = new ArrayList<>(getNbMensEmployed().keySet());
			for(Iterator<Job> it = jobs.iterator(); it.hasNext();){
				Job job = it.next(); if(getNbMensEmployed(job) <= 0) it.remove();
			}
			while(nbRemoved<-nb && jobs.size()>0){
				final int i= GlobalRandom.aleat.getInt(jobs.size(), (int)-nb);
				final long nbMensHere = getNbMensEmployed(jobs.get(i));
				final long removedHere = Math.min(nbMensHere, Math.min(- (nb + nbRemoved), 2 -nb/10));
				nbRemoved += removedHere;
//				getNbMensEmployed().put(jobs.get(i), nbMensHere - nbRemoved);
				addNbMensEmployed(jobs.get(i), -removedHere);
				logln(", \"removefrom "+jobs.get(i)+"\":{ \"removed\": "+removedHere+",\"nowHas\":"+getNbMensEmployed(jobs.get(i))
					+",\"previously\":"+nbMensHere+"}");
				
				if(nbMensHere - removedHere <= 0) jobs.remove(i);
			}
			//then try with army
			if(nbRemoved<-nb){
				final long removedHere = Math.min(getNbMensInArmy(), - (nb + nbRemoved));
				addNbMensInArmy(-removedHere);
				logln(", \"removed from army\": "+removedHere);
				nbRemoved += removedHere;
			}
			if(nbRemoved<-nb){
				GlobalDefines.logFlush();
				System.err.println("error, can't find the adult of "+(- (nb+nbRemoved))+" died adults.");
			}
		}
		//check
		long nbPopInJobs = getNbMensChomage() + getNbMensInArmy();
		for(Entry<Job> entry : getNbMensEmployed().object2LongEntrySet()){
			nbPopInJobs+=entry.getLongValue();
		}
		if(nbPopInJobs != getNbAdult()){
			System.err.println("Error: nbAdults="+getNbAdult()+", but i have "+nbPopInJobs+" in jobs");
		}
//		logln("pop "+popType+" now has "+nbAdult+" adult = "+(nbMensChomage+nbMensInArmy+nbMensEmployed.values().stream().mapToLong(val -> val).sum()));
	}
	public long getNbChildren() { return nbChildren; }
	public void addChildren(final long nb){ 
		nbChildren += nb; 
		if(nbChildren<0){
			System.err.println("Error, too low number of children in pop: "+nbChildren);
		}
	}
	public long getNbElder() { return nbElder; }
	public void addElder(final long nb){ 
		nbElder += nb; 
		if(nbElder<0){
			System.err.println("Error, too low number of nbElder in pop: "+nbElder);
		}
	}
	public int getPopType() { return popType; }
	public void setPopType(int popType) { this.popType = popType; }
	//0 for poor, little for middle, many for rich
	public int getCoeffInvestors(){ int type = getPopType(); return type*type*type; }
	public float getFoodEffectiveness() { return foodEffectiveness; }
	public void setFoodEffectiveness(float ratio) { this.foodEffectiveness = ratio; }
	
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
		gain = jsonObj.getJsonNumber("gain").longValue();
		
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
		foodEffectiveness = (float) jsonObj.getJsonNumber("foodEff").doubleValue();
		
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
		jsonOut.add("gain", gain);

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
		jsonOut.add("foodEff", foodEffectiveness);

		jsonOut.add("cultName", culture.getName());
	}
	
}
