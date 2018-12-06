package ie.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.xml.xquery.XQException;

import org.eclipse.emf.common.util.EList;

import cyberPhysical_Incident.Activity;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiator.PotentialIncidentInstance;

public class PredicateHandler {

	private HashMap<String, Predicate> predicates;
	private HashMap<String, Activity> incidentActivities;
	private Digraph<String> activitiesGraph;
	private LinkedList<LinkedList<String>> activitySequences;
	private Logger logger;
	// generated transitions
	List<GraphPath> transitions;

	// used for transition generation by the recursive task
	private List<Integer> preconditionStates;
	private List<Integer> postconditionStates;
	private ForkJoinPool mainPool;

	private SystemInstanceHandler systemHandler;
	private TransitionSystem transitionSystem;
	private String incidentDocument;

	private List<Predicate> bottelNecksStatesInOrder = new LinkedList<Predicate>();
	private int bottleNeckNumber = 1;
	private String instanceName;
	public static final String INSTANCE_NAME_GLOBAL = "Predicate-Handler";

	private Map<Integer, List<Integer>> preconditionStatesWithTransitions = new HashMap<Integer, List<Integer>>();

	public PredicateHandler() {
		predicates = new HashMap<String, Predicate>();
		incidentActivities = new HashMap<String, Activity>();
		activitySequences = new LinkedList<LinkedList<String>>();
		logger = null;
		systemHandler = SystemsHandler.getCurrentSystemHandler();
		transitionSystem = systemHandler != null ? systemHandler.getTransitionSystem() : null;

	}

	public PredicateHandler(Logger logger) {
		this();
		this.logger = logger;

	}

	public PredicateHandler(Logger logger, SystemInstanceHandler sysHandler, String incidentDoc) {
		this();
		this.logger = logger;
		systemHandler = sysHandler;
		transitionSystem = systemHandler.getTransitionSystem();
		incidentDocument = incidentDoc;
	}

	public PredicateHandler(SystemInstanceHandler sysHandler) {
		this();
		systemHandler = sysHandler;
		transitionSystem = systemHandler.getTransitionSystem();
	}

	public void setLogger(Logger logger) {

		this.logger = logger;
	}

	public HashMap<String, Predicate> getPredicates() {
		return predicates;
	}

	public void setPredicates(HashMap<String, Predicate> predicates) {
		this.predicates = predicates;
	}

	public boolean addPredicate(String predName, Predicate pred) {
		boolean isAdded = false;

		if (pred != null) {
			predicates.put(predName, pred);
			isAdded = true;
		}

		return isAdded;
	}

	public boolean addPredicate(Predicate pred) {

		if (pred != null) {
			predicates.put(pred.getName(), pred);
			return true;
		}

		return false;
	}

	public String toString() {
		StringBuilder res = new StringBuilder();

		if (predicates != null && !predicates.isEmpty())
			for (Predicate p : predicates.values()) {
				res.append(p.toString());
			}

		return res.toString();
	}

	public boolean validatePredicates() {
		boolean isValid = true;
		// to be done...how to validate them against bigrapher file (e.g.,
		// controls available, connections)

		return isValid;
	}

