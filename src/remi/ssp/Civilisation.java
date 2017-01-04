package remi.ssp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import remi.ssp.army.DivisionUnit;
import remi.ssp.army.EquipmentDevelopped;
import remi.ssp.economy.Needs;

public class Civilisation {

	List<Province> provinces;
	
	 // for army
	List<DivisionUnit> divisions = new ArrayList<>();
	int mensInReserve = 0;
	Map<EquipmentDevelopped, Integer> equipmentReserve= new HashMap<>();


	public List<Province> getProvinces() { return provinces; }
	public List<DivisionUnit> getDivisions() { return divisions; }
	public int getMensInReserve() { return mensInReserve; }
	public Map<EquipmentDevelopped, Integer> getEquipmentReserve() { return equipmentReserve; }
	
	
	
}
