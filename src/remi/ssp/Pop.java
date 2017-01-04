package remi.ssp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import remi.ssp.economy.Good;
import remi.ssp.economy.Needs;
import remi.ssp.economy.ProvinceIndustry;

public class Pop {
	Culture culture;
	Province prv; //weak?
	
	int nbMensTotal=0;
	int[] nombreHabitantsParAge = new int[100]; // de 0 à 100ans
	int nbMensInArmy=0;
	int nbMensChomage=0;
	int nbMensCommerce=0; // more = more imports & exports. A part is used as a baseline for shop = efficacity of stock retention (TODO)
	Map<ProvinceIndustry, Integer> nbMensEmployed = new HashMap<>();
	
	int revenueCommerce = 0; //industry one is stored in factories, military in civilisation.

	int cash = 0; // during a tunr, people receive cash. Then, they use it to buy goods.
	Map<Good, Integer> stock = new HashMap<>();
	List<Needs> myNeeds = new ArrayList<>();
//	SortedSet<Needs> popNeeds = new TreeSet<>();
	public Collection<Needs> getPopNeeds() { return myNeeds; }
	
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
	public int[] getNombreHabitantsParAge() { return nombreHabitantsParAge; }
	public int getNbMensInArmy() { return nbMensInArmy; }
	public int getNbMensChomage() { return nbMensChomage; }
	public int getNbMensCommerce() { return nbMensCommerce; }
	public Map<ProvinceIndustry, Integer> getNbMensEmployed() { return nbMensEmployed; }
	public float getEducationMoy() { return educationMoy; }
	public float getSante() { return sante; }
//	public SocialStatus getStatus() { return status; }
	public int getNbMens() { return nbMensTotal; }
	public int getMoney() { return cash; }
	public void addMoney(int money) { this.cash += money; }
	public void setMoney(int money) { this.cash = money; }
	public Map<Good, Integer> getStock() { return stock; }
	public float getRepartitionRevenuMult() { return repartitionMult; }
	public float getRepartitionRevenu(int i){ return 1-(1/(1+(i*repartitionMult))); }
	
	
	//public float criminalite=0; //0=> auncun, 1=> anarchie

	
	
}
