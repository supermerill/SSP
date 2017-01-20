package remi.ssp.economy;

import java.util.HashMap;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import remi.ssp.politic.Pop;

public abstract class PopNeed extends Needs {

	public static abstract class PopNeedFactory implements Function<Pop,PopNeed>{
		public PopNeed create(Pop pop){
			return apply(pop);
		}
	}

	public static class PopNeedFactoryStorage{
		public static HashMap<String, Function<Pop,PopNeed>> popNeedList = new HashMap<>();
		public static Function<Pop,PopNeed> get(String name) { return popNeedList.get(name); }
		public static void put(String name, Function<Pop,PopNeed> obj) { popNeedList.put(name, obj); }
	}
	public static PopNeed create(String name, Pop pop){
		return PopNeedFactoryStorage.get(name).apply(pop);
	}
	
	protected Pop myPop;
	
	public PopNeed(Pop pop){
		myPop = pop;
	}

	public void load(JsonObject jsonObject) {}

	public void save(JsonObjectBuilder objectBuilder) {}

}
