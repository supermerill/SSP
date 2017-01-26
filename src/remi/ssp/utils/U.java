package remi.ssp.utils;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public class U {
	public static final <T> void add(Object2LongMap<T> map, T key, long addVal){
		long  val = map.getLong(key);
		map.put(key, val + addVal);
	}
}
