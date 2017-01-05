package remi.ssp.utils;

import java.util.Comparator;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public class ComparatorValueAsc<T> implements Comparator<T>{

	private Object2IntMap<T> map;

	public ComparatorValueAsc(Object2IntMap<T> map){
		this.map = map;
	}
	
	@Override
	public int compare(T o1, T o2) {
		return Integer.compare(map.getInt(o1), map.getInt(o2));
	}

}
