package remi.ssp.algorithmes;

import java.util.Random;

import remi.ssp.Province;

public abstract class Nouriture {

	public void getNouritureSemaine(Province prv) {

		// premier: culture
		int rationNecessaire = prv.nbHabitants * 7;
		int rationRecolte = culture.culture(prv) + peche.peche(prv)
				+ elevage.elevage(prv, 0);
		if (rationNecessaire - rationRecolte > prv.rationReserve) {
			rationRecolte += chasse.chasse(prv) + elevage.elevage(prv, 0.5f);
		}
		if (rationNecessaire - rationRecolte > prv.rationReserve) {
			rationRecolte += elevage.elevage(prv, 1f);
		}
		prv.rationReserve += rationNecessaire - rationRecolte;
		if (prv.rationReserve < 0) {
			// famine!
			for (int i = 0; i < -prv.rationReserve / 2; i++) {
				// la moitié des habitant non nouris meurent, les plus
				// vieux(TODO) et jeunes d'abord
				Random rand = new Random();
				int age = Math.abs(rand.nextInt(20) + rand.nextInt(20) - 20);
				prv.nombreHabitantsParAge[age] = Math.max(0,
						prv.nombreHabitantsParAge[age] - 1);
				// TODO: ne pas impacter les elites => remonter leur %age
			}
			prv.rationReserve = 0;
		}

	}

	// get nb ration(per week) (1 ration = nouriture pour 1 homme pour 1
	// journée)
	public interface Chasse {
		public abstract int chasse(Province prv);
	}

	public interface Peche {
		public abstract int peche(Province prv);
	}

	public interface Culture {
		public abstract int culture(Province prv);
	}

	// urgence: 0 si il y a abondance, 1 si extreme famine
	public interface Elevage {
		public abstract int elevage(Province prv, float urgence);
	}

	// really bare algos

	// chasse & ceuilette (max ~1200 hab)
	public Chasse chasse = new Chasse() {
		@Override
		public int chasse(Province prv) {
			return (int) (prv.pourcentForet * prv.surfaceSol);
		}
	};
	// peche : un peu mieux que la subsistance
	public Peche peche = new Peche() {

		@Override
		public int peche(Province prv) {
			// check how many sea tiles
			int seaTiles = 0;
			for (int i = 0; i < prv.proche.length; i++) {
				if (prv.proche[i] != null && prv.proche[i].surfaceSol < 10)
					seaTiles++;
			}
			return (int)( seaTiles*prv.nbHabitants*prv.pourcentPaysan*10);
		}
	};
	 // agriculture
	public Culture culture = new Culture() {
		
		@Override
		public int culture(Province prv) {
			//TODO: evolution de la surface agricole
			return (int)( prv.surfaceSol*prv.pourcentChamps*prv.champsRendement);
		}
	};
	 // viande issu de l'elevage,
	public Elevage elevage = new Elevage() {
		
		@Override
		public int elevage(Province prv, float urgence) {
			//TODO: evolution du cheptel
			return (int)( prv.surfaceSol*prv.pourcentPrairie*prv.elevageRendement*(0.5+urgence));
		}
	};

}
