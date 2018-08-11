package i.e.lero.spare.pattern_instantiation;

import java.util.HashMap;

import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import externalUtility.IncidentModelHandler;
import ie.lero.spare.franalyser.utility.SystemModelHandler;

public class ModelsHandler {
	
	private static HashMap<String, IncidentDiagram> incidentModels = new HashMap<String, IncidentDiagram>();
	private static HashMap<String, EnvironmentDiagram> systemModels = new HashMap<String, EnvironmentDiagram>();
	private static IncidentDiagram currentIncidentModel;
	private static EnvironmentDiagram currentSystemModel;
	
	public static HashMap<String, IncidentDiagram> getIncidentModels() {
		return incidentModels;
	}
	
	public static IncidentDiagram getIncidentModel(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		IncidentDiagram model = null;
		
		model = incidentModels.get(filePath);
		
		//if model is not loaded then load it and save it in the map
		if(model == null) {
			model = addIncidentModel(filePath);
		}
		
		return model;
	}
	
	public static IncidentDiagram addIncidentModel(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		IncidentDiagram model = null;
		
		model = IncidentModelHandler.loadIncidentFromFile(filePath);
		incidentModels.put(filePath, model);
		
		if(incidentModels.size() == 1) {
			currentIncidentModel = model;
		}
		
		return model;
		
	}

	public static IncidentDiagram removeIncidentModel(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		return incidentModels.remove(filePath);
		
	}
	
	public static void setIncidentModels(HashMap<String, IncidentDiagram> incidentModels) {
		ModelsHandler.incidentModels = incidentModels;
	}
	
	public static HashMap<String, EnvironmentDiagram> getSystemModels() {
		return systemModels;
	}
	
	public static EnvironmentDiagram getSystemModel(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		EnvironmentDiagram model = null;
		
		model = systemModels.get(filePath);
		
		//if model is not loaded then load it and save it in the map
		if(model == null) {
			model = addSystemModel(filePath);
		}
		
		return model;
	}
	
	public static EnvironmentDiagram addSystemModel(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		EnvironmentDiagram model = null;
		
		model = SystemModelHandler.loadSystemFromFile(filePath);
		systemModels.put(filePath, model);
		
		if(systemModels.size() == 1) {
			currentSystemModel = model;
		}
		
		return model;
		
	}

	public static EnvironmentDiagram removeSystemModel(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		return systemModels.remove(filePath);
		
	}
	
	public static void setSystemModels(HashMap<String, EnvironmentDiagram> systemModels) {
		ModelsHandler.systemModels = systemModels;
	}

	public static IncidentDiagram getCurrentIncidentModel() {
		return currentIncidentModel;
	}

	public static void setCurrentIncidentModel(IncidentDiagram currentIncidentModel) {
		ModelsHandler.currentIncidentModel = currentIncidentModel;
	}

	public static EnvironmentDiagram getCurrentSystemModel() {
		return currentSystemModel;
	}

	public static void setCurrentSystemModel(EnvironmentDiagram currentSystemModel) {
		ModelsHandler.currentSystemModel = currentSystemModel;
	}
	
	

}
