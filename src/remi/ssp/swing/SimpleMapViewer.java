package remi.ssp.swing;

import static remi.ssp.GlobalDefines.logln;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

import remi.ssp.CurrentGame;
import remi.ssp.PluginLoader;
import remi.ssp.map.AnneauCarteV2;
import remi.ssp.politic.Carte;
import remi.ssp.politic.Plot;
import remi.ssp.politic.Province;

@SuppressWarnings("serial")
public class SimpleMapViewer extends JComponent{

	private static PluginLoader manager;
	private Carte map;
	
	public SimpleMapViewer() {
	}

	public static void main(String[] args) {
		JFrame fenetre = new JFrame();
		SimpleMapViewer view = new SimpleMapViewer();
		

		//load algos & static data
		manager = new PluginLoader();
		logln(new File(".").getAbsolutePath());
		manager.loadJars("src");
		List<String> pluginNames = manager.getPluginNames();
		manager.loadStaticData(pluginNames);

		//create map
		view.map = new AnneauCarteV2().createMap(8, 8);
		CurrentGame.get().setMap(view.map);
		
		//create civs
		
		//create pops
		
		//assign starting tech (basic industry & needs) 
		
		fenetre.add(view);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.setSize(800,1000);
		fenetre.setVisible(true);
		
		view.updateSimu();
	}
	
	public void updateSimu(){
//		Nourriture algoN = new Nourriture(){};
		while(true){
			//Economy.ptr.doTurn(map, 30);
//			for(int i=0;i<map.getProvinces().size();i++){
//				for(int j=0;j<map.getProvinces().get(i).size();j++){
//					Province prv = map.getProvinces().get(i).get(j);
//					
////						algoN.getNourritureSemaine(prv);
//				}
//			}
			this.repaint();
			try {
				Thread.sleep(1000);
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
		for(int i=0;i<map.getProvinces().size();i++){
			for(int j=0;j<map.getProvinces().get(i).size();j++){
				Province prv = map.getProvinces().get(i).get(j);
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
					

//					logln("coline:"+prv.relief+", humidite:"+prv.humidite
//							+" , "+Math.min(1, prv.humidite*2)+" : "+Math.max(0,1-prv.humidite*2));
//					logln(altitudeColor.getRed()+":"+altitudeColor.getGreen()+":"+altitudeColor.getBlue()
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
				g.fillOval((i*3*taille)/4, ((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))), taille, taille);
				maxX = Math.max(maxX, (i*3*taille)/4 + taille*2);
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
					g.fillOval((i*3*taille)/4 + taille/2 -tailleRond/2, ((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))) + taille/2 - tailleRond/2, tailleRond, tailleRond);
				}
			}
		}
		for(int x=0;x<map.getProvinces().size();x++){
			for(int y=0;y<map.getProvinces().get(x).size();y++){
				Province prv = map.getProvinces().get(x).get(y);
				int i= prv.centerPlot.getX();
				int j= prv.centerPlot.getY();
				g.setColor(Color.BLACK);
				g.fillOval(maxX - taille/2 -2 + (i*3*taille)/4, ((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))) - taille/2 - 2, taille*2+4, taille*2+4);
				if(x%2==0){
					if(y%2==0) g.setColor(Color.ORANGE);
					else g.setColor(Color.YELLOW);
				}else{
					if(y%2==0) g.setColor(Color.BLUE);
					else g.setColor(Color.CYAN);
				}
				g.fillOval(maxX - taille/2 + (i*3*taille)/4, ((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))) - taille/2, taille*2, taille*2);
			}
		}
		//for each hex, draw it in blue or green
		for(int i=0;i<map.getPlots().size();i++){
			for(int j=0;j<map.getPlots().get(i).size();j++){
				Plot plot = map.getPlots().get(i).get(j);
				Color mix = Color.BLACK;
				if(plot != null){
					Province prv = plot.getProvince();
					Color altitudeColor = Color.BLACK; //new Color(0, 1-prv.relief,0);
					Color grass = Color.GREEN;
					Color desertColor = Color.YELLOW;
					
					mix = grass;
					mix = mix(mix, Math.min(1, prv.humidite*2), 
							desertColor, Math.max(0,1-prv.humidite*2));
					mix = mix(mix, 1-prv.relief,
							altitudeColor, prv.relief);
					
					
				}
				g.setColor(mix);
				g.fillOval(1+maxX+(i*3*taille)/4, 1+((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))), taille-2, taille-2);
			}
		}
		for(int x=0;x<map.getProvinces().size();x++){
			for(int y=0;y<map.getProvinces().get(x).size();y++){
				if(x%2==0){
					if(y%2==0) g.setColor(Color.ORANGE);
					else g.setColor(Color.YELLOW);
				}else{
					if(y%2==0) g.setColor(Color.BLUE);
					else g.setColor(Color.CYAN);
				}
				Province prv = map.getProvinces().get(x).get(y);
				int i= prv.centerPlot.getX();
				int j= prv.centerPlot.getY();
				g.fillOval(maxX + taille/4 + (i*3*taille)/4, ((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))) + taille/4, taille/2, taille/2);
			}
		}
		for(int i=0;i<map.getPlots().size();i++){
			for(int j=0;j<map.getPlots().get(i).size();j++){
				Plot plot = map.getPlots().get(i).get(j);
				if(plot != null){
					for(int n=0;n<plot.around.length;n++){
						if(plot.around[n] != null){
							g.setColor(Color.RED);
							g.drawLine(maxX + (i*3*taille)/4 + taille/2, 1+((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))) + taille/2, 
									maxX + (plot.around[n].getX()*3*taille)/4 + taille/2, 
										1+((plot.around[n].getX()%2==0)?(plot.around[n].getY()*(taille-1)):(taille/2+plot.around[n].getY()*(taille-1))) + taille/2);
						}
					}
				}
			}
		}

//		for(int x=0;x<map.getProvinces().size();x++){
//			for(int y=0;y<map.getProvinces().get(x).size();y++){
//				Province prv = map.getProvinces().get(x).get(y);
//				int i= prv.centerPlot.getX();
//				int j= prv.centerPlot.getY();
//				g.setColor(Color.RED);
//				for(int n=0;n<prv.proche.length;n++){
//					if(prv.proche[n] != null){
//						g.setColor(Color.RED);
//						g.drawLine(maxX + (i*3*taille)/4 + taille/2, 1+((i%2==0)?(j*(taille-1)):(taille/2+j*(taille-1))) + taille/2, 
//								maxX + (prv.proche[n].centerPlot.getX()*3*taille)/4 + taille/2, 
//									1+((prv.proche[n].centerPlot.getX()%2==0)?(prv.proche[n].centerPlot.getY()*(taille-1)):(taille/2+prv.proche[n].centerPlot.getY()*(taille-1))) + taille/2);
//					}
//				}
//			}
//		}
		
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
