package ie.lero.spare.franalyser.utility;

import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ie.lero.spare.franalyser.GraphPath;
import ie.lero.spare.franalyser.Predicate;

public class TransitionSystem {
	
	private static TransitionSystem transitionSystem = null;
	private static Digraph<Integer> transitionGraph;
	private Integer startState;
	private Integer endState;
	private Predicate predicateDes;
	private Predicate predicateSrc;
	private LinkedList<GraphPath> paths;
	private static String fileName;
	private int numberOfStates;
	
	private TransitionSystem() {
		transitionGraph = new Digraph<Integer>();
		numberOfStates = -1;
		
		//fileName = BigraphAnalyser.getBigrapherExecutionOutputFolder() + "/transitions";
		createDigraph();
	}
	
	public static TransitionSystem getTransitionSystemInstance() {
		
		if(transitionSystem != null) {
			return transitionSystem;
		}
		
		transitionSystem = new TransitionSystem();
		
		return transitionSystem;
		
	}
	
	private void createDigraph() {

		String[] transitionsFileLines = null;
		Integer st1;
		Integer st2;
		float probability = -1;
		String label = null;
		String[] tmp;

		transitionsFileLines = FileManipulator.readFileNewLine(fileName);

		numberOfStates = new Integer(transitionsFileLines[0].split(" ")[0]); //gets the number of states
		
		for (int i = 1; i < transitionsFileLines.length; i++) {
			probability = -1;
			label = null;
			tmp = transitionsFileLines[i].split(" ");
			st1 = new Integer(Integer.parseInt(tmp[0]));
			st2 = new Integer(Integer.parseInt(tmp[1]));
			if (tmp.length >= 3) { // if bigraph is probabilistic
				if (tmp[2].matches("^(?:(?:\\-{1})?\\d+(?:\\.{1}\\d+)?)$")) { //if the 3rd element is a probability
					probability = Float.parseFloat(tmp[2]);
					if(tmp.length == 4) { //if it has labels
						label = tmp[3];
				}
				
				} else { //if there is no probab
					label = tmp[2];
				}
			}
			transitionGraph.add(st1, st2, probability, label);	
		}
		
	}
	
	private void createDigraphFromJSON() {

		//to be done************
		
		String[] transitionsFileLines = null;
		Integer st1;
		Integer st2;
		float probability = -1;
		String label = null;
		String[] tmp;

		//transitionsFileLines = FileManipulator.readFileNewLine(fileName);

		JSONParser parser = new JSONParser();
		JSONObject obj;
		
		try {
			obj = (JSONObject)parser.parse(new FileReader(fileName));
			JSONArray ary = (JSONArray)obj.get("transition_system");
			numberOfStates = ary.size();
			
		} catch(Exception ie) {
			ie.printStackTrace();
		}
		
		//numberOfStates = new Integer(transitionsFileLines[0].split(" ")[0]); //gets the number of states
		
		for (int i = 1; i < transitionsFileLines.length; i++) {
			probability = -1;
			label = null;
			tmp = transitionsFileLines[i].split(" ");
			st1 = new Integer(Integer.parseInt(tmp[0]));
			st2 = new Integer(Integer.parseInt(tmp[1]));
			if (tmp.length >= 3) { // if bigraph is probabilistic
				if (tmp[2].matches("^(?:(?:\\-{1})?\\d+(?:\\.{1}\\d+)?)$")) { //if the 3rd element is a probability
					probability = Float.parseFloat(tmp[2]);
					if(tmp.length == 4) { //if it has labels
						label = tmp[3];
				}
				
				} else { //if there is no probab
					label = tmp[2];
				}
			}
			transitionGraph.add(st1, st2, probability, label);	
		}
		
	}
	
	public int loadNumberOfStates() {
		
		JSONParser parser = new JSONParser();
		JSONObject obj;
		
		try {
			obj = (JSONObject)parser.parse(new FileReader(fileName));
			JSONArray ary = (JSONArray)obj.get("transition_system");
			numberOfStates = ary.size();
			
		} catch(Exception ie) {
			ie.printStackTrace();
		}
		
		return numberOfStates;
	}
	
