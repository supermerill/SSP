package remi.ssp.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class U {
	public static final <T> void add(Object2IntMap<T> map, T key, int addVal){
		int  val = map.get(key);
		map.put(key, val + addVal);
	}
}
