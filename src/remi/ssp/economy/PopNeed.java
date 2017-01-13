package remi.ssp.economy;

import java.util.HashMap;

import remi.ssp.politic.Pop;

public abstract class PopNeed extends Needs {


	public static class NeedStorage{
		public static HashMap<String, PopNeed> goodList = new HashMap<>();
		public static PopNeed get(String name) { return goodList.get(name); }
		public static void put(String name, PopNeed obj) { goodList.put(name, obj); }
	}
	public static PopNeed get(String name){
		return NeedStorage.get(name);
	}
	
	protected Pop myPop;
	
	public PopNeed(Pop pop){
		myPop = pop;
	}

}
