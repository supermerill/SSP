package remi.ssp.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import remi.ssp.CurrentGame;
import remi.ssp.PluginLoader;
import remi.ssp.algorithmes.Economy;
import remi.ssp.politic.Carte;
import remi.ssp.politic.Plot;
import remi.ssp.politic.Province;
import remi.ssp_basegame.BaseSpawner;

@SuppressWarnings("serial")
public class SimpleMapViewerV3 extends JComponent{

	private static PluginLoader manager;
	private Carte map;
	
	public SimpleMapViewerV3() {
	}

	public static void main(String[] args) {
		JFrame fenetre = new JFrame();
		SimpleMapViewerV3 view = new SimpleMapViewerV3();
		

		//load algos & static data
		manager = new PluginLoader();
		System.out.println(new File(".").getAbsolutePath());
		manager.loadJars("src");
		List<String> pluginNames = manager.getPluginNames();
		manager.loadStaticData(pluginNames);

		BaseSpawner spawner = new BaseSpawner();
		//create map
//		view.map = new FlatCarteV3().createMap(10, 10);
//		CurrentGame.map = view.map;
		spawner.createMap();
		view.map = CurrentGame.map;
		
		//create civs
		spawner.createCivs();
		
		//create pops
		spawner.createPop();
		
		//assign starting tech (basic industry & needs) 
		
		fenetre.add(view);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.setSize(80,100);
		fenetre.setVisible(true);
		
		view.updateSimu();
	}
	
