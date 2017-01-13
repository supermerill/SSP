package remi.ssp.politic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import remi.ssp.army.DivisionUnit;
import remi.ssp.army.EquipmentDevelopped;
import remi.ssp.economy.Needs;

public class Civilisation {

	List<Province> provinces;
	
	 // for army
	List<DivisionUnit> divisions = new ArrayList<>();
	int mensInReserve = 0;


	public List<Province> getProvinces() { return provinces; }
	public List<DivisionUnit> getDivisions() { return divisions; }
	public int getMensInReserve() { return mensInReserve; }
//	public Map<EquipmentDevelopped> getEquipmentReserve() { return equipmentReserve; }
	
	
	
}
