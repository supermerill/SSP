package remi.ssp.army;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.algorithmes.GlobalRandom;
import remi.ssp.politic.Civilisation;

//a battalion in a divisionUnit
public class BattalionUnit {
	
	//int x,y;
//	Plot plot; //do this in divisionunit
//	Plot goingTo;
	
	Battalion template;

	//ie, at how much my battalions are manned
	int availableMens;
	//ie, at how much my battalions are effective
	int woundedMens;
	//ie the power of mens for each of my battalions
	Object2IntMap<EquipmentDevelopped> equipmentForArmy = new Object2IntOpenHashMap<>();
	
	int battlePosition;
	int nbMsRunInCurrentBattle;
	//cache
	BattalionBattleDecision currentBattleDecision;

	
	
	public int getAvailableMens() { return availableMens;}
	public void setAvailableMens(int availableMens) { this.availableMens = availableMens; }
	public int getWoundedMens() { return woundedMens; }
	public void setWoundedMens(int woundedMens) { this.woundedMens = woundedMens; }
	public Object2IntMap<EquipmentDevelopped> getEquipmentForArmy() { return equipmentForArmy; }
	public void setEquipmentForArmy(Object2IntMap<EquipmentDevelopped> equipmentForArmy) { this.equipmentForArmy = equipmentForArmy; }
	public int getPosition() { return battlePosition; }
	public void setPosition(int position) { this.battlePosition = position; }
	public int getNbMsRunInCurrentBattle() { return nbMsRunInCurrentBattle; }
	public void setNbMsRunInCurrentBattle(int nbMsRunInCurrentBattle) { this.nbMsRunInCurrentBattle = nbMsRunInCurrentBattle; }
	public Battalion getTemplate() { return template; }
	public BattalionBattleDecision getCurrentBattleDecision() { return currentBattleDecision; }
//	public int getX() { return plot.getX(); }
//	public int getY() { return plot.getY(); }
//	public Plot getPlot() { return plot; }
//	public void setPlot(Plot plot) { this.plot = plot; }
//	public Plot getGoingTo() { return goingTo; }
//	public void setGoingTo(Plot goingTo) { this.goingTo = goingTo; }
	public int getBattlePosition() { return battlePosition; }
	public void setBattlePosition(int battlePosition) { this.battlePosition = battlePosition; }
	
	
	// return wounded. killed = wounded/4
	public int attack(BattalionUnit enemy, int nbSeconds){
		//compute ennemyDefense enemy 
		IntList enemyArmor = new IntArrayList(Collections.nCopies(DamageType.values.size(), 0));
		for(EquipmentDevelopped equip : enemy.template.equipment){
			int realQuantity = enemy.equipmentForArmy.getInt(equip);
			if(equip.type == EquipmentType.Protection){
				enemyArmor.set(equip.DamageType, Math.max(enemyArmor.getInt(equip.DamageType), 
							(int) (equip.armor * ((float)realQuantity)/enemy.template.nbFightingMens)));
			}
		}
		//compute offense
		int bestWounded = 0;
		int distance = Math.abs(this.battlePosition - enemy.battlePosition);
//		ArmyStat statsAllies = new ArmyStat();
		for(EquipmentDevelopped equip : this.template.equipment){
			if(equip.type == EquipmentType.MainWeapon){
				if(equip.rangeInMeter < distance){
					//out of range
					continue;
				}else if(equip.rangeInMeter > 2 && distance <= 2){
					//range weapon at melee
					continue;
				}

				//get number of hit
				float numberOfHit = this.equipmentForArmy.getInt(equip);
				if(equip.rangeInMeter > 2){
					numberOfHit *= equip.precisionForMDist / ((float)distance);
				}else{
					// parry/esquive
					numberOfHit = numberOfHit / 2;
				}
				numberOfHit *= ((float)equip.rateOfFirePerHour)/(nbSeconds*3600);
				if(numberOfHit<10){
					//look for an extra chance of hit
					if(GlobalRandom.aleat.getInt(10000, (int)numberOfHit) < 10000 * (numberOfHit-(int)numberOfHit)){
						numberOfHit += 1;
					}
				}
				
				int power = equip.force; //force
				if(power == 0) continue;
				if(equip.rangeInMeter > 2){
					power *= (1- (((float)distance)/equip.rangeInMeter)); //power loss from distance (range weapon)
				}

				// double asymptote
				//if att = 4def, nbWound = nbHit * 0.8
				//if att = 2def, nbWound = nbHit * 0.5
				//if att = def+1, nbWound = nbHit * 0.2
				//if att = def, nbWound = nbHit * 0.1
				//if 2att = def, nbWound = nbHit * 0.03
				int nbWounded = 0;
				final int def = enemyArmor.getInt(equip.DamageType);
				final float powerDivDef = (power/(def+def));
				nbWounded = (int)(1-(1/(1+powerDivDef*powerDivDef)) * numberOfHit);
				if(power <= def){
					//armor cap
					nbWounded /= 2;
				}
				
				bestWounded = Math.max(bestWounded, nbWounded);
			}
		}
		
		return bestWounded;
	}
	
	/*
	 * 
	
	Battalion template;

	//ie, at how much my battalions are manned
	int availableMens;
	//ie, at how much my battalions are effective
	int woundedMens;
	//ie the power of mens for each of my battalions
	Object2IntMap<EquipmentDevelopped> equipmentForArmy = new Object2IntOpenHashMap<>();
	
	int battlePosition;
	int nbMsRunInCurrentBattle;
	BattalionBattleDecision currentBattleDecision;
	 */
	public void load(JsonObject jsonObject, Civilisation civ) {
		availableMens = jsonObject.getInt("amens");
		woundedMens = jsonObject.getInt("wmens");
		battlePosition = jsonObject.getInt("pos");
		nbMsRunInCurrentBattle = jsonObject.getInt("time");
		int id2find = jsonObject.getInt("batId");
		for(Battalion bat : civ.getBattalionTemplate()){
			if(id2find == bat.getId()){
				template = bat;
				break;
			}
		}
		JsonArray array = jsonObject.getJsonArray("equips");
		for(int i=0;i+1<array.size(); i+=2){
			id2find = array.getInt(i);
			free: for(EquipmentDevelopped equi : civ.getEquipmentReserve().keySet()){
				if(id2find == equi.getId()){
					equipmentForArmy.put(equi, array.getInt(i+1));
					break free;
				}
			}
		}
	}
	public void save(JsonObjectBuilder objSave) {
		objSave.add("amens", availableMens);
		objSave.add("wmens", woundedMens);
		objSave.add("pos", battlePosition);
		objSave.add("time", nbMsRunInCurrentBattle);
		objSave.add("batId", template.getId());
		JsonArrayBuilder array = Json.createArrayBuilder();
		for(Entry<EquipmentDevelopped> equi : equipmentForArmy.object2IntEntrySet()){
			array.add(equi.getKey().getId());
			array.add(equi.getIntValue());
		}
		objSave.add("equips", array);
	}
	
}
