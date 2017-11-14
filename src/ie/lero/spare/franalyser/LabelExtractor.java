package ie.lero.spare.franalyser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.TransitionSystem;

public class LabelExtractor {

	// private String[] predicatesFileLines;
	private String outputPath = "output";
	private String transitionFileName = "transitions";
	// private String predicateFilePath = outputPath+"/pred";
	// private ArrayList<ReactionRule> reactionRules;
	private String rulesKeywordsFileName = "rules_keywords.txt";
	private String [] rulesKeywords;
	TransitionSystem transitionSystem;

	public LabelExtractor() {
		// reactionRules = new ArrayList<ReactionRule>();
		TransitionSystem.setFileName(outputPath + "/" + transitionFileName);

		transitionSystem = TransitionSystem.getTransitionSystemInstance();
	}

	public LabelExtractor(String outputFolder) {
		// reactionRules = new ArrayList<ReactionRule>();
		this.outputPath = outputFolder;
		TransitionSystem.setFileName(outputPath + "/" + this.transitionFileName);
		transitionSystem = TransitionSystem.getTransitionSystemInstance();
	}

	public static void main(String[] args) {

		LabelExtractor ex = new LabelExtractor("output");
		
		//##### one way to identify the labels is by using the redex and reactum of
		// rules as predicates
		// however, this is requires more computational power and it can assign
		// more than one label
		// to a single transition
		/*
		 * ex.getRulesStates(); ex.removeNoneImmediateTransitionStates();
		 * ex.updateTransitionFile();
		 */
		// ex.extractLabel(1, 4);

		//##### another way is to define a keyword for each such as its name as a
		// control which is then used to identify transitions
		// it requires adding controls that identify the action in each reaction rule
		ex.extractLabels();
		ex.createNewLabelledTransitionFile();

	}

	/**
	 * Extracts labels for each state and its edges defined in the digraph. This
	 * is based on the assumption that all reaction rules have a unique keyword
	 * such as its name that defines each rule. Extraction is done based on the
	 * presence of a keyword in a source state and it being missing in the
	 * target state The Digraph in the TransitionSystem class is updated after
	 * this with the labels name
	 */
	public Digraph<Integer> extractLabels() {

		Digraph<Integer> digraph = transitionSystem.getDigraph();
		ArrayList<String> labels = new ArrayList<String>();
		String label = "";

		rulesKeywords = FileManipulator.readFile(outputPath + "/" + rulesKeywordsFileName);
		
		//remove any unnecessary white spaces
		for(int i=0;i<rulesKeywords.length; i++) {
			rulesKeywords[i]=rulesKeywords[i].trim();
		}
		
		
		for (Integer stateSrc : digraph.getNodes()) {
			System.out.println(stateSrc);
			System.out.println(digraph.outboundNeighbors(stateSrc));
			for (Integer stateDes : digraph.outboundNeighbors(stateSrc)) {
				label = extractLabel(stateSrc, stateDes);
				labels.add(label);
				digraph.setLabel(stateSrc, stateDes, label);
			}
		}
		
		System.out.println(digraph);
		return digraph;

	}

