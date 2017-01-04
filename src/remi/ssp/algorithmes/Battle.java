package remi.ssp.algorithmes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import remi.ssp.army.BattalionUnit;
import remi.ssp.army.DivisionUnit;

public abstract class Battle {

	protected DivisionUnit attacker;
	protected DivisionUnit defender;
	protected int attWounded;
	protected int defWounded;
	protected int attKilled;
	protected int defKilled;
	
	protected int roundDurationSeconds;
	
	public void init(DivisionUnit attacker, DivisionUnit defender){
		this.attacker = attacker;
		this.defender = defender;
		attWounded = 0;
		defWounded= 0;
		attKilled = 0;
		defKilled = 0;
		roundDurationSeconds = 20;
	}
	
	public void doBattleRound()
	{
		//each round is for ~20 sec
		//1 choose to move or attack
		for(BattalionUnit bat : attacker.getBattalions()){
			bat.getTemplate().getBehavior().doTurn(bat, attacker, defender, roundDurationSeconds);
		}
		for(BattalionUnit bat : defender.getBattalions()){
			bat.getTemplate().getBehavior().doTurn(bat, defender, attacker, roundDurationSeconds);
		}
		//2 attacks TODO: target other nearby bat (friendly fire) for a part of the time
		Map<BattalionUnit, Integer> attBats2wounded = new HashMap<>();
		for(BattalionUnit bat : attacker.getBattalions()){
			if(bat.getCurrentBattleDecision().wantToShot && !bat.getCurrentBattleDecision().wantToMove){
				attBats2wounded.put(bat.getCurrentBattleDecision().wantedTarget, bat.attack(bat.getCurrentBattleDecision().wantedTarget, roundDurationSeconds));
			}
		}
		Map<BattalionUnit, Integer> defBats2wounded = new HashMap<>();
		for(BattalionUnit bat : defender.getBattalions()){
			if(bat.getCurrentBattleDecision().wantToShot && !bat.getCurrentBattleDecision().wantToMove){
				defBats2wounded.put(bat.getCurrentBattleDecision().wantedTarget, bat.attack(bat.getCurrentBattleDecision().wantedTarget, roundDurationSeconds));
			}
		}
		//apply wounded, dead, morale
		int moraleAttacker = attacker.getMorale();
		int moraleDefender = attacker.getMorale();
		for(Entry<BattalionUnit, Integer> bat2Wounded : attBats2wounded.entrySet()){
			BattalionUnit bat = bat2Wounded.getKey();
			//compute
			int nbWounded = Math.min( bat.getWoundedMens() + bat2Wounded.getValue(), bat.getAvailableMens());
			int nbKilled = Math.min(bat2Wounded.getValue()/4, bat.getAvailableMens());
			int woundedKilled = (int)(nbWounded * nbKilled) / bat.getAvailableMens();
			int validKilled = nbKilled - woundedKilled;
			woundedKilled *= 1.5; //wounded are more susceptible to death
			nbKilled = validKilled + woundedKilled;
			//set
			bat.setWoundedMens(nbWounded - woundedKilled);
			this.attWounded += nbWounded - woundedKilled;
			bat.setAvailableMens(bat.getAvailableMens() - nbKilled);
			this.attKilled += nbKilled;
			
			//morale: increase if we do as good as them. effondrement si l'on meurt davantage.
			moraleAttacker -= nbKilled*10 + bat2Wounded.getValue()*2;
			moraleDefender += nbKilled*4 + bat2Wounded.getValue()*4;
		}
		
		for(Entry<BattalionUnit, Integer> bat2Wounded : defBats2wounded.entrySet()){
			BattalionUnit bat = bat2Wounded.getKey();
			//compute
			int nbWounded = Math.min( bat.getWoundedMens() + bat2Wounded.getValue(), bat.getAvailableMens());
			int nbKilled = Math.min(bat2Wounded.getValue()/4, bat.getAvailableMens());
			int woundedKilled = (int)(nbWounded * nbKilled) / bat.getAvailableMens();
			int validKilled = nbKilled - woundedKilled;
			woundedKilled *= 1.5; //wounded are more susceptible to death
			nbKilled = validKilled + woundedKilled;
			//set
			bat.setWoundedMens(nbWounded - woundedKilled);
			this.defWounded += nbWounded - woundedKilled;
			bat.setAvailableMens(bat.getAvailableMens() - nbKilled);
			this.defKilled += nbKilled;
			
			//morale: increase if we do as good as them. effondrement si l'on meurt davantage.
			moraleDefender -= nbKilled*10 + bat2Wounded.getValue()*2;
			moraleAttacker += nbKilled*4 + bat2Wounded.getValue()*4;
		}

		moraleAttacker = Math.max(moraleAttacker, 0);
		moraleDefender = Math.max(moraleDefender, 0);
		attacker.setMorale(attacker.getMorale() + ((moraleAttacker - attacker.getMorale())/2));
		defender.setMorale(defender.getMorale() + ((moraleDefender - defender.getMorale())/2));
		
		//TODO: add desertion if morale is low
		
		//3 walks
		//TODO: move slower if morale is low and going toward an enemy
		for(BattalionUnit bat : attacker.getBattalions()){
			if(bat.getCurrentBattleDecision().wantToMove){
				//getSpeed
				int speed = bat.getTemplate().getMaxWalkSpeed();
				//run? yes if we can at least run for the duration of the round
				if(bat.getCurrentBattleDecision().wantToRun){
					speed = bat.getTemplate().getMaxRunSpeed();
				}
				
				//check if an enemy is on the way
				boolean isOnTheWay = false;
				for(BattalionUnit batDef : defender.getBattalions()){
					if(Math.abs(bat.getPosition() - batDef.getPosition()) < 2){
						isOnTheWay = true;
						break;
					}
				}
				
				if(isOnTheWay){
					//move by 2 meter only per minute (120 per hour)
					speed = 120;
				}
				
				if(bat.getPosition() < bat.getCurrentBattleDecision().wantedPosition){
					bat.setPosition( bat.getPosition() + (speed * roundDurationSeconds) / 3600);
				}else{
					bat.setPosition( bat.getPosition() - (speed * roundDurationSeconds) / 3600);
				}
				
			}
		}

		for(BattalionUnit bat : defender.getBattalions()){
			if(bat.getCurrentBattleDecision().wantToMove){
				//getSpeed
				int speed = bat.getTemplate().getMaxWalkSpeed();
				//run? yes if we can at least run for the duration of the round
				if(bat.getCurrentBattleDecision().wantToRun){
					speed = bat.getTemplate().getMaxRunSpeed();
				}
				
				//check if an enemy is on the way
				boolean isOnTheWay = false;
				for(BattalionUnit batDef : attacker.getBattalions()){
					if(Math.abs(bat.getPosition() - batDef.getPosition()) < 2){
						isOnTheWay = true;
						break;
					}
				}
				
				if(isOnTheWay){
					//move by 2 meter only per minute (120 per hour)
					speed = 120;
				}
				
				if(bat.getPosition() < bat.getCurrentBattleDecision().wantedPosition){
					bat.setPosition( bat.getPosition() + (speed * roundDurationSeconds) / 3600);
				}else{
					bat.setPosition( bat.getPosition() - (speed * roundDurationSeconds) / 3600);
				}
				
			}
		}
	}
	
	
}
