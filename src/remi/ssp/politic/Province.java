package remi.ssp.politic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import remi.ssp.army.DivisionUnit;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceCommerce;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.economy.TradeRoute;
import remi.ssp.network.SimpleSerializable;
import remi.ssp.technology.Idea;

public class Province implements SimpleSerializable{
	private static final long serialVersionUID = ((long)"Province".hashCode()) + 1L;
	
	//for plug-ins
	public Map<String,Serializable> pluginData = new HashMap<>(0);
	
	//can be 5 if making a globe... be careful!
	public Plot[] myPLots = new Plot[6];
	public Plot centerPlot;
	public Province[] proche = new Province[6];
	public int x, y; //'cache'
	
	@Override public String toString() {return "prv"+x+":"+y;}

	///meteo
	float pluie; //0-1
	float barometre; //en Pa
	float nuages; //0-1
	float ensoleillementAnnuel; // 0=> nuit perpétuelle, 1=> soleil perpétuel
	float temperatureAnnuelle; //en °c
	float pluieAnnuelle; // en mm
	
	//relief /geographie
	public int surface=10000; //km² total
	public int surfaceSol=10000; //km² 'sec'
	public float relief=0; //0 plat, 1 montagne extreme
	public float humidite = 0.5f; //0 sec, 1 marécage sans terre emergée.
	public float plages=0; //si cote, % d'espace cotier avec plage (0->1), sinon -1?
	
	//ressource spéciale => on Plot
//	ProvinceResources probaRessource = null;
//	int positionresource = 0; //in the 5-6 hex around
	
	//ecologie
	public float pourcentPollution = 0f;
	public float pourcentFaune = 1f; // epuisement de la chasse
	
	//agriculture pourcent are pourcent de sol
	public float pourcentSterile=0; //0=> aucune, 1= >tout
	public float pourcentFriche=0.5f; //0=> aucune, 1= >tout
	public float pourcentForet=0.5f; //0=> aucune, 1= >tout
	
	public float pourcentChamps=0; //0=> aucune, 1= >tout
	public float mecanisationChamps=0; //0=> aucune, X=> score moyen de X
	public float champsRendement=1;
	public float rationReserve=0; // nb de ration stocké dans cette province.

	public float pourcentPrairie=0; //0=> aucune, 1= >tout
	public float elevageRendement=1;
	public int cheptel=0; // nb bete (chevaux-equivalent ~500 portions nouriture)

	public float pourcentUrbanisation=0.0f; //0=> aucune, 1= >tout
	//infrastructure
	 /*
	  * 0=> pas de route, 
	  * 10=> chemin de terre
	  * 100=> route pavée
	  * 1000=> route bitumée
	  * 10000=> autoroute
	  */
	float routes;
	 /*
	  * 0=> pas de rail, 
	  * 10=> rail à voie réduite
	  * 100=> rail
	  * 1000=> rail électrifié
	  * 10000=> ligne grande vitesse / maglev
	  */
	float rail;
	
	//population
//	public int nbHabitants = 0; // cache pour sum(nombreHabitantsParAge) -1 si pas mit à jour
//	public int[] nombreHabitantsParAge = new int[100]; // de 0 à 100ans
//	public float educationMoy=0; //0 ignare, 1 érudit
//	public float sante=0.5f;//0 épidémie foudroyante, 1 santé parfaite.
//	public float pourcentEsclave=0; ///primaire
//	public float pourcentPaysan=0; ///primaire (sauf mines)
//	public float pourcentOuvrier=0; //et artisan, secondaire (+mines)
//	public float pourcentTertiaire=0;
//	public float pourcentElite=0; //les riches, nobles, clergé, intellectuels
	public float criminalite=0; //0=> auncun, 1=> anarchie
//	public Map<Communaute, Float> pourcentCommunaute = new HashMap<>(1);
	public List<Pop> pops = new ArrayList<>();
	public int nbElites = 0; //each week, some pop are promoted elites.
	
	//politics
	Civilisation owner;
	
