package ie.lero.spare.pattern_extraction;

import org.eclipse.emf.common.util.EList;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityInitiator;
import cyberPhysical_Incident.ActivityType;
import cyberPhysical_Incident.Actor;
import cyberPhysical_Incident.Asset;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.CPIFactory;
import cyberPhysical_Incident.Connection;
import cyberPhysical_Incident.Entity;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.Postcondition;
import cyberPhysical_Incident.Precondition;

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
	int numOfActors = 3;
	int numOfAssets = 6;
	
	Activity [] activities = new Activity[numOfActivities];
	Actor [] actors = new Actor[numOfActors];
	Asset [] assets = new Asset[numOfAssets];
	IncidentDiagram inc = instance.createIncidentDiagram();
	
	//create actors
	for(int i=0;i<numOfActors;i++) {
		actors[i] = instance.createActor();
		actors[i].setName("actor"+i);
		inc.getActor().add(actors[i]);
	}

	//create assets
	for(int i=0;i<numOfAssets;i++) {
		assets[i] = instance.createAsset();
		assets[i].setName("asset"+i);
		inc.getAsset().add(assets[i]);
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
	
	//create connections
	Connection con1 = instance.createConnection();

	con1.setEntity1(assets[1]);
	con1.setEntity2(assets[3]);
	inc.getConnection().add(con1);
	
	//set next sequence
	for(int i=0;i<numOfActivities-1;i++) {
		activities[i].getNextActivities().add(activities[i+1]);
	}
	
	//set previous sequence
	for (int i = 1; i < numOfActivities; i++) {
		activities[i].getPreviousActivities().add(activities[i - 1]);
	}

/*	System.out.println("Incident activity sequence:");
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
	}*/
	
	//test conditions
	Precondition pre = instance.createPrecondition();
	Postcondition post = instance.createPostcondition();
	Precondition pre2 = instance.createPrecondition();
	Postcondition post2 = instance.createPostcondition();
	
	BigraphExpression preExp1 = instance.createBigraphExpression();
	BigraphExpression preExp2 = instance.createBigraphExpression();
	BigraphExpression postExp1 = instance.createBigraphExpression();
	BigraphExpression postExp2 = instance.createBigraphExpression();

	Entity ent1 = instance.createEntity();
	Entity ent2 = instance.createEntity();
	Entity ent3 = instance.createEntity();
	Entity ent4 = instance.createEntity();
	Entity ent5 = instance.createEntity();
	Entity ent6 = instance.createEntity();
	Entity ent7 = instance.createEntity();
	
	ent1.setName(assets[0].getName());
	ent2.setName(assets[1].getName());
	ent5.setName(assets[2].getName());
	ent6.setName(assets[3].getName());
	
	//initiator
	ent3.setName(actors[0].getName());
	ent4.setName(actors[0].getName());
	ent7.setName(actors[0].getName());

	ent1.getEntity().add(ent3); //ent1 contains ent3 & ent2 contains ent4 (which has the same name as ent3). This expresses entity movement
	//ent5.getEntity().add(ent3); 
	ent2.getEntity().add(ent4);
	
	preExp1.getEntity().add(ent1);
	postExp1.getEntity().add(ent2);
	
	ent6.getEntity().add(ent7); //ent6 contains ent7 (which is the same as ent3 and ent4)
	
	preExp2.getEntity().add(ent5);
	postExp2.getEntity().add(ent6);
	
	pre.setExpression(preExp1);
	post.setExpression(postExp1);
	
	pre2.setExpression(preExp2);
	post2.setExpression(postExp2);
	
	activities[0].setPrecondition(pre);
	activities[0].setPostcondition(post);
	activities[0].getTargetedAssets().add(assets[1]);
	
	activities[1].setPrecondition(pre2);
	activities[1].setPostcondition(post2);
	activities[1].getTargetedAssets().add(assets[3]);
	
	//takes only the first two activities and checks if they can be merged
	Activity act = inc.mergeActivities(inc.getActivity().get(0), inc.getActivity().get(1)); 
	
	printActivityInfo(act);
	
	System.out.println("Incident activity sequence:");
	for(Activity ac: inc.getActivity()) {
		System.out.print(ac.getName()+"->");
	}
	//System.out.println("\n\ninitiator moved? "+activities[0].isInitiatorMoved());
	//System.out.println(preExp1.getContainer(ent2.getName()));
	
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
			
			//print condition
			Precondition pre = act.getPrecondition();
			Postcondition post = act.getPostcondition();
			
			if(pre != null) {
				System.out.println("pre: " + ((BigraphExpression)pre.getExpression()).getEntity().get(0).getName());
			}
			
			if(post != null) {
				System.out.println("post: " + ((BigraphExpression)post.getExpression()).getEntity().get(0).getName());
			}
			
		} else {
			System.out.println("no merge");
		}
		//a2.mergeActivities();
	
		}
	
}
