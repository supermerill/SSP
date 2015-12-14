package remi.ssp.map;

import remi.ssp.Carte;

public interface MapFactory {
	public Carte createMap(int width, int height);
}