	public String insertPredicatesIntoBigraphFile(String fileName) {

		BufferedWriter writer = null;
		ArrayList<String> list = new ArrayList<String>();
		String preds = "";
		String predsBorders = "\r\n#########################Generated Predicates##############################\r\n";
		int index = -1;
		int indexRules = -1;
		boolean isPredDefined = false;
		// check file name to contain a .big file
		String outputFile = "";
		StringBuilder existingPreds = new StringBuilder();
		int indexPred = -1;
		int indexPredEnd = -1;

		// get the big pred_name = predicate statements
		preds = convertToBigraphPredicateStatement();

		try {

			String[] lines = FileManipulator.readFileNewLine(fileName);

			for (String s : lines) {
				list.add(s);
			}

			// determine the last time the keyword ctrl is used as predicates
			// cannot be defined before ctrl
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).startsWith("ctrl") || list.get(i).startsWith("atomic ctrl")
						|| list.get(i).startsWith("fun ctrl") || list.get(i).startsWith("atomic fun ctrl")) {
					index = i;
				} else if (list.get(i).startsWith("preds")) {
					isPredDefined = true;
					indexPred = i;
					existingPreds.append(list.get(i));
					if (list.get(i).contains(";")) { // there are existing
														// predicates and all on
														// the same line
						indexPredEnd = i;
					} else { // predefined predicates take more than one line
						for (int j = i + 1; j < list.size(); j++) {
							existingPreds.append(list.get(j));
							if (list.get(j).contains(";")) {
								indexPredEnd = j;
								break;
							}
						}
					}
				} else if (list.get(i).startsWith("rules")) {
					indexRules = i;
				}
			}

			// insertion of preds names before insertion of predicate statements
			// as preds names come after them
			if (isPredDefined) {
				list.add(indexPred, "\r\n####updated predicates#######\r\n"
						+ getBigraphPredicateNames(existingPreds.toString()) + "\r\n###########################");
				for (int i = indexPredEnd + 1; i >= indexPred + 1; i--) {
					list.remove(i);
				}

			} else // insert pred names after 'begin brs' section (i.e bigraph
					// definition section) and before 'end' if preds are not
					// already defined
			{
				for (int i = indexRules; i < list.size(); i++) {
					if (list.get(i).contains(";")) {
						list.add(i + 1, "\r\n####updated predicates#######\r\n" + getBigraphPredicateNames("")
								+ "\r\n###########################");
						break;
					}
				}

			}

			// check that the last ctrl statement has semicolon in the same
			// line, then insert predicates
			if (list.get(index).contains(";")) {
				list.add(index + 1, predsBorders + preds + predsBorders);
			} else {
				for (int i = index + 1; i < list.size(); i++) {
					if (list.get(i).contains(";")) {
						list.add(i + 1, predsBorders + preds + predsBorders);
						break;
					}
				}
			}

			outputFile = fileName.split("\\.")[0] + "_generated.big";
			writer = new BufferedWriter(new FileWriter(outputFile));
			for (int i = 0; i < list.size(); i++)
				writer.write(list.get(i).toString() + "\r\n");

			writer.close();

			return outputFile;
		} catch (Exception e) {
			e.printStackTrace();
			outputFile = "";
		}

		return outputFile;

	}

	public String convertToBigraphPredicateStatement() {
		StringBuilder res = new StringBuilder();

		for (Predicate pred : predicates.values()) {
			res.append(pred.getBigraphPredicateStatement());
		}
		return res.toString();
	}

	private String getBigraphPredicateNames(String existingPreds) {
		StringBuilder res = new StringBuilder();

		res.append("preds = {");
		// append existing predicates in the file
		if (!existingPreds.equals("")) {
			res.append(existingPreds.substring(existingPreds.indexOf("{") + 1, existingPreds.lastIndexOf("}")))
					.append(", ");
		}
		for (Predicate pred : predicates.values()) {
			res.append(pred.getBigraphPredicateName()).append(", ");
		}

		res.deleteCharAt(res.lastIndexOf(","));
		res.append("};");

		return res.toString();
	}

	public ArrayList<Predicate> getActivityPredicates(String activityName) {
		// ArrayList<Predicate> result = new ArrayList<Predicate>();

		/*
		 * for (Predicate p : predicates) { if
		 * (activityName.contentEquals(p.getActivityName())) { result.add(p); }
		 * }
		 */

		return ((IncidentActivity) incidentActivities.get(activityName)).getPredicates();
	}

	/*
	 * public String insertPredicatesIntoBigraphFile2(String fileName) { String
	 * outputFileName = "";
	 * 
	 * return outputFileName; }
	 */

	public ArrayList<Predicate> getPredicates(String activityName, PredicateType type) {
		ArrayList<Predicate> preds = new ArrayList<Predicate>();

		for (Predicate pred : getActivityPredicates(activityName)) {
			if (pred.getPredicateType() == type) {
				preds.add(pred);
			}
		}
		return preds;
	}

	public ArrayList<String> getActivitNames() {
		ArrayList<String> names = new ArrayList<String>();

		for (String nm : incidentActivities.keySet()) {
			names.add(nm);
		}

		return names;
	}

	public HashMap<String, Activity> getIncidentActivities() {
		return incidentActivities;
	}

	public void setIncidentActivities(HashMap<String, Activity> incidentActivities) {
		this.incidentActivities = incidentActivities;
	}

	public void updateNextPreviousActivitiesUsingXquery() {
		String[] tmp;
		String[] result;
		IncidentActivity act;

		try {
			result = XqueryExecuter.returnNextPreviousActivities(incidentDocument);

			for (String res : result) {
				tmp = res.split("##|!!");
				act = (IncidentActivity) incidentActivities.get(tmp[0]);
				/*
				 * act.setNextActivities(new ArrayList<IncidentActivity>());
				 * act.setPreviousActivities(new ArrayList<IncidentActivity>());
				 */
				if (tmp.length > 1 && tmp[1] != null) {
					for (String nxt : tmp[1].split(" ")) {
						if (nxt != null && !nxt.contentEquals("") && !nxt.contains(" ")) {
							act.addNextActivity(incidentActivities.get(nxt));
						}

					}
				}
				if (tmp.length > 2 && tmp[2] != null) {
					for (String pre : tmp[2].split(" ")) {
						if (pre != null && !pre.contentEquals("") && !pre.contains(" ")) {
							act.addPreviousActivity(incidentActivities.get(pre));
						}

					}
				}
			}

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateNextPreviousActivities() {

		List<IncidentActivity> nxtActs = new LinkedList<IncidentActivity>();
		List<IncidentActivity> preActs = new LinkedList<IncidentActivity>();

		for (Activity act : incidentActivities.values()) {

			// update next activities
			for (Activity nxtAct : act.getNextActivities()) {
				nxtActs.add((IncidentActivity) incidentActivities.get(nxtAct.getName()));
			}

			act.getNextActivities().clear();
			act.getNextActivities().addAll(nxtActs);
			nxtActs.clear();

			// update previous activities
			for (Activity preAct : act.getPreviousActivities()) {
				preActs.add((IncidentActivity) incidentActivities.get(preAct.getName()));
			}

			act.getPreviousActivities().clear();
			act.getPreviousActivities().addAll(preActs);
			preActs.clear();

		}
	}

	public void addActivityPredicate(String activityName, Predicate pred) {
		((IncidentActivity) incidentActivities.get(activityName)).addPredicate(pred);
		addPredicate(pred);
	}

	public void addIncidentActivity(Activity activity) {

		// convert Activity object to IncidentActivity object

		incidentActivities.put(activity.getName(), activity);
	}

	/**
	 * Returns the first activity in the incident. This activity is the one
	 * without any previous activities
	 * 
	 * @return IncidentActivity object representing the initial activity, null
	 *         if there is not one
	 */
	public Activity getInitialActivity() {

		for (Activity act : incidentActivities.values()) {
			if (act.getPreviousActivities() == null || act.getPreviousActivities().isEmpty()) {
				return act;
			}
		}

		return null;
	}

	public Activity getFinalActivity() {

		for (Activity act : incidentActivities.values()) {
			if (act.getNextActivities() == null || act.getNextActivities().isEmpty()) {
				return act;
			}
		}

		return null;
	}

	public LinkedList<GraphPath> getPathsBetweenActivities(IncidentActivity sourceActivity,
			IncidentActivity destinationActivity) {

		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();

		ArrayList<Predicate> preconditions = getPredicates(sourceActivity.getName(), PredicateType.Precondition);
		ArrayList<Predicate> postconditions = getPredicates(destinationActivity.getName(), PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = transitionSystem.getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

		LinkedList<Activity> activities = getMiddleActivities(sourceActivity, destinationActivity);

		// add the first and the last activities
		activities.addFirst(sourceActivity);
		activities.addLast(destinationActivity);

		boolean isCheckingPrecondition = true;
		// boolean isCheckingPostcondition = false;

		// check if each path contains at least one of the satisfied states for
		// each activity
		// can be parallelised
		ListIterator<GraphPath> pathsIterator = paths.listIterator();
		ListIterator<Activity> activitiesIterator = activities.listIterator();

		if (activities != null) {
			while (pathsIterator.hasNext()) {

				GraphPath path = pathsIterator.next();
				LinkedList<Integer> states = path.getStateTransitions();

				int j = 0;// first state is for the src and des activities
				// isSatisfied = true;
				isCheckingPrecondition = true;
				// isCheckingPostcondition = false;
				activitiesIterator = activities.listIterator();
				outerLoop: while (activitiesIterator.hasNext()) {
					IncidentActivity activity = (IncidentActivity) activitiesIterator.next();
					// get precondition of the activity (assumption: there is
					// only one precondition)
					Predicate pre = activity.getPredicates(PredicateType.Precondition).get(0);
					LinkedList<Integer> preStates = pre.getBigraphStates();

					// get precondition of the activity (assumption: there is
					// only one precondition)
					Predicate post = activity.getPredicates(PredicateType.Postcondition).get(0);
					LinkedList<Integer> postStates = post.getBigraphStates();

					// assumption: each predicate should satisfy different state
					// in the transition
					for (; j < states.size(); j++) { // last state is for the
														// src and des
														// activities
						int state = states.get(j);

						// if it is the last element and either it is still
						// checking the precondition or the postcondition does
						// not contain the state then remove the path and break
						// to outerloop
						if (j == states.size() - 1 && (activitiesIterator.hasNext() || // if
																						// there
																						// are
																						// still
																						// activities
																						// to
																						// iterate
																						// over
								isCheckingPrecondition || // or if it is the
															// last activity but
															// it is still
															// checking
															// precondition
								!postStates.contains(state))) { // or if it is
																// last activity
																// and and the
																// postcondition
																// does not have
																// the state as
																// one of its
																// own
							pathsIterator.remove();
							break outerLoop;
						}

						// find a match for the precondition
						if (isCheckingPrecondition) {
							if (!preStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = false;
							}

							// find a match for the postcondition
						} else {
							if (!postStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = true;
								break;
							}
						}
					}
				}
			}
		}

		return paths;
	}

	/**
	 * Returns state transitions between the first and last activities, which
	 * pass through all the other activities between the first and the last
	 * 
	 * @return
	 */
	public LinkedList<GraphPath> getPaths() {

		logger.putMessage("PredicateHandler>>Generating transitions...");
		IncidentActivity sourceActivity = (IncidentActivity) getInitialActivity();
		IncidentActivity destinationActivity = (IncidentActivity) getFinalActivity();

		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();

		ArrayList<Predicate> preconditions = getPredicates(sourceActivity.getName(), PredicateType.Precondition);
		ArrayList<Predicate> postconditions = getPredicates(destinationActivity.getName(), PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = transitionSystem.getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

		logger.putMessage("PredicateHandler>>Analysing generated transitions [" + paths.size() + "]...");

		LinkedList<Activity> activities = getMiddleActivities(sourceActivity, destinationActivity);

		// add the first and the last activities
		activities.addFirst(sourceActivity);
		activities.addLast(destinationActivity);

		boolean isCheckingPrecondition = true;
		// boolean isCheckingPostcondition = false;

		// check if each path contains at least one of the satisfied states for
		// each activity
		// can be parallelised
		ListIterator<GraphPath> pathsIterator = paths.listIterator();
		ListIterator<Activity> activitiesIterator = activities.listIterator();

		if (activities != null) {
			while (pathsIterator.hasNext()) {

				GraphPath path = pathsIterator.next();
				LinkedList<Integer> states = path.getStateTransitions();

				int j = 0;

				isCheckingPrecondition = true;

				activitiesIterator = activities.listIterator();

				outerLoop: while (activitiesIterator.hasNext()) {
					IncidentActivity activity = (IncidentActivity) activitiesIterator.next();

					// get precondition of the activity (assumption: there is
					// only one precondition)
					Predicate pre = activity.getPredicates(PredicateType.Precondition).get(0);
					LinkedList<Integer> preStates = pre.getBigraphStates();

					// get precondition of the activity (assumption: there is
					// only one postcondition)
					Predicate post = activity.getPredicates(PredicateType.Postcondition).get(0);
					LinkedList<Integer> postStates = post.getBigraphStates();

					// assumption: each predicate should satisfy different state
					// in the transition
					for (; j < states.size(); j++) { // last state is for the
														// src and des
														// activities
						int state = states.get(j);

						// if it is the last element and either it is still
						// checking the precondition or the postcondition does
						// not contain the state then remove the path and break
						// to outerloop
						if (j == states.size() - 1 && (activitiesIterator.hasNext() || // if
																						// there
																						// are
																						// still
																						// activities
																						// to
																						// iterate
																						// over
								isCheckingPrecondition || // or if it is the
															// last activity but
															// it is still
															// checking
															// precondition
								!postStates.contains(state))) { // or if it is
																// last activity
																// and and the
																// postcondition
																// does not have
																// the state as
																// one of its
																// own
							pathsIterator.remove();
							break outerLoop;
						}

						// find a match for the precondition
						if (isCheckingPrecondition) {
							if (!preStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = false;
							}

							// find a match for the postcondition
						} else {
							if (!postStates.contains(state)) {
								continue;
							} else {
								isCheckingPrecondition = true;
								break;
							}
						}
					}
				}
			}
		}

		logger.putMessage("PredicateHandler>>Analysis is completed... resulted paths number [" + paths.size() + "]");

		return paths;
	}

	// not correct
	/*
	 * public LinkedList<GraphPath>
	 * getPathsBetweenActivitiesOriginal(IncidentActivity sourceActivity,
	 * IncidentActivity destinationActivity) {
	 * 
	 * //not done //
	 * 
	 * LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
	 * 
	 * ArrayList<Predicate> preconditions =
	 * getPredicates(sourceActivity.getName(), PredicateType.Precondition);
	 * ArrayList<Predicate> postconditions =
	 * getPredicates(destinationActivity.getName(),
	 * PredicateType.Postcondition);
	 * 
	 * for (Predicate pre : preconditions) { pre.removeAllPaths(); for
	 * (Predicate post : postconditions) { // this can be limited to //
	 * conditions that are // associated with each // other
	 * post.removeAllPaths(); paths =
	 * SystemInstanceHandler.getTransitionSystem().getPaths(pre, post);
	 * pre.addPaths(paths); post.addPaths(paths); } }
	 * LinkedList<IncidentActivity> middleActivities =
	 * getMiddleActivities(sourceActivity, destinationActivity);
	 * 
	 * LinkedList<Integer> indices = new LinkedList<Integer>(); GraphPath tmp;
	 * 
	 * //check if each path contains at least one of the satisfied states for
	 * each activity for(int i=0;i<paths.size();i++) { if(middleActivities !=
	 * null) { for(IncidentActivity activity: middleActivities) { tmp =
	 * paths.get(i); if (!tmp.satisfiesActivity(activity)) {
	 * //System.out.println("remove path " + tmp.toSimpleString());
	 * indices.add(i); } } } }
	 * 
	 * //if there are paths that do not go through all activities then remove
	 * them for(int i=0;i<indices.size();i++) {
	 * paths.remove((int)(indices.get(i))); //this is needed since removing an
	 * element from the list will shift the indices for(int
	 * j=i+1;j<indices.size();j++) { int v = indices.get(j)-1; indices.set(j,
	 * v); } }
	 * 
	 * return paths; }
	 */

	public LinkedList<Activity> getMiddleActivities(IncidentActivity sourceActivity,
			IncidentActivity destinationActivity) {
		LinkedList<Activity> result = new LinkedList<Activity>();

		if (sourceActivity.equals(destinationActivity)
				|| sourceActivity.getNextActivities().contains(destinationActivity)) {
			return result;
		}

		activitySequences.clear();
		LinkedList<String> visited = new LinkedList<String>();
		visited.add(sourceActivity.getNextActivities().get(0).getName());

		depthFirst(destinationActivity.getName(), visited);

		if (activitySequences.size() > 0) {
			LinkedList<String> activityNames = activitySequences.get(0);
			HashMap<String, Activity> acts = getIncidentActivities();

			for (String name : activityNames) {

				result.add(acts.get(name));
			}
		}

		return result;
	}
	/*
	 * public LinkedList<HashMap<String, LinkedList<GraphPath>>>
	 * getPathsForIncident() {
	 * 
	 * LinkedList<IncidentActivity> activities = new
	 * LinkedList<IncidentActivity>(); LinkedList<IncidentActivity>
	 * visitedActivities = new LinkedList<IncidentActivity>(); IncidentActivity
	 * tmp; LinkedList<HashMap<String, LinkedList<GraphPath>>> paths = new
	 * LinkedList<HashMap<String, LinkedList<GraphPath>>>();
	 * 
	 * IncidentActivity initialActivity = getInitialActivity();
	 * activities.add(initialActivity);
	 * 
	 * while (!activities.isEmpty()) { tmp = activities.pop();
	 * 
	 * if (tmp != null) {
	 * 
	 * // check if visited before if (visitedActivities.contains(tmp)) {
	 * continue; } paths.add(tmp.getIntraInterPaths()); for (IncidentActivity
	 * act : tmp.getNextActivities()) { activities.add(act); }
	 * 
	 * visitedActivities.add(tmp); }
	 * 
	 * }
	 * 
	 * return paths; }
	 */

	/*
	 * public void findAllPossiblePaths() {
	 * 
	 * LinkedList<HashMap<String, LinkedList<GraphPath>>> paths =
	 * getPathsForIncident(); GraphPath p = new GraphPath(); GraphPath p2 = new
	 * GraphPath(); HashMap<String, LinkedList<GraphPath>> tmpHash;
	 * 
	 * Random rand = new Random(); int tries = 0; boolean isPathSelected =
	 * false;
	 * 
	 * //get random initial path
	 * 
	 * } LinkedList<GraphPath> tmpPath =
	 * paths.get(0).get(getInitialActivity().getNextActivities()
	 * .get(rand.nextInt(getInitialActivity().getNextActivities().size())).
	 * getName());
	 * 
	 * p2 = tmpPath.get(rand.nextInt(tmpPath.size()));
	 * 
	 * System.out.println(p2); for(int i =1; i<paths.size();i++) {
	 * for(LinkedList<GraphPath> pa : paths.get(i).values()) {
	 * while(!isPathSelected && tries <10000) { p =
	 * pa.get(rand.nextInt(pa.size())); if(p2.getStateTransitions().size() > 0
	 * && p2.getStateTransitions().getLast().compareTo(p.getStateTransitions().
	 * getFirst()) == 0){ p2 = p2.combine(p);
	 * System.out.println("path selected: "+p);
	 * System.out.println(p2.toSimpleString()); isPathSelected = true; } else if
	 * (p2.getStateTransitions().size() == 0){ p2 = p2.combine(p); } else {
	 * tries++; } } isPathSelected = false; tries = 0; break; } }
	 * 
	 * System.out.println(p2.toSimpleString()); }
	 */

	public Digraph<String> createActivitiesDigraph() {
		activitiesGraph = new Digraph<String>();

		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		Activity tmp;

		// assuming there is only one initial activity. can be extended to
		// multi-initials
		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			for (Activity act : tmp.getNextActivities()) {
				IncidentActivity incAct = (IncidentActivity) act;
				activitiesGraph.add(tmp.getName(), incAct.getName(), -1);
				if (!acts.contains(incAct.getName())) {
					acts.add(incAct);
				}
			}

			actsVisited.add(tmp);
		}

		return activitiesGraph;
	}

	private void depthFirst(String endActivity, LinkedList<String> visited) {
		List<String> nodes = activitiesGraph.outboundNeighbors(visited.getLast());

		// examine adjacent nodes
		for (String node : nodes) {
			if (visited.contains(node)) {
				continue;
			}
			if (node.equals(endActivity)) {
				// visited.add(node);
				// addTransitiontoList(visited);
				LinkedList<String> newList = new LinkedList<String>();
				newList.addAll(visited);
				activitySequences.add(newList);
				visited.removeLast();
				break;
			}
		}
		for (String node : nodes) {
			if (visited.contains(node) || node.equals(endActivity)) {
				continue;
			}
			visited.addLast(node);
			depthFirst(endActivity, visited);
			visited.removeLast();
		}
	}

	/*
	 * private void addTransitiontoList(List<String> transition, LinkedList<>) {
	 * LinkedList<String> newList = new LinkedList<String>(); GraphPath path =
	 * new GraphPath();
	 * 
	 * newList.addAll(transition); activitySequences.add(path);
	 * 
	 * }
	 */

	public LinkedList<LinkedList<String>> getActivitiesSequences() {
		return getActivitiesSequences(getInitialActivity().getName(), getFinalActivity().getName());
	}

	public LinkedList<LinkedList<String>> getActivitiesSequences(String initialActivity, String finalActivity) {
		LinkedList<String> visited = new LinkedList<String>();
		visited.add(initialActivity);

		if (activitySequences == null || activitySequences.size() == 0) {
			depthFirst(finalActivity, visited);
		}

		return activitySequences;
	}

	public boolean areAllSatisfied() {

		IncidentActivity act = null;

		for (Activity activity : incidentActivities.values()) {
			act = (IncidentActivity) activity;
			if (!act.isActivitySatisfied()) {
				return false;
			}
		}

		return true;
	}

	public LinkedList<String> getActivitiesNotSatisfied() {

		LinkedList<String> names = new LinkedList<String>();
		IncidentActivity act = null;

		for (Activity activity : incidentActivities.values()) {
			act = (IncidentActivity) activity;
			if (!act.isActivitySatisfied()) {
				names.add(act.getName());
			}
		}

		return names;
	}

	public void printAll() {
		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		IncidentActivity tmp;

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = (IncidentActivity) acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			System.out.println("Activity name: " + tmp.getName());
			System.out.println("**Paths from preconditions to postconditions within the activity");
			/*
			 * for(Predicate p : tmp.getPredicates(PredicateType.Precondition))
			 * { System.out.println("predicate name: "+p.getName());
			 * for(GraphPath pa : p.getPaths()) { System.out.println(pa); } }
			 */
			for (GraphPath p : tmp.getPathsBetweenPredicates()) {
				System.out.println(p);
			}
			if (tmp.getNextActivities() != null && tmp.getNextActivities().size() > 0) {

				for (Activity act : tmp.getNextActivities()) {
					IncidentActivity incAct = (IncidentActivity) act;
					System.out.println("**Paths from postconditions of current activity to preconditions of"
							+ " next activity [" + act.getName() + "] are:");
					for (GraphPath p : tmp.findPathsToNextActivity(incAct)) {
						System.out.println(p);
						if (!acts.contains(incAct)) {
							acts.add(incAct);
						}
					}

					System.out.println("**Paths from preconditions of current activity to preconditions of next"
							+ "activity are:");
					for (GraphPath p : tmp.getPathsToNextActivity(incAct)) {
						System.out.println(p);
					}
				}

			}
			System.out.println();

			actsVisited.add(tmp);
		}
	}

	public String getSummary() {

		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		IncidentActivity tmp;
		StringBuilder res = new StringBuilder();
		String newLine = "\n";
		String separator = "###########################";

		updateInterStatesSatisfied();

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = (IncidentActivity) acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			// get activity name
			res.append(newLine).append(separator).append(newLine).append(">>>Activity name: " + tmp.getName());

			ArrayList<Predicate> pre = tmp.getPredicates(PredicateType.Precondition);
			ArrayList<Predicate> post = tmp.getPredicates(PredicateType.Postcondition);

			// get preconditions
			if (pre != null && !pre.isEmpty()) {
				res.append(newLine).append(">>>Precondition: ").append(pre.get(0).getName()) // assumption
																								// made
																								// is
																								// that
																								// there
																								// is
																								// only
																								// one
																								// precondition
						.append(newLine).append("States matched: ").append(pre.get(0).getBigraphStates())
						// get states satisfying preconditions to postconditions
						// within the activity, and states that satisfy
						// condiitions between
						// the post of current activity and the precondition of
						// the next activity
						.append(newLine).append("States satisfying intra-conditions (i.e. pre-post): ")
						.append(pre.get(0).getStatesIntraSatisfied());
			}

			// get postcondition
			if (post != null && !post.isEmpty()) {
				res.append(newLine).append(">>>Postcondition: ").append(post.get(0).getName()) // assumption
																								// made
																								// is
																								// that
																								// there
																								// is
																								// only
																								// one
																								// precondition
						.append(newLine).append("States matched: ").append(post.get(0).getBigraphStates())
						// get states satisfying preconditions to postconditions
						// within the activity, and states that satisfy
						// condiitions between
						// the post of current activity and the precondition of
						// the next activity
						.append(newLine).append("States satisfying intra-conditions (i.e. pre-post): ")
						.append(post.get(0).getStatesIntraSatisfied()).append(newLine)
						.append("States satisfying inter-conditions (i.e. post-pre,next): ")
						.append(post.get(0).getStatesInterSatisfied());
			}

			res.append(newLine).append(separator).append(newLine);
			EList<Activity> next = tmp.getNextActivities();
			if (next != null && next.size() > 0) {
				for (Activity activity : next) {
					IncidentActivity incAct = (IncidentActivity) activity;
					if (!acts.contains(incAct)) {
						acts.add(incAct);
					}
				}
			}
			actsVisited.add(tmp);
		}

		return res.toString();
	}

	public void updateInterStatesSatisfied() {

		LinkedList<Activity> acts = new LinkedList<Activity>();
		LinkedList<Activity> actsVisited = new LinkedList<Activity>();
		IncidentActivity tmp;

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = (IncidentActivity) acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			tmp.findPathsToNextActivities();

			for (Activity act : tmp.getNextActivities()) {
				IncidentActivity incAct = (IncidentActivity) act;
				if (!acts.contains(incAct)) {
					acts.add(incAct);
				}
			}

			actsVisited.add(tmp);
		}
	}

	public List<GraphPath> findTransitions(int threadID) {

		long startTime = Calendar.getInstance().getTimeInMillis();

		// create thread pool
		mainPool = new ForkJoinPool();

		// create instance name
		instanceName = PotentialIncidentInstance.INSTANCE_GLOABAL_NAME + "[" + threadID + "]"
				+ Logger.SEPARATOR_BTW_INSTANCES + PredicateHandler.INSTANCE_NAME_GLOBAL
				+ Logger.SEPARATOR_BTW_INSTANCES;

		IncidentActivity sourceActivity = (IncidentActivity) getInitialActivity();
		IncidentActivity destinationActivity = (IncidentActivity) getFinalActivity();

		ArrayList<Predicate> preconditions = getPredicates(sourceActivity.getName(), PredicateType.Precondition);
		ArrayList<Predicate> postconditions = getPredicates(destinationActivity.getName(), PredicateType.Postcondition);

		Predicate precondition = preconditions.get(0);// assuming 1 precondition
		List<Integer> preconditionStates = precondition != null ? precondition.getBigraphStates() : null;

		Predicate postcondition = postconditions.get(0);// assuming 1

		// posytcondition
		List<Integer> postconditionStates = postcondition != null ? postcondition.getBigraphStates() : null;

		if (preconditionStates == null || preconditionStates.isEmpty() || postconditionStates == null
				|| postconditionStates.isEmpty()) {
			return null;
		}

		this.preconditionStates = preconditionStates;
		this.postconditionStates = postconditionStates;

		// generate nodes neighbor in the digraph to reduce processing time for
		// threads that will be created next
		logger.putMessage(instanceName + "Generating neighbor nodes in the Digraph...");
		transitionSystem.getDigraph().generateNeighborNodesMap();

		logger.putMessage(instanceName + "Identifying intra transitions between precondition states...");

		// identify transitions between states of precondition
		preconditionStatesWithTransitions = findIntraStatesTransitions(preconditionStates);

		PreconditionMatcher preMatcher = new PreconditionMatcher(0, preconditionStates.size());

		logger.putMessage(instanceName + "Identifying transitions...");
		transitions = mainPool.invoke(preMatcher);

		LinkedList<Activity> activities = getMiddleActivities(sourceActivity, destinationActivity);

		activities.addFirst(sourceActivity);
		activities.addLast(destinationActivity);

		TransitionAnalyser analyser = new TransitionAnalyser(0, transitions.size(), activities);
		// analyseTransitions(sourceActivity, destinationActivity);

		logger.putMessage(instanceName + "Removing from identified transitions (" + transitions.size()
				+ ") those that don't contain a state from each activity...");
		List<GraphPath> transitionsToRemove = mainPool.invoke(analyser);

		int numOfTransitionsRemoved;

		if (transitionsToRemove != null && !transitionsToRemove.isEmpty()) {
			transitions.removeAll(transitionsToRemove);
			numOfTransitionsRemoved = transitions.size();
		} else {
			numOfTransitionsRemoved = 0;
		}

		logger.putMessage(instanceName + "(" + numOfTransitionsRemoved
				+ ") transitions removed. Total generated transitions is (" + transitions.size() + ")");

		mainPool.shutdown();

		try {
			mainPool.awaitTermination(7, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mainPool.shutdownNow();
		}

		long endTime = Calendar.getInstance().getTimeInMillis();

		long duration = endTime - startTime;

		int secMils2 = (int) duration % 1000;
		int hours2 = (int) (duration / 3600000) % 60;
		int mins2 = (int) (duration / 60000) % 60;
		int secs2 = (int) (duration / 1000) % 60;

		// execution time
		logger.putMessage(instanceName + " Transitions identification time: " + duration + "ms [" + hours2 + "h:"
				+ mins2 + "m:" + secs2 + "s:" + secMils2 + "ms]");

		return transitions;

	}

	protected Map<Integer, List<Integer>> findIntraStatesTransitions(List<Integer> conStates) {

		if (conStates == null || conStates.isEmpty()) {
			return null;
		}

		// List<Integer> test = new LinkedList<Integer>();

		// test.add(390);
		// test.add(23);
		//
		ConditionIntraTransitionsIdentifier condIdentifier = new ConditionIntraTransitionsIdentifier(conStates, 0,
				conStates.size());

		Map<Integer, List<Integer>> result = mainPool.invoke(condIdentifier);

		logger.putMessage(instanceName + "Printing intra transitions identification...");
		logger.putMessage(instanceName + "# of states with intra transitions = " + result.size() + ". Total states = "
				+ conStates.size());
		for (Entry<Integer, List<Integer>> entry : result.entrySet()) {
			logger.putMessage(instanceName + "S[" + entry.getKey() + "]: " + entry.getValue());
		}

		return result;
	}

	/**
	 * Finds states in a condition that have transitions to other states in this
	 * condition
	 * 
	 * @author Faeq
	 *
	 */
	class ConditionIntraIntraTransitionsIdentifier extends RecursiveTask<Map<Integer, List<Integer>>> {

		private static final long serialVersionUID = 1L;
		List<Integer> states;
		int indexStart;
		int indexEnd;
		public static final int THRESHOLD = 100;
		private Map<Integer, List<Integer>> result;
		private int startState;
		private int endState;
		private Digraph<Integer> transitionDigraph = transitionSystem.getDigraph();
		private boolean hasTransition = false;

		public ConditionIntraTransitionsIdentifier(List<Integer> states, int startIndex, int endIndex) {
			this.states = states;
			indexStart = startIndex;
			indexEnd = endIndex;
			result = new HashMap<Integer, List<Integer>>();
		}

		@Override
		protected Map<Integer, List<Integer>> compute() {
			if ((indexEnd - indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<ConditionIntraTransitionsIdentifier, Map<Integer, List<Integer>>>() {

							@Override
							public Map<Integer, List<Integer>> apply(ConditionIntraTransitionsIdentifier arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<Map<Integer, List<Integer>>>() {

							@Override
							public Map<Integer, List<Integer>> apply(Map<Integer, List<Integer>> arg0,
									Map<Integer, List<Integer>> arg1) {
								// TODO Auto-generated method stub
								for (Entry<Integer, List<Integer>> entry1 : arg1.entrySet()) {
									// if arg0 has this entry then add whatever
									// list to the exisitng list
									if (arg0.containsKey(entry1.getKey())) {
										arg0.get(entry1.getKey()).addAll(entry1.getValue());
									}
									// else add a new entry
									else {
										arg0.put(entry1.getKey(), entry1.getValue());
									}
								}
								return arg0;
							}

						});

			} else {

				for(int j = indexStart;j<indexEnd;j++) {

					Integer state = states.get(j);

					for (int i = 0; i < states.size(); i++) {

						Integer desState = states.get(i);

						if (state == desState) {
							continue;
						}

						// check if the srcState already had identified the
						// desState
						if (result.containsKey(state)) {
							if (result.get(state).contains(desState)) {
								continue;
							}
						}
						// tries to find a transition from src to des
						hasATransition(state, desState);
					}

				}

				return result;
			}

		}

		protected boolean hasATransition(Integer srcState, Integer desState) {

			hasTransition = false;

			LinkedList<Integer> v = new LinkedList<Integer>();
			this.startState = srcState;
			v.add(srcState);
			this.endState = desState;

			// search for a transition
			depthFirst(v);

			// add desState to srcState
			if (hasTransition) {
				if (result.containsKey(startState)) {
					result.get(startState).add(endState);
				}
				// create a new entry for the src state
				else {
					List<Integer> tmp = new LinkedList<Integer>();
					tmp.add(endState);
					result.put(startState, tmp);
				}

				// remove first and last since they are the targets
				v.removeFirst();
				v.removeLast();

			} else {
				// just remove the srcState
				v.removeFirst();

			}

			if (!v.isEmpty()) {
				if (!result.containsKey(startState)) {
					List<Integer> tmp = new LinkedList<Integer>();
					result.put(startState, tmp);
				}
			}

			for (Integer node : v) {
				// then add desState to srcState list of states
				List<Integer> tmp1 = result.get(startState);
				if (!tmp1.contains(node)) {
					tmp1.add(node);
				}
			}

			return hasTransition;

		}

		private void depthFirst(LinkedList<Integer> visited) {

			List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(visited.getLast());

			for (Integer node : nodes) {

				if (visited.contains(node)) {
					continue;
				}

				if (node.equals(endState)) {
					visited.addLast(node);
					hasTransition = true;
					// visited.removeLast();
					return;
				}

				visited.addLast(node);
				depthFirst(visited);
				// visited.removeLast();

				if (hasTransition) {
					return;
				}
			}
		}

		protected List<ConditionIntraTransitionsIdentifier> createSubTasks() {

			List<ConditionIntraTransitionsIdentifier> dividedTasks = new LinkedList<ConditionIntraTransitionsIdentifier>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new ConditionIntraTransitionsIdentifier(states, indexStart, mid));
			dividedTasks.add(new ConditionIntraTransitionsIdentifier(states, mid, indexEnd));

			return dividedTasks;
		}

	}

	
	class ConditionIntraIntraTransitionsIdentifier
	class PreconditionMatcher extends RecursiveTask<List<GraphPath>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private List<GraphPath> result;

		// number of states in the precondition on which the division into sub
		// threads should take place
		private int preThreshold = 100;

		public PreconditionMatcher(int indexStart, int indexEnd) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			result = new LinkedList<GraphPath>();
		}

		@Override
		protected List<GraphPath> compute() {
			if ((indexEnd - indexStart) > preThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<PreconditionMatcher, List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(PreconditionMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(List<GraphPath> arg0, List<GraphPath> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				// do the matching by slicing Assets to match to into different
				// pieces
				List<ForkJoinTask<List<GraphPath>>> postCons = new LinkedList<ForkJoinTask<List<GraphPath>>>();

				for (int i = indexStart; i < indexEnd; i++) {
					int preconditionState = preconditionStates.get(i);
					// List<GraphPath> threadResult = mainPool
					// .invoke(new PostconditionMatcher(0,
					// postconditionStates.size(), preconditionState));
					// PostconditionMatcher tmp = new PostconditionMatcher(0,
					// postconditionStates.size(), preconditionState);
					// postCons.add(tmp);
					postCons.add(mainPool
							.submit(new PostconditionMatcher(0, postconditionStates.size(), preconditionState)));
				}

				for (ForkJoinTask<List<GraphPath>> task : postCons) {

					try {
						result.addAll(task.get());
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				// result.addAll(threadResult);
				return result;
			}
		}

		protected List<PreconditionMatcher> createSubTasks() {

			List<PreconditionMatcher> dividedTasks = new LinkedList<PredicateHandler.PreconditionMatcher>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new PreconditionMatcher(indexStart, mid));
			dividedTasks.add(new PreconditionMatcher(mid, indexEnd));

			return dividedTasks;
		}

	}

	class PostconditionMatcher extends RecursiveTask<List<GraphPath>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private int preState;
		private List<GraphPath> result;
		private List<GraphPath> localResult;
		private Integer startState;
		private Integer endState;
		private Digraph<Integer> transitionDigraph = transitionSystem.getDigraph();
		// number of states in the precondition on which the division into sub
		// threads should take place
		private int postThreshold = 100;

		public PostconditionMatcher(int indexStart, int indexEnd, int preState) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.preState = preState;
			result = new LinkedList<GraphPath>();
		}

		@Override
		protected List<GraphPath> compute() {
			if ((indexEnd - indexStart) > postThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<PostconditionMatcher, List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(PostconditionMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(List<GraphPath> arg0, List<GraphPath> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				// do the matching by slicing Assets to match to into different
				// pieces
				List<GraphPath> stateResult;
				for (int i = indexStart; i < indexEnd; i++) {
					int postconditionState = postconditionStates.get(i);
					stateResult = getPaths(preState, postconditionState);
					result.addAll(stateResult);
				}

				return result;
			}
		}

		protected List<PostconditionMatcher> createSubTasks() {

			List<PostconditionMatcher> dividedTasks = new LinkedList<PredicateHandler.PostconditionMatcher>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new PostconditionMatcher(indexStart, mid, preState));
			dividedTasks.add(new PostconditionMatcher(mid, indexEnd, preState));

			return dividedTasks;
		}

		public List<GraphPath> getPaths(Integer srcState, Integer desState) {
			LinkedList<Integer> v = new LinkedList<Integer>();
			localResult = new LinkedList<GraphPath>();
			// predicateSrc = null;
			// predicateDes = null;
			GraphPath tmpG;
			LinkedList<Integer> tmp;

			// adds the state itself if both the source and the destination
			// states
			// are the same
			if (srcState.equals(desState)) {
				tmpG = new GraphPath(transitionSystem);
				tmpG.setPredicateSrc(null);
				tmpG.setPredicateDes(null);
				tmp = new LinkedList<Integer>();
				tmp.add(srcState);
				tmp.add(srcState);
				tmpG.setStateTransitions(tmp);
				localResult.add(tmpG);

				return localResult;
			}

			this.startState = srcState;
			v.add(this.startState);
			this.endState = desState;
			depthFirst(v);

			return localResult;
		}

		private void depthFirst(LinkedList<Integer> visited) {

			List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(visited.getLast());

			// examine adjacent nodes
			// for (Integer node : nodes) {
			// if (visited.contains(node)) {
			// continue;
			// }
			// if (node.equals(endState)) {
			// visited.add(node);
			// addTransitiontoList(visited);
			// visited.removeLast();
			// break;
			// }
			// }

			for (Integer node : nodes) {

				// if (visited.contains(node) || node.equals(endState)) {
				// continue;
				// }

				if (visited.contains(node)) {
					continue;
				}

				if (node.equals(endState)) {
					visited.add(node);
					addTransitiontoList(visited);
					visited.removeLast();
					continue;
				}

				visited.addLast(node);
				depthFirst(visited);
				visited.removeLast();
			}
		}

		private void addTransitiontoList(List<Integer> transition) {
			LinkedList<Integer> newList = new LinkedList<Integer>();
			GraphPath path = new GraphPath(transitionSystem);

			newList.addAll(transition);

			// path.setPredicateSrc(null);
			// path.setPredicateDes(null);
			path.setStateTransitions(newList);
			localResult.add(path);

		}

	}

	class DepthFirstSearcher implements Callable<GraphPath> {

		public DepthFirstSearcher(Integer start, Integer endState) {

		}

		@Override
		public GraphPath call() throws Exception {
			// TODO Auto-generated method stub

			return null;
		}

	}

	class TransitionAnalyser extends RecursiveTask<List<GraphPath>> {

		private static final long serialVersionUID = 1L;
		private int threshold = 100;
		private int indexStart;
		private int indexEnd;

		// list of activities in sequence
		private List<Activity> activities;

		// indices of transitions that should be removed from the list of
		// transitions
		private List<GraphPath> transitionsToRemove;

		public TransitionAnalyser(int startIndex, int endIndex, List<Activity> activities) {
			this.indexStart = startIndex;
			this.indexEnd = endIndex;
			this.activities = activities;
			transitionsToRemove = new LinkedList<GraphPath>();
		}

		@Override
		protected List<GraphPath> compute() {

			if ((indexEnd - indexStart) > threshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<TransitionAnalyser, List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(TransitionAnalyser arg0) {
								// TODO Auto-generated method stub
								return arg0.transitionsToRemove;
							}

						}).reduce(transitionsToRemove, new BinaryOperator<List<GraphPath>>() {

							@Override
							public List<GraphPath> apply(List<GraphPath> arg0, List<GraphPath> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				analyseTransitions();
			}

			return transitionsToRemove;
		}

		protected List<TransitionAnalyser> createSubTasks() {

			List<TransitionAnalyser> dividedTasks = new LinkedList<PredicateHandler.TransitionAnalyser>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new TransitionAnalyser(indexStart, mid, activities));
			dividedTasks.add(new TransitionAnalyser(mid, indexEnd, activities));

			return dividedTasks;
		}

		protected void analyseTransitions() {

			// LinkedList<Activity> activities =
			// getMiddleActivities(sourceActivity, destinationActivity);

			// add the first and the last activities
			// activities.addFirst(sourceActivity);
			// activities.addLast(destinationActivity);

			boolean isCheckingPrecondition = true;
			// boolean isCheckingPostcondition = false;

			// check if each path contains at least one of the satisfied states
			// for
			// each activity
			// can be parallelised
			// ListIterator<GraphPath> pathsIterator =
			// transitions.listIterator();
			ListIterator<Activity> activitiesIterator = activities.listIterator();

			if (activities != null) {
				for (int i = indexStart; i < indexEnd; i++) {

					GraphPath path = transitions.get(i);
					LinkedList<Integer> states = path.getStateTransitions();

					int j = 0;

					isCheckingPrecondition = true;

					activitiesIterator = activities.listIterator();

					outerLoop: while (activitiesIterator.hasNext()) {
						IncidentActivity activity = (IncidentActivity) activitiesIterator.next();

						// get precondition of the activity (assumption: there
						// is
						// only one precondition)
						List<Predicate> preCons = activity.getPredicates(PredicateType.Precondition);
						List<Predicate> postCons = activity.getPredicates(PredicateType.Postcondition);

						Predicate pre = (preCons != null && !preCons.isEmpty()) ? preCons.get(0) : null;
						LinkedList<Integer> preStates = pre != null ? pre.getBigraphStates() : null;

						// get precondition of the activity (assumption: there
						// is
						// only one postcondition)
						Predicate post = (postCons != null && !postCons.isEmpty()) ? postCons.get(0) : null;
						LinkedList<Integer> postStates = post != null ? post.getBigraphStates() : null;

						// the activity has no pre or post conditions, then move
						// to the next activity
						if (pre == null && post == null) {
							continue;
						}

						// assumption: each predicate should satisfy different
						// state
						// in the transition
						for (; j < states.size(); j++) { // last state is for
															// the
															// src and des
															// activities
							int state = states.get(j);

							// if it is the last element and either it is still
							// checking the precondition or the postcondition
							// does
							// not contain the state then remove the path and
							// break
							// to outerloop if there are still activities to
							// iterate
							if (j == states.size() - 1 && (activitiesIterator.hasNext() ||
							// or if it is the last activity but it is still
							// checking precondition // over
									(preStates != null && isCheckingPrecondition) ||
									// or if it is last activity and and the
									// postcondition does not have the state as
									// one of its own
									(postStates != null && !postStates.contains(state)))) {
								// pathsIterator.remove();
								transitionsToRemove.add(path);
								break outerLoop;
							}

							// find a match for the precondition
							if (isCheckingPrecondition && preStates != null) {
								// if no match is found then move to next state
								// in transition
								if (!preStates.contains(state)) {
									continue;
									// else, if a state matches a precondition
									// state, then move to the postcondition, if
									// there's one, if not then break to next
									// activity
								} else {
									if (postStates != null) {
										isCheckingPrecondition = false;
									} else {
										isCheckingPrecondition = true;
										break;
									}
								}

								// find a match for the postcondition
							} else {
								if (!postStates.contains(state)) {
									continue;
								} else {
									isCheckingPrecondition = true;
									break;
								}
							}
						}
					}
				}
			}
		}

	}

}
