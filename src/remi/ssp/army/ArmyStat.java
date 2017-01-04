//package remi.ssp.army;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
///*y = y2-((y2-y1)/(1+(x/a)^b))
// b= pente
// 1x= 0.5y
// 
// * How it work:
// * at each shot, the attacker have force/def chance / 5 of hitting, and /20 of killing.
// * if force/def < 1, then the chances are reduce by 2 more.
// */
//public class ArmyStat {
//	
//	ArrayList<Integer> force = new ArrayList<>(Collections.nCopies(DamageType.values.size(), 0));
//	ArrayList<Integer> armor = new ArrayList<>(Collections.nCopies(DamageType.values.size(), 0));
//	
//	int rateOfFirePerMs;
//	int rangeInMeter; // 1 or 2 for melee. For range, realForce = force * 1- (distance/rangeInMeter)
//	//chance of hitting = precisionForMDist/distanceM;
//	int precisionForMDist; // radius in M of precision for a km shoot, -1 for melee
//	
//	
//	int speedInMH; // max speed
//
//	//int explosiveForce; //create many crushing & piercing pellets //TODO: later
//	int getRealForce(int distanceInMeter, int force){ return force * (1- (distanceInMeter/rangeInMeter)); }
//
//	
//	//todo: other stat like influence du stress sur l'efficacité
//	
//
//	public void reset() {
//		Collections.fill(force, 0);
//		Collections.fill(armor, 0);
//		rateOfFirePerMs = 0;
//		rangeInMeter = 0;
//		precisionInMForKMDist = 0;
//		speedInMH = 0;
//	}
//
//
////	public void setMax(ArmyStat combatMax, float coeff) {
////		for(int i=0;i<force.size();i++){
////			force.set(i, Math.max(force.get(i), (int) (combatMax.force.get(i) * coeff)));
////		}
////		for(int i=0;i<armor.size();i++){
////			armor.set(i, Math.max(armor.get(i), (int) (combatMax.armor.get(i) * coeff)));
////		}
////		rateOfFirePerMs = Math.max(rateOfFirePerMs, (int) (combatMax.rateOfFirePerMs * coeff));
////		rangeInMeter = Math.max(rangeInMeter, (int) (combatMax.rangeInMeter * coeff));
////		precisionInMForKMDist = Math.max(precisionInMForKMDist, (int) (combatMax.precisionInMForKMDist * coeff));
////		speedInMH = Math.min(speedInMH, (int) (combatMax.speedInMH * coeff));
////	}
//
//	public void setMaxArmor(ArmyStat combatMax, float coeff) {
//		for(int i=0;i<armor.size();i++){
//			armor.set(i, Math.max(armor.get(i), (int) (combatMax.armor.get(i) * coeff)));
//		}
//	}
//
////	public void add(ArmyStat combatAdd, float coeff) {
////		for(int i=0;i<force.size();i++){
////			force.set(i, force.get(i) + (int) (combatAdd.force.get(i) * coeff));
////		}
////		for(int i=0;i<armor.size();i++){
////			armor.set(i, armor.get(i) + (int) (combatAdd.armor.get(i) * coeff));
////		}
////		rateOfFirePerMs += (int) (combatAdd.rateOfFirePerMs * coeff);
////		rangeInMeter += (int) (combatAdd.rangeInMeter * coeff);
////		precisionInMForKMDist += (int) (combatAdd.precisionInMForKMDist * coeff);
////		speedInMH += (int) (combatAdd.speedInMH * coeff);
////	}
//}
