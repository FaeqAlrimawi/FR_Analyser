package ie.lero.spare.franalyser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Match;
import it.uniud.mads.jlibbig.core.std.Matcher;

public class BigraphAnalyser {

	private String bigrapherFileName;
	private String bigrapherValidateCmd;
	private static String bigrapherExecutionOutputFolder;
	private String bigrapherOutputFormat;
	private int maximumNumberOfStates;
	private String validBigrapherString = "model file parsed correctly";
	private PredicateHandler predicateHandler;
	private String[] predicatesFileLines;
	private LinkedList<GraphPath> paths;


	public BigraphAnalyser() {
		bigrapherFileName = "";
		maximumNumberOfStates = 10000;
		bigrapherOutputFormat = "txt"; // its possible to specify more than one
										// format for different parts (e.g.,
										// states, transitions) formats: svg,
										// json, dot, and txt
		bigrapherExecutionOutputFolder = "Output";
		bigrapherValidateCmd = "bigrapher validate -n ";
		predicateHandler = null;

	}

	public BigraphAnalyser(String fileName) {
		this();
		bigrapherFileName = fileName;
	}

	public BigraphAnalyser(PredicateHandler predHandler, String fileName) {
		this();
		bigrapherFileName = fileName;
		predicateHandler = predHandler;
	}

	public PredicateHandler analyse() {
		
		identifyRelevantStates();
		return identifyStateTransitions();

	}

	


	/*
	 * public String createBigrapherExecutionCmd(String bigrapherFileName,
	 * String outputFolder, String outputFormat, int MaximumNumberOfStates) {
	 * StringBuilder res = new StringBuilder();
	 * 
	 * if (MaximumNumberOfStates <= 0) MaximumNumberOfStates = 1000;
	 * 
	 * res.append("bigrapher full -q -M ").append(MaximumNumberOfStates).
	 * append(" -t ").append(outputFolder).append("/transitionSystem -s ")
	 * .append(outputFolder).append(" -l ").append(outputFolder).
	 * append("/transtionLabel -p ")
	 * .append(outputFolder).append("/predicates -f ").append(outputFormat).
	 * append(" ").append(bigrapherFileName);
	 * 
	 * return res.toString(); }
	 */

	public String getBigrapherFileName() {
		return bigrapherFileName;
	}

	public void setBigrapherFileName(String bigrapherFileName) {
		this.bigrapherFileName = bigrapherFileName;
	}

	public static String getBigrapherExecutionOutputFolder() {
		return bigrapherExecutionOutputFolder;
	}

	public void setBigrapherExecutionOutputFolder(String bigrapherExecutionOutputFolder) {
		BigraphAnalyser.bigrapherExecutionOutputFolder = bigrapherExecutionOutputFolder;
	}

	public String getBigrapherOutputFormat() {
		return bigrapherOutputFormat;
	}

	public void setBigrapherOutputFormat(String bigrapherOutputFormat) {
		this.bigrapherOutputFormat = bigrapherOutputFormat;
	}

	public int getMaximumNumberOfStates() {
		return maximumNumberOfStates;
	}

	public void setMaximumNumberOfStates(int maximumNumberOfStates) {
		this.maximumNumberOfStates = maximumNumberOfStates;
	}

	public String getValidBigrapherString() {
		return validBigrapherString;
	}

