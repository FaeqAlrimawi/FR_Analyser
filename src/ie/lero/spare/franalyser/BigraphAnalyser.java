package ie.lero.spare.franalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.PredicateType;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.Match;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;

public class BigraphAnalyser {

	private PredicateHandler predicateHandler;
	private LinkedList<GraphPath> paths;
	private boolean isDebugging = true;

	public BigraphAnalyser() {
		predicateHandler = null;

	}


	public BigraphAnalyser(PredicateHandler predHandler) {
		this();
		predicateHandler = predHandler;
	}

	public PredicateHandler analyse() {
		
		identifyRelevantStates();
		return identifyStateTransitions();

	}

	public PredicateHandler getPredicateHandler() {
		return predicateHandler;
	}

	public void setPredicateHandler(PredicateHandler predicateHandler) {
		this.predicateHandler = predicateHandler;
	}

	public PredicateHandler identifyRelevantStates() {
		ArrayList<String> activitiesName = predicateHandler.getActivitNames();

		for (String nm : activitiesName) {
			identifyRelevantStates(nm);
		}

		return predicateHandler;
	}

	// could be transfered to predicateHandler class
	public ArrayList<Predicate> identifyRelevantStates(String activityName) {
		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		for (Predicate p : preds) {
			identifyRelevantStates(p);
		}

		return preds;
	}
	
