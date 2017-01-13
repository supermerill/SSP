package remi.ssp.algorithmes;

import remi.ssp.politic.Province;

public abstract class Stability {
	static public Stability ptr;

	public abstract void computeStabilityProvince(Province prv);
		//get the culture rejection sentiment
			//how many other people share our culture?
			// if 100% of empire, 0% culture rejection
			// if 0% of other provinces has our culture(s), 100% rejection
		//get the backwater unhapiness
			//check if we (the x% of the empire) are the poorer of the empire
			//if the mean reveu is in par or higher than the mean of the empire: 0% rejection
			//if we are <10% of the eman revenu of the empire, =>100% rejection
		//get the liberty unhapiness
			//if other people with same culture aer more free than us => rejection
		//get the military suppression
		
		//TODO check for recent military defeat/victory nearby
		//TODO colonial rejection (extension)
	
	public abstract void computeCorruption(Province prv);
		//compute the time to go from capital to the province
		// add 10% per day (multiplicative)
		// may be reduced by policies (extension)
	
	
	
	
}
