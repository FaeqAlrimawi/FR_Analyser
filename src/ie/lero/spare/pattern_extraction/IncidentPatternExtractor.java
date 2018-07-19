package ie.lero.spare.pattern_extraction;

import java.io.File;

import cyberPhysical_Incident.IncidentDiagram;
import ie.lero.spare.franalyser.utility.IncidentModelHandler;

public class IncidentPatternExtractor {
	
	
	public IncidentPatternExtractor() {
		
	}
	
	public IncidentDiagram extract(IncidentDiagram incidentModel) {
		
		if(incidentModel == null) {
			return null;
		}
		
		IncidentDiagram abstractedModel = null;
		
		//create a copy
		String tmpFileName = "tmpModel.cpi";
		IncidentModelHandler.SaveIncidentToFile(incidentModel, tmpFileName);
		abstractedModel = IncidentModelHandler.loadIncidentFromFile(tmpFileName);
		
		//remove tmp file
		File tmpFile = new File(tmpFileName);
		
		if(tmpFile.exists()) {
			tmpFile.delete();
		}
		
	
		System.out.println("num of acts= "+ abstractedModel.getActivity().size());
	
	//	System.out.println("test: "+abstractedModel.getInitialActivity().getConnectionChangesBetweenEntities("offender", "hallway"));
		abstractedModel.abstractActivities();
		
		System.out.println("num of acts= "+ abstractedModel.getActivity().size());
		
		System.out.println("exploited: " + abstractedModel.getInitialActivity().getExploitedAssets().get(0).getName());

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
