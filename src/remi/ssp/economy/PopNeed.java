package remi.ssp.economy;

import remi.ssp.Pop;

public abstract class PopNeed extends Needs {

	protected Pop myPop;
	
	public PopNeed(Pop pop){
		myPop = pop;
	}

}
