package ie.lero.spare.franalyser;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Site;

public class SystemInstanceHandler {

	private static String outputFolder;
	private static SystemExecutor executor;
	private static TransitionSystem transitionSystem;
	private static String fileName;
	private static boolean isSystemAnalysed = false;
	private static HashMap<Integer, Bigraph> states;
	private static Signature globalBigraphSignature;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private static LocalDateTime now;
	private static boolean isDebugging = true;

	public static boolean analyseSystem(String fileName) {

		if (executor == null || fileName == null) {
			return isSystemAnalysed = false;
		}

		if (isSystemAnalysed && fileName.equals(SystemInstanceHandler.fileName)) {
			return true;
		}

		SystemInstanceHandler.fileName = fileName;

		outputFolder = executor.execute(fileName);

		if (outputFolder != null) {
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
		if (transitionSystem == null) {
			if (outputFolder != null) {
				TransitionSystem.setFileName(outputFolder + "/transitions.txt");
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
		return states;
	}

	public static Signature getGlobalBigraphSignature() {
		return globalBigraphSignature;
	}

	public static void setBigraphSignature(Signature bigraphSignature) {
		SystemInstanceHandler.globalBigraphSignature = bigraphSignature;
	}

	public static void print(String msg) {

		if (isDebugging) {
			System.out.println(msg);
		}
	}

	/**
	 * converts the states of a bigraph execution to bigraph objects then adds
	 * them to a hashmap
	 * 
	 * @return HashMap containing the Bigraphs keyed using their state number
	 *         (e.g., key 0, value Bigraph0)
	 */
	public static HashMap<Integer, Bigraph> loadStates() {

		// for testing
		// outputFolder = "sb3_output";

		states = new HashMap<Integer, Bigraph>();
		// should rethink how to know how many states are there/ Currently
		// depends on the transition file

		int numOfStates = getTransitionSystem().getNumberOfStates();
	
		JSONObject state;
		JSONParser parser = new JSONParser();
		boolean isSignatureCreated = false;

		print("[" + dtf.format(LocalDateTime.now()) + "] loading states...");

		// create signature
		if (createSignatureFromBRS() != null) {
			print("[" + dtf.format(LocalDateTime.now()) + "] bigraph Signature is created.");
			isSignatureCreated = true;
		} else {
			print("Creating Bigraph signature by visiting all states");
			if (createSignatureFromStates() != null) {
				print("[" + dtf.format(LocalDateTime.now()) + "] bigraph Signature is created.");
				isSignatureCreated = true;
			} else {
				print("Could not create signature...exiting the program.");
			}
		}

		if (isSignatureCreated) {
			for (int i = 0; i < numOfStates; i++) {
				try {
					// read state from file
					state = (JSONObject) parser.parse(new FileReader(outputFolder + "/" + i + ".json"));
					Bigraph bigraph = convertJSONtoBigraph(state);
					states.put(i, bigraph);

				} catch (IOException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			print("[" + dtf.format(LocalDateTime.now()) + "] " + states.size() + " states loaded.");
		} else {
			print("Could not create signature...exiting the program.");
			return null;
		}
		
		return states;
	}

	/**
	 * creates a state signature based on the provied JSONObject
	 * 
	 * @param state
	 *            the JSONObject holding the state information
	 * @return Signature object
	 */
	public static Signature createStateSignature(JSONObject state) {

		SignatureBuilder sigBuilder = new SignatureBuilder();
		JSONArray ary;
		Iterator<?> it;
		JSONObject tmpObj, tmpCtrl;
		String tmp, tmpArity;
		LinkedList<String> controls = new LinkedList<String>();

		ary = (JSONArray) state.get("nodes");
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info

			tmpCtrl = (JSONObject) tmpObj.get("control");
			tmp = tmpCtrl.get("control_id").toString();
			tmpArity = tmpCtrl.get("control_arity").toString();

			if (!controls.contains(tmp)) {
				// to avoid duplicates
				controls.add(tmp);
				sigBuilder.add(tmp, true, Integer.parseInt(tmpArity));
			}
		}

		return sigBuilder.makeSignature();
	}

	/**
	 * creates a signature from the Bigrapher file provided (i.e. fileName set
	 * by method setFileName)
	 * 
	 * @return The signature of the Bigrapher as a Signature object from the
	 *         LibBig library
	 */
	public static Signature createSignatureFromBRS() {
		SignatureBuilder sigBuilder = new SignatureBuilder();

		String[] lines = FileManipulator.readFileNewLine(fileName);
		String tmp;

		for (int i = 0; i < lines.length; i++) {
			// if there are functions in the control then creating the signature
			// should be done in alternative
			// way i.e. by looking into all states and extracting the controls
			if ((lines[i].startsWith("fun") && lines[i].contains(" ctrl "))
					|| (lines[i].startsWith("atomic") && lines[i].contains(" fun "))) {
				return null;
			}
		}
		// determine the last time the keyword ctrl is used as predicates
		for (int i = 0; i < lines.length; i++) {
			tmp = lines[i];
			if (tmp.startsWith("ctrl") || (tmp.startsWith("atomic") || tmp.contains(" ctrl "))) {
				if (!tmp.contains(";")) {
					for (int j = i + 1; j < lines.length; j++) {
						tmp += lines[j];
						if (lines[j].contains(";")) {
							i = j;
							break;
						}
					}
				}
				// remove comments
				if (tmp.contains("#")) {
					tmp = tmp.split("#")[0];

				}
				// remove semicolon
				tmp = tmp.replace(";", "");
				tmp = tmp.trim();
				String[] tmp2 = tmp.split("=");

				// get control arity
				String controlArity = tmp2[1].trim();

				// get control name
				String[] tmp3 = tmp2[0].split(" ");
				String controlName = tmp3[tmp3.length - 1];

				// if control holds brackets i.e. () then create a global
				// signature from all other states
				if (controlName.contains("(")) {
					controlName = controlName.substring(0, controlName.indexOf("("));
				}
				controlName.trim();

				// create signature
				sigBuilder.add(controlName, true, Integer.parseInt(controlArity));

			}
		}

		globalBigraphSignature = sigBuilder.makeSignature();

		return globalBigraphSignature;
	}

	/**
	 * creates a global signature by traversing through all states and
	 * extracting controls from the nodes
	 * 
	 * @return Signature object
	 */
	public static Signature createSignatureFromStates() {
		SignatureBuilder sigBuilder = new SignatureBuilder();
		JSONArray ary;
		Iterator<?> it;
		JSONObject tmpObj, tmpCtrl;
		String tmp, tmpArity;
		LinkedList<String> controls = new LinkedList<String>();
		int numOfStates = getTransitionSystem().getNumberOfStates();
		JSONParser parser = new JSONParser();
		JSONObject state;

		for (int i = 0; i < numOfStates; i++) {
			try {
				// read state from file
				state = (JSONObject) parser.parse(new FileReader(outputFolder + "/" + i + ".json"));
				ary = (JSONArray) state.get("nodes");
				it = ary.iterator();
				while (it.hasNext()) {
					tmpObj = (JSONObject) it.next(); // gets hold of node info

					tmpCtrl = (JSONObject) tmpObj.get("control");
					tmp = tmpCtrl.get("control_id").toString();
					tmpArity = tmpCtrl.get("control_arity").toString();

					if (!controls.contains(tmp)) {
						// to avoid duplicates
						controls.add(tmp);
						sigBuilder.add(tmp, true, Integer.parseInt(tmpArity));
					}

				}
			} catch (IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		globalBigraphSignature = sigBuilder.makeSignature();

		return globalBigraphSignature;
	}

	/**
	 * converts a given bigraph in JSON format to a Bigraph object from the
	 * LibBig library. A signature should be created first using the
	 * buildSignature method.
	 * 
	 * @param state
	 *            the JSON object containing the bigtaph
	 * @return Bigraph object
	 */
	public static Bigraph convertJSONtoBigraph(JSONObject state) {

		String tmp;
		String tmpArity;
		JSONObject tmpObj;
		JSONObject tmpCtrl;
		HashMap<String, BigraphNode> nodes = new HashMap<String, BigraphNode>();
		BigraphNode node;
		JSONArray ary;
		JSONArray innerAry;
		JSONArray outerAry;
		JSONArray portAry;
		Iterator<JSONObject> it;
		Iterator<JSONObject> itInner;
		Iterator<JSONObject> itOuter;
		Iterator<JSONObject> itPort;
		int src, target;
		LinkedList<String> outerNames = new LinkedList<String>();
		LinkedList<String> innerNames = new LinkedList<String>();
		LinkedList<String> outerNamesFull = new LinkedList<String>();
		LinkedList<String> innerNamesFull = new LinkedList<String>();

		HashMap<String, OuterName> libBigOuterNames = new HashMap<String, OuterName>();
		HashMap<String, InnerName> libBigInnerNames = new HashMap<String, InnerName>();
		HashMap<String, Node> libBigNodes = new HashMap<String, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();
		LinkedList<Site> libBigSites = new LinkedList<Site>();

		// number of roots, sites, and nodes respectively
		int numOfRoots = Integer.parseInt(((JSONObject) state.get("place_graph")).get("regions").toString());
		int numOfSites = Integer.parseInt(((JSONObject) state.get("place_graph")).get("sites").toString());
		int numOfNodes = Integer.parseInt(((JSONObject) state.get("place_graph")).get("nodes").toString());

		// get controls & their arity [defines signature]. Controls are assumed
		// to be active (i.e. true)
		ary = (JSONArray) state.get("nodes");
		it = ary.iterator();
		while (it.hasNext()) {
			node = new BigraphNode();
			tmpObj = (JSONObject) it.next(); // gets hold of node info

			tmpCtrl = (JSONObject) tmpObj.get("control");
			tmp = tmpCtrl.get("control_id").toString();
			tmpArity = tmpCtrl.get("control_arity").toString();

			// set node id
			node.setId(tmpObj.get("node_id").toString());
			// set node control
			node.setControl(tmp);
			nodes.put(node.getId(), node);
		}

		// get parents for nodes from the place_graph=> dag. Caution using the
		// roots and sites numbers
		ary = (JSONArray) ((JSONObject) state.get("place_graph")).get("dag");
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get("source").toString());
			target = Integer.parseInt(tmpObj.get("target").toString());

			if (src >= numOfRoots) {
				// set parent node in the target node
				nodes.get(Integer.toString(target)).setParent(nodes.get(Integer.toString(src - numOfRoots)));
				// add child node to source node
				nodes.get(Integer.toString(src - numOfRoots)).addChildNode(nodes.get(Integer.toString(target)));
			} else { // source is a root
				nodes.get(Integer.toString(target)).setParentRoot(src);

			}

			// should pay attention to sites

		}

		// get outer names and inner names for the nodes. Currently, focus on
		// outer names
		// while inner names are extracted they are not updated in the nodes
		ary = (JSONArray) (state.get("link_graph"));
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			outerNames.clear();
			innerNames.clear();

			// get inner names
			innerAry = (JSONArray) (tmpObj.get("inner"));
			itInner = innerAry.iterator();
			while (itInner.hasNext()) {
				innerNames.add(itInner.next().get("name").toString());
				innerNamesFull.addAll(innerNames);
			}

			// get outer names
			outerAry = (JSONArray) (tmpObj.get("outer"));
			itOuter = outerAry.iterator();
			while (itOuter.hasNext()) {
				outerNames.add(itOuter.next().get("name").toString());
				outerNamesFull.addAll(outerNames);
			}

			// get nodes connected to outer names. Inner names should be
			// considered
			portAry = (JSONArray) (tmpObj.get("ports"));
			itPort = portAry.iterator();
			while (itPort.hasNext()) {
				node = nodes.get(itPort.next().get("node_id").toString());
				node.addOuterNames(outerNames);
				node.addInnerNames(innerNames);
			}
		}

		BigraphBuilder biBuilder = new BigraphBuilder(globalBigraphSignature);

		// create roots for the bigraph
		for (int i = 0; i < numOfRoots; i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}

		// create outer names
		for (String outer : outerNamesFull) {
			libBigOuterNames.put(outer, biBuilder.addOuterName(outer));
		}

		// create inner names
		for (String inner : innerNamesFull) {
			libBigInnerNames.put(inner, biBuilder.addInnerName(inner));
		}

		// initial creation of nodes
		for (BigraphNode nd : nodes.values()) {
			if (libBigNodes.containsKey(nd.getId())) {
				continue;
			}
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes);
		}

		// add sites to bigraph
		for (BigraphNode n : nodes.values()) {
			if (n.hasSite()) {
				biBuilder.addSite(libBigNodes.get(n.getId()));
			}
		}

		return biBuilder.makeBigraph();
	}

	private static Node createNode(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots,
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes) {

		LinkedList<Handle> names = new LinkedList<Handle>();
		for (String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}

		// if the parent is a root
		if (node.isParentRoot()) { // if the parent is a root
			Node n = biBuilder.addNode(node.getControl(), libBigRoots.get(node.getParentRoot()), names);
			nodes.put(node.getId(), n);
			return n;
		}

		// if the parent is already created as a node in the bigraph
		if (nodes.containsKey(node.getParent().getId())) {
			Node n = biBuilder.addNode(node.getControl(), nodes.get(node.getParent().getId()), names);
			nodes.put(node.getId(), n);
			return n;
		}

		Node n = biBuilder.addNode(node.getControl(),
				createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes), names);
		nodes.put(node.getId(), n);
		return n;

	}

	public static void clearSystem() {
		isSystemAnalysed = false;
		outputFolder = null;
		states = null;
		System.gc();

	}

	
	public static void main(String[] args) {

		fileName = "sav/savannah-general.big";
		outputFolder = "sav/output10000";
		//fileName = "sb3.big";
		//outputFolder = "sb3_output";
		Matcher matcher = new Matcher();
		JSONParser parser = new JSONParser();

		Bigraph redex;
//		createSignatureFromStates();
		//loadStates();
		//print(""+getTransitionSystem().loadNumberOfStates());

		try {
			if (loadStates() == null) {
				return;
			}
			redex = convertJSONtoBigraph((JSONObject) parser.parse(new FileReader(outputFolder + "/99.json")));
			/*for (int i = 0; i < states.size(); i++) {
				if (matcher.match(states.get(i), redex).iterator().hasNext()) {
					print("state " + i + " matched");
				}
			}*/
			int numberOFThreads = 15;
			int size = 10000/numberOFThreads;
			BigraphMatcherThread [] threads = new BigraphMatcherThread[numberOFThreads];
			print("matching started at " + dtf.format(LocalDateTime.now()));
			for(int i=0;i<numberOFThreads;i++) {
//				print((i*size)+ " "+((i*size)+size));
				threads[i] = new BigraphMatcherThread(i*size, (i*size)+size, redex);
				threads[i].start();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	}

}

class BigraphMatcherThread implements Runnable {

	private int startIndex;
	private int endIndex;
	private LinkedList<Integer> statesMatched;
	private Bigraph redex;
	private String threadID;
	private Thread t;
	
	 public BigraphMatcherThread(int start, int end, Bigraph redex) {
		startIndex = start;
		endIndex = end;
		statesMatched = new LinkedList<Integer>();		
		this.redex = redex;
		threadID= "bla";
	}
	 
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Matcher matcher = new Matcher();
		
		for(int i=startIndex;i<endIndex;i++) {
			if (matcher.match(SystemInstanceHandler.getStates().get(i), redex).iterator().hasNext()) {
				statesMatched.add(i);
				System.out.println("state " + i + " matched");
		}
		}
	}
	
	public void start() {
		//System.out.println("Starting " + threadID);
		if (t == null) {
			t = new Thread(this, "" + threadID);
			t.start();
		}
	}


}
