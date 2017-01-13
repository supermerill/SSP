package remi.ssp.politic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class Carte {

	// ceci est une implémentation, gaffe!
	//il faudrait une Province -> x,y,image pour etre plus générique, pt-etre stoké dans Province?
	public List<List<Province>> provinces = new ArrayList<>();
	public List<List<Plot>> plots = new ArrayList<>();
	public int nbLigne = 0, nbColonne = 0;
	Random rand = new Random();
	
	public void load(JsonObject readObject) {
		plots.clear();
		JsonArray arrayPlot = readObject.getJsonArray("plots");
		for(int i=0;i<arrayPlot.size();i++){
			Plot plot = new Plot();
			plot.load(arrayPlot.getJsonObject(i));
			while(plots.size()<=plot.x){
				plots.add(new ArrayList<>());
			}
			List<Plot> listeX = plots.get(plot.x);
			while(listeX.size()<=plot.y){
				listeX.add(null);
			}
			listeX.set(plot.y, plot);
		}
		arrayPlot = readObject.getJsonArray("plots_links");
		for(int i=0;i<arrayPlot.size();i++){
			JsonObject object = arrayPlot.getJsonObject(i);
			plots.get(object.getInt("x")).get(object.getInt("y")).loadLinks(object, this);
		}
		provinces.clear();
		JsonArray arrayPrv = readObject.getJsonArray("prvs");
		for(int i=0;i<arrayPrv.size();i++){
			Province prv = new Province();
			prv.load(arrayPrv.getJsonObject(i));
			while(provinces.size()<=prv.x){
				provinces.add(new ArrayList<>());
			}
			List<Province> listeX = provinces.get(prv.x);
			while(listeX.size()<=prv.y){
				listeX.add(null);
			}
			listeX.set(prv.y, prv);
		}
		arrayPrv = readObject.getJsonArray("prvs_links");
		for(int i=0;i<arrayPrv.size();i++){
			JsonObject object = arrayPlot.getJsonObject(i);
			provinces.get(object.getInt("x")).get(object.getInt("y")).loadLinks(object, this);
		}
	}

}
