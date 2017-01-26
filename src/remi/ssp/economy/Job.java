package remi.ssp.economy;

public interface Job {

	public long getPreviousSalary();
//	public int getNbWorkers(); //stored in pop
//	public void addWorkers(int nb);

	public String getName(); //industry name, for serialization purpose

	public void addMoney(long investment);
}