	/**
	 * Extracts labels for state transitions based on the assumption that all
	 * reaction rules have a unique keyword such as its name that defines each
	 * rule. Extraction is done based on the presence of a keyword in a source
	 * state and it being missing in the target state. It is assumed that there
	 * is a txt file in the output folder holding the keywords for rules (named
	 * "rules_keywords") in which each rule keyword is separated by a semicolon. Also the
	 * states are in json format
	 * 
	 */
	public String extractLabel(Integer stateSrc, Integer stateDes) {

		JSONParser parser = new JSONParser();
		String tmp;
		ArrayList<String> srcControlNames = new ArrayList<String>();
		ArrayList<String> desControlNames = new ArrayList<String>();
		ArrayList<String> missingControls = new ArrayList<String>();
		String label = "";
		int index = 0;

		boolean isLabelFound = false;

		try {
			JSONObject src = (JSONObject) parser.parse(new FileReader(outputPath + "/" + stateSrc + ".json"));
			JSONObject des = (JSONObject) parser.parse(new FileReader(outputPath + "/" + stateDes + ".json"));

			JSONArray arSrc = (JSONArray) src.get("nodes");
			Iterator<JSONObject> itSrc = arSrc.iterator();

			JSONArray arDes = (JSONArray) des.get("nodes");
			Iterator<JSONObject> itDes = arDes.iterator();

			// get controls in the source state file
			while (itSrc.hasNext()) {
				tmp = (String) ((JSONObject) itSrc.next().get("control")).get("control_id");
				srcControlNames.add(tmp);
			}

			// get controls in the destination state file
			while (itDes.hasNext()) {
				tmp = (String) ((JSONObject) itDes.next().get("control")).get("control_id");
				desControlNames.add(tmp);

			}

			// find controls of source state that are missing in the destination
			// state
			for (String controlName : srcControlNames) {
				if (!desControlNames.contains(controlName)) {
					missingControls.add(controlName);
					// check if the control name is a rule keyword
					for (String ruleKeyword : rulesKeywords) {
						if (ruleKeyword.equalsIgnoreCase(controlName)) {
							label = controlName;
							isLabelFound = true;
							break;
						}
					}
				} else {
					index = desControlNames.indexOf(controlName);
					if (index != -1) {
						desControlNames.remove(index);
					}

				}

				if (isLabelFound) {
					break;
				}
			}

			// System.out.println(state.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return label;
	}

	public void createNewLabelledTransitionFile() {
		StringBuilder res = new StringBuilder();
		ArrayList<Integer> nodes = (ArrayList) transitionSystem.getDigraph().getNodes();
		Digraph<Integer> digraph = transitionSystem.getDigraph();
		float prob = 0;
		String label;
		String newFileName = transitionFileName + "_labelled";
		res.append(nodes.size()).append(" ").append(digraph.getNumberOfEdges()).append("\r\n");
		for (Integer state : transitionSystem.getDigraph().getNodes()) {
			for (Integer stateDes : digraph.outboundNeighbors(state)) {
				prob = digraph.getProbability(state, stateDes);
				label = digraph.getLabel(state, stateDes);
				if (prob != -1) {
					if (label != null && !label.isEmpty()) {
						res.append(state).append(" ").append(stateDes).append(" ").append(prob).append(" ")
								.append(label).append("\r\n");
					} else {
						res.append(state).append(" ").append(stateDes).append(" ").append(prob).append("\r\n");
					}
				} else {
					if (label != null && !label.isEmpty()) {
						res.append(state).append(" ").append(stateDes).append(" ").append(label).append("\r\n");
					} else {
						res.append(state).append(" ").append(stateDes).append("\r\n");
					}
				}
			}
		}

		// write the new file
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(outputPath + "/" + newFileName));

			writer.write(res.toString());

			writer.close();
		} catch (IOException e1) { // TODO Auto-generated catch
			e1.printStackTrace();
		}
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getTransitionFileName() {
		return transitionFileName;
	}

	public void setTransitionFileName(String transitionFileName) {
		this.transitionFileName = transitionFileName;
	}

	public String getRulesKeywordsFileName() {
		return rulesKeywordsFileName;
	}

