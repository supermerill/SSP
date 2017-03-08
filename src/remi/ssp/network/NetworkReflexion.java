package remi.ssp.network;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import remi.ssp.CurrentGame;

public class NetworkReflexion implements Function<String, String>{

	@Override
	public String apply(String t) {
		
		//first level: raccourci
		Object result = decode(CurrentGame.get(), t);
		if(result == null){
			return "null";
		}else{
			return result.toString();
		}
	}
	

	Pattern regexp = Pattern.compile("([^(]+)\\((([^)\\\\]|\\\\\\\\|\\\\\\))*)\\)(.|;)(.*)");
	Matcher match = regexp.matcher("");
	
	
	public Object decode(Object currentObject, String cmd){
		match.reset(cmd);
		Object result;
		
		if(match.find()){
			String functionName = match.group(1);
			String functionArgs = match.group(2);
			String leftover = match.group(5);
			
			try {
				if(functionArgs.length()>0){
					//for now, i only support one-string argument
					result = currentObject.getClass().getMethod(functionName, String.class).invoke(currentObject, functionArgs);
				}else{
					result = currentObject.getClass().getMethod(functionName).invoke(currentObject);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "error: "+e.toString();
			}
			
			if(leftover.length()>0 && result != null){
				if(result.getClass().getName().contains("ssp")){
					return decode(result, leftover);
				}else{
					return "error, bad object: "+result.getClass().getName()+", can't call "+leftover+" on it.";
				}
			}else{
				return result;
			}
			
		}
		
		return "can't find "+currentObject.getClass().getName()+" . "+cmd;
	}
	
	
	
	

}