	public void updateSimu(){
//		Nourriture algoN = new Nourriture(){};
		while(true){
			System.out.println("================================== start turn =======================================");
			Economy.ptr.doTurn(map, 10);
			System.out.println("================================= economy done ======================================");
			this.repaint();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintInside(g);
	}

	private void paintInside(Graphics g) {
//		g.setColor(Color.RED);
//		g.fillOval(0, 0, 400, 400);
		
		int taille = 20;
		
		int maxX = 0;
		
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
//					if(prv.relief>0.7){
//						g.setColor(Color.GRAY);
//					}else if(prv.relief>0.3){
//						g.setColor(Color.ORANGE);
//					}else{
//						g.setColor(Color.GREEN);
//					}
					g.setColor(mix);
				}else{
					g.setColor(Color.BLUE);
				}
				g.fillOval(((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))), (j*3*taille)/4, taille, taille);
				maxX = Math.max(maxX, ((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) + taille*2);
				if(prv.getNbAdult()>0){
					g.setColor(Color.RED);
					int tailleRond = 1;
					if(prv.getNbAdult()>100000){
						tailleRond = 8;
					}else if(prv.getNbAdult()>25000){
						tailleRond = 7;
					}else if(prv.getNbAdult()>5000){
						tailleRond = 6;
					}else if(prv.getNbAdult()>1000){
						tailleRond = 5;
					}else if(prv.getNbAdult()>250){
						tailleRond = 4;
					}else if(prv.getNbAdult()>50){
						tailleRond = 3;
					}else if(prv.getNbAdult()>10){
						tailleRond = 2;
					}
					g.setColor(Color.RED);
					g.fillOval(((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) + taille/2 - tailleRond/2, (j*3*taille)/4 + taille/2 -tailleRond/2, tailleRond, tailleRond);
				}
			}
		}
		for(int x=0;x<map.provinces.size();x++){
			for(int y=0;y<map.provinces.get(x).size();y++){
				Province prv = map.provinces.get(x).get(y);
				int i= prv.centerPlot.getX();
				int j= prv.centerPlot.getY();
				g.setColor(Color.BLACK);
				g.fillOval(maxX + ((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) - taille/2 - 2, (j*3*taille)/4 - taille/2 -2 , taille*2+4, taille*2+4);
				if(x%2==0){
					if(y%2==0) g.setColor(Color.ORANGE);
					else g.setColor(Color.YELLOW);
				}else{
					if(y%2==0) g.setColor(Color.BLUE);
					else g.setColor(Color.CYAN);
				}
				g.fillOval(maxX  + ((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) - taille/2, (j*3*taille)/4 - taille/2, taille*2, taille*2);
			}
		}
		//for each hex, draw it in blue or green
		for(int i=0;i<map.plots.size();i++){
			for(int j=0;j<map.plots.get(i).size();j++){
				Plot plot = map.plots.get(i).get(j);
				Color mix = Color.BLACK;
				if(plot != null){
					Province prv = plot.getProvince();
					if(prv.surfaceSol > 10){
						Color altitudeColor = Color.BLACK; //new Color(0, 1-prv.relief,0);
						Color grass = Color.GREEN;
						Color desertColor = Color.YELLOW;
						
						mix = grass;
						mix = mix(mix, Math.min(1, prv.humidite*2), 
								desertColor, Math.max(0,1-prv.humidite*2));
						mix = mix(mix, 1-prv.relief,
								altitudeColor, prv.relief);
						g.setColor(mix);
					}else{
						g.setColor(Color.BLUE);
					}
				}else{
					g.setColor(Color.BLACK);
				}
				g.fillOval(maxX + 1+((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))), 1+(j*3*taille)/4, taille-2, taille-2);
			}
		}
		//???
//		for(int x=0;x<map.provinces.size();x++){
//			for(int y=0;y<map.provinces.get(x).size();y++){
//				if(x%2==0){
//					if(y%2==0) g.setColor(Color.ORANGE);
//					else g.setColor(Color.YELLOW);
//				}else{
//					if(y%2==0) g.setColor(Color.BLUE);
//					else g.setColor(Color.CYAN);
//				}
//				Province prv = map.provinces.get(x).get(y);
//				int i= prv.centerPlot.getX();
//				int j= prv.centerPlot.getY();
//				g.fillOval(maxX + ((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) + taille/4, taille/4 + (j*3*taille)/4, taille/2, taille/2);
//			}
//		}
		for(int i=0;i<map.plots.size();i++){
			for(int j=0;j<map.plots.get(i).size();j++){
				Plot plot = map.plots.get(i).get(j);
				if(plot != null){
					for(int n=0;n<plot.around.length;n++){
						if(plot.around[n] != null){
							g.setColor(Color.RED);
							g.drawLine(maxX + 1 +((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) + taille/2, (j*3*taille)/4 + taille/2, 
									maxX + 1 + ((plot.around[n].getY()%2==0)?(plot.around[n].getX()*(taille-1)):(taille/2+plot.around[n].getX()*(taille-1))) + taille/2, 
									(plot.around[n].getY()*3*taille)/4 + taille/2);
						}
					}
				}
			}
		}

		for(int x=0;x<map.provinces.size();x++){
			for(int y=0;y<map.provinces.get(x).size();y++){
				Province prv = map.provinces.get(x).get(y);
				int i= prv.centerPlot.getX();
				int j= prv.centerPlot.getY();
				g.setColor(Color.RED);
				for(int n=0;n<prv.proche.length;n++){
					if(prv.proche[n] != null){
						g.setColor(Color.RED);
//						g.drawLine(maxX + 1+((j%2==0)?(i*(taille-1)):(taille/2+i*(taille-1))) + taille/2, (j*3*taille)/4 + taille/2, 
//								maxX + 1+((prv.proche[n].centerPlot.getY()%2==0)?(prv.proche[n].centerPlot.getX()*(taille-1)):(taille/2+prv.proche[n].centerPlot.getX()*(taille-1))) + taille/2, 
//								(prv.proche[n].centerPlot.getY()*3*taille)/4 + taille/2);
					}
				}
			}
		}
		
	}

	public Color mix(Color c1, float percent1, Color c2, float percent2){
		return new Color((int)((c1.getRed()*percent1 + c2.getRed()*percent2)/(percent1+percent2))
				,(int)((c1.getGreen()*percent1 + c2.getGreen()*percent2)/(percent1+percent2))
				,(int)((c1.getBlue()*percent1 + c2.getBlue()*percent2)/(percent1+percent2)));
	}
	public Color mean(Color c1, float percent1, Color c2, float percent2){
		return new Color((int)((c1.getRed()*percent1/(percent1+percent2) + c2.getRed()*percent2)/(percent1+percent2))
				,(int)((c1.getGreen()*percent1/(percent1+percent2) + c2.getGreen()*percent2)/(percent1+percent2))
				,(int)((c1.getBlue()*percent1/(percent1+percent2) + c2.getBlue()*percent2)/(percent1+percent2)));
	}
	
}
