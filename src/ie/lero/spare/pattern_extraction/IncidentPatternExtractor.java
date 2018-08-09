package ie.lero.spare.pattern_extraction;

import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.IncidentModelHandler;
import ie.lero.spare.franalyser.utility.SystemModelHandler;

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

		EnvironmentDiagram systemModel = SystemModelHandler.loadSystemFromFile(systemFileName);
		
		if(systemModel == null) {
			System.out.println("system model is NULL");
		}
		
		////Create an abstract model\\\\
		abstractedModel = incidentModel.createAbstractIncident(systemModel);
		
		//or
//		incidentModel.setSystemModel(systemModel);
//		abstractedModel = incidentModel.createAbstractIncident();
		
		
		IncidentModelHandler.SaveIncidentToFile(abstractedModel, "D:/runtime-EclipseApplication/Scenarios/Scenario1/inc_abs.cpi");
		
		System.out.println("num of activities in original = "+ incidentModel.getActivity().size());
		System.out.println("num of activities in abstract = "+ abstractedModel.getActivity().size());

		return abstractedModel;
	}
	
	public IncidentDiagram extract(String fileName) {
		
		IncidentDiagram model = IncidentModelHandler.loadIncidentFromFile(fileName);
		
		return extract(model);
	}

	public static void main(String[] args){
		
		IncidentPatternExtractor extractor = new IncidentPatternExtractor();	
		String fileName = "D:/runtime-EclipseApplication/Scenarios/Scenario1/incidentInstance.cpi";
		
		extractor.extract(fileName);
		
	}
}
