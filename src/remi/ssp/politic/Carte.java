package remi.ssp.politic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Carte {

	// ceci est une implémentation, gaffe!
	//il faudrait une Province -> x,y,image pour etre plus générique, pt-etre stoké dans Province?
	public List<List<Province>> provinces = new ArrayList<>();
	public int nbLigne = 0, nbColonne = 0;
	Random rand = new Random();

}
