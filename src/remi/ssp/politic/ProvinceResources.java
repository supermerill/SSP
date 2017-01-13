package remi.ssp.politic;


public class ProvinceResources {

	static final int TRES_FACILE = 0;
	static final int FACILE = 1;
	static final int MOYEN = 2;
	static final int DUR = 3;
	static final int TRES_DUR = 4;
	static final int[] DIFFICULTE = new int[]{TRES_FACILE, FACILE, MOYEN, DUR, TRES_DUR};
	
	
	Resources r;
	float[] probabiliteNouveauFilon = new float[DIFFICULTE.length];
	float[] quantiteRestante = new float[DIFFICULTE.length];
	
}
