package ie.lero.spare.pattern_extraction;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.GenericXMLResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityType;
import cyberPhysical_Incident.Actor;
import cyberPhysical_Incident.Asset;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.CPIFactory;
import cyberPhysical_Incident.Connection;
import cyberPhysical_Incident.Connectivity;
import cyberPhysical_Incident.Entity;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.Postcondition;
import cyberPhysical_Incident.Precondition;

public class IncidentModelBuilder {
	
	//private CPIFactory instance;
	private String fileName;
	private IncidentDiagram incidentInstance;
	private static CPIFactory instance = CPIFactory.eINSTANCE;
	
	public IncidentModelBuilder() {
		instance = CPIFactory.eINSTANCE;
		setIncidentInstance(instance.createIncidentDiagram());
	}
	
	public IncidentModelBuilder(String fileName) {
		this();
		setFileName(fileName);
	}

	public static IncidentDiagram buildIncidentFromFile() {
		
	// generate EPackages from schemas	
	XSDEcoreBuilder xsdEcoreBuilder = new XSDEcoreBuilder();
		Collection<EObject> generatedPackages = xsdEcoreBuilder.generate(URI.createFileURI("D:/workspace-neon/CyberPhysical_Incident.v1/model/cyberPhysical_Incident.xsd"));
		
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
		    .put("cpi", new GenericXMLResourceFactoryImpl());
	
	
		ResourceSet resourceSet = new ResourceSetImpl();
		/*Resource resource = resourceSet.getResource(URI.createFileURI(""), true);
		resource.load(Collections.EMPTY_MAP);
		EObject root = resource.getContents().get(0);*/
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		resourceSet.getPackageRegistry().put("http://www.eclipse.org/uml2/2.0.0/UML", UMLPackage.eINSTANCE);
		Resource r = resourceSet.getResource(URI.createFileURI("etc/example/interruption_incident-pattern.cpi"),true);
		System.out.println(r);
		IncidentDiagram diagram = (IncidentDiagram) r.getContents().get(0);
		
		System.out.println(diagram);
		
		return null;
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
		
		//test1();
		buildIncidentFromFile();
	}
	
	
	public static void test1() {
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
		Entity ent11 = instance.createEntity();
		Entity ent12 = instance.createEntity();
		Entity ent2 = instance.createEntity();
		Entity ent3 = instance.createEntity();
		Entity ent4 = instance.createEntity();
		Entity ent41 = instance.createEntity();
		Entity ent42 = instance.createEntity();
		Entity ent43 = instance.createEntity();
		Entity ent44 = instance.createEntity();
		Entity ent5 = instance.createEntity();
		Entity ent6 = instance.createEntity();
		Entity ent7 = instance.createEntity();
		
		Connectivity conn1 = instance.createConnectivity();
		Connectivity conn2 = instance.createConnectivity();
		Connectivity conn3 = instance.createConnectivity();
		Connectivity conn4 = instance.createConnectivity();
		Connectivity conn41 = instance.createConnectivity();
		Connectivity conn42 = instance.createConnectivity();
		Connectivity conn43 = instance.createConnectivity();
		Connectivity conn44 = instance.createConnectivity();


		conn1.setName("conn1");
		conn2.setName("conn1");
		conn3.setName("conn2");
		conn4.setName("conn2");
		conn41.setName("conn2");
		conn42.setName("conn3");
		conn43.setName("conn2");
		conn44.setName("conn3");
		
		ent1.setName(assets[0].getName());
		ent11.setName(assets[0].getName());
		ent12.setName(assets[0].getName());
		ent2.setName(assets[1].getName());
		ent5.setName(assets[2].getName());
		ent6.setName(assets[3].getName());
		
		//initiator
		ent3.setName(actors[0].getName());
		ent4.setName(actors[0].getName());
		ent7.setName(actors[0].getName());

		//set connectivity of entities
		//ent1.getConnectivity().add(conn2);
		ent11.getConnectivity().add(conn3);
		//ent1.getConnectivity().add(conn4);
		//ent3.getConnectivity().add(conn2);
//		ent3.getConnectivity().add(conn3);
		ent4.getConnectivity().add(conn4);
		
		ent1.getEntity().add(ent3); //ent1 contains ent3 & ent2 contains ent4 (which has the same name as ent3). This expresses entity movement
		//ent3.getEntity().add(ent5); 
		ent2.getEntity().add(ent4);
		
		preExp1.getEntity().add(ent1);
		postExp1.getEntity().add(ent2);
		postExp1.getEntity().add(ent11);
		
		//System.out.println("conn: "+preExp1.getConnections("asset0", "actor0"));
		
		ent6.getEntity().add(ent7); //ent6 contains ent7 (which is the same as ent3 and ent4)
		ent7.getConnectivity().add(conn41);
		ent7.getConnectivity().add(conn42);
		ent12.getConnectivity().add(conn43);
		ent12.getConnectivity().add(conn44);
		
		preExp2.getEntity().add(ent5);
		postExp2.getEntity().add(ent6);
		postExp2.getEntity().add(ent12);
		
		pre.setExpression(preExp1);
		post.setExpression(postExp1);
		
		pre2.setExpression(preExp2);
		post2.setExpression(postExp2);
		
		activities[0].setPrecondition(pre);
		activities[0].setPostcondition(post);
		activities[0].getTargetedAssets().add(assets[0]);
		
		
		activities[1].setPrecondition(pre2);
		activities[1].setPostcondition(post2);
		activities[1].getTargetedAssets().add(assets[0]);

		//System.out.println("contianed assets: "+activities[0].getInitiatorContainedEntities(BigraphExpression.PRECONDITION_EXPRESSION));
		//System.out.println("conns changed: "+activities[0].connectionsChanged("actor1"));
		
		//takes only the first two activities and checks if they can be merged
		LinkedList<Activity> acts = new LinkedList<Activity>();
		acts.add(activities[0]);
		acts.add(activities[1]);
		acts.add(activities[2]);

		
		//merging activities
		Activity act = inc.mergeActivities(acts); 

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
				System.out.println("merged activity name: "+act.getName());
				
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
