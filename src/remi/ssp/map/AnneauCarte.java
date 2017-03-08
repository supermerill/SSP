package remi.ssp.map;

import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import remi.ssp.politic.Carte;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

public class AnneauCarte implements MapFactory {


	public List<List<Province>> provinces = new ArrayList<>();
	int nbLigne = 0, nbColonne = 0;
	Random rand = new Random();
	
	@Override
	public Carte createMap(int width, int height) {
		Carte carte = new Carte();
//		carte.provinces = provinces;
		provinces = carte.getProvinces();
		createAnneau(height, width);
		carte.setNbLigne(nbLigne);
		carte.setNbColonne(nbColonne);
		return carte;
	}

	public void createAnneau(int nbLigne, int nbColonne) {
		this.nbLigne = nbLigne;
		this.nbColonne = nbColonne;
		// need pair
		if (nbColonne % 2 == 1)
			nbColonne++;
		// firstColonne
		ArrayList<Province> firstColonne = new ArrayList<>();
		for (int j = 0; j < nbLigne; j++) {
			Province prv = new Province();
			if (j > 0) {
				prv.proche[0] = firstColonne.get(firstColonne.size() - 1);
				firstColonne.get(firstColonne.size() - 1).proche[3] = prv;
			}
			firstColonne.add(prv);
		}
		provinces.add(firstColonne);
		ArrayList<Province> previousColonne = new ArrayList<>(firstColonne);
		for (int i = 1; i < nbColonne; i++) {
			ArrayList<Province> currentColonne = new ArrayList<>();
			// les impair sont un peu plus bas
			for (int j = 0; j < nbLigne; j++) {
				Province prv = new Province();
				if (j > 0) {
					prv.proche[0] = currentColonne
							.get(currentColonne.size() - 1);
					currentColonne.get(currentColonne.size() - 1).proche[3] = prv;
				}
				if (i % 2 == 1) {
					prv.proche[5] = previousColonne.get(j);
					previousColonne.get(j).proche[2] = prv;
					if (j < nbLigne - 1) {
						prv.proche[4] = previousColonne.get(j + 1);
						previousColonne.get(j + 1).proche[1] = prv;
					}
				} else {
					if (j > 0) {
						prv.proche[5] = previousColonne.get(j - 1);
						previousColonne.get(j - 1).proche[2] = prv;
					}
					prv.proche[4] = previousColonne.get(j);
					previousColonne.get(j).proche[1] = prv;
				}
				currentColonne.add(prv);
			}
			provinces.add(currentColonne);
			previousColonne = currentColonne;
			currentColonne = new ArrayList<>();
		}
		// final merge with first colonne
		for (int j = 0; j < nbLigne; j++) {
			if (j > 0) {
				firstColonne.get(j).proche[5] = previousColonne.get(j - 1);
				previousColonne.get(j - 1).proche[2] = firstColonne.get(j);
			}
			firstColonne.get(j).proche[4] = previousColonne.get(j);
			previousColonne.get(j).proche[1] = firstColonne.get(j);
		}

		// now, the structure is done, launch a creation of map
		createSol();
		createRelief();
		createDesert();
		populate();
	}

	private void populate() {
		Random rand = new Random();
		for(int i=0;i<provinces.size();i++){
			for(int j=0;j<provinces.get(i).size();j++){
				Province prv = provinces.get(i).get(j);
				if(prv.surfaceSol>0){
					Pop pop = new Pop(prv);
					pop.addAdult(Math.abs((int)(rand.nextFloat() * Math.exp(rand.nextInt(10)))));
					prv.getPops().add(pop);
//					logln("create pop of " + prv.nombreHabitantsParAge[20]);
				}
			}
		}
		
	}

