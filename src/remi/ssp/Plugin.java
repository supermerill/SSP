package remi.ssp;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Plugin implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private static ArrayList<Plugin> loadedPlugins = new ArrayList<>();

	protected static void load(Plugin plugin){ loadedPlugins.add(plugin); }
	protected static void isLoaded(Plugin plugin){ loadedPlugins.contains(plugin); }
	

	//before anything, but after all plugin are loaded.
	public void init(){}

	//load data & algo into containers
	public void loadGoods(){} //1
	public void loadEquipment(){} //2
	public void loadIndustry(){} //3
	
	
	
	
}
