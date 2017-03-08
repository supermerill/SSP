package remi.ssp.politic;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.network.SimpleSerializable;

public class Carte implements SimpleSerializable {

	// ceci est une implémentation, gaffe!
	//il faudrait une Province -> x,y,image pour etre plus générique, pt-etre stoké dans Province?
	protected List<List<Province>> provinces = new ArrayList<>();
	protected List<Province> allprovinces = new ArrayList<>();
	protected List<List<Plot>> plots = new ArrayList<>();
	protected int nbLigne = 0, nbColonne = 0;
	protected int nbPlotLigne = 0, nbPlotColonne = 0;
	


	public List<Province> getAllProvinces() {
		return allprovinces;
	}
	
	public int getNbLigne() {
		return nbLigne;
	}

	public void setNbLigne(int nbLigne) {
		this.nbLigne = nbLigne;
	}

	public int getNbColonne() {
		return nbColonne;
	}

	public void setNbColonne(int nbColonne) {
		this.nbColonne = nbColonne;
	}

	public int getNbPlotLigne() {
		return nbPlotLigne;
	}

	public void setNbPlotLigne(int nbPlotLigne) {
		this.nbPlotLigne = nbPlotLigne;
	}

	public int getNbPlotColonne() {
		return nbPlotColonne;
	}

	public void setNbPlotColonne(int nbPlotColonne) {
		this.nbPlotColonne = nbPlotColonne;
	}

	public List<List<Province>> getProvinces() {
		return provinces;
	}
	public Province getProvince(int i, int j) {
		return provinces.get(i).get(j);
	}
	public Province setProvince(int i, int j) {
		return provinces.get(i).get(j);
	}

	public List<List<Plot>> getPlots() {
		return plots;
	}

	public void load(JsonObject readObject) {
		plots.clear();
		JsonArray arrayPlot = readObject.getJsonArray("plots");
		for(int i=0;i<arrayPlot.size();i++){
			Plot plot = new Plot(arrayPlot.getJsonObject(i));
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
			allprovinces.add(prv);
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

	public void save(JsonObjectBuilder jsonOut) {
		JsonArrayBuilder arrayPlots = Json.createArrayBuilder();
		for(int i=0;i<plots.size();i++){
			for(int j=0;j<plots.get(i).size();j++){
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				plots.get(i).get(j).save(objectBuilder);
				arrayPlots.add(objectBuilder);
			}
		}
		jsonOut.add("plots", arrayPlots);

		arrayPlots = Json.createArrayBuilder();
		for(int i=0;i<plots.size();i++){
			for(int j=0;j<plots.get(i).size();j++){
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				plots.get(i).get(j).saveLinks(objectBuilder);
				arrayPlots.add(objectBuilder);
			}
		}
		jsonOut.add("plots_links", arrayPlots);
		
		JsonArrayBuilder arrayPrvs = Json.createArrayBuilder();
		for(int i=0;i<provinces.size();i++){
			for(int j=0;j<provinces.get(i).size();j++){
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				provinces.get(i).get(j).save(objectBuilder);
				arrayPrvs.add(objectBuilder);
			}
		}
		jsonOut.add("prvs", arrayPrvs);

		arrayPrvs = Json.createArrayBuilder();
		for(int i=0;i<provinces.size();i++){
			for(int j=0;j<provinces.get(i).size();j++){
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				provinces.get(i).get(j).saveLinks(objectBuilder);
				arrayPrvs.add(objectBuilder);
			}
		}
		jsonOut.add("prvs_links", arrayPrvs);
	}

}