	private void createRelief() {
//		double nbCases = nbLigne * nbColonne;
//		nbCases *= 0.3; //nb terre emerge
//		nbCases /= 2; //nb de terre anguleuses

		int ordreGrandeurInit = (int) Math.log1p(nbLigne * nbColonne) - 2;
		for (int tailleMontagne = 0; tailleMontagne < ordreGrandeurInit*1.5; tailleMontagne++) {
			int nbRelief = ordreGrandeurInit*2;
			for (int i = 0; i < nbRelief; i++) {
				// on prend un point sur la terre au pif
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				while(provinces.get(abscice).get(ordonnee).surfaceSol < 10){
					abscice = rand.nextInt(nbColonne);
					ordonnee = rand.nextInt(nbLigne);
				}
				Province centre = provinces.get(abscice).get(ordonnee);
				if (centre.surfaceSol > 10) {
					// on fait une grosse montagne dessus
					centre.relief = (float)Math.min(1,(1/((3*i/((float)nbRelief))+1))+0.2);
					// on continue pendant tailleMontagne
					int nbMont = 1;
					centre = centre.proche[rand.nextInt(centre.proche.length)];
					while(centre != null && centre.surfaceSol>0 && nbMont<tailleMontagne){
						centre.relief = (float)Math.min(1,(1/((3*i/(float)nbRelief)+1))+0.2);
								//(float)((i>ordreGrandeurInit)?0.6:1);
						centre = centre.proche[rand.nextInt(centre.proche.length)];
						nbMont++;
					}
					nbMont = 0;
				}
			}
		}
	}
	

	private void createDesert() {
		int ordreGrandeurInit = (int) Math.log1p(nbLigne * nbColonne) - 2;

		for (int tailleMontagne = 0; tailleMontagne < ordreGrandeurInit*2; tailleMontagne++) {
			for (int i = 0; i < ordreGrandeurInit; i++) {
				// on prend un point sur la terre au pif
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				while(provinces.get(abscice).get(ordonnee).surfaceSol < 10){
					abscice = rand.nextInt(nbColonne);
					ordonnee = rand.nextInt(nbLigne);
				}
				Province centre = provinces.get(abscice).get(ordonnee);
				final float humidite = (float)(i/(2.0*ordreGrandeurInit));
				Dessiner dessineur = new Dessiner() {
					@Override public void dessine(Province prv) {
						prv.humidite = humidite;
						logln("humidite: "+humidite+", "+Math.abs(humidite-0.5f));
						prv.pourcentForet = (0.5f-Math.abs(humidite-0.5f))/2;
						logln("pourcentForet: "+prv.pourcentForet);
						prv.pourcentFriche = prv.pourcentForet;
						prv.pourcentPrairie = prv.pourcentFriche/2;
						prv.pourcentFriche = prv.pourcentFriche/2;
						prv.pourcentSterile = Math.abs(humidite-0.5f);
					}};
				dessine(centre, tailleMontagne/3, dessineur);
//				centre.humidite = (float)( 0+(i/(2.0*ordreGrandeurInit)));
//				if(tailleMontagne > 3){
//					for(int t=0;t<6;t++){
//						if(centre.proche[t]!=null){
//							centre.proche[t].humidite = (float)( 0+(i/(2.0*ordreGrandeurInit)));
//						}
//					}
//				}
				int nbMont = 1;
				centre = centre.proche[rand.nextInt(centre.proche.length)];
				while(centre != null && centre.surfaceSol>0 && nbMont<tailleMontagne){
					dessine(centre, tailleMontagne/3, dessineur);
					centre = centre.proche[rand.nextInt(centre.proche.length)];
					nbMont++;
				}
				nbMont = 0;
			}
		}
	}

