package ie.lero.spare.franalyser;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ie.lero.spare.franalyser.utility.BigraphNode;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;

public class SystemInstanceHandler {

	private static String outputFolder;
	private static SystemExecutor executor;
	private static TransitionSystem transitionSystem;
	private static String fileName;
	private static boolean isSystemAnalysed = false;
	private static HashMap<Integer, Bigraph> states;
	
	public static boolean analyseSystem(String fileName) {
		

		if(executor == null || fileName == null) {
			return isSystemAnalysed = false;
		}
		
		if(isSystemAnalysed && fileName.equals(SystemInstanceHandler.fileName)) {
			return true;
		}
		
		SystemInstanceHandler.fileName = fileName;
		
		outputFolder = executor.execute(fileName);
		
		if(outputFolder != null) {
			return isSystemAnalysed = true;
		}
		
		return isSystemAnalysed = false;
	}
	
	public static boolean analyseSystem(String fileName, SystemExecutor exec) {
		executor = exec;
		return analyseSystem(fileName);
	}

	public static String getOutputFolder() {
		return outputFolder;
	}

	public static void setOutputFolder(String outputFolder) {
		SystemInstanceHandler.outputFolder = outputFolder;
	}

	public static SystemExecutor getExecutor() {
		return executor;
	}

	public static void setExecutor(SystemExecutor executor) {
		SystemInstanceHandler.executor = executor;
	}

	public static TransitionSystem getTransitionSystem() {
		if(transitionSystem == null) {
			if(outputFolder != null) {
				TransitionSystem.setFileName(outputFolder+"/transitions");
				transitionSystem = TransitionSystem.getTransitionSystemInstance();
			}
		}
		
		return transitionSystem;
	}

	public static void setTransitionSystem(TransitionSystem transitionsystem) {
		SystemInstanceHandler.transitionSystem = transitionsystem;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String fileName) {
		SystemInstanceHandler.fileName = fileName;
		clearSystem();
	}

	public static boolean isSystemAnalysed() {
		return isSystemAnalysed;
	}

	public static void setSystemAnalysed(boolean isSystemAnalysed) {
		SystemInstanceHandler.isSystemAnalysed = isSystemAnalysed;
	}
	
	public static HashMap<Integer, Bigraph> getStates() {
		if(states != null) {
			return states;
		} 
		
		return loadStates();	
	}
	
	public static HashMap<Integer, Bigraph> loadStates() {
		HashMap<Integer, Bigraph> states = new HashMap<Integer, Bigraph>();
		
		//for testing
		outputFolder = "sb3_output";
		int numOfStates = getTransitionSystem().getNumberOfStates();
		
		JSONObject state;
		JSONParser parser = new JSONParser();
		
		
		for(int i=0;i<numOfStates;i++) {
			try {
				//read state from file
				state = (JSONObject) parser.parse(new FileReader(outputFolder+"/"+i+".json"));
				states.put(i, convertJSONtoBigraph(state));
				
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//convert them to bigraph format
		//add them with state number to the hash map
		//return the hashmap
		return states;
	}
	public static Bigraph convertJSONtoBigraph(JSONObject state){
		
		Bigraph result = null;
		LinkedList<String> controls = new LinkedList<String>();
		String tmp;
		String tmpArity;
		JSONObject tmpObj;
		JSONObject tmpCtrl;
		HashMap<Integer,BigraphNode> nodes = new HashMap<Integer, BigraphNode>();
		BigraphNode node;
		JSONArray ary;
		Iterator<JSONObject> it;
		int src, target;
		
		SignatureBuilder sigBuilder = new SignatureBuilder();
		LinkedList<String> outerNames = new LinkedList<String>();
		LinkedList<String> innerNames = new LinkedList<String>();
		
		// number of roots, sites, and nodes respectively
		int numOfRoots = Integer.parseInt(((JSONObject)state.get("place_graph")).get("regions").toString());
		int numOfSites = Integer.parseInt(((JSONObject)state.get("place_graph")).get("sites").toString());
		int numOfNodes = Integer.parseInt(((JSONObject)state.get("place_graph")).get("nodes").toString());
		
		//get controls & their arity [defines signature]. Controls are assumed to be active (i.e. true)
		ary = (JSONArray) state.get("nodes");
		it = ary.iterator();
		while(it.hasNext()) {
			node = new BigraphNode();
			tmpObj = (JSONObject) it.next(); //gets hold of node info
			
			
			tmpCtrl = (JSONObject)tmpObj.get("control");			
			tmp = tmpCtrl.get("control_id").toString();
			tmpArity = tmpCtrl.get("control_arity").toString();
		
			if(!controls.contains(tmp)) {
				controls.add(tmp); //to avoid duplicates
				sigBuilder.add(tmp,true, Integer.parseInt(tmpArity));
			}
			
			//set node id
			node.setId(Integer.parseInt(tmpObj.get("node_id").toString()));
			//set node control
			node.setControl(tmp);
			nodes.put(node.getId(), node);
		}
		
		//get parents for nodes from the place_graph=> dag. Caution using the roots and sites numbers
		ary = (JSONArray)((JSONObject)state.get("place_graph")).get("dag");
		it = ary.iterator();
		while(it.hasNext()) {
			tmpObj = (JSONObject) it.next(); //gets hold of node info
			src = Integer.parseInt(tmpObj.get("source").toString());
			target = Integer.parseInt(tmpObj.get("target").toString());
			
			if(src >= numOfRoots) {
				//set parent node in the target node
				nodes.get(target).setParent(nodes.get(src-numOfRoots));
				//add child node to source node
				nodes.get(src).addBigraphNode(nodes.get(target));
			} else { //target parent is a root
				nodes.get(target).setParentRoot(src);
			}
			
			//should pay attention to sites
		
		}
		
		for(BigraphNode n : nodes.values()) {
			System.out.println(n.toString());
		}
		BigraphBuilder biBuilder = new BigraphBuilder(sigBuilder.makeSignature());
		
		return result;
	}
	
	public static void clearSystem(){
		isSystemAnalysed = false;
		outputFolder = null;
		states = null;
		System.gc();
		
	}

	public static void main(String [] args) {
		
		JSONObject state;
		JSONParser parser = new JSONParser();
		
		try {
			state = (JSONObject) parser.parse(new FileReader("output/1.json"));
			convertJSONtoBigraph(state);
		
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
}
