//package remi.ssp.algorithmes;
//
//import java.util.Random;
//
//import remi.ssp.Pop;
//import remi.ssp.Province;
//
////TODO: replace this by 4 industry : fishing, harvesting, hunting, elevage (taunting?)
//public abstract class Nourriture {
// 
//	public void getNourritureSemaine(Province prv) {
//		if(prv.getNbMens()==0) return;
//
//		// premier: culture
//		int rationNecessaire = prv.getNbMens() * 7;
//		int rationRecolte = culture.culture(prv) + peche.peche(prv)
//				+ elevage.elevage(prv, 0);
//		if (rationNecessaire - rationRecolte > prv.rationReserve) {
//			logln("pas asser de recolte: "+rationRecolte+", par chasser pour "+chasse.chasse(prv)+", humidite:"+prv.humidite+", faune="+prv.pourcentFaune);
//			rationRecolte += chasse.chasse(prv) + elevage.elevage(prv, 0.5f);
//		}
//		if (rationNecessaire - rationRecolte > prv.rationReserve) {
//			rationRecolte += elevage.elevage(prv, 1f);
//		}
//		prv.rationReserve += rationRecolte - rationNecessaire;
//		if (prv.rationReserve < 0) {
//			// famine!
//			log("famine! ("+rationNecessaire+">"+rationRecolte+" => "+prv.rationReserve+")"+prv.getNbMens()+" => ");
////			for (int i = 0; i < -prv.rationReserve / 2; i++) {
//			while(prv.rationReserve<0 && prv.getNbMens() >0){
//				// la moitié des habitant non nouris meurent, les plus
//				// vieux(TODO) et jeunes d'abord
//				//TODO: les esclave en premier
//				Random rand = new Random();
//				int age = Math.abs(rand.nextInt(20) + rand.nextInt(20) - 20);
//				for( Pop pop : prv.pops)
//					if(pop.nombreHabitantsParAge[age]>0){
//						pop.nombreHabitantsParAge[age]--;
//						prv.rationReserve += 7;
//					}
//				// TODO: ne pas impacter les elites => remonter leur %age
//				
//			}
//			prv.rationReserve = 0;
//			logln(prv.getNbMens());
//		}else{
//			logln("OK! ("+rationNecessaire+">"+rationRecolte+" => "+prv.rationReserve+")"+prv.getNbMens());
//		}
//
//	}
//
//	// get nb ration(per week) (1 ration = nourriture pour 1 homme pour 1
//	// journee)
//	public interface Chasse {
//		public abstract int chasse(Province prv);
//	}
//
//	public interface Peche {
//		public abstract int peche(Province prv);
//	}
//
//	public interface Culture {
//		public abstract int culture(Province prv);
//	}
//
//	// urgence: 0 si il y a abondance, 1 si extreme famine
//	public interface Elevage {
//		public abstract int elevage(Province prv, float urgence);
//	}
//
//	// really bare algos
//
//	// chasse & ceuilette (max ~1200 hab)
//	public Chasse chasse = new Chasse() {
//		@Override
//		public int chasse(Province prv) {
//			int nbPrit = (int) ((prv.pourcentFaune * prv.pourcentForet * prv.surfaceSol) * Math.min(1,prv.getNbMens()/25f));
//			//TODO: reduire faune locale
//			prv.pourcentFaune -= prv.pourcentFaune*0.2*Math.min(1,prv.getNbMens()/25f);
//			return nbPrit;
//		}
//	};
//	// peche : un peu mieux que la subsistance
//	public Peche peche = new Peche() {
//
//		@Override
//		public int peche(Province prv) {
//			// check how many sea tiles
//			int seaTiles = 0;
//			for (int i = 0; i < prv.proche.length; i++) {
////				if (prv.proche[i] != null && prv.proche[i].surfaceSol < 10)
////					seaTiles++;
//				if (prv.proche[i] != null && prv.proche[i].isSea) seaTiles ++;
//			}
//			return (int)( seaTiles*prv.getNbMens()*prv.pourcentPaysan*10);
//		}
//	};
//	 // agriculture
//	public Culture culture = new Culture() {
//		
//		@Override
//		public int culture(Province prv) {
//			//TODO: evolution de la surface agricole
//			return (int)( prv.surfaceSol*prv.pourcentChamps*prv.champsRendement);
//		}
//	};
//	 // viande issu de l'elevage,
//	public Elevage elevage = new Elevage() {
//		
//		@Override
//		public int elevage(Province prv, float urgence) {
//			//TODO: evolution du cheptel
//			return (int)( prv.surfaceSol*prv.pourcentPrairie*prv.elevageRendement*(0.5+urgence));
//		}
//	};
//
//}
