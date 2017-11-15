package ie.lero.spare.franalyser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.xquery.XQException;

import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class PredicateHandler {

	private HashMap<String, Predicate> predicates;
	private HashMap<String, IncidentActivity> incidentActivities;
	private Digraph<IncidentActivity> activitiesGraph;

	public PredicateHandler() {
		predicates = new HashMap<String, Predicate>();
		incidentActivities = new HashMap<String, IncidentActivity>();

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

			String [] lines = FileManipulator.readFileNewLine(fileName);
			
			for(String s : lines) {
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

		return incidentActivities.get(activityName).getPredicates();
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

	public HashMap<String, IncidentActivity> getIncidentActivities() {
		return incidentActivities;
	}

	public void setIncidentActivities(HashMap<String, IncidentActivity> incidentActivities) {
		this.incidentActivities = incidentActivities;
	}

	public void updateNextPreviousActivities() {
		String[] tmp;
		String[] result;
		IncidentActivity act;

		try {
			result = XqueryExecuter.returnNextPreviousActivities();

			for (String res : result) {
				tmp = res.split("##|!!");
				act = incidentActivities.get(tmp[0]);
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

	public void addActivityPredicate(String activityName, Predicate pred) {
		incidentActivities.get(activityName).addPredicate(pred);
		addPredicate(pred);
	}

	public void addIncidentActivity(IncidentActivity activity) {
		if (!incidentActivities.containsValue(activity)) {
			incidentActivities.put(activity.getName(), activity);
		}
	}

	/**
	 * Returns the first activity in the incident. This activity is the one
	 * without any previous activities
	 * 
	 * @return IncidentActivity object representing the initial activity, null
	 *         if there is not one
	 */
	public IncidentActivity getInitialActivity() {

		for (IncidentActivity act : incidentActivities.values()) {
			if (act.getPreviousActivities() == null || act.getPreviousActivities().isEmpty()) {
				return act;
			}
		}

		return null;
	}

	public IncidentActivity getFinalActivity() {

		for (IncidentActivity act : incidentActivities.values()) {
			if (act.getNextActivities() == null || act.getNextActivities().isEmpty()) {
				return act;
			}
		}

		return null;
	}

	public LinkedList<GraphPath> getPathsBetweenActivities(IncidentActivity sourceActivity,
		
			//not done
			//
			IncidentActivity destinationActivity) {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();

		return paths;
	}

	/*public LinkedList<HashMap<String, LinkedList<GraphPath>>> getPathsForIncident() {

		LinkedList<IncidentActivity> activities = new LinkedList<IncidentActivity>();
		LinkedList<IncidentActivity> visitedActivities = new LinkedList<IncidentActivity>();
		IncidentActivity tmp;
		LinkedList<HashMap<String, LinkedList<GraphPath>>> paths = new LinkedList<HashMap<String, LinkedList<GraphPath>>>();

		IncidentActivity initialActivity = getInitialActivity();
		activities.add(initialActivity);

		while (!activities.isEmpty()) {
			tmp = activities.pop(); 

			if (tmp != null) {

				// check if visited before
				if (visitedActivities.contains(tmp)) {
					continue;
				}
				paths.add(tmp.getIntraInterPaths());
				for (IncidentActivity act : tmp.getNextActivities()) {
					activities.add(act);
				}

				visitedActivities.add(tmp);
			}

		}

		return paths;
	}*/

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

	public void createActivitiesDigraph() {
		activitiesGraph = new Digraph<IncidentActivity>();

		LinkedList<IncidentActivity> acts = new LinkedList<IncidentActivity>();
		LinkedList<IncidentActivity> actsVisited = new LinkedList<IncidentActivity>();
		IncidentActivity tmp;

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			for (IncidentActivity act : tmp.getNextActivities()) {
				activitiesGraph.add(tmp, act, -1);
				if (!acts.contains(act)) {
					acts.add(act);
				}
			}

			actsVisited.add(tmp);
		}

		System.out.println(activitiesGraph);
	}

	public void printAll() {
		LinkedList<IncidentActivity> acts = new LinkedList<IncidentActivity>();
		LinkedList<IncidentActivity> actsVisited = new LinkedList<IncidentActivity>();
		IncidentActivity tmp;

		acts.add(getInitialActivity());

		while (!acts.isEmpty()) {
			tmp = acts.pop();

			if (tmp == null || actsVisited.contains(tmp)) {
				continue;
			}

			System.out.println("Activity name: " + tmp.getName());
			System.out.println("**Paths from preconditions to postconditions within the activity");
			/*for(Predicate p : tmp.getPredicates(PredicateType.Precondition)) {
				System.out.println("predicate name: "+p.getName());
				for(GraphPath pa : p.getPaths()) {
					System.out.println(pa);
				}
			}*/
			for(GraphPath p : tmp.getPathsBetweenPredicates()) {
				System.out.println(p);
			}
			if (tmp.getNextActivities() != null && tmp.getNextActivities().size() > 0) {
				
				for (IncidentActivity act : tmp.getNextActivities()) {
					System.out.println("**Paths from postconditions of current activity to preconditions of"
							+ " next activity [" + act.getName() + "] are:");
					for (GraphPath p : tmp.findPathsToNextActivity(act)) {
						System.out.println(p);
						if (!acts.contains(act)) {
							acts.add(act);
						}
					}
					
					System.out.println("**Paths from preconditions of current activity to preconditions of next"
							+ "activity are:");
					for(GraphPath p : tmp.getPathsToNextActivity(act)) {
						System.out.println(p);
					}
				}
				
			} 
			System.out.println();

			actsVisited.add(tmp);
		}
	}
}
