package remi.ssp.army;

import remi.ssp.economy.Good;

//an equipment unlocked by researched
// it can be used by only one men, but many other may be needed to handle it.
public class EquipmentTemplate extends Good{
	
	EquipmentType type;
	byte DamageType; //DamageType
	
	
	//stats
//	ArmyStat combatStat;
	// * at each shot, the attacker have force/def chance / 5 of hitting, and /20 of killing. (4 less kill than wounded)
	// * if force/def < 1, then the chances are reduce by 2 more.
	int force;
	int armor;
	
	int rateOfFirePerHour;
	int rangeInMeter; // 1 or 2 for melee. For range, realForce = force * 1- (distance/rangeInMeter)
	//chance of hitting = precisionForMDist/distanceM;
	int precisionForMDist; // radius in M of precision for a km shoot, -1 for melee

	int speedWalkInMH; // max walking speed in m/h
	int speedRunInMH; // max speed in m/h
	int maxRunTimeInS; // max runnning time in ms

	int halfliveInStorage; //in how many days half of the equipment will break when in storage?
	int halfliveInField; //in how many days half of the equipment will break when used/handled by a men?
	int halfLivePerCombat; //in how many combat round half of the equipment will break?

	int manHourCostPer1000; // how many manHour to produce 1000 item?
	int toolsNeededInManHour; //how nay tools needed to create an assembly line for this equipment. Tools cost in manHour
	int educationNeeded; // level of education for the worker to have be be able to produce this item at 100%.
	
	int transportCapacity; //TODO: not in use yet
	
	byte nbMenToHandle; // number of men that can't fight, because they are needed to handle this equipment
	short trainingTimeInDayToUse;
	
}
