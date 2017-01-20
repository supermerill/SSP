package remi.ssp_basegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.Spawner;
import remi.ssp.economy.Good;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.economy.ProvinceIndustry.ProvinceIndustryFactory;
import remi.ssp.map.FlatCarteV3;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp_basegame.economy.AgricultureIndustry;
import remi.ssp_basegame.economy.ElevageIndustry;
import remi.ssp_basegame.economy.FoodNeed;
import remi.ssp_basegame.economy.HuntingIndustry;
import remi.ssp_basegame.economy.WoodGoodsArtisanalIndustry;
import remi.ssp_basegame.economy.WoodHouseIndustry;
import remi.ssp_basegame.economy.WoodcutterIndustry;

public class BaseSpawner extends Spawner {

	

	/**
	 * populate CurrentGame.map (provinces +plots)
	 * 
	 */
	public void createMap(){
		CurrentGame.map = new FlatCarteV3().createMap(10, 10);
	}
	
	/**
	 * use CurrentGame.map to populate CurrentGame.civs
	 * 
	 */
	public void createCivs(){
		CurrentGame.civs = new ArrayList<>();
		//we create 1 civ per land hex at startup
		for(List<Province> prvs : CurrentGame.map.provinces){
			for(Province prv : prvs){
				if(prv.surfaceSol > 10){
					Civilisation civ = new Civilisation();
					civ.getProvinces().add(prv);
					prv.setOwner(civ);
					//TODO add base techs to civ
					//TODO add base ideas to civ&prv
					//add industry to prv
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(ElevageIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(AgricultureIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(HuntingIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(WoodcutterIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(WoodGoodsArtisanalIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(WoodHouseIndustry.get()).setProvince(prv).create());
					CurrentGame.civs.add(civ);
					
					//add all province goods possible
					for(Good good : Good.GoodFactory.goodList.values()){
						prv.getStock().put(good, new ProvinceGoods());
					}
					//place some food and houses
					prv.getStock().get(Good.get("meat")).stock = 5000;
					prv.getStock().get(Good.get("meat")).price = 1;
					prv.getStock().get(Good.get("wood_house")).stock = 10;
					prv.getStock().get(Good.get("wood_house")).price = 1;
				}
			}
		}
	}

	/**
	 * use CurrentGame.map and CurrentGame.civs to add pop on provinces
	 * 
	 */
	public void createPop(){
		Random rand = new Random();
		for(int i=0;i<CurrentGame.map.provinces.size();i++){
			for(int j=0;j<CurrentGame.map.provinces.get(i).size();j++){
				Province prv = CurrentGame.map.provinces.get(i).get(j);
				if(prv.surfaceSol > 10){
					Pop pop = new Pop(prv);
					pop.addHabitants(20, Math.abs((int)(rand.nextFloat() * Math.exp(rand.nextInt(10)))));
					prv.getPops().add(pop);
	//						System.out.println("create pop of " + prv.nombreHabitantsParAge[20]);
					//set pop to chomage
					pop.setNbMensChomage(pop.getNbMens());
					//create some money from "the previous time" (ie, fine air)
					pop.setMoney(pop.getNbMens()*1000);
					//add needs (TODO: get them from techs researched at startup)
					pop.getPopNeeds().add(FoodNeed.create("food", pop));
//					pop.getPopNeeds().add(FoodNeed.create("house", pop));
				}
			}
		
		}
	}
}
