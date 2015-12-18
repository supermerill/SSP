package remi.ssp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Province {

	//for plug-ins
	public Map<String,Serializable> pluginData = new HashMap<>(0);
	
	//can be 5 if making a globe... be careful!
	public Province[] proche = new Province[6];

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
	public float plages=0; //si cote, % d'espace cotier avec plage (0->1)
	
	//ressource spéciale
	ProvinceRessource probaRessource = null;
	
	//agriculture pourcent are pourcent de sol
	public float pourcentHostile=0; //0=> aucune, 1= >tout
	public float pourcentFriche=0.5f; //0=> aucune, 1= >tout
	public float pourcentForet=0.5f; //0=> aucune, 1= >tout
	
	public float pourcentChamps=0; //0=> aucune, 1= >tout
	public float mecanisationChamps=0; //0=> aucune, X=> score moyen de X
	public float champsRendement=1;
	public float rationReserve=0; // nb de ration stocké dans cette province.

	public float pourcentPrairie=0; //0=> aucune, 1= >tout
	public float elevageRendement=1;
	public int cheptel=0; // nb bete (chevaux-equivalent ~500 portions nouriture)
	
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
	public int nbHabitants = 0; // cache pour sum(nombreHabitantsParAge)
	public int[] nombreHabitantsParAge = new int[100]; // de 0 à 100ans
	public float educationMoy=0; //0 ignare, 1 érudit
	public float sante=0.5f;//0 épidémie foudroyante, 1 santé parfaite.
	public float pourcentPaysan=0; ///primaire (sauf mines)
	public float pourcentOuvrier=0; //et artisan, secondaire (+mines)
	public float pourcentTertiaire=0;
	public float pourcentElite=0; //les riches, nobles, clergé, intellectuels
	public float criminalite=0; //0=> auncun, 1=> anarchie
	public Map<Communaute, Float> pourcentCommunaute = new HashMap<>(1);
	
	//militaire
	List<Division> divisions = new ArrayList<>(1);
	//bataille(s) ?
	
	
	//autres
	float rayonnementCulturel = 0; //0=nul, X=nombre de personnes connaissant cette province.
}
