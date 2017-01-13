package remi.ssp.economy;

import remi.ssp.politic.Pop;

public abstract class PopNeed extends Needs {

	protected Pop myPop;
	
	public PopNeed(Pop pop){
		myPop = pop;
	}

}
