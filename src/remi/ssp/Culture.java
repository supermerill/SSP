package remi.ssp;

public class Culture {
	//TODO tenet, etc
	
	String name;
	
	//caracteristiques: linéaire de -1 000 000 à + 1 000 000
	
	//gros pompage de stellaris
	int esclavage2freedom = 0;
	int spiritual2athee = 0;
	int collectivist2individualist = 0;
	int warrior2peaceful = 0;
	int rascite2tolerant = 0;
	
	//un peu d'autres pour le fun
	int violence2talking = 0;
	int polytheiste2monotheiste = 0;
	int puritain2hippie = 0;
	int vengance2mercy = 0;
	int unrespectful2honor = 0;
	
	
	//croyances spirituelles. <-500 000 => banned, >100 000 => accepted
	int nature = 0;
	int stars = 0;
	int moon = 0;
	int sea = 0;
	int divination = 0;
	int sorcery = 0;
	
	//religion (TODO: extension) (ie, organized beliefs)
	String religionName = "Flying Spaghetti Monster";

}
