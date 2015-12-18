package remi.ssp.swing;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

import remi.ssp.Carte;
import remi.ssp.Province;
import remi.ssp.map.AnneauCarte;

@SuppressWarnings("serial")
public class SimpleMapViewer extends JComponent{

	private Carte map;
	
	public SimpleMapViewer() {
	}

	public static void main(String[] args) {
		JFrame fenetre = new JFrame();
		SimpleMapViewer view = new SimpleMapViewer();
		
		view.map = new AnneauCarte().createMap(30, 30);

		fenetre.add(view);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.setSize(800,1000);
		fenetre.setVisible(true);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintInside(g);
	}

	private void paintInside(Graphics g) {
//		g.setColor(Color.RED);
//		g.fillOval(0, 0, 400, 400);
		
		int taille = 10;
		
		//for each hex, draw it in blue or green
		for(int i=0;i<map.provinces.size();i++){
			for(int j=0;j<map.provinces.get(i).size();j++){
				Province prv = map.provinces.get(i).get(j);
				if(prv.surfaceSol > 10){

					Color altitudeColor = Color.BLACK; //new Color(0, 1-prv.relief,0);
					Color grass = Color.GREEN;
					Color desertColor = Color.YELLOW;
//					Color mix = altitudeColor;
					Color mix = grass;
					mix = mix(mix, Math.min(1, prv.humidite*2), 
							desertColor, Math.max(0,1-prv.humidite*2));
					mix = mix(mix, 1-prv.relief,
							altitudeColor, prv.relief);
					

//					System.out.println("coline:"+prv.relief+", humidite:"+prv.humidite
//							+" , "+Math.min(1, prv.humidite*2)+" : "+Math.max(0,1-prv.humidite*2));
//					System.out.println(altitudeColor.getRed()+":"+altitudeColor.getGreen()+":"+altitudeColor.getBlue()
//							+" :: "+mix.getRed()+":"+mix.getGreen()+":"+mix.getBlue());
					if(prv.relief>0.7){
						g.setColor(Color.GRAY);
					}else if(prv.relief>0.3){
						g.setColor(Color.ORANGE);
					}else{
						g.setColor(Color.GREEN);
					}
					g.setColor(mix);
				}else{
					g.setColor(Color.BLUE);
				}
				g.fillOval((i*3*taille)/4, ((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))), taille, taille);
			}
		}
		
	}
	
	public Color mix(Color c1, float percent1, Color c2, float percent2){
		return new Color((int)((c1.getRed()*percent1 + c2.getRed()*percent2)/(percent1+percent2))
				,(int)((c1.getGreen()*percent1 + c2.getGreen()*percent2)/(percent1+percent2))
				,(int)((c1.getBlue()*percent1 + c2.getBlue()*percent2)/(percent1+percent2)));
	}
	
}
