package remi.ssp.actions;

import java.util.List;
import java.util.Optional;

import javax.json.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import remi.ssp.CurrentGame;
import remi.ssp.army.Battalion;
import remi.ssp.army.BattalionUnit;
import remi.ssp.army.DivisionTemplate;
import remi.ssp.army.DivisionUnit;
import remi.ssp.army.EquipmentDevelopped;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Plot;
import remi.ssp.politic.Province;
import remi.ssp.utils.U;

//all actions that players and ai can do to alter the current state of the game
public class Actions {
	
	public void createNewDivisionUnit(JsonObject data){
		Optional<Civilisation> player = CurrentGame.get().getCivs().stream().filter(c -> c.getName().equals(data.getString("player"))).findFirst();
		if(!player.isPresent() && player.get().getProvinces().size() > 0){
			System.err.println("Error, cannot create new regiment because player "+data.getString("player")+" isn't here.");
			return;
		}
		String templateName = data.getString("template");
		
		//get template
		DivisionTemplate divisionT = null;
		for( DivisionTemplate dt : player.get().getDivisionTemplate()){
			if(dt.getName().equals(templateName)){
				divisionT = dt;
				break;
			}
		}
		if(divisionT == null){
			System.err.println("Error, cannot create new regiment because template "+templateName+" doesn't exist.");
			return;
		}
		
		//TODO: check if manpower & goods & civilisation can create this.
		
		//spawn it
		DivisionUnit du = new DivisionUnit();
		du.setTemplate(divisionT);
		for(Entry<Battalion> bat : divisionT.getNbBattalions().object2IntEntrySet()){
			for(int i=0;i<bat.getIntValue();i++){
				BattalionUnit bu = new BattalionUnit();
				bu.setTemplate(bat.getKey());
				
				//add mens
				int nbMens = bat.getKey().getNbFightingMens() + bat.getKey().getNbHandlingMens();
				bu.setAvailableMens(nbMens);
				player.get().addMensInReserve(-nbMens);
				
				//add equip
				for(EquipmentDevelopped equip : bat.getKey().getEquipment()){
					long nbEquipReserve = player.get().getEquipmentReserve().getLong(equip);
					nbEquipReserve -= bat.getKey().getNbFightingMens();
					bu.getEquipmentForArmy().put(equip, bat.getKey().getNbFightingMens());
					player.get().getEquipmentReserve().put(equip, nbEquipReserve);
				}
				
				//insert a battalion into division
				du.getBattalions().add(bu);
			}
		}
		
		//place it in a random plot
		Province spawnPoint = player.get().getProvinces().get(0);
		du.setPosition(spawnPoint.centerPlot);
	}

	
	public void sendDivisionTo(JsonObject data){
			String playerName = data.getString("player");
			String divisionName = data.getString("division");
			int x = data.getInt("x");
			int y = data.getInt("y");
			
			//check data
			if(x <0 || y<0 ){
				throw new RuntimeException("Error, cannot move division at  "+x+":"+y+".");
			}
			Optional<Civilisation> player = CurrentGame.get().getCivs().stream().filter(c -> c.getName().equals(playerName)).findFirst();
			if(!player.isPresent()){
				throw new RuntimeException("Error, cannot move division because player "+playerName+" isn't here.");
			}
			Optional<DivisionUnit> unit = player.get().getDivisions().stream().filter(c -> c.getName().equals(divisionName)).findFirst();
			if(!unit.isPresent()){
				throw new RuntimeException("Error, cannot move division because division "+divisionName+" isn't here.");
			}
			Plot newPlot = CurrentGame.get().getMap().getPlots().get(x).get(y);
			
			//check if the plot is near the previous one
			List<Plot> targetList = unit.get().getTarget();
			if(!U.containsAddr(targetList.get(targetList.size()-1).getAround(), newPlot)){
				//find the nearest ones
				List<Plot> nearests = unit.get().getPosition().nearest(newPlot);
				unit.get().getTarget().addAll(nearests);
				//TODO: check if the list is correct
			}
			
			if(U.last(unit.get().getTarget()) != newPlot) unit.get().addTarget(newPlot);
			
	}

}
