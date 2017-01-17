package remi.ssp.army;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Civilisation;

public class Battalion {

	private final int id;
	public Battalion(int id){this.id = id;}
	public Battalion(Civilisation others){
		int i=0;
		for(Battalion other : others.getBattalionTemplate()){
			if(i <= other.id){
				i = other.id+1;
			}
		}
		this.id = i;
	}
	public final int getId(){return id;}
	

	BattalionBehavior behavior;
	int nbFightingMens;
	int nbHandlingMens;
	
	//equipement
	//each equipment must be in the same quantity as the number of fighting mens. A battalion is a specialized unit in a division.
	// ex: a tank battalion. 10 fighting mens, 10 tanks, 30 handling mens.
	// ex2: a cavalry battalion: 10 fighting mens, 10 horses, 10 lances, 10 swords, 10 armor, 10 shields, 30 squires(écuyers)
	// ex3: a troup transport battalion. 100 "fighting" mens (but in support), 100 trucks, 100 handling mens.
	// ex4: an archery unit: 100 fighters, 100 arcs, 100 knives.
	List<EquipmentDevelopped> equipment = new ArrayList<>();
	
//	ArmyStat stats = new ArmyStat();
//	//stats (cache from equipment)
//	
//	
//	void init(){
//		for(Entry<EquipmentDevelopped, Integer> equipAndMens : equipment2Quantity.entrySet()){
//			stats.crushingArmor = equipAndMens.
//		}
//	}
	
//	void setStats(ArmyStat stats, Object2IntMap<EquipmentDevelopped> dispo){
//		stats.reset();
//		for(EquipmentDevelopped equip : equipment2Quantity){
//			int realQuantity = dispo.getInt(equip);
//			if(equip.type == EquipmentType.MainWeapon){
//				stats.setMax(equip.combatStat, ((float)realQuantity)/nbFightingMens);
//			}
//		}
//		for(EquipmentDevelopped equip : equipment2Quantity){
//			int realQuantity = dispo.get(equip);
//			if(equip.type == EquipmentType.SecondaryWeapon){
//				stats.add(equip.combatStat, ((float)realQuantity)/nbFightingMens);
//			}
//		}
//	}

	public BattalionBehavior getBehavior() { return behavior; }
	public int getNbFightingMens() { return nbFightingMens; }
	public int getNbHandlingMens() { return nbHandlingMens; }
	public Collection<EquipmentDevelopped> getEquipment() { return equipment; }
	
	
	/**
	 * @return meter/hours
	 */
	public int getMaxWalkSpeed(){
		int minSpeed = 0;
		for(EquipmentDevelopped equip : equipment){
			minSpeed = Math.min(minSpeed, equip.speedWalkInMH);
		}
		return minSpeed;
	}
	/**
	 * @return meter/hours
	 */
	public int getMaxRunSpeed(){
		int minSpeed = 0;
		for(EquipmentDevelopped equip : equipment){
			minSpeed = Math.min(minSpeed, equip.speedRunInMH);
		}
		return minSpeed;
	}
	/**
	 * @return seconds
	 */
	public int getMaxRunTime(){
		int min = 0;
		for(EquipmentDevelopped equip : equipment){
			min = Math.min(min, equip.maxRunTimeInS);
		}
		return min;
	}
	/**
	 * @return meter
	 */
	public int getMaxRange() {
		int max = 0;
		for(EquipmentDevelopped equip : equipment){
			max = Math.max(max, equip.rangeInMeter);
		}
		return max;
	}
	
	public void load(JsonObject objBat, Civilisation civ) {
		behavior = BattalionBehavior.get(objBat.getString("behavior"));
		nbFightingMens = objBat.getInt("nbFmens");
		nbHandlingMens = objBat.getInt("nbHmens");
		JsonArray arrayEquip = objBat.getJsonArray("equips");
		for(int i=0;i<arrayEquip.size();i++){
			int id = arrayEquip.getInt(i);
			free: for(EquipmentDevelopped equip : civ.getEquipmentReserve().keySet()){
				if(equip.getId() == id){
					equipment.add(equip);
					break free;
				}
			}
		}
	}
	public void save(JsonObjectBuilder jsonOut) {
		jsonOut.add("behavior", behavior.getClass().getName());
		jsonOut.add("nbFmens", nbFightingMens);
		jsonOut.add("nbHmens", nbHandlingMens);

		JsonArrayBuilder arrayEquip = Json.createArrayBuilder();
		for(EquipmentDevelopped equip : equipment){
			arrayEquip.add(equip.getId());
		}
		jsonOut.add("equips", arrayEquip);
	}
	
}
