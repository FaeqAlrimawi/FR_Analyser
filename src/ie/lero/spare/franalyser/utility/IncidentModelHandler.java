package ie.lero.spare.franalyser.utility;

import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import cyberPhysical_Incident.CyberPhysicalIncidentPackage;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.impl.IncidentDiagramImpl;

public class IncidentModelHandler {
	
	private static final String EXTENSION = "cpi";
	
	/**
	 * Load an incident model from the given file name
	 * @param fileName the XMI file of the incident model
	 * @return an IncidentDigram object containing the model information
	 */
	public static IncidentDiagram loadIncidentFromFile(String fileName) {
	
	IncidentDiagram incidentDiagram = null;
	
	// generate EPackages from schemas	
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				    "ecore", new EcoreResourceFactoryImpl());

				ResourceSet rs = new ResourceSetImpl();
				// enable extended metadata
				final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
				rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
				    extendedMetaData);

				EPackage.Registry.INSTANCE.put(CyberPhysicalIncidentPackage.eNS_URI, CyberPhysicalIncidentPackage.eINSTANCE);
				
				Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(EXTENSION, new XMIResourceFactoryImpl() {
			    	
			    	public Resource createResource(URI uri) {
			    		
			    		XMIResource xmiResource = new XMIResourceImpl(uri);
			 
			    		return xmiResource;			
			    	}
			    });
	
		try {
			
			Resource r = rs.getResource(URI.createFileURI(fileName), true);
				
			EObject eObject = r.getContents().get(0);
			
			if (eObject instanceof EPackage) {
				  EPackage p = (EPackage)eObject;
				  rs.getPackageRegistry().put(p.getNsURI(), p);
			}

			incidentDiagram = (IncidentDiagramImpl) eObject;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return incidentDiagram;
		
	}
	
	/**
	 * Save an incident model (given by the IncidentDiagram object) to the given file name
	 * @param incidentDiagram the object holding the incident model information
	 * @param fileName the target file to save the model to
	 * @return True if saving is successful. False otherwise.
	 */
	public static boolean SaveIncidentToFile(IncidentDiagram incidentDiagram, String fileName) {
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
			    "ecore", new EcoreResourceFactoryImpl());

			ResourceSet rs = new ResourceSetImpl();
			// enable extended metadata
			final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
			rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
			    extendedMetaData);

			EPackage.Registry.INSTANCE.put(CyberPhysicalIncidentPackage.eNS_URI, CyberPhysicalIncidentPackage.eINSTANCE);
			
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(EXTENSION, new XMIResourceFactoryImpl() {
		    	
		    	public Resource createResource(URI uri) {
		    		
		    		XMIResource xmiResource = new XMIResourceImpl(uri);
		 
		    		return xmiResource;			
		    	}
		    });
			
			try {
				Resource r = rs.createResource(URI.createFileURI(fileName));
					
				r.getContents().add(incidentDiagram);
				
				r.save(Collections.EMPTY_MAP);
				
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return false;
			
	}
	
	public static void main(String[]args) {
	
		IncidentDiagram incidentDiagram = loadIncidentFromFile("etc/example/interruption_incident-pattern.cpi");
		
		if (incidentDiagram != null) {
			System.out.println(incidentDiagram.getActivity().get(0).getName());
			
			
			SaveIncidentToFile(incidentDiagram, "etc/example/inc.cpi");
		}
		
		//re-reading saved model
		IncidentDiagram inci = loadIncidentFromFile("etc/example/inc.cpi");
		
		System.out.println(inci.getActivity().get(0).getName());
	}
	
	
/*	public static void test1() {
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

		System.out.println("Incident activity sequence:");
		for(Activity ac: inc.getActivity()) {
			System.out.print(ac.getName()+"->");
		}
		
		Activity act = inc.mergeActivities(inc.getActivity().get(2), inc.getActivity().get(3));
		//Activity act2 = inc.mergeActivities(inc.getActivity().get(0), inc.getActivity().get(1));
		
		//printActivityInfo(act);
		//printActivityInfo(act2);
		
		System.out.println("Incident activity sequence:");
		for(Activity ac: inc.getActivity()) {
			System.out.print(ac.getName()+"->");
		}
		
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
		//Activity act2 = inc.mergeActivities(acts); 

		//printActivityInfo(act2);
		
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
		
	}*/
	
}
