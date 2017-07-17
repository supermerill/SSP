package remi.ssp_basegame.technology;

import com.google.auto.service.AutoService;

import remi.ssp.Plugin;
import remi.ssp.technology.Idea;
import remi.ssp.technology.Technology;
import remi.ssp.technology.Technology.TechnologyFactory;

@AutoService(Plugin.class)
public class BasicTechsPlugin extends Plugin {
	private static final long serialVersionUID = 1L;

	@Override
	public void loadIdeas() {
		//rotation des cultures
		Idea.register(new AgriIdea("agri_RotationCulture",1));
		Idea.register(new AgriIdea("agri_Jachere",1));
		Idea.register(new AgriIdea("agri_Fumier",1));
		Idea.register(new AgriIdea("agri_engraiGoemon",1));
		Idea.register(new AgriIdea("agri_Charrue",1));
		Idea.register(new AgriIdea("agri_moulinEau",1));
		Idea.register(new AgriIdea("agri_moulinVent",1));
	}

	@Override
	public void loadTechnologies() {
		Technology tech_moulin_eau = TechnologyFactory.create("tech_moulin_eau").addIdea(Idea.get("agri_moulinEau")).setReserchable(false).setScienceCost(100).setVisible(false).create();
		Technology tech_moulin_vent = TechnologyFactory.create("tech_moulin_vent").addPrerequisite(tech_moulin_eau).addIdea(Idea.get("agri_moulinVent")).setReserchable(true).setScienceCost(100).setVisible(true).create();
		Technology tech_fumier = TechnologyFactory.create("tech_fumier").addIdea(Idea.get("agri_Fumier")).setReserchable(false).setScienceCost(100).setVisible(false).create();
		Technology tech_fumier_oiseau = TechnologyFactory.create("tech_fumier_oiseau").addPrerequisite(tech_fumier).addIdea(Idea.get("agri_engraiGoemon")).setReserchable(true).setScienceCost(100).setVisible(true).create();
		Technology tech_rotation_culture = TechnologyFactory.create("tech_rotation_culture").addIdea(Idea.get("agri_RotationCulture")).setReserchable(false).setScienceCost(100).setVisible(false).create();
		Technology tech_jachere = TechnologyFactory.create("tech_jachere").addPrerequisite(tech_rotation_culture).addIdea(Idea.get("agri_Jachere")).setReserchable(true).setScienceCost(100).setVisible(true).create();
		Technology agri_Charrue = TechnologyFactory.create("tech_charrue").addIdea(Idea.get("agri_Charrue")).setReserchable(false).setScienceCost(100).setVisible(false).create();
	}


}
