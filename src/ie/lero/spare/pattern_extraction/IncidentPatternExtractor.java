package ie.lero.spare.pattern_extraction;

import cyberPhysical_Incident.IncidentDiagram;
import ie.lero.spare.franalyser.utility.IncidentModelHandler;

public class IncidentPatternExtractor {
	
	
	public IncidentPatternExtractor() {
		
	}
	
	public IncidentDiagram extract(IncidentDiagram incidentModel) {
		
		if(incidentModel == null) {
			return null;
		}
		
		//the process for extracting/abstracting a model
		//incidentModel.mer
		IncidentDiagram abstractedModel = null;
		
		
		
		return abstractedModel;
	}
	
	public IncidentDiagram extract(String fileName) {
		
		IncidentDiagram model = IncidentModelHandler.loadIncidentFromFile(fileName);
		
		return extract(model);
	}

}
