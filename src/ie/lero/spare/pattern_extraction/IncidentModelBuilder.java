package ie.lero.spare.pattern_extraction;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityInitiator;
import cyberPhysical_Incident.ActivityType;
import cyberPhysical_Incident.Actor;
import cyberPhysical_Incident.Asset;
import cyberPhysical_Incident.CPIFactory;
import cyberPhysical_Incident.IncidentDiagram;

public class IncidentModelBuilder {
	
	private CPIFactory instance;
	private String fileName;
	private IncidentDiagram incidentInstance;
	
	
	public IncidentModelBuilder() {
		instance = CPIFactory.eINSTANCE;
		setIncidentInstance(instance.createIncidentDiagram());
	}
	
	public IncidentModelBuilder(String fileName) {
		this();
		setFileName(fileName);
	}

	public IncidentDiagram buildIncidentFromFile() {
		// generate EPackages from schemas
	/*	XSDEcoreBuilder xsdEcoreBuilder = new XSDEcoreBuilder();
		Collection generatedPackages = xsdEcoreBuilder.generate(schemaURI);

		// register the packages loaded from XSD
		for (EObject generatedEObject : generatedPackages) {
		    if (generatedEObject instanceof EPackage) {
		        EPackage generatedPackage = (EPackage) generatedEObject;
		        EPackage.Registry.INSTANCE.put(generatedPackage.getNsURI(),
		            generatedPackage);
		    }
		}

		// add file extension to registry
		ResourceFactoryRegistryImpl.INSTANCE.getExtensionToFactoryMap()
		    .put(MY_FILE_EXTENSION, new GenericXMLResourceFactoryImpl());*/
		
		return incidentInstance;
	}
	
	public IncidentDiagram buildIncidentFromFile(String fileName) {
		setFileName(fileName);
		return buildIncidentFromFile();
	}
	

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public IncidentDiagram getIncidentInstance() {
		return incidentInstance;
	}

	public void setIncidentInstance(IncidentDiagram incidentInstance) {
		this.incidentInstance = incidentInstance;
	}
	
	
	public static void main(String[]args) {
	CPIFactory instance = CPIFactory.eINSTANCE;
	
	int numOfActivities = 5;
	int numOfActors = 2;
	int numOfAssets = 2;
	
	Activity [] activities = new Activity[numOfActivities];
	Actor [] actors = new Actor[numOfActors];
	Asset [] assets = new Asset[numOfAssets];
	IncidentDiagram inc = instance.createIncidentDiagram();
	
	//create actors
	for(int i=0;i<numOfActors;i++) {
		actors[i] = instance.createActor();
		actors[i].setName("actor"+i);
	}

	//create assets
	for(int i=0;i<numOfAssets;i++) {
		assets[i] = instance.createAsset();
		assets[i].setName("asset"+i);
	}
	
	//create activities
	for(int i=0;i<numOfActivities;i++) {
		activities[i] = instance.createActivity();
		activities[i].setName("activity"+i);
		activities[i].setSystemAction("enter");
		activities[i].setType(ActivityType.PHYSICAL);
		activities[i].setInitiator(actors[0]);
		inc.getActivity().add(activities[i]);
	}
	
	//set next sequence
	for(int i=0;i<numOfActivities-1;i++) {
		activities[i].getNextActivities().add(activities[i+1]);
	}
	
	//set previous sequence
	for (int i = 1; i < numOfActivities; i++) {
		activities[i].getPreviousActivities().add(activities[i - 1]);
	}
	
	System.out.println("Incident activity sequence:");
	for(Activity ac: inc.getActivity()) {
		System.out.print(ac.getName()+"->");
	}
	
	Activity act = inc.mergeActivities(inc.getActivity().get(2), inc.getActivity().get(3));
	//Activity act2 = inc.mergeActivities(inc.getActivity().get(0), inc.getActivity().get(1));
	
	printActivityInfo(act);
	//printActivityInfo(act2);
	
	System.out.println("Incident activity sequence:");
	for(Activity ac: inc.getActivity()) {
		System.out.print(ac.getName()+"->");
	}
	
}
	
	static void printActivityInfo(Activity act) {
		if(act != null) {
			System.out.println("\n\nmerged activity name: "+act.getName());
			
			for(Activity ac : act.getPreviousActivities()) {
				System.out.println("previous activity name: "+ac.getName());
			}
			
			for(Activity ac : act.getNextActivities()) {
				System.out.println("next activity name: "+ac.getName());
			}
		} else {
			System.out.println("no merge");
		}
		//a2.mergeActivities();
	
		}
	

}