	public void setValidBigrapherString(String validBigrapherString) {
		this.validBigrapherString = validBigrapherString;
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

	// could be transfered to predicateHandler class
/*	public boolean identifyRelevantStates(Predicate pred) {
		
		boolean areStatesIdentified = false;

		pred.removeAllBigraphStates();
		String predName = pred.getBigraphPredicateName();
		
		if (predicatesFileLines == null || predicatesFileLines.length == 0) {
			// remove "sb3_" after testing
			predicatesFileLines = FileManipulator.readFile(bigrapherExecutionOutputFolder + "/predicates");
		}
		
		// format: e.g., label "phi" = x = 14 | x = 13 | x = 11 | x = 12 | x =
		// 10 | x = 9
		String predicateLine = "";
		String[] tmp;
		String[] tmp2;
		for (String line : predicatesFileLines) {
			if (line.contains(predName)) {
				predicateLine = line;
				break; // exit the loop as it is assumed that a predicate states
						// occupy one line only!
			}
		}

		if(predicateLine.contains("false")) {
			return true;
		}
		
		if (!predicateLine.isEmpty()) {
			tmp = predicateLine.split("\""); // Delimiters for the predicate line
			tmp2 = tmp[2].split("=|x|\\|");
			for (int i = 1; i < tmp2.length; i++) { // i starts from one to drop
				tmp2[i] = tmp2[i].trim();							// the phrase [label "name"]
				if (tmp2[i].matches("\\d+")) {
					pred.addBigraphState(new Integer(Integer.parseInt(tmp2[i])));
				}
			}
			areStatesIdentified = true;
		} else {
			tmp = null;
		}

		return areStatesIdentified;
	}*/
	
	public boolean identifyRelevantStates(Predicate pred) {
		boolean areStatesIdentified = false;
		Iterable<Match> m;
		
		//method to convert predicate to required format
		Bigraph redex = pred.convertPredicateToBigraph();
		
		//null should be replaced with the function that returns states
		HashMap<Integer, Bigraph> states = SystemInstanceHandler.getStates(); 
		
		//matcher object
		Matcher matcher = new Matcher();
		
		for(int i =0; i<states.size();i++) {
			if(matcher.match(states.get(i), redex).iterator().hasNext()){
				pred.addBigraphState(i);
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
				paths = TransitionSystem.getTransitionSystemInstance().getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

	}

/*	public void checkIncidentActivitiesSatisfaction() {
		//satisfaction of an incident depends on finding at least one path from a postcondition in previous
		//activity to preconditions of an activity being examined
		// if an activity has multiple preconditions to satisfy then at least one for each is required
		//if the activity has no preconditions then there should be paths from postconditions of
		//previous activities to postconditions of activity being examined
		//could return all possible ways these activities can be satisfied
		//HOWEVER, is a path really needed for satisfaction??
		
		//IncidentActivity initialActivity = predicateHandler.getInitialActivity();
		boolean previousPredicateSatisfied = false;
		ArrayList<GraphPath> paths = new ArrayList<GraphPath>();
		
		for (IncidentActivity activity : predicateHandler.getIncidentActivities().values()) {
			
			if(!activity.isActivitySatisfied()) {
				System.out.println("activity [" + activity.getName() + "] is NOT satisfied");
				continue; //for now it skips checking the rest
			}
			
			System.out.println("activity [" + activity.getName() + "] is satisfied");
			for(IncidentActivity actPrev : activity.getPreviousActivities()) {
				//find at least one path that connects the post of previous with pre of current
				for(Predicate predPrev : actPrev.getPredicates(PredicateType.Postcondition)) {
					previousPredicateSatisfied = false;
					for (Predicate predCurrent : activity.getPredicates(PredicateType.Precondition)) {
						if(!TransitionSystem.getTransitionSystemInstance().getPaths(predPrev, predCurrent).isEmpty()) {
							previousPredicateSatisfied = true;
							break; //to get all possible paths this requires looping all the preconditions and storing all paths
						}
					}
					if(!previousPredicateSatisfied) { //could be a deal breaker as no postconditions of previous match any of current preconditions
						System.out.println(predPrev.getName()+"-"+predPrev.getIncidentActivity().
								getName()+" has NO paths to " + activity.getName());
					} else {
						System.out.println(predPrev.getName()+"-"+predPrev.getIncidentActivity().
								getName()+" has paths to " + activity.getName()+ " preconditions");
					}
				}
			}
		}
		
	}*/

}
