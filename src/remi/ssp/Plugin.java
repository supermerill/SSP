package remi.ssp;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * The plugin name is his class.getName();
 * 
 * 
 * @author Merill
 *
 */
public abstract class Plugin implements Serializable{
	private static final long serialVersionUID = 1L;
	
//	private static ArrayList<Plugin> loadedPlugins = new ArrayList<>();
//
//	protected static void load(Plugin plugin){ loadedPlugins.add(plugin); }
//	protected static void isLoaded(Plugin plugin){ loadedPlugins.contains(plugin); }
	

	public Collection<String> modsNeeded(){return Collections.emptyList();}
	public Collection<String> modsIncompatible(){return Collections.emptyList();}
	
	//load data & algo into containers
	public void loadGoods(){} //1
	public void loadEquipment(){} //2
	public void loadIndustry(){} //3
	


	//before anything, but after all plugin are loaded.
	public void init(){}

	
	
}
