package ie.lero.spare.pattern_extraction;

import java.util.List;
import java.util.Map.Entry;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityPattern;
import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.ActivityPatternModelHandler;
import ie.lero.spare.franalyser.utility.IncidentModelHandler;
import ie.lero.spare.franalyser.utility.ModelsHandler;

public class IncidentPatternExtractor {
	
	
	public IncidentPatternExtractor() {
		
	}
	
	public IncidentDiagram extract(IncidentDiagram incidentModel) {
		
		String systemFileName = "D:/runtime-EclipseApplication/Scenarios/Scenario1/Research_centre.cps";
		
		if(incidentModel == null) {
			return null;
		}
		
		IncidentDiagram abstractedModel = null;
		
		//create a copy
		/*String tmpFileName = "tmpModel.cpi";
		IncidentModelHandler.SaveIncidentToFile(incidentModel, tmpFileName);
		abstractedModel = IncidentModelHandler.loadIncidentFromFile(tmpFileName);
		*/
		/*//remove tmp (i.e. the copy) file
		File tmpFile = new File(tmpFileName);
		
		if(tmpFile.exists()) {
			tmpFile.delete();
		}*/
		
	
	//	System.out.println("test: "+abstractedModel.getInitialActivity().getConnectionChangesBetweenEntities("offender", "hallway"));
		//abstractedModel.abstractActivities();
		
		//abstract entities
		
		/*for(Asset ast : systemModel.getAsset()) {
			Asset tmp = ast.abstractType();
			
			if(tmp != null) {
				System.out.println("o:"+ ast.getClass().getSimpleName() +"  a: " + tmp.getClass().getSimpleName()); 
			}else {
				System.out.println("o:"+ ast.getClass().getSimpleName() +"  abstracted asset is NULL");		
			}
			
		}*/
//		Random rand = new Random();
//		
//		int tries = 50;
		
		/*for(int i = 0;i<tries;i++) {
		Asset original  = systemModel.getAsset().get(rand.nextInt(systemModel.getAsset().size()));
		Asset abstracted = original.abstractAsset();
		
		System.out.println("Original Asset: "+original + 
				"\nContainedAssets ["+original.getContainedAssets().size()+"]: "+original.getContainedAssets() +
				"\nConnections ["+original.getConnections().size()+"]: "+original.getConnections() );
		
		System.out.println("Abstracted Asset: "+abstracted+ 
				"\nContainedAssets ["+abstracted.getContainedAssets().size()+"]: "+abstracted.getContainedAssets() +
				"\nConnections ["+abstracted.getConnections().size()+"]: "+abstracted.getConnections());
		System.out.println();
		}*/
		
		//status: abstraction is done for the basic attributes (type, control, properties) and contained assets
		//next is to implement connections abstraction in assets
		
	
//		abstractedModel.abstractActivities();
//		abstractedModel.abstractEntities(systemModel);

		EnvironmentDiagram systemModel = ModelsHandler.getSystemModel(systemFileName);
		
		if(systemModel == null) {
			System.out.println("system model is NULL");
		}
		
		String patternFileName = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/collectDataPattern.cpi";
		
		ActivityPattern ptrCollectData = ModelsHandler.addActivityPattern(patternFileName);
		
		Activity actInit = incidentModel.getInitialScene().getNextScenes().get(0).getInitialActivity();
		Activity activity1 = actInit.getNextActivities().get(0);
//		Activity activity2 = actInit.getNextActivities().get(0).getNextActivities().get(0);
		System.out.println(actInit.getName()+" "+activity1.getName() );
		
		Activity result = ptrCollectData.applyTo(actInit, activity1);
		
		if(result != null) {
			System.out.println(result.getName());
		} else {
			System.out.println("result is null");
		}
		
		
		////Create an abstract model\\\\
/*		abstractedModel = incidentModel.createAbstractIncident(systemModel);
		
		
		//or
//		incidentModel.setSystemModel(systemModel);
//		abstractedModel = incidentModel.createAbstractIncident();
		
			
		if(abstractedModel != null) {
			IncidentModelHandler.SaveIncidentToFile(abstractedModel, "D:/runtime-EclipseApplication_design/Examples/Scenario1_B/abstractIncident_steal.cpi");
			
			System.out.println("num of activities in original = "+ incidentModel.getActivity().size());
			System.out.println("num of activities in abstract = "+ abstractedModel.getActivity().size());
			
			System.out.println();
			
			int index = 0;
			 for(Entry<Activity, List<Activity>> entry : incidentModel.getMergedActivities().entrySet()) {
			 System.out.println("new-Activity: "+entry.getKey().getName() + 
					 "\nmerged-activities: "+entry.getValue().get(0).getName() + 
					 " & " +entry.getValue().get(1).getName() +
					 "\nRule: [" + getMergeRuleName(incidentModel.getMergedRules().get(index)) + "]");
			 index++;
			 }
		

		} else {
			System.out.println("Abstract model = null");
		}*/
		
		
		return abstractedModel;
	}
	
	protected String getMergeRuleName(int rule) {
	
		switch(rule) {
		 case IncidentDiagram.COLLECTDATA_MERGE_RULE:
			 return "Collect Data";
		 case IncidentDiagram.CONTAINMENT_MERGE_RULE:
			 return "Containment";
		 case IncidentDiagram.CONNECTIVITY_MERGE_RULE:
			 return "Connectivity";
			 default:
				 return "";
		 }
	}
	
	public IncidentDiagram extract(String fileName) {
		
		IncidentDiagram model = ModelsHandler.getIncidentModel(fileName);
		
		return extract(model);
	}

	public static void main(String[] args){
		
		IncidentPatternExtractor extractor = new IncidentPatternExtractor();	
		String fileName = "D:/runtime-EclipseApplication_design/Examples/Scenario1_B/incidentInstance_steal.cpi";
		
		extractor.extract(fileName);
		
	}
}
