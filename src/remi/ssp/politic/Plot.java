package remi.ssp.politic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.network.SimpleSerializable;
import remi.ssp.utils.U;

public class Plot implements SimpleSerializable {
	short x;
	short y;
	//can be 5 if making a globe... be careful!
	public Plot[] around = new Plot[6]; // 0= center, 1=up(or upleft), then direct direction (6=upperright)
	Province province;
	byte positionInProvince; // 0= center, 1=  up, then direct direction
	
	
	///TODO: replace by "terrainview": rien(limite), sea, ocean, coast, island, plain, forest, hills, mountains
	// sea, ocean, island and coast is navigable.
	// coast, island, plain, forest, hills, mountains is walkable
	public boolean isSea = false;
	
	
	
	
	public Plot(short x, short y, Province province, byte positionInProvince) {
		super();
		this.x = x;
		this.y = y;
		this.province = province;
		this.positionInProvince = positionInProvince;
	}
	public Plot(JsonObject jsonObject) {
		load(jsonObject);
	}
	
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
	
	
	public List<Plot> nearest(Plot farAway){
		Object2IntMap<Plot> dist = new Object2IntOpenHashMap<>();
		dist.defaultReturnValue(Integer.MAX_VALUE);
		Map<Plot, Plot> previous = new HashMap<>();
		dist.put(this, 0);
		while(dist.size()>0){
			Plot best = U.getMin(dist);
			int pos = dist.getInt(best);
			for(Plot p : best.around){
				if(dist.getInt(p) > pos+1){
					previous.put(p, best);
					dist.put(p, pos+1);
				}
				if(p == farAway) break;
			}
		}

		//create list
		LinkedList<Plot> retList = new LinkedList<>();
		retList.addLast(farAway);
		while(previous.containsKey(retList.getFirst())){
			retList.addFirst(previous.get(retList.getFirst()));
		}
		return retList;
	}
	
}
