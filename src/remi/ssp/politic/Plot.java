package remi.ssp.politic;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Plot {
	short x,y;
	//can be 5 if making a globe... be careful!
	public Plot[] around = new Plot[6]; // 0=  up, then direct direction
	Province province;
	byte positionInProvince; // 0= center, 1=  up, then direct direction
	
	public boolean isSea = false;

	public short getX() { return x; }
	public short getY() { return y; }
	public Plot[] getAround() { return around; }
	public Province getProvince() { return province; }
	public byte getPositionInProvince() { return positionInProvince; }
	public boolean isSea() { return isSea; }
	
	

	public void load(JsonObject jsonObject) {
		x = (short) jsonObject.getInt("x");
		y = (short) jsonObject.getInt("y");
		positionInProvince = (byte) jsonObject.getInt("pos");
		isSea = jsonObject.getBoolean("sea");
	}
	public void save(JsonObjectBuilder objectBuilder) {
		objectBuilder.add("x", x);
		objectBuilder.add("y", y);
		objectBuilder.add("pos", positionInProvince);
		objectBuilder.add("sea", isSea);
	}
	public void loadLinks(JsonObject jsonObject, Carte map) {
		JsonArray arrayPlot = jsonObject.getJsonArray("plots");
		if(around.length != arrayPlot.size()/2) around = new Plot[arrayPlot.size()/2];
		for(int i=0;i<arrayPlot.size();i+=2){
			around[i/2] = map.plots.get(arrayPlot.getInt(i)).get(arrayPlot.getInt(i+1));
		}
	}
	public void saveLinks(JsonObjectBuilder jsonOut) {
		JsonArrayBuilder arrayPlots = Json.createArrayBuilder();
		for(int i=0;i<around.length;i++){
			arrayPlots.add(around[i].x);
			arrayPlots.add(around[i].y);
		}
		jsonOut.add("plots", arrayPlots);
	}
	
}
