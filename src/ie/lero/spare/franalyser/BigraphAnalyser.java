package ie.lero.spare.franalyser;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.xquery.XQException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;

public class BigraphAnalyser {

	private PredicateHandler predicateHandler;
	private LinkedList<GraphPath> paths;


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
		
		//method to convert predicate to required format
		
		//Bigraph redex = pred.convertPredicateToBigraph();
		JSONParser parser = new JSONParser();
		try {
			SystemInstanceHandler.setFileName("sb3.big");
			SystemInstanceHandler.setOutputFolder("sb3_output");
			SystemInstanceHandler.buildSignature();
			Predicate p = new Predicate();
			
			org.json.JSONObject o = XqueryExecuter.getBigraphConditions("activity1", PredicateType.Precondition);
			Bigraph redex = p.convertJSONtoBigraph(o);
			//Bigraph redex = SystemInstanceHandler.convertJSONtoBigraph((JSONObject) parser.parse(new FileReader("sb3_output/0.json")));
		
		//null should be replaced with the function that returns states
		HashMap<Integer, Bigraph> states = SystemInstanceHandler.loadStates(); 
		
		//matcher object
		Matcher matcher = new Matcher();
	
		for(int i =0; i<states.size();i++) {
			if(matcher.match(states.get(i), redex).iterator().hasNext()){
				pred.addBigraphState(i);
				areStatesIdentified = true;
				System.out.println("state " + i + " matched");		
			}
		}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public static void main(String[] args){
		BigraphAnalyser a = new BigraphAnalyser();
		Predicate p = null;
		a.identifyRelevantStates(p);
	}

}
