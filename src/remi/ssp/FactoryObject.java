package remi.ssp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class FactoryObject {

	protected final long id;
	
	private static Map<Class<? extends FactoryObject>, Long2ObjectMap<Object>> storage = new HashMap<>();
	static protected void initFacotry(Class<? extends FactoryObject> myClass){
		if(!storage.containsKey(myClass)){
			storage.put(myClass, new Long2ObjectOpenHashMap<>());
		}
	}
	static private AtomicLong idFactory = new AtomicLong();
	
	@SuppressWarnings("unchecked")
	static public <T extends FactoryObject> T get(Class<T> realClass, long id){
		return (T)storage.get(realClass).get(id);
	}
	
	protected FactoryObject(){
		id = idFactory.get();
	}
	
	protected FactoryObject(final long id){
		this.id = id;
	}
	
}
