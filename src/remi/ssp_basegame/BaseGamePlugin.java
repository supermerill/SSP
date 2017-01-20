package remi.ssp_basegame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.auto.service.AutoService;

import remi.ssp.Plugin;
import remi.ssp.algorithmes.Battle;
import remi.ssp.algorithmes.Economy;
import remi.ssp.algorithmes.Spawner;
import remi.ssp.algorithmes.Stability;

@AutoService(Plugin.class)
public class BaseGamePlugin extends Plugin{
	private static final long serialVersionUID = 1L;
	
	public Collection<String> modsNeeded(){
		return Arrays.asList("BasicEconomyNeeds");
	}

	public Collection<String> modsIncompatible(){return Collections.emptyList();}

	//before anything, but after all plugin are loaded.
	public void init(){
		Economy.ptr = new BaseEconomy();
		Stability.ptr = new BaseStability();
		Battle.ptr = new BaseBattle.BaseBattleFactory();
		Spawner.ptr = new BaseSpawner();
	}

	//load data & algo into containers
	public void loadGoods(){} //1
	public void loadEquipment(){} //2
	public void loadIndustry(){} //3

}
