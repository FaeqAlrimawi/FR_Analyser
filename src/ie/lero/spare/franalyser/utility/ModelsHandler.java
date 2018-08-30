package ie.lero.spare.franalyser.utility;

import java.util.HashMap;
import java.util.Map;

import cyberPhysical_Incident.ActivityPattern;
import cyberPhysical_Incident.IncidentDiagram;
import environment.EnvironmentDiagram;
import externalUtility.IncidentModelHandler;

public class ModelsHandler {
	
	//the string is the filePath to the model
	private static Map<String, IncidentDiagram> incidentModels = new HashMap<String, IncidentDiagram>();
	private static Map<String, EnvironmentDiagram> systemModels = new HashMap<String, EnvironmentDiagram>();
	private static IncidentDiagram currentIncidentModel;
	private static EnvironmentDiagram currentSystemModel;
	private static Map<String, ActivityPattern> activityPatterns = new HashMap<String, ActivityPattern>();
	private static ActivityPattern currentActivityPattern;
	
	public static Map<String, IncidentDiagram> getIncidentModels() {
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
	
	public static void setIncidentModels(Map<String, IncidentDiagram> newIncidentModels) {
		incidentModels = newIncidentModels;
	}
	
	public static Map<String, EnvironmentDiagram> getSystemModels() {
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
	
	public static Map<String, ActivityPattern> getActivityPatterns() {
		return activityPatterns;
	}
	
	public static ActivityPattern getActivityPattern(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		ActivityPattern pattern = null;
		
		pattern = activityPatterns.get(filePath);
		
		//if model is not loaded then load it and save it in the map
		if(pattern == null) {
			pattern = addActivityPattern(filePath);
		}
		
		return pattern;
	}
	
	public static ActivityPattern addActivityPattern(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		ActivityPattern pattern = null;
		
		pattern = ActivityPatternModelHandler.loadActivityPatternFromFile(filePath);
		activityPatterns.put(filePath, pattern);
		
		if(activityPatterns.size() == 1) {
			currentActivityPattern = pattern;
		}
		
		return pattern;
		
	}

	public static ActivityPattern removeActivityPattern(String filePath) {
		
		if(filePath == null) {
			return null;
		}
		
		return activityPatterns.remove(filePath);
		
	}
	
	public static void setActivityPatterns(Map<String, ActivityPattern> activityPatterns) {
		ModelsHandler.activityPatterns = activityPatterns;
	}
	
	public static void setSystemModels(Map<String, EnvironmentDiagram> systemModels) {
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
	
	public static ActivityPattern getCurrentActivityPattern() {
		return currentActivityPattern;
	}

	public static void setCurrentSystemModel(EnvironmentDiagram currentSystemModel) {
		ModelsHandler.currentSystemModel = currentSystemModel;
	}
	
	public static void setCurrentActivityPattern(ActivityPattern newCurrentActivityPattern) {
		ModelsHandler.currentActivityPattern = newCurrentActivityPattern;
	}
	
	

}
