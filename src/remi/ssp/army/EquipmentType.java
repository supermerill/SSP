package remi.ssp.army;

public enum EquipmentType {

	MainWeapon, // weapon. We use MAX rule for this
	//SecondaryWeapon, // specialised weapon (grenade, knives, ...). We Add force of it to the global one
	Protection, //like MainWeapon but do something for defensive stats
	Transport, //a thing used to tranport things like a horse cart or a car. May influence the speed.
	Machine, //a thing we use to fight (catapult, tank...). like a MainWeapon but cannot be carried
	Communication; //used to communicate (TODO)
	
}