	public LinkedList<GraphPath> getPaths(Predicate predSrc, Predicate predDes) {
		LinkedList<Integer> v = new LinkedList<Integer>();
		predicateSrc = predSrc;
		predicateDes = predDes;
		GraphPath tmpG;
		int size = 0;
		LinkedList<Integer> tmp;
		paths = new LinkedList<GraphPath>();
		for (Integer startState : predSrc.getBigraphStates()) {
			v.clear();

			this.startState = startState;
			v.add(this.startState);
			for (Integer endState : predDes.getBigraphStates()) {
				this.endState = endState;
				
				if(startState.compareTo(endState) == 0) {
					tmpG = new GraphPath();
					tmpG.setPredicateSrc(predicateSrc);
					predicateSrc.addIntraSatisfiedState(startState);
					tmpG.setPredicateDes(predicateDes);
					predicateDes.addIntraSatisfiedState(startState);
					tmp = new LinkedList<Integer>();
					tmp.add(startState);
					tmp.add(startState);
					tmpG.setStateTransitions(tmp);
					paths.add(tmpG);
					
				} else {
					depthFirst(transitionGraph, v);
					if(paths.size() > size) {
						predicateSrc.addIntraSatisfiedState(startState);
						predicateDes.addIntraSatisfiedState(endState);
						size = paths.size();
					}
				}
				
			}
		}
		
		return paths;
	}
	
	public LinkedList<GraphPath> getPaths(Predicate predSrc, Predicate predDes, boolean useSatisfiedStates) {
		LinkedList<Integer> v = new LinkedList<Integer>();
		predicateSrc = predSrc;
		predicateDes = predDes;
		GraphPath tmpG;
		int size = 0;
		LinkedList<Integer> tmp;
		paths = new LinkedList<GraphPath>();
		for (Integer startState : predSrc.getStatesIntraSatisfied()) {
			v.clear();

			this.startState = startState;
			v.add(this.startState);
			for (Integer endState : predDes.getStatesIntraSatisfied()) {
				this.endState = endState;
				
				if(startState.compareTo(endState) == 0) {
					tmpG = new GraphPath();
					tmpG.setPredicateSrc(predicateSrc);
					predicateSrc.addInterSatisfiedState(startState);
					tmpG.setPredicateDes(predicateDes);
					predicateDes.addInterSatisfiedState(startState);
					tmp = new LinkedList<Integer>();
					tmp.add(startState);
					tmp.add(startState);
					tmpG.setStateTransitions(tmp);
					paths.add(tmpG);
					
				} else {
					depthFirst(transitionGraph, v);
					if(paths.size() > size) { //can be changed to check whether a state matches all other states
						predicateSrc.addInterSatisfiedState(startState);
						predicateDes.addInterSatisfiedState(endState);
						size = paths.size();
					}
				}
				
			}
		}
		
		return paths;
	}
	
	public LinkedList<GraphPath> getPaths(Integer srcState, Integer desState) {
		LinkedList<Integer> v = new LinkedList<Integer>();
		paths = new LinkedList<GraphPath>();
		predicateSrc = null;
		predicateDes = null;
		GraphPath tmpG;
		LinkedList<Integer> tmp;
		
		if(srcState.compareTo(desState) == 0) {
			tmpG = new GraphPath();
			tmpG.setPredicateSrc(null);
			tmpG.setPredicateDes(null);
			tmp = new LinkedList<Integer>();
			tmp.add(srcState);
			tmp.add(srcState);
			tmpG.setStateTransitions(tmp);
			paths.add(tmpG);
			
			return paths;
		}
		
			this.startState = srcState;
			v.add(this.startState);
				this.endState = desState;
				depthFirst(transitionGraph, v);
				
				return paths;
			}
		
	
	private void depthFirst(Digraph<Integer> graph, LinkedList<Integer> visited) {
		List<Integer> nodes = graph.outboundNeighbors(visited.getLast());

		// examine adjacent nodes
		for (Integer node : nodes) {
			if (visited.contains(node)) {
				continue;
			}
			if (node.compareTo(endState) == 0) {
				visited.add(node);
				addTransitiontoList(visited);
				visited.removeLast();
				break;
			}
		}
		for (Integer node : nodes) {
			if (visited.contains(node) || node.compareTo(endState) == 0) {
				continue;
			}
			visited.addLast(node);
			depthFirst(graph, visited);
			visited.removeLast();
		}
	}

	private void addTransitiontoList(List<Integer> transition) {
		LinkedList<Integer> newList = new LinkedList<Integer>();
		GraphPath path = new GraphPath();

		newList.addAll(transition);

		path.setPredicateSrc(predicateSrc);
		path.setPredicateDes(predicateDes);
		path.setStateTransitions(newList);
		paths.add(path);

	}
	
	public static void setFileName(String fileName) {
		TransitionSystem.fileName = fileName;
		transitionSystem = null;
	}
	
	public Digraph<Integer> getDigraph() {
		return transitionGraph;
	}
	
	public String getLabel(Integer srcState, Integer desState) {
		
		return transitionGraph.getLabel(srcState, desState);
	}
	
	public float getProbability(Integer srcState, Integer desState) {
		
		return transitionGraph.getProbability(srcState, desState);
	}
	
	public int getNumberOfStates() {
		return numberOfStates;
	}

	public void setNumberOfStates(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}

	public String toString() {
		return transitionGraph.toString();
	}
}
