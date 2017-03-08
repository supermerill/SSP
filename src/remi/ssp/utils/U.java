package remi.ssp.utils;

import java.util.List;

import javax.json.JsonObjectBuilder;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

public class U {
	public static final <T> void add(Object2LongMap<T> map, T key, long addVal){
		long  val = map.getLong(key);
		map.put(key, val + addVal);
	}
	
	public static final <T> boolean containsAddr(T[] array, T value){
		for(int i=array.length-1;i>=0;i--){
			if(array[i] == value) return true;
		}
		return false;
	}
	public static final <T> T getMin(Object2IntMap<T> coll){
		int min = Integer.MAX_VALUE;
		T best = null;
		for(Object2IntMap.Entry<T> e : coll.object2IntEntrySet()){
			if(e.getIntValue() < min){
				min = e.getIntValue();
				best = e.getKey();
			}
		}
		return best;
	}

	public static final <T> T last(List<T> target) {
		return target.get(target.size()-1);
	}
	

	public static final void addStrOrNull(JsonObjectBuilder json, String name, String val) {
		if(val == null) json.addNull(name);
		else json.add(name, val);
	}
	
}
