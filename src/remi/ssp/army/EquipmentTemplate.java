package remi.ssp.army;

import java.util.HashMap;

import remi.ssp.economy.Good;

//an equipment unlocked by researched
// it can be used by only one men, but many other may be needed to handle it.
public class EquipmentTemplate extends Good{
	

	public static class EquipmentFactory extends GoodFactory{
		public static HashMap<String, EquipmentTemplate> equipList = new HashMap<>();
		public static EquipmentTemplate get(String name) { return equipList.get(name); }
		private static EquipmentTemplate create(String name, int weight, float storageLossPerYear, int desirability){
			EquipmentTemplate good = new EquipmentTemplate();
			good.name = name;
			good.transportability = weight;
			good.storageKeepPerYear = storageLossPerYear;
			good.desirability = desirability;
			equipList.put(name, good);
			return good;
		}

		private EquipmentTemplate currentEquipment = null;
		private static EquipmentFactory me = new EquipmentFactory();;
		private EquipmentFactory(){}
		
		public static EquipmentFactory create(String name, float storageLossPerYear, int desirability){
			me.currentEquipment = create(name, 1000, storageLossPerYear, desirability);
			me.currentGood = me.currentEquipment;
			return me;
		}

		public EquipmentFactory force(int val) {	currentEquipment.force = val; return me; }
		public EquipmentFactory armor(int val) {	currentEquipment.armor = val; return me; }
		public EquipmentFactory rateOfFirePerHour(int val) {	currentEquipment.rateOfFirePerHour = val; return me; }
		public EquipmentFactory rangeInMeter(int val) {	currentEquipment.rangeInMeter = val; return me; }
		public EquipmentFactory precisionForMDist(int val) {	currentEquipment.precisionForMDist = val; return me; }
		public EquipmentFactory speedWalkInMH(int val) {	currentEquipment.speedWalkInMH = val; return me; }
		public EquipmentFactory speedRunInMH(int val) {	currentEquipment.speedRunInMH = val; return me; }
		public EquipmentFactory maxRunTimeInS(int val) {	currentEquipment.maxRunTimeInS = val; return me; }
		public EquipmentFactory halfliveInStorage(int val) {	currentEquipment.halfliveInStorage = val; return me; }
		public EquipmentFactory halfliveInField(int val) {	currentEquipment.halfliveInField = val; return me; }
		public EquipmentFactory halfLivePerCombat(int val) {	currentEquipment.halfLivePerCombat = val; return me; }
		public EquipmentFactory manHourCostPer1000(int val) {	currentEquipment.manHourCostPer1000 = val; return me; }
		public EquipmentFactory toolsNeededInManHour(int val) {	currentEquipment.toolsNeededInManHour = val; return me; }
		public EquipmentFactory educationNeeded(int val) {	currentEquipment.educationNeeded = val; return me; }
		public EquipmentFactory transportCapacity(int val) {	currentEquipment.transportCapacity = val; return me; }
		public EquipmentFactory nbMenToHandle(byte val) {	currentEquipment.nbMenToHandle = val; return me; }
		public EquipmentFactory trainingTimeInDayToUse(short val) {	currentEquipment.trainingTimeInDayToUse = val; return me; }
		public EquipmentTemplate get() { return currentEquipment; }
	}
	public static EquipmentTemplate get(String name){
		return EquipmentFactory.get(name);
	}
	
	
	
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