	//militaire
	public List<DivisionUnit> divisions = new ArrayList<>(1);
	//bataille(s) ?
	
	
	//economic
	ProvinceCommerce commerceLand = new ProvinceCommerce("commLand", this); //fake industry to contains commerce data
	ProvinceCommerce commerceSea = new ProvinceCommerce("commSea", this); //fake industry to contains commerce data
	Map<Industry, ProvinceIndustry> industries = new HashMap<>();
	Map<Good, ProvinceGoods> stock = new HashMap<>(); //market : industry & pop & merchant buy goods from this. industry & merchant sell goods into this
	long money; //BFR from the marketplace
	long previousMoney;
	long moneyConsolidated=0;
	long moneyChangePerDay;
	long moneyChangePerDayConsolidated=0;
	List<TradeRoute> tradeRoutes = new ArrayList<>();
	//to compute the cultural exchange
	Object2LongMap<Province> lastTradeRouteExchange = new Object2LongOpenHashMap<>(); //TODOSAVE, cache value for economy (TODO: reasert)
	
	//autres
	public float rayonnementCulturel = 0; //0=nul, X=nombre de personnes connaissant cette province.
	
	//TODO: serialization
	List<Idea> ideas = new ArrayList<>();
	
	
	public int getNbAdult(){
		int nbHabitants = 0;
//		if(nbHabitants<0){
		for(Pop pop : pops)
			nbHabitants += pop.getNbAdult();
		return nbHabitants;
	}
	
	public void updateSemaine(){
		//croissance de la faune 
		if(pourcentFaune>0.5){
			//10% en plus par ans => 0.2% par semaine, asymptote en 1
			// note: en pratique, c'est deux fois moins au mieux.
			pourcentFaune = 1-((1-pourcentFaune)*0.998f);
		}else{
			//inverse, pour que le croissance soit moins forte quand il y a moins d'individus
			pourcentFaune = pourcentFaune*0.998f;
		}
	}
	

	public ProvinceCommerce getLandCommerce() { return commerceLand; }
	public ProvinceCommerce getSeaCommerce() { return commerceSea; }
	public void addIndustry(ProvinceIndustry industry) { industries.put(industry.getIndustry(), industry); }
	public Collection<ProvinceIndustry> getIndustries() { return industries.values(); }
	public ProvinceIndustry getIndustry(Industry indus) { return industries.get(indus); }
	public Map<Good, ProvinceGoods> getStock() { return stock; }
	public List<Pop> getPops() { return this.pops; }
	public void addPop(Pop newPop) { this.pops.add(newPop); this.pops.sort((p1,p2) -> - Integer.compare(p1.getPopType(), p2.getPopType())); }
	public Civilisation getOwner() { return owner; }
	public void setOwner(Civilisation civ) { this.owner = civ; }
	public long getMoney() { return money; }
	public long getPreviousMoney() { return previousMoney; }
	public long getMoneyConsolidated() { return moneyConsolidated; }
//	public void setMoney(long money) { this.money = money; }
	public void addMoney(long moneyAdd) { 
		this.money += moneyAdd; 
		moneyChangePerDay+=Math.abs(moneyAdd); 
//		if(this.money<0){
//			GlobalDefines.logFlush();
//			System.err.println("Error, now province has no money");
//		}
	}
	public long getMoneyChangePerDay() { return moneyChangePerDay; }
	public long getMoneyChangePerDayConsolidated() { return moneyChangePerDayConsolidated; }
	public List<TradeRoute> getTradeRoute() { return tradeRoutes; }
	public Object2LongMap<Province> getLastTradeRouteExchange(){return lastTradeRouteExchange;}

	public float getRoutes() { return routes; }
	public void setRoutes(float routes) { this.routes = routes; }
	public float getRail() { return rail; }
	public void setRail(float rail) { this.rail = rail; }
	public float getRelief() { return relief; }
	public boolean isCoastal(){ return plages>0; }

	public void resetMoneyComputeNewTurn(int nbDays) { 
		moneyChangePerDayConsolidated = (long)(moneyChangePerDayConsolidated*5+ (this.moneyChangePerDay/(double)nbDays) )/6 ;
		this.moneyChangePerDay = 0; 
		previousMoney = money; 
		moneyConsolidated = (moneyConsolidated*5+this.previousMoney)/6 ;
	}

	public void loadLinks(JsonObject jsonProvince, Carte carte){

		JsonArray arrayPlot = jsonProvince.getJsonArray("plots");
		centerPlot = carte.plots.get(arrayPlot.getInt(0)).get(arrayPlot.getInt(1));
		if(myPLots.length != arrayPlot.size()/2) myPLots = new Plot[arrayPlot.size()/2];
		for(int i=2;i<arrayPlot.size();i+=2){
			myPLots[i/2] = carte.plots.get(arrayPlot.getInt(i)).get(arrayPlot.getInt(i+1));
		}
		
		arrayPlot = jsonProvince.getJsonArray("prvs");
		if(proche.length != arrayPlot.size()/2) proche = new Province[arrayPlot.size()/2];
		for(int i=0;i<arrayPlot.size();i+=2){
			proche[i/2] = carte.provinces.get(arrayPlot.getInt(i)).get(arrayPlot.getInt(i+1));
		}
	}