	private void createSol() {
		// 50% water

		// on va envoyer des patch de terre et d'eau de + en plus petits au pif
		// (ou presque)

		// on commance par tout mettre en eau

		for (List<Province> prvs : provinces) {
			for (Province prv : prvs) {
				prv.surfaceSol = 0;
			}
		}

		// on prend des points au pif
		int ordreGrandeurInit = (int) Math.log1p(nbLigne * nbColonne);
		// logln("ordreGrandeurInit:" + ordreGrandeurInit);
		for (int ordreGrandeur = ordreGrandeurInit; ordreGrandeur > 0; ordreGrandeur--) {
			int nbPatchs = 1 + (ordreGrandeurInit - ordreGrandeur) * 8;
			// logln("nbPatchs:" + nbPatchs+", "+nbLigne *
			// nbColonne+" / "+(ordreGrandeur*ordreGrandeur));
			int taillePatch = ordreGrandeur;
			// logln("taillePatch:" + taillePatch);
			// add some points
			for (int numPatch = 0; numPatch < 2; numPatch++) {
				// get random pos
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				Province centre = provinces.get(abscice).get(ordonnee);
				dessine(centre, 1, 10000);
				abscice = rand.nextInt(nbColonne);
				ordonnee = rand.nextInt(nbLigne);
				centre = provinces.get(abscice).get(ordonnee);
				dessine(centre, 1, 0);
			}

			// ajouter de la terre
			for (int numPatch = 0; numPatch < 2; numPatch++) {
				// get random pos
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				Province centre = provinces.get(abscice).get(ordonnee);
				// dessiner un truc (si proche de son element?)
				if (taillePatch != 2)
					dessine(centre, taillePatch, 10000);
			}
			for (int numPatch = 0; numPatch < nbPatchs; numPatch++) {
				// get random pos
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				Province centre = provinces.get(abscice).get(ordonnee);
				// dessiner un truc (si proche de son element?)
				// if(ordreGrandeur>ordreGrandeurInit-2
				// || ordreGrandeur<2
				// || checkIsTerre(centre))
				if (checkIsTerre(centre))
					dessine(centre, taillePatch, 10000);
			}
			int maxIter = 0;
			while (percentEau() > 0.72 && maxIter < 10000) {
				// get random pos
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				Province centre = provinces.get(abscice).get(ordonnee);
				// dessiner un truc (si proche de son element?)
				if (checkIsTerre(centre) || ordreGrandeur == 2)
					dessine(centre, taillePatch, 10000);
				maxIter++;
			}
			maxIter = 0;
			// ajouter de l'eau
			for (int numPatch = 0; maxIter < 10000
					&& (numPatch < nbPatchs || (ordreGrandeur < 3 && percentEau() < 0.68)); numPatch++) {
				// get random pos
				int abscice = rand.nextInt(nbColonne);
				int ordonnee = rand.nextInt(nbLigne);
				Province centre = provinces.get(abscice).get(ordonnee);
				// dessiner un truc (si proche de son element?)
				if (checkIsEau(centre))
					dessine(centre, taillePatch, 0);
				maxIter++;
			}
		}
	}

	public boolean checkIsTerre(Province centre) {
		if (centre.surfaceSol > 0)
			return true;
		boolean ret = false;
		for (Province prv : centre.proche) {
			if (prv != null)
				ret |= prv.surfaceSol > 0;
		}
		return ret;
	}

	public boolean checkIsEau(Province centre) {
		if (centre.surfaceSol == 0)
			return true;
		boolean ret = false;
		for (Province prv : centre.proche) {
			if (prv != null)
				ret |= prv.surfaceSol == 0;
		}
		return ret;
	}

	public double percentEau() {
		int nbEau = 0;
		for (List<Province> prvs : provinces) {
			for (Province prv : prvs) {
				nbEau += prv.surfaceSol == 0 ? 1 : 0;
			}
		}
		return nbEau / (double) (provinces.size() * provinces.get(0).size());
	}

	void dessine(Province centre, int taillePatch, final int kmCaree) {
		dessine(centre, taillePatch, new Dessiner() {
			
			@Override
			public void dessine(Province prv) {
				prv.surfaceSol = kmCaree;
			}
		});
//		centre.surface = kmCaree;
//		HashSet<Province> alreadySeen = new HashSet<>();
//		alreadySeen.add(centre);
//		HashSet<Province> toSee = new HashSet<>();
//		toSee.addAll(Arrays.asList(centre.proche));
//		for (int i = 0; i < taillePatch; i++) {
//			HashSet<Province> toSeeAfter = new HashSet<>();
//			for (Province prv : toSee) {
//				if (prv != null && !alreadySeen.contains(prv)) {
//					alreadySeen.add(prv);
//					prv.surface = kmCaree;
//					toSeeAfter.addAll(Arrays.asList(prv.proche));
//				}
//			}
//			toSee = toSeeAfter;
//		}
	}
	
	void dessine(Province centre, int taillePatch, Dessiner todo) {
		todo.dessine(centre);
		HashSet<Province> alreadySeen = new HashSet<>();
		alreadySeen.add(centre);
		HashSet<Province> toSee = new HashSet<>();
		toSee.addAll(Arrays.asList(centre.proche));
		for (int i = 0; i < taillePatch; i++) {
			HashSet<Province> toSeeAfter = new HashSet<>();
			for (Province prv : toSee) {
				if (prv != null && !alreadySeen.contains(prv)) {
					alreadySeen.add(prv);
					todo.dessine(prv);
					toSeeAfter.addAll(Arrays.asList(prv.proche));
				}
			}
			toSee = toSeeAfter;
		}
	}

	interface Dessiner{
		void dessine(Province prv);
	}

	
}
