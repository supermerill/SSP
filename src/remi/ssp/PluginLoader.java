package remi.ssp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import remi.ssp.politic.Carte;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Province;

public class PluginLoader{

	protected List<Plugin> availablePlugin = new ArrayList<>();
	protected List<Plugin> loadedPlugin = new ArrayList<>();
	


	public List<String> getPluginNames() {
		return availablePlugin.stream().map(plugin -> plugin.getClass().getName()).collect(Collectors.toList());
	}
	
	public void loadJars(String folderPath){
		File folder = new File(folderPath);
		Collection<File> jars = getJars(folder);
		
		List<URL> urls = new ArrayList<>(jars.size());
		Iterator<File> itJar = jars.iterator();
        for (int i = 0; i < jars.size() && itJar.hasNext(); i++){
    		try{
            urls.add(itJar.next().toURI().toURL());
    		}catch(Exception e){
    			System.err.println(e);
    		}
        }
        URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[urls.size()]));

        
        //load jars
        ServiceLoader<Plugin> sl = ServiceLoader.load(Plugin.class, ucl);
        Iterator<Plugin> apit = sl.iterator();
        while (apit.hasNext()){
        	Plugin plugin = apit.next();
        	System.out.println("Load plugin "+plugin.getClass().getName());
        	availablePlugin.add(plugin);
        }
	}
	
	protected Collection<File> getJars(File folder){
		ArrayList<File> jars = new ArrayList<>();
		if(folder.exists() && folder.isDirectory())
		{
			for(File file : folder.listFiles()){
				if(file.isDirectory()){
					jars.addAll(getJars(file));
				}else{
					if(file.getName().contains(".")){
						String[] splitName = file.getName().split("\\.");
						if(splitName[splitName.length-1].equalsIgnoreCase("jar")){
							System.out.println("find jar: "+file.getAbsolutePath());
							jars.add(file);
						}
					}
				}
			}
		}
		return jars;
	}
	
	
	public void loadStaticData(List<String> orderedNames){
		
		
		forEachPlugin(orderedNames, plugin->System.out.println("Activate plugin "+plugin.getClass().getName()));
		
		//1 goods
		forEachPlugin(orderedNames, plugin->plugin.loadGoods());
		//2 equipment
		forEachPlugin(orderedNames, plugin->plugin.loadEquipment());
		//3 industry, needs
		forEachPlugin(orderedNames, plugin->plugin.loadIndustry());
		//3 ideas
		//4 research
		
		//after
		forEachPlugin(orderedNames, plugin->plugin.init());
	}
	
	protected void forEachPlugin(List<String> orderedNames, Consumer<Plugin> procedure)
	{
		for(String name : orderedNames){
			for(Plugin plugin : availablePlugin){
				if(plugin.getClass().getName().equalsIgnoreCase(name)){
					procedure.accept(plugin);
					break;
				}
			}
		}
	}
	
	public void loadSavedData(URL url){
		try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
		
			//Carte -> province -> pop -> popneed, stock, prvindus
			Carte carte = new Carte();
			carte.load(rdr.readObject());
			
			// civilization 
			JsonArray arrayCiv = rdr.readArray();
			for(int i=0;i<arrayCiv.size();i++){
				Civilisation civ = new Civilisation();
				civ.load(arrayCiv.getJsonObject(i));
			}
			
			//				-> equipment developed
			// 				-> division -> battalion
			//				-> division unit -> battalion unit
			// province trade routes & province-province links
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
