package remi.ssp.economy;

import remi.ssp.politic.Province;

public interface Job {

	public double getPreviousSalary();
//	public int getNbWorkers(); //stored in pop
//	public void addWorkers(int nb);

	public String getName(); //industry name, for serialization purpose

	public void addMoney(long investment);

	public float wantToFire(Province prv, long nbEmployed, int nbDays);
}