	public void saveLinks(JsonObjectBuilder jsonOut){


		JsonArrayBuilder array = Json.createArrayBuilder();
		array.add(centerPlot.x);
		array.add(centerPlot.y);
		for(Plot plot : myPLots){
			array.add(plot.x);
			array.add(plot.y);
		}
		jsonOut.add("plots", array);
		
		array = Json.createArrayBuilder();
		for(Province prv : proche){
			array.add(prv.x);
			array.add(prv.y);
		}
		jsonOut.add("prvs", array);
	}
	
	public void load(JsonObject jsonProvince){

		x = jsonProvince.getInt("x");
		y = jsonProvince.getInt("y");
		pluie = (float)jsonProvince.getJsonNumber("pluie").doubleValue();
		barometre = (float)jsonProvince.getJsonNumber("barometre").doubleValue();
		nuages = (float)jsonProvince.getJsonNumber("nuages").doubleValue();
		ensoleillementAnnuel = (float)jsonProvince.getJsonNumber("ensoleillementAnnuel").doubleValue();
		temperatureAnnuelle = (float)jsonProvince.getJsonNumber("temperatureAnnuelle").doubleValue();
		pluieAnnuelle = (float)jsonProvince.getJsonNumber("pluieAnnuelle").doubleValue();
		surface = jsonProvince.getInt("surface");
		surfaceSol = jsonProvince.getInt("surfaceSol");
		relief = (float)jsonProvince.getJsonNumber("relief").doubleValue();
		humidite = (float)jsonProvince.getJsonNumber("humidite").doubleValue();
		plages = (float)jsonProvince.getJsonNumber("plages").doubleValue();
		pourcentPollution = (float)jsonProvince.getJsonNumber("pourcentPollution").doubleValue();
		pourcentFaune = (float)jsonProvince.getJsonNumber("pourcentFaune").doubleValue();
		pourcentSterile = (float)jsonProvince.getJsonNumber("pourcentSterile").doubleValue();
		pourcentFriche = (float)jsonProvince.getJsonNumber("pourcentFriche").doubleValue();
		pourcentForet = (float)jsonProvince.getJsonNumber("pourcentForet").doubleValue();	
		pourcentChamps = (float)jsonProvince.getJsonNumber("pourcentChamps").doubleValue();
		mecanisationChamps = (float)jsonProvince.getJsonNumber("mecanisationChamps").doubleValue();
		champsRendement = (float)jsonProvince.getJsonNumber("champsRendement").doubleValue();
		rationReserve = (float)jsonProvince.getJsonNumber("rationReserve").doubleValue();
		pourcentPrairie = (float)jsonProvince.getJsonNumber("pourcentPrairie").doubleValue();
		elevageRendement = (float)jsonProvince.getJsonNumber("elevageRendement").doubleValue();
		cheptel = jsonProvince.getInt("cheptel");
		pourcentUrbanisation = (float)jsonProvince.getJsonNumber("pourcentUrbanisation").doubleValue();
		routes = (float)jsonProvince.getJsonNumber("routes").doubleValue();
		rail = (float)jsonProvince.getJsonNumber("rail").doubleValue();
		criminalite = (float)jsonProvince.getJsonNumber("criminalite").doubleValue();
		money = jsonProvince.getJsonNumber("money").longValue();
		previousMoney = jsonProvince.getJsonNumber("previousMoney").longValue();
		moneyConsolidated = jsonProvince.getJsonNumber("moneyConsolidated").longValue();
		moneyChangePerDay = jsonProvince.getJsonNumber("moneyChange").longValue();
		moneyChangePerDayConsolidated = jsonProvince.getJsonNumber("moneyChangeConso").longValue();
		rayonnementCulturel = (float)jsonProvince.getJsonNumber("rayonnementCulturel").doubleValue();
		nbElites = jsonProvince.getInt("nbElites");
		
		JsonArray array;
		JsonObject object;
		

		//divisions are loaded separatly
		divisions.clear();
		
		array = jsonProvince.getJsonArray("indus");
		industries.clear();
		for(int i=0;i<array.size();i++){
			ProvinceIndustry indus = new ProvinceIndustry();
			indus.load(array.getJsonObject(i));
			indus.setProvince(this);
			industries.put(indus.getIndustry(), indus);
		}
		

			commerceLand = new ProvinceCommerce("commLand",this);
			commerceLand.load(jsonProvince.getJsonObject("trl"), this);
			commerceSea = new ProvinceCommerce("commSea",this);
			commerceSea.load(jsonProvince.getJsonObject("trs"), this);

		array = jsonProvince.getJsonArray("stock");
		stock.clear();
		for(int i=0;i<array.size();i++){
			object = array.getJsonObject(i);
			Good good = Good.get(object.getString("name"));
			ProvinceGoods prvgood = new ProvinceGoods(good, this);
			prvgood.load(object);
			stock.put(good, prvgood);
		}
		
		//need to be done after loading industries
		array = jsonProvince.getJsonArray("pops");
		pops.clear();
		for(int i=0;i<array.size();i++){
			Pop pop = new Pop(this);
			pop.load(array.getJsonObject(i), this);
			pop.prv = this;
			pops.add(pop);
		}
		
	}
	
