package remi.ssp;
import static remi.ssp.GlobalDefines.logln;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import remi.ssp.politic.Carte;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Culture;

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
        	logln(", \"Load plugin "+plugin.getClass().getName()+"\":true");
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
							logln(", \"find jar: "+file.getAbsolutePath()+"\":true");
							jars.add(file);
						}
					}
				}
			}
		}
		return jars;
	}
	
	
	public void loadStaticData(List<String> orderedNames){
		
		
		forEachPlugin(orderedNames, plugin->logln(", \"Activate plugin "+plugin.getClass().getName()+"\":true"));
		
		//1 goods
		forEachPlugin(orderedNames, plugin->plugin.loadGoods());
		//2 equipment
		forEachPlugin(orderedNames, plugin->plugin.loadEquipment());
		//3 industry, needs
		forEachPlugin(orderedNames, plugin->plugin.loadIndustry());
		//3 ideas
		forEachPlugin(orderedNames, plugin->plugin.loadIdeas());
		//4 research
		forEachPlugin(orderedNames, plugin->plugin.loadTechnologies());
		
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
		
			JsonObject root = rdr.readObject();
			
			JsonArray arrayCult = root.getJsonArray("cults");
			for(int i=0;i<arrayCult.size();i++){
				Culture cult = new Culture();
				cult.load(arrayCult.getJsonObject(i));
				CurrentGame.get().cultures.put(cult.getName(), cult);
			}
			
			//cultures
			CurrentGame.get().cultures = new HashMap<>();
			
			
			//Carte -> province -> pop -> popneed, stock, prvindus
			// province-province links & plotplot links
			CurrentGame.get().map = new Carte();
			CurrentGame.get().map.load(root.getJsonObject("map"));
			
			// civilization 
			//				-> equipment developed
			// 				-> division -> battalion
			//				-> division unit -> battalion unit
			CurrentGame.get().civs = new ArrayList<>();
			JsonArray arrayCiv = root.getJsonArray("civs");
			for(int i=0;i<arrayCiv.size();i++){
				Civilisation civ = new Civilisation();
				civ.load(arrayCiv.getJsonObject(i), CurrentGame.get().map);
				CurrentGame.get().civs.add(civ);
			}

			// province trade routes ?
			
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void savedData(URL url){
		try (OutputStream out = url.openConnection().getOutputStream(); JsonWriter wtr = Json.createWriter(out)) {
		
			JsonObjectBuilder root = Json.createObjectBuilder();

			JsonArrayBuilder arrayCults = Json.createArrayBuilder();
			for(int i=0;i<CurrentGame.get().cultures.size();i++){
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				CurrentGame.get().cultures.get(i).save(objectBuilder);
				arrayCults.add(objectBuilder);
			}
			root.add("cults", arrayCults);
			
			//Carte -> province -> pop -> popneed, stock, prvindus
			JsonObjectBuilder object = Json.createObjectBuilder();
			CurrentGame.get().map.save(object);
			root.add("map", object);
			
			// civilization 
			JsonArrayBuilder arrayCivs = Json.createArrayBuilder();
			for(int i=0;i<CurrentGame.get().civs.size();i++){
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				CurrentGame.get().civs.get(i).save(objectBuilder);
				arrayCivs.add(objectBuilder);
			}
			root.add("civs", arrayCivs);
			
			//				-> equipment developed
			// 				-> division -> battalion
			//				-> division unit -> battalion unit
			// province trade routes & province-province links
			//
			wtr.writeObject(root.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
