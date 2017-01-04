package remi.ssp.army;

public class BattalionBattleDecision {

	public boolean wantToMove;
	public int wantedPosition;
	public boolean wantToRun;
	
	public boolean wantToShot;
	public BattalionUnit wantedTarget;
	
	
	public void reset() {
		wantToMove = false;
		wantedPosition = 0;
		wantToRun = false;
		wantToShot = false;
		wantedTarget = null;
	}
	
	
}
