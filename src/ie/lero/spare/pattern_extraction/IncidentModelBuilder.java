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
	
	Activity a1 = instance.createActivity();
	Activity a2 = instance.createActivity();
	Activity a3 = instance.createActivity();
	Actor actor1 = instance.createActor();
	Actor actor2 = instance.createActor();
	Asset asset1 = instance.createAsset();
	Asset asset2 = instance.createAsset();
	Asset asset3 = instance.createAsset();
	
	//names
	actor1.setName("act1");
	actor2.setName("act2");

	asset1.setName("ast1");
	asset2.setName("ast2");
	asset3.setName("ast3");
	
	a1.setName("activity1");
	a2.setName("activity2");
	a3.setName("activity3");
	
	//set activities sequence
	a2.getNextActivities().add(a3);
	a2.getPreviousActivities().add(a1);
	
	//set action
	a1.setSystemAction("enter");
	a2.setSystemAction("enter");
	a3.setSystemAction("enter");
	
	//set type
	a1.setType(ActivityType.PHYSICAL);
	a2.setType(ActivityType.PHYSICAL);
	a3.setType(ActivityType.PHYSICAL);
	
	a1.setInitiator((ActivityInitiator)actor1);
	a2.setInitiator(actor2);
	a3.setInitiator(actor1);
	
	//a2.mergeActivities();
	

	System.out.println(asset1.equals(asset2));
	
	}
	
	

}