	public boolean identifyRelevantStates(Predicate pred) {
		boolean areStatesIdentified = false;
		
/*		//method to convert predicate to required format
		
		//Bigraph redex = pred.convertPredicateToBigraph();
		JSONParser parser = new JSONParser();
		try {
			SystemInstanceHandler.setFileName("sb3.big");
			SystemInstanceHandler.setOutputFolder("sb3_output");
			if(SystemInstanceHandler.createSignatureFromBRS() == null) {
				SystemInstanceHandler.createSignatureFromStates();
			}
			
			org.json.JSONObject o = XqueryExecuter.getBigraphConditions("activity1", PredicateType.Precondition);
			
			//Bigraph redex = SystemInstanceHandler.convertJSONtoBigraph((JSONObject) parser.parse(new FileReader("sb3_output/0.json")));
		
		HashMap<Integer, Bigraph> states = SystemInstanceHandler.loadStates(); 
		
		//matcher object
		Matcher matcher = new Matcher();
		Bigraph redex = Predicate.convertJSONtoBigraph(o);
		*/
		
		
		if(pred == null) {
			return false;
		}
		
		Bigraph redex = pred.getBigraphPredicate();
		
		if(redex == null) {
			return false;
		}
		
		HashMap<Integer, Bigraph> states = SystemInstanceHandler.getStates();
		Matcher matcher = new Matcher();
		
		////test code
		BigraphBuilder bi = new BigraphBuilder(SystemInstanceHandler.getGlobalBigraphSignature());
		BigraphBuilder bi2 = new BigraphBuilder(SystemInstanceHandler.getGlobalBigraphSignature());
		
		Node bld = bi.addNode("Building", bi.addRoot());
		OuterName n1 = bi.addOuterName("n1");
		OuterName n2 = bi.addOuterName("n2");
		OuterName n3 = bi.addOuterName("n3");
		OuterName n4 = bi.addOuterName("n4");
		OuterName n5 = bi.addOuterName("n5");
		OuterName n6 = bi.addOuterName("n6");
		
		Node flr = bi.addNode("Floor", bld);
		Node neti = bi.addNode("InstallationBus", flr, n1, n2,n3,n4,n5,n6);
		OuterName o1 = bi.addOuterName("wk1");
		OuterName o2 = bi.addOuterName("wk2");
		OuterName o3 = bi.addOuterName("wk3");
//		bi.closeOuterName(o1);
//		bi.closeOuterName(o2);
//		bi.closeOuterName(o3);
		
/*		LinkedList<Handle> hnd = new LinkedList<Handle>();
		hnd.add(o1);
		hnd.add(o2);
		hnd.add(o3);*/
		
		Node hal = bi.addNode("Hallway", flr, o1,o2,o3);
		Node rm1 = bi.addNode("Room", flr, o1);
		Node rm2 = bi.addNode("Room", flr, o2);
		Node rm3 = bi.addNode("Room", flr, o3);
		
		Node s1 = bi.addNode("SmartLight", rm1, n1);
		Node s2 = bi.addNode("SmartLight", rm2, n2);
		Node s3 = bi.addNode("SmartLight", rm3, n3);
		
		Node f = bi.addNode("FireAlarm", rm2, n4);
		Node h = bi.addNode("HVAC", rm2, n5);
		Node ser = bi.addNode("Workstation", rm3, n6);
		//	Node rm2 = bi.addNode("Room", flr, o2);
	//	Node rm3 = bi.addNode("Room", flr, o3);
		Node visitor = bi.addNode("Visitor", hal);
		
		bi.ground();
		Bigraph st = bi.makeBigraph();
		//bi.addSite(visitor);
	//	bi.addSite(bld);
		//bi.addSite(flr);
//		bi.addSite(rm3);
//		bi.addSite(rm2);
		//bi.addSite(rm);
		//InnerName n1 = bi.addInnerName("n1", o1);
		//InnerName n2 = bi.addInnerName("n2", o1);
		
/*		OuterName u1 = bi2.addOuterName("u1");
		OuterName u2 = bi2.addOuterName("u2");
		OuterName u3 = bi2.addOuterName("u3");
		OuterName u4 = bi2.addOuterName("u4");
		OuterName u5 = bi2.addOuterName("u5");
		OuterName u6 = bi2.addOuterName("u6");
		OuterName r1 = bi2.addOuterName("r1");
		OuterName r2 = bi2.addOuterName("r2");
		OuterName r3 = bi2.addOuterName("r3");
		OuterName tst = bi2.addOuterName("tst");*/
		
		Node flor = bi2.addNode("Floor", bi2.addRoot());
		//Node room1 = bi2.addNode("Room", flor, r1);
		//Node room2 = bi2.addNode("Room", flor, r2);
		//Node device = bi2.addNode("SmartLight", room1, u1);
		//Node device2 = bi2.addNode("SmartLight", room2, tst);
		Node hlway = bi2.addNode("Hallway", flor);
		//Node net = bi2.addNode("InstallationBus", bi2.addRoot(), u1, u2,u3,u4,u5, u6);
		
		bi2.addSite(hlway);
		//bi2.addSite(room1);
		bi2.addSite(flor);
		
		
		
		Bigraph red = bi2.makeBigraph();
	//	redex =  bi.makeBigraph();
		////
	/*
		print("\nidentifyRelevantStates: "+red.toString()+"\n\nstate: "+st+"\n");
		
		if(matcher.match(st, red).iterator().hasNext()){
			print("state matched");		
		}*/
		
		
		print("\nidentifyRelevantStates: \nOriginal redex:"+redex.toString()+"\nCreated redex: "+ red+"\n\nOriginal state: "+states.get(0)+"\n\nCreated state:"+st+"\n\n");
		//print("\nidentifyRelevantStates: \nOriginal redex:"+redex.toString()+"\n\nOriginal state: "+states.get(0)+"\n\n");
		
		//check outernames defined each node whether they are less or more than that of in a control in the signature
		//assuming knowledge is partial, if the number of outernames in a redex node is less than that in the signature (and knolwdege is partial for that node),
		//then add outernames to equal the number of outernames in the signature for that node.
		//if knowledge is complete, then any 
		
/*		if(matcher.match(st, red).iterator().hasNext()){
			areStatesIdentified = true;
			print("created matched");		
		}*/

	/*	if(matcher.match(states.get(0), redex).iterator().hasNext()){
			areStatesIdentified = true;
			print("original matched");		
		}*/

		for(int i =0; i<states.size();i++) {	
			if(matcher.match(states.get(i), red).iterator().hasNext()){
				pred.addBigraphState(i);
				areStatesIdentified = true;
				print("state " + i + " matched");		
			}
		}
			/*Iterator<? extends Match> t = matcher.match(states.get(i), redex).iterator();
			int cnt = 0;
			while(t.hasNext()){
				Match m = t.next();
				//pred.addBigraphState(i);
				//areStatesIdentified = true;
				print("state " + i + " matched...match:");//+m.toString());		
			cnt++;
			}
			
			print("number of time matched for state " + i + " is:"+cnt);*/
			
		return areStatesIdentified;
	}

	public PredicateHandler identifyStateTransitions() {

		for (String activityName : predicateHandler.getActivitNames()) {
			identifyStateTransitions(activityName);
		}

		return predicateHandler;
	}

	public void identifyStateTransitions(String activityName) {

		// identifyRelevantStates(activityName);
		ArrayList<Predicate> preconditions = predicateHandler.getPredicates(activityName, PredicateType.Precondition);
		ArrayList<Predicate> postconditions = predicateHandler.getPredicates(activityName, PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = SystemInstanceHandler.getTransitionSystem().getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

	}
	
	private void print(String msg) {
		if(isDebugging) {
			System.out.println("BigraphAnalyser: "+msg);
		}
	}
	public static void main(String[] args){
		BigraphAnalyser a = new BigraphAnalyser();
		Predicate p = null;
		a.identifyRelevantStates(p);
	}

}
