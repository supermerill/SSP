package remi.ssp;

import java.util.List;
import java.util.Map;

public class Province {

	public Province[] proche = new Province[6];

	///meteo
	float pluie; //0-1
	float barometre; //en Pa
	float nuages; //0-1
	float ensoleillementAnnuel; // 0=> nuit perpétuelle, 1=> soleil perpétuel
	float temperatureAnnuelle; //en °c
	float pluieAnnuelle; // en mm
	
	//relief /geographie
	public int surface=10000; //km² 'sec'
	public float relief=0; //0 plat, 1 montagne extreme
	public float humidite = 0.5f; //0 sec, 1 marécage sans terre emergée.
	float plages=0; //si cote, % d'espace cotier avec plage (0->1)
	
	//ressources
	ProvinceRessource[] probaRessources = new ProvinceRessource[Ressource.values().length];
	
	//agriculture
	float pourcentChamps; //0=> aucune, 1= >tout
	float mecanisationChamps; //0=> aucune, X=> score moyen de X
	
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
	int[] nombreHabitantsParAge;
	float educationMoy; //0 ignare, 1 érudit
	float sante;//0 épidémie foudroyante, 1 santé parfaite.
	int nbPaysan; ///primaire (sauf mines)
	int nbOuvrier; //et artisan, secondaire (+mines)
	int nbtertiaire;
	int nbElite; //les riches, nobles, clergé, intellectuels
	float criminalite; //0=> auncun, 1=> anarchie
	Map<Communaute, Float> percentCommunaute;
	
	//militaire
	List<Division> divisions;
	//bataille(s) ?
	
	
	//autres
	float rayonnementCulturel; //0=nul, X=nombre de personnes connaissant cette province.
}
