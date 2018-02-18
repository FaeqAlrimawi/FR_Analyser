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
		SignatureBuilder sigBuilder = new SignatureBuilder();
		int numOfRoots = Integer.parseInt(((JSONObject)state.get("place_graph")).get("regions").toString());
	
		
		//get controls & their arity [defines signature]
		JSONArray ary = (JSONArray) state.get("nodes");
		Iterator<JSONObject> it = ary.iterator();
		while(it.hasNext()) {
			tmpObj = (JSONObject) it.next().get("control");
			tmp = tmpObj.get("control_id").toString();
			tmpArity = tmpObj.get("control_arity").toString();
		
			if(!controls.contains(tmp)) {
				controls.add(tmp); //to avoid duplicates
				sigBuilder.add(tmp,true, Integer.parseInt(tmpArity));
			}
			
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
		JSONParser parser = new JSONParser();
		JSONObject state;
		
		try {
			state = (JSONObject) parser.parse(new FileReader("output/0.json"));
			
			int numOfRoots =Integer.parseInt(((JSONObject)state.get("place_graph")).get("regions").toString());
			System.out.println(numOfRoots);
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
}
