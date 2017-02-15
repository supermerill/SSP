package remi.ssp.map;

import static remi.ssp.GlobalDefines.log;
import static remi.ssp.GlobalDefines.logln;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import remi.ssp.politic.Carte;
import remi.ssp.politic.Plot;
import remi.ssp.politic.Pop;
import remi.ssp.politic.Province;

/*  
 *   0_
 * 1/  \5
 * 2\__/4
 *    3
 *    
 *  1  2  3  4  5  6  7
 *  
 *  O     O     O     O
 *     O     O     O  
 *  1     1     1     O
 *     1     1     1  
 *  2     2     2     2 
 *     2     2     2   
 */
public class AnneauCarteV2 implements MapFactory {


	public List<List<Province>> provinces = new ArrayList<>();
	public List<List<Plot>> plots = new ArrayList<>();
	int nbLigne = 0, nbColonne = 0;
	Random rand = new Random();
	Carte currentcarte;
	
	@Override
	public Carte createMap(int width, int height) {
		Carte carte = new Carte();
		currentcarte = carte;
		carte.provinces = provinces;
		carte.plots = plots;
		carte.nbLigne = nbLigne;
		carte.nbColonne = nbColonne;
		createAnneau(height, width);
		return carte;
	}

	public void createAnneau(int nbLigne, int nbColonne) {
		this.nbLigne = nbLigne;
		this.nbColonne = nbColonne;
		// need pair
		if (nbColonne % 2 == 1)
			nbColonne++;
		if (nbLigne % 2 == 1) //opas nécéssaire, mais au cas ou...
			nbLigne++;
		// firstColonne
		ArrayList<Province> firstColonne = new ArrayList<>();
		for (int j = 0; j < nbLigne; j++) {
			Province prv = new Province();
			prv.x = 0;
			prv.y = j;
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
				prv.x = i;
				prv.y = j;
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

		int offsetPlotY = nbColonne - (nbColonne/4) - 1;
		//init plots array to null
		//upper left corner
		logln("nbColonnes: "+nbColonne+", nbLines: "+nbLigne);
		int minplotx = 0;
		//check upper right corner
		int minploty = 0;//offset + ((nbColonne-1)%2) - 2 * ((nbColonne-1)/2);
		//lower right corner
		int maxplotx = 1 + 5 * ((nbColonne-1)/2) + 3 * ((nbColonne-1)%2) + (nbLigne-1);
		currentcarte.nbPlotColonne = maxplotx;
		//lower left corner
		int maxploty = 1 + offsetPlotY + 2*(nbLigne-1) + (nbLigne-1)/2;
		currentcarte.nbPlotLigne = maxploty;
		//add offset (from province to plot)
		maxploty += 2;
		maxplotx += 2;
		logln("coloneplotfrom 0 to: "+currentcarte.nbPlotColonne+", lines from "+(-offsetPlotY)+" to "+(currentcarte.nbPlotLigne-offsetPlotY)+" ("+currentcarte.nbPlotLigne+")");
		//check lower left 
		for (int i = 0; i < maxplotx; i++) {
			List<Plot> colonne = new ArrayList<>();
			plots.add(colonne);
			for (int j = 0; j < maxploty; j++) {
				colonne.add(null);
			}
		}
		//create plots
		for (int i = 0; i < nbColonne; i++) {
			List<Province> currentColonne = provinces.get(i);
			int plotx = 1 + 5 * (i/2) + 3 * (i%2);
//			int ploty = 1 + offsetPlotY + (i%2) - 2 * (i/2);
			int ploty = offsetPlotY + -3*(i/4) + (i%2)*(((i/2)+1)%2) - ((i/2)%2);
			// les impair sont un peu plus bas
			for (int j = 0; j < nbLigne; j++) {
				//for each prv, we add  all plots
				Province currentPrv = currentColonne.get(j);
				logln("create plots for prv "+i+":"+j+" => "+plotx+":"+ploty);
				logln("offsetPlotY="+offsetPlotY+", -3*(i/4)="+(-3*(i/4))+", (i%2)*(((i/2)+1)%2)="+(i%2)*(((i/2)+1)%2)+", - ((i/2)%2)="+(- ((i/2)%2)));
				currentPrv.centerPlot = new Plot((short)(plotx),(short)(ploty),currentPrv, (byte)-1);
				plots.get(plotx).set(ploty, currentPrv.centerPlot);
				if(plotx%2==1){
					currentPrv.myPLots[0] = new Plot((short)(plotx),(short)(ploty-1),currentPrv, (byte)0);
					plots.get(plotx).set(ploty-1, currentPrv.myPLots[0]);
					currentPrv.myPLots[1] = new Plot((short)(plotx-1),(short)(ploty),currentPrv, (byte)1);
					plots.get(plotx-1).set(ploty, currentPrv.myPLots[1]);
					currentPrv.myPLots[2] = new Plot((short)(plotx-1),(short)(ploty+1),currentPrv, (byte)2);
					plots.get(plotx-1).set(ploty+1, currentPrv.myPLots[2]);
					currentPrv.myPLots[3] = new Plot((short)(plotx),(short)(ploty+1),currentPrv, (byte)3);
					plots.get(plotx).set(ploty+1, currentPrv.myPLots[3]);
					currentPrv.myPLots[4] = new Plot((short)(plotx+1),(short)(ploty+1),currentPrv, (byte)4);
					plots.get(plotx+1).set(ploty+1, currentPrv.myPLots[4]);
					currentPrv.myPLots[5] = new Plot((short)(plotx+1),(short)(ploty),currentPrv, (byte)5);
					plots.get(plotx+1).set(ploty, currentPrv.myPLots[5]);
				}else{
					currentPrv.myPLots[0] = new Plot((short)(plotx),(short)(ploty-1),currentPrv, (byte)0);
					plots.get(plotx).set(ploty-1, currentPrv.myPLots[0]);
					currentPrv.myPLots[1] = new Plot((short)(plotx-1),(short)(ploty-1),currentPrv, (byte)1);
					plots.get(plotx-1).set(ploty-1, currentPrv.myPLots[1]);
					currentPrv.myPLots[2] = new Plot((short)(plotx-1),(short)(ploty),currentPrv, (byte)2);
					plots.get(plotx-1).set(ploty, currentPrv.myPLots[2]);
					currentPrv.myPLots[3] = new Plot((short)(plotx),(short)(ploty+1),currentPrv, (byte)3);
					plots.get(plotx).set(ploty+1, currentPrv.myPLots[3]);
					currentPrv.myPLots[4] = new Plot((short)(plotx+1),(short)(ploty),currentPrv, (byte)4);
					plots.get(plotx+1).set(ploty, currentPrv.myPLots[4]);
					currentPrv.myPLots[5] = new Plot((short)(plotx+1),(short)(ploty-1),currentPrv, (byte)5);
					plots.get(plotx+1).set(ploty-1, currentPrv.myPLots[5]);
				}
				//relance
				plotx ++;
				ploty += (2 + (j+1)%2 + (j%2)*(((i+1)/2)%2) - ((j+1)%2)*(((i+1)/2)%2));
				
//				for (int j2 = 0; j2 < maxploty; j2++) {
//					logln();
//					for (int i2 = 0; i2 < maxplotx; i2++) {
//						log(plots.get(i2).get(j2) == null?".":
//							((plots.get(i2).get(j2).getPositionInProvince())<0?"o":plots.get(i2).get(j2).getPositionInProvince()));
//						
//					}
//				}
//				for (int j2 = 0; j2 < maxploty; j2++) {
//					logln();
//					for (int i2 = 0; i2 < maxplotx; i2++) {
//						log(plots.get(i2).get(j2) == null?"   ":
//							((plots.get(i2).get(j2).getProvince() ==null)?".:.":plots.get(i2).get(j2).getProvince().x+":"+plots.get(i2).get(j2).getProvince().y));
//						
//					}
//				}
			}
		}
		//create plots links
		for (int i = 0; i < nbColonne; i++) {
			List<Province> currentColonne = provinces.get(i);
			int plotx = 1 + 5 * (i/2) + 3 * (i%2);
			int ploty = offsetPlotY + -3*(i/4) + (i%2)*(((i/2)+1)%2) - ((i/2)%2);
			// les impair sont un peu plus bas
			for (int j = 0; j < nbLigne; j++) {
				//for each prv, we add  all plots
				Province currentPrv = currentColonne.get(j);
				currentPrv.centerPlot.around=currentPrv.myPLots;
				logln("set nearby plots for prv "+i+":"+j+" => "+plotx+":"+ploty);
				if(plotx%2==1){
					setNearbyPlot(plotx, ploty-1);
					setNearbyPlot(plotx-1, ploty);
					setNearbyPlot(plotx-1, ploty+1);
					setNearbyPlot(plotx, ploty+1);
					setNearbyPlot(plotx+1, ploty+1);
					setNearbyPlot(plotx+1, ploty);
				}else{
					setNearbyPlot(plotx, ploty-1);
					setNearbyPlot(plotx-1, ploty-1);
					setNearbyPlot(plotx-1, ploty);
					setNearbyPlot(plotx, ploty+1);
					setNearbyPlot(plotx+1, ploty);
					setNearbyPlot(plotx+1, ploty-1);
				}
				//relance
				plotx ++;
				ploty += (2 + (j+1)%2 + (j%2)*(((i+1)/2)%2) - ((j+1)%2)*(((i+1)/2)%2));
			}
		}

		// now, the structure is done, launch a creation of map
		createSol();
		createRelief();
		createDesert();
		populate();
	}
	
	//note: this doesn't work: it put null pointer because the map isn't square
	private void setNearbyPlot(int x, int y){
		int xm1 = x-1;
		int xp1 = x+1;
		int ym1 = y-1;
		int yp1 = y+1;
		int xpoffy = 0;
		int xmoffy = 0;
		int ypoffx = 0;
		int ymoffx = 0;
		if(xm1<0){
			xm1 += currentcarte.nbPlotColonne;
//			xmoffy = 2 - nbColonne;
		}else if(xp1>=currentcarte.nbPlotColonne){
			xp1 -= currentcarte.nbPlotColonne;
//			xpoffy = nbColonne-2;
		}
		if(ym1<0){
			ym1 += currentcarte.nbPlotLigne;
//			ymoffx = 
		}
		if(yp1>=currentcarte.nbPlotLigne){
			yp1 -= currentcarte.nbPlotLigne;
		}
		logln("setnearby plot "+x+":"+y+ " "+xm1+"->"+xp1+" : "+ym1+"->"+yp1);
		logln("plot.size="+plots.size()+", plot(x).size="+plots.get(x).size()
				+", plot(x-1).size="+plots.get(xm1).size()+", plot(x+1).size="+plots.get(xp1).size());
		Object o = plots.get(x).get(ym1);
		Object o2 = plots.get(x).get(y);
		if(x%2==0){
			plots.get(x).get(y).around[0] = plots.get(x).get(ym1);
			plots.get(x).get(y).around[1] = plots.get(xm1).get(ym1+xmoffy);
			plots.get(x).get(y).around[2] = plots.get(xm1).get(y+xmoffy);
			plots.get(x).get(y).around[3] = plots.get(x).get(yp1);
			plots.get(x).get(y).around[4] = plots.get(xp1).get(y+xpoffy);
			plots.get(x).get(y).around[5] = plots.get(xp1).get(ym1+xpoffy);
		}else{
			plots.get(x).get(y).around[0] = plots.get(x).get(ym1);
			plots.get(x).get(y).around[1] = plots.get(xm1).get(y+xmoffy);
			plots.get(x).get(y).around[2] = plots.get(xm1).get(yp1+xmoffy);
			plots.get(x).get(y).around[3] = plots.get(x).get(yp1);
			plots.get(x).get(y).around[4] = plots.get(xp1).get(yp1+xpoffy);
			plots.get(x).get(y).around[5] = plots.get(xp1).get(y+xpoffy);
		}
	}

	public void populate() {
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
						prv.pourcentChamps = prv.pourcentFriche/4;
						prv.pourcentPrairie = prv.pourcentFriche/4;
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
