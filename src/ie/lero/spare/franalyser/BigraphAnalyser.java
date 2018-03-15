package ie.lero.spare.franalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.PredicateType;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
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
		Node bld = bi.addNode("Building", bi.addRoot());
		Node flr = bi.addNode("Floor", bld);
		
		OuterName o1 = bi.addOuterName("wk1");
		OuterName o2 = bi.addOuterName("wk2");
		OuterName o3 = bi.addOuterName("wk3");
//		bi.closeOuterName(o1);
		bi.closeOuterName(o2);
//		bi.closeOuterName(o3);
		
		LinkedList<Handle> hnd = new LinkedList<Handle>();
		hnd.add(o1);
		hnd.add(o2);
		hnd.add(o3);
		
		Node hal = bi.addNode("Hallway", flr, hnd);
		Node rm = bi.addNode("Room", flr, o1);
	
	//	Node rm2 = bi.addNode("Room", flr, o2);
	//	Node rm3 = bi.addNode("Room", flr, o3);
		Node visitor = bi.addNode("Visitor", hal);
		
		bi.addSite(visitor);
		bi.addSite(bld);
		bi.addSite(flr);
//		bi.addSite(rm3);
//		bi.addSite(rm2);
		bi.addSite(rm);
		InnerName n1 = bi.addInnerName("n1", o1);
		InnerName n2 = bi.addInnerName("n2", o1);
		
		redex =  bi.makeBigraph();
		////
		
		print("\nidentifyRelevantStates: "+redex.toString()+"\n\nstate: "+states.get(0)+"\n");

		for(int i =0; i<states.size();i++) {	
			if(matcher.match(states.get(i), redex).iterator().hasNext()){
				pred.addBigraphState(i);
				areStatesIdentified = true;
				print("state " + i + " matched");		
			}
		}
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
