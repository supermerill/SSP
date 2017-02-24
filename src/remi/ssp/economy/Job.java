package remi.ssp.economy;

import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp.utils.LongInterval;

public interface Job {

	public double getPreviousSalary();
//	public int getNbWorkers(); //stored in pop
//	public void addWorkers(int nb);

	public String getName(); //industry name, for serialization purpose

	public void addMoney(long investment);

	public LongInterval needFire(LongInterval toReturn, Province prv, Pop pop, int nbDays);
	public LongInterval needHire(LongInterval toReturn, Province prv, Pop pop, int nbDays);
}
