package remi.ssp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import remi.ssp.army.DivisionUnit;
import remi.ssp.economy.Good;
import remi.ssp.economy.Industry;
import remi.ssp.economy.ProvinceCommerce;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.economy.TradeRoute;

public class Province implements Serializable{
	private static final long serialVersionUID = ((long)"Province".hashCode()) + 1L;
	
	//for plug-ins
	public Map<String,Serializable> pluginData = new HashMap<>(0);
	
	//can be 5 if making a globe... be careful!
	public Plot[] myPLots = new Plot[6];
	public Province[] proche = new Province[6];
	public int x, y; //'cache'

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
	
	//ressource spéciale
	ProvinceResources probaRessource = null;
	int positionresource = 0; //in the 5-6 hex around
	
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
	Map<Industry, ProvinceIndustry> industries = new HashMap<>();
	Map<Good, ProvinceGoods> stock = new HashMap<>(); //market : industry & pop & merchant buy goods from this. industry & merchant sell goods into this
	int money; //BFR from the marketplace
	int moneyChangePerDay;
	List<TradeRoute> tradeRoutes = new ArrayList<>();
	
	//autres
	public float rayonnementCulturel = 0; //0=nul, X=nombre de personnes connaissant cette province.
	
	public int getNbMens(){
		int nbHabitants = 0;
//		if(nbHabitants<0){
		for(Pop pop : pops)
			nbHabitants += pop.getNbMens();
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
	

	public Collection<ProvinceIndustry> getIndustries() { return industries.values(); }
	public ProvinceIndustry getIndustry(Industry indus) { return industries.get(indus); }
	public Map<Good, ProvinceGoods> getStock() { return stock; }
	public Collection<Pop> getPops() { return this.pops; }
	public Civilisation getOwner() { return owner; }
	public int getMoney() { return money; }
	public void setMoney(int money) { this.money = money; }
	public void addMoney(int money) { this.money += money; }
	public int getMoneyChangePerDay() { return moneyChangePerDay; }
	public void setMoneyChangePerDay(int moneyChangePerDay) { this.moneyChangePerDay = moneyChangePerDay; }
	public List<TradeRoute> getTradeRoute() { return tradeRoutes; }

	public float getRoutes() { return routes; }
	public void setRoutes(float routes) { this.routes = routes; }
	public float getRail() { return rail; }
	public void setRail(float rail) { this.rail = rail; }
	public float getRelief() { return relief; }
	public boolean isCoastal(){ return plages>0; }
	
	
	
}
