package remi.ssp.algorithmes;

import remi.ssp.army.DivisionUnit;
import remi.ssp.politic.Carte;

public abstract class Battle {
	
	static public abstract class BattleFactory{
		public abstract Battle newBattle();

	}
	static public BattleFactory ptr;

	public abstract void init(DivisionUnit attacker, DivisionUnit defender);
	
	public abstract void doBattleRound();

	
}
