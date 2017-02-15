package remi.ssp_basegame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import remi.ssp.CurrentGame;
import remi.ssp.algorithmes.Spawner;
import remi.ssp.economy.Good;
import remi.ssp.economy.Job;
import remi.ssp.economy.PopNeed;
import remi.ssp.economy.ProvinceGoods;
import remi.ssp.economy.ProvinceIndustry;
import remi.ssp.economy.ProvinceIndustry.ProvinceIndustryFactory;
import remi.ssp.map.FlatCarteV3;
import remi.ssp.politic.Civilisation;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;
import remi.ssp_basegame.economy.AgricultureIndustry;
import remi.ssp_basegame.economy.ElevageIndustry;
import remi.ssp_basegame.economy.HuntingIndustry;
import remi.ssp_basegame.economy.HuntingSportIndustry;
import remi.ssp_basegame.economy.PersonalServiceIndustry;
import remi.ssp_basegame.economy.WoodGoodsArtisanalIndustry;
import remi.ssp_basegame.economy.WoodHouseIndustry;
import remi.ssp_basegame.economy.WoodcutterIndustry;

public class BaseSpawner extends Spawner {

	

	/**
	 * populate CurrentGame.map (provinces +plots)
	 * 
	 */
	public void createMap(){
		CurrentGame.map = new FlatCarteV3().createMap(2, 2);
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
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(HuntingSportIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(WoodcutterIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(WoodGoodsArtisanalIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(WoodHouseIndustry.get()).setProvince(prv).create());
					prv.addIndustry(ProvinceIndustryFactory.creator.setInustry(PersonalServiceIndustry.get()).setProvince(prv).create());
					
//					for( ProvinceIndustry indus : prv.getIndustries()){
//						logln("province has "+indus.getName());
//					}
					
					CurrentGame.civs.add(civ);
					
					//add all province goods possible
					for(Good good : Good.GoodFactory.goodList.values()){
						prv.getStock().put(good, new ProvinceGoods(good));
						prv.getStock().get(good).setPrice(100);
					}
					//place some food and houses
					prv.getStock().get(Good.get("meat")).setStock(500);
					prv.getStock().get(Good.get("crop")).setStock(13500);
					prv.getStock().get(Good.get("wood_house")).setStock(10);
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
					//rich
					Pop pop = new Pop(prv);
					pop.addAdult(60);
					pop.setPopType(Pop.popTypeName.indexOf("rich"));
					prv.addPop(pop);
					//set pop to chomage
					pop.setNbMensChomage(pop.getNbAdult());
					//create some money from "the previous time" (ie, fine air)
					pop.addMoney(pop.getNbAdult()*1000000); // 1000/100/10 "coin" per men (can be /1000)
					//add needs (TODO: get them from techs researched at startup)
					pop.getPopNeeds().add(PopNeed.create("food", pop));
					pop.getStock().put(Good.get("crop"), pop.getNbAdult()*250); //~some day of food
//					pop.getPopNeeds().add(HouseNeed.create("house", pop));
					pop.getPopNeeds().add(PopNeed.create("rich_service", pop));
					//middle
					pop = new Pop(prv);
					pop.addAdult(300);
					pop.setPopType(Pop.popTypeName.indexOf("middle"));
					prv.addPop(pop);
					pop.setNbMensChomage(pop.getNbAdult());
					pop.addMoney(pop.getNbAdult()*100000);
					pop.getPopNeeds().add(PopNeed.create("food", pop));
					pop.getStock().put(Good.get("crop"), pop.getNbAdult()*250); //~some day of food
					pop.getPopNeeds().add(PopNeed.create("middle_service", pop));
//					pop.getPopNeeds().add(HouseNeed.create("house", pop));
					//poor
					pop = new Pop(prv);
					pop.addAdult(1000);
					pop.setPopType(Pop.popTypeName.indexOf("poor"));
					prv.addPop(pop);
					pop.setNbMensChomage(pop.getNbAdult());
					pop.addMoney(pop.getNbAdult()*10000);
					pop.getPopNeeds().add(PopNeed.create("food", pop));
					pop.getStock().put(Good.get("crop"), pop.getNbAdult()*750); //~some day of food
					pop.getPopNeeds().add(PopNeed.create("middle_service", pop));
//					pop.getPopNeeds().add(HouseNeed.create("house", pop));
				}
			}
		
		}
		
		fixJobFromIndustryAndCommerce();
	}

	/**
	 * call this when you add a new indutry to some provinces. Or do the job yourself ffs!
	 */
	public static void fixJobFromIndustryAndCommerce(){
		for(int i=0;i<CurrentGame.map.provinces.size();i++){
			for(int j=0;j<CurrentGame.map.provinces.get(i).size();j++){
				Province prv = CurrentGame.map.provinces.get(i).get(j);
				Set<Job> jobs = new HashSet<>();
				for(Pop pop: prv.getPops()){
					jobs.clear();
					jobs.addAll(prv.getIndustries());
//					jobs.add(pop.getLandCommerce());
//					jobs.add(pop.getSeaCommerce());
					for(Entry<Job> job : pop.getNbMensEmployed().object2LongEntrySet()){
						jobs.remove(job.getKey());
					}
					for(Job job : jobs){
						pop.getNbMensEmployed().put(job, 0);
					}
				}
			}
		}
	}
}