	public void save(JsonObjectBuilder jsonOut){

		jsonOut.add("bla", x);
		jsonOut.add("x", x);
		jsonOut.add("y", y);
		jsonOut.add("pluie", pluie);
		jsonOut.add("barometre", barometre);
		jsonOut.add("nuages", nuages);
		jsonOut.add("ensoleillementAnnuel", ensoleillementAnnuel);
		jsonOut.add("temperatureAnnuelle", temperatureAnnuelle);
		jsonOut.add("pluieAnnuelle", pluieAnnuelle);
		jsonOut.add("surface", surface);
		jsonOut.add("surfaceSol", surfaceSol);
		jsonOut.add("relief", relief);
		jsonOut.add("humidite", humidite);
		jsonOut.add("plages", plages);
		jsonOut.add("pourcentPollution", pourcentPollution);
		jsonOut.add("pourcentFaune", pourcentFaune);
		jsonOut.add("pourcentSterile", pourcentSterile);
		jsonOut.add("pourcentFriche", pourcentFriche);
		jsonOut.add("pourcentForet", pourcentForet);
		jsonOut.add("pourcentChamps", pourcentChamps);
		jsonOut.add("mecanisationChamps", mecanisationChamps);
		jsonOut.add("champsRendement", champsRendement);
		jsonOut.add("rationReserve", rationReserve);
		jsonOut.add("pourcentPrairie", pourcentPrairie);
		jsonOut.add("elevageRendement", elevageRendement);
		jsonOut.add("cheptel", cheptel);
		jsonOut.add("pourcentUrbanisation", pourcentUrbanisation);
		jsonOut.add("routes", routes);
		jsonOut.add("rail", rail);
		jsonOut.add("criminalite", criminalite);
		jsonOut.add("money", money);
		jsonOut.add("previousMoney", previousMoney);
		jsonOut.add("moneyConsolidated", moneyConsolidated);
		jsonOut.add("moneyChange", moneyChangePerDay);
		jsonOut.add("moneyChangeConso", moneyChangePerDayConsolidated);
		jsonOut.add("rayonnementCulturel", rayonnementCulturel);
		jsonOut.add("nbElites", nbElites);
		
		JsonArrayBuilder array;
		JsonObjectBuilder objectBuilder;
		
		array = Json.createArrayBuilder();
		for(Pop pop : pops){
			objectBuilder = Json.createObjectBuilder();
			pop.save(objectBuilder);
			array.add(objectBuilder);
		}
		jsonOut.add("pops", array);

		JsonObjectBuilder jsonObj = Json.createObjectBuilder();
		commerceLand.save(jsonObj);
		jsonOut.add("trl", jsonObj);
		jsonObj = Json.createObjectBuilder();
		commerceSea.save(jsonObj);
		jsonOut.add("trs", jsonObj);

		//divisions are saved separatly


		array = Json.createArrayBuilder();
		for(ProvinceIndustry indus : industries.values()){
			objectBuilder = Json.createObjectBuilder();
			indus.save(objectBuilder);
			array.add(objectBuilder);
		}
		jsonOut.add("indus", array);

		array = Json.createArrayBuilder();
		for(Entry<Good, ProvinceGoods> good : stock.entrySet()){
			objectBuilder = Json.createObjectBuilder();
			good.getValue().save(objectBuilder);
			objectBuilder.add("name", good.getKey().getName());
			array.add(objectBuilder);
		}
		jsonOut.add("stock", array);
		
	}

	
	
}
