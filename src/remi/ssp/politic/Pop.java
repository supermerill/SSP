package remi.ssp.politic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.economy.Good;
import remi.ssp.economy.Needs;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceCommerce;
import remi.ssp.economy.ProvinceIndustry;

public class Pop {
	Culture culture;
	Province prv; //weak?

	public Pop(){}
	public Pop(/*Culture culture,*/ Province prv){ this.prv = prv; }
	
	int nbMensTotal=0;
	int[] nombreHabitantsParAge = new int[100]; // de 0 à 100ans
	int nbMensInArmy=0;
	int nbMensChomage=0;
	int nbMensCommerce=0; // more = more imports & exports. A part is used as a baseline for shop = efficacity of stock retention (TODO)
	Object2IntMap<ProvinceIndustry> nbMensEmployed = new Object2IntOpenHashMap<>();

	// commerce
	ProvinceCommerce commerceLand = new ProvinceCommerce(); //fake industry to contains commerce data
	ProvinceCommerce commerceSea = new ProvinceCommerce(); //fake industry to contains commerce data
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
	public int getNbMensCommerce() { return nbMensCommerce; }
	public Object2IntMap<ProvinceIndustry> getNbMensEmployed() { return nbMensEmployed; }
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

	
	
}
