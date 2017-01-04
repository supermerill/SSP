package remi.ssp.army;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public abstract class BattalionBehavior implements Serializable{
	private static final long serialVersionUID = ((long)"BattalionBehaviorMelee".hashCode())<<30 + 1;
//	MELEE, //go to the enemy
//	RANGE, //advance to dist/2 then fire
//	FLANK,
//	SUPPORT


	public BattalionUnit nearestEnemy(BattalionUnit us, DivisionUnit enemy){
		int smallestDist = Integer.MAX_VALUE;
		BattalionUnit best = null;
		for(BattalionUnit eux : enemy.battalions){
			if(smallestDist > Math.abs(us.battlePosition - eux.battlePosition)){
				smallestDist = Math.abs(us.battlePosition - eux.battlePosition);
				best = eux;
			}
		}
		return best;
	}
	

	public int enemyCenter(DivisionUnit enemy){
		int center = 0;
		for(BattalionUnit eux : enemy.battalions){
			center = eux.battlePosition;
		}
		return center / enemy.battalions.size();
	}
	
//	//called to know how much do you want to move your position. If we move, we can't attack in the round.
//	public abstract int move(BattalionUnit us, DivisionUnit enemy, int deltaSeconds);
//
//	// when we have finished moving, we can shoot at a target.
//	public abstract BattalionUnit chooseTarget(BattalionUnit us, DivisionUnit enemy);
	
	public abstract void doTurn(BattalionUnit us, DivisionUnit allies, DivisionUnit enemy, int deltaSeconds);

	public void newBattle(BattalionUnit us, DivisionUnit ourArmy, DivisionUnit enemy){}
	public void endOfBattle(BattalionUnit us, DivisionUnit ourArmy, DivisionUnit enemy, float victorious){}


	public static class BattalionBehaviorMelee extends BattalionBehavior{
		private static final long serialVersionUID = ((long)"BattalionBehaviorMelee".hashCode())<<30 + 1;
		protected BattalionBehaviorMelee(){}

		static public BattalionBehaviorMelee MELEE_SWORD = new BattalionBehaviorMelee(1);
		static public BattalionBehaviorMelee MELEE_LANCE = new BattalionBehaviorMelee(2);
		
		protected byte meleeRange;
		
		protected BattalionBehaviorMelee(int meleeRange){
			this.meleeRange = (byte) (meleeRange%3);
		}

		@Override
		public void doTurn(BattalionUnit us, DivisionUnit allies, DivisionUnit enemy, int deltaSeconds) {
			us.currentBattleDecision.reset();
			//are we in melee?
			BattalionUnit eux = nearestEnemy(us, enemy);
			//lance or sword?
			final int distInM = Math.max(Math.abs(us.battlePosition - eux.battlePosition) - meleeRange, 0);
			if(distInM == 0){
				us.currentBattleDecision.wantToMove = false;
				us.currentBattleDecision.wantToShot = true;
				us.currentBattleDecision.wantedTarget = eux;
			}else{
				us.currentBattleDecision.wantToMove = true;
				//run? yes if we can at least run for the double of the distance
				if( ((us.template.getMaxRunSpeed() * us.template.getMaxRunTime())/3600 - us.nbMsRunInCurrentBattle) / 2 > distInM){
					us.currentBattleDecision.wantToRun = true;
				}

				if(meleeRange<2){
					us.currentBattleDecision.wantedPosition = eux.battlePosition;
				}else if(us.battlePosition < eux.battlePosition){
					us.currentBattleDecision.wantedPosition = eux.battlePosition - meleeRange;
				}else{
					us.currentBattleDecision.wantedPosition = eux.battlePosition + meleeRange;
				}
			}
		}
		
	}
	

	public static class BattalionBehaviorRange extends BattalionBehavior{
		private static final long serialVersionUID = ((long)"BattalionBehaviorRange".hashCode())<<30 + 1;
		protected BattalionBehaviorRange(){}
		
		@Override
		public void doTurn(BattalionUnit us, DivisionUnit allies, DivisionUnit enemy, int deltaSeconds) {
			us.currentBattleDecision.reset();
			//are we in range?
			BattalionUnit nearestEux = nearestEnemy(us, enemy);
			//are we in melee?
			if(Math.max(Math.abs(us.battlePosition - nearestEux.battlePosition) - 2, 0) == 0){
				//yes! withdrawl!
				us.currentBattleDecision.wantToMove = true;
				us.currentBattleDecision.wantToRun = true;
				
				int speed = us.template.getMaxRunSpeed();
				if(enemyCenter(enemy) > us.battlePosition){
					us.currentBattleDecision.wantedPosition = us.battlePosition - (speed * deltaSeconds) / 3600;
				}else{
					us.currentBattleDecision.wantedPosition = us.battlePosition + (speed * deltaSeconds) / 3600;
				}
			}
			final int distInM = Math.max(Math.abs(us.battlePosition - nearestEux.battlePosition) - us.template.getMaxRange(), 0);
			if(distInM < us.template.getMaxRange()/2){
				//shoot
				us.currentBattleDecision.wantToMove = false;
			}else{
				us.currentBattleDecision.wantToMove = true;
				//advance
				int speed = us.template.getMaxWalkSpeed();
				if(enemyCenter(enemy) > us.battlePosition){
					us.currentBattleDecision.wantedPosition = us.battlePosition - (speed * deltaSeconds) / 3600;
				}else{
					us.currentBattleDecision.wantedPosition = us.battlePosition + (speed * deltaSeconds) / 3600;
				}
			}
			
			if(! us.currentBattleDecision.wantToMove){
				List<BattalionUnit> enemyToShoot = new LinkedList<>();
				for(BattalionUnit eux : enemy.battalions){
					if(us.template.getMaxRange() < Math.abs(us.battlePosition - eux.battlePosition)){
						ListIterator<BattalionUnit> it = enemyToShoot.listIterator();
						BattalionUnit previousUnit = null;
						while(it.hasNext()){
							previousUnit = it.next();
							if(Math.abs(us.battlePosition - previousUnit.battlePosition) > Math.abs(us.battlePosition - eux.battlePosition)){
								break;
							}
						}
						if(previousUnit != null && Math.abs(us.battlePosition - previousUnit.battlePosition) > Math.abs(us.battlePosition - eux.battlePosition)){
							it.previous();
						}
						it.add(eux);
					}
				}
				
				//check for each enemy batalion if it's not too near to our own
				for(BattalionUnit eux : enemyToShoot){
					boolean ok = true;
					for(BattalionUnit ally : allies.battalions){
						if(Math.abs(eux.battlePosition - ally.battlePosition) < 3){
							ok = false;
							break;
						}
					}
					if(ok){
						//shoot!
						us.currentBattleDecision.wantToShot = true;
						us.currentBattleDecision.wantedTarget = eux;
					}
				}
				

				// if false == us.currentBattleDecision.wantToShot then do nothing.
				//TODO: if nothing to do, move to a better place?
				
			}
		}
		
	}
	
}