	public void setRulesKeywordsFileName(String rulesKeywordsFileName) {
		this.rulesKeywordsFileName = rulesKeywordsFileName;
	}
	
	
	/*
	 * public void getRulesStates() {
	 * 
	 * if (predicatesFileLines == null || predicatesFileLines.length == 0) { //
	 * remove "sb3_" after testing predicatesFileLines =
	 * FileManipulator.readFile(predicateFilePath); }
	 * 
	 * ReactionRule tmpR = null; String ruleName; ArrayList<Integer> states;
	 * 
	 * // format: e.g., label "phi" = x = 14 | x = 13 | x = 11 | x = 12 String[]
	 * tmp = null; for (String line : predicatesFileLines) { tmpR = null; tmp =
	 * line.split("=|\\|");
	 * 
	 * //checks if the current predicate being checked is a reaction rule or not
	 * if(!tmp[0].split("\"")[1].substring(tmp[0].split("\"")[1].lastIndexOf('_'
	 * )).contentEquals("_redex") &&
	 * !tmp[0].split("\"")[1].substring(tmp[0].split("\"")[1].lastIndexOf('_')).
	 * contentEquals("_reactum")) { continue; } ruleName =
	 * tmp[0].split("\"")[1].replaceFirst("_redex|_reactum", ""); // gets rule
	 * name
	 * 
	 * if(line.contains("false")) { for (ReactionRule r : reactionRules) { if
	 * (r.getName().contentEquals(ruleName)) {
	 * System.out.println("rule "+r.getName()); reactionRules.remove(r); break;
	 * } } continue; } //if there are no states satisfying the redex or reactum
	 * 
	 * 
	 * // check if rule exists in the linkedList of rules for (ReactionRule r :
	 * reactionRules) { if (r.getName().contentEquals(ruleName)) { tmpR = r;
	 * break; } }
	 * 
	 * if (tmpR == null) { tmpR = new ReactionRule(); }
	 * 
	 * // set reaction rule name tmpR.setName(ruleName); // get states states =
	 * new ArrayList<Integer>(); for (int i = 1; i < tmp.length; i++) { tmp[i] =
	 * tmp[i].trim(); if (tmp[i].matches("\\d+")) { // defines integers
	 * states.add(Integer.parseInt(tmp[i])); } }
	 * 
	 * if (line.contains("redex")) { tmpR.setRedexStates(states); } else if
	 * (line.contains("reactum")) { tmpR.setReactumStates(states); }
	 * 
	 * if (tmpR != null && !reactionRules.contains(tmpR)) {
	 * reactionRules.add(tmpR); }
	 * 
	 * }
	 * 
	 * }
	 */
	/*
	 * public void removeNoneImmediateTransitionStates() {
	 * 
	 * for(ReactionRule r : reactionRules) {
	 * removeNoneImmediateTransitionStates(r); } }
	 */
	/**
	 * Removes all states from the redex and reactum of each rule that has no
	 * direct transition to the redex or reactum
	 */
	/*
	 * public void removeNoneImmediateTransitionStates(ReactionRule rule) {
	 * 
	 * ArrayList<Integer> newRedex = new ArrayList<Integer>();
	 * ArrayList<Integer> newReactum = new ArrayList<Integer>();
	 * LinkedList<GraphPath> tmp;
	 * 
	 * // first for the redex for (Integer stateSrc : rule.getRedexStates()) {
	 * for (Integer stateDes : rule.getReactumStates()) { //shortest path
	 * algorithm can be used tmp = transitionSystem.getPaths(stateSrc,
	 * stateDes); // if tmp has more than one path and if (tmp != null &&
	 * !tmp.isEmpty()) { for (GraphPath p : tmp) { // if it has more than two
	 * states it is not direct so it // is ignored if
	 * (p.getStateTransitions().size() != 2) { continue; } // if a path has the
	 * same first state as stateSrc and // last state as statDes // then the
	 * reaction rule can be the one invoking the // transition //this allows for
	 * when both states are the same (e.g., 1 and 1) if
	 * (p.getStateTransitions().getFirst().compareTo(stateSrc) == 0 &&
	 * p.getStateTransitions().getLast().compareTo(stateDes) == 0) {
	 * if(!newRedex.contains(stateSrc)) { newRedex.add(stateSrc); }
	 * if(!newReactum.contains(stateDes)) { newReactum.add(stateDes); }
	 * 
	 * } } } } }
	 * 
	 * if(newRedex.isEmpty()) { //the rule never invoked in this execution
	 * rule.setRedexStates(null); rule.setReactumStates(null); return; }
	 * 
	 * rule.setRedexStates(newRedex); rule.setReactumStates(newReactum);
	 * 
	 * }
	 */
	/*
	 * public void updateTransitionFile() {
	 * 
	 * StringBuilder res = new StringBuilder(); Integer st1; Integer st2; String
	 * label=""; float probability = -1; String[] tmp;
	 * 
	 * String [] lines = FileManipulator.readFileNewLine(transitionFilePath);
	 * 
	 * //headline containing the number of states and transitions
	 * res.append(lines[0]).append("\r\n"); //str.add(lines[0]); for (int i = 1;
	 * i < lines.length; i++) { label = ""; tmp = lines[i].split(" "); st1 = new
	 * Integer(Integer.parseInt(tmp[0])); st2 = new
	 * Integer(Integer.parseInt(tmp[1]));
	 * 
	 * //find all rules that have states that satisfy st1 and st2
	 * for(ReactionRule r : reactionRules) { if(r != null && r.getRedexStates()
	 * != null && r.getReactumStates() != null) {
	 * if(r.getRedexStates().contains(st1) &&
	 * r.getReactumStates().contains(st2)) { label+=r.getName()+","; } }
	 * 
	 * }
	 * 
	 * //remove extra comma if(label.length()>1) { label = label.substring(0,
	 * label.length()-1); }
	 * 
	 * // if bigraph is probabilistic if (tmp.length == 3) { probability =
	 * Float.parseFloat(tmp[2]);
	 * res.append(st1).append(" ").append(st2).append(" ").append(probability)
	 * .append(" ").append(label).append("\r\n"); // stmt =
	 * st1+" "+st2+" "+" "+probability+" "+label; } else {
	 * res.append(st1).append(" ").append(st2).append(" ")
	 * .append(" ").append(label).append("\r\n"); //stmt =
	 * st1+" "+st2+" "+" "+" "+label; }
	 * 
	 * // System.out.println(stmt); // str.add(stmt); }
	 * 
	 * //write new file String outputFile = transitionFilePath+"_labelled";
	 * BufferedWriter writer; try { writer = new BufferedWriter(new
	 * FileWriter(outputFile));
	 * 
	 * writer.write(res.toString());
	 * 
	 * writer.close(); } catch (IOException e1) { // TODO Auto-generated catch
	 * block e1.printStackTrace(); }
	 * 
	 * }
	 */

}
