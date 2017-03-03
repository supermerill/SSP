package remi.ssp.actions;

import java.util.Optional;

import javax.json.Json;
import javax.json.JsonObject;

import remi.ssp.CurrentGame;
import remi.ssp.politic.Civilisation;

//all actions that players and ai can do to alter the current state of the game
public class Actions {
	
	public void createNewRegiment(JsonObject data){
		Optional<Civilisation> player = CurrentGame.get().civs.stream().filter(c -> c.getName().equals(data.getString("player"))).findFirst();
		if(!player.isPresent()){
			System.err.println("Error, cannot create new regiment because player "+data.getString("player")+" isn't here.");
		}
		String templateName = data.getString("template");
		
		//TODO: check if manpower & civilisation can create this.
		
	}
	

}
