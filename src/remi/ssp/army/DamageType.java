package remi.ssp.army;

import java.util.ArrayList;

public class DamageType {
	
	public static final ArrayList<DamageType> values = new ArrayList<>();
	public static final DamageType PIERCING = new DamageType();
	public static final DamageType CRUSHING = new DamageType();
	public static final DamageType CUTTING = new DamageType();
	static{
		values.add(PIERCING);
		values.add(CRUSHING);
		values.add(CUTTING);
	}

}
