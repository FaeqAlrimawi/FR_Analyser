package ie.lero.spare.franalyser.utility;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ie.lero.spare.franalyser.JSONTerms;
import ie.lero.spare.franalyser.SystemExecutor;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Site;

public class BigrapherHandler implements SystemExecutor {

	private String bigrapherFileName; //BRS file name e.g., smart_building.big
	private String bigrapherValidateCmd = "bigrapher validate -n ";
	private String outputFolder;
	private String bigrapherOutputFormat = "json";
	private int maximumNumberOfStates = 1000;
	private String validBigrapherString = "model file parsed correctly";
	private TransitionSystem transitionSystem;
	private String transitionFileName = "transitions.txt";
	private Signature bigraphSignature;
	private static HashMap<Integer, Bigraph> states;
	
	private boolean isTesting = true; //used to skip executing the bigrapher file
	
	public BigrapherHandler(String BRSfileName) {
	
		bigrapherFileName = BRSfileName;
		//output folder = fileName_output e.g., smart_building_output
		outputFolder = bigrapherFileName.split("\\.")[0] + "_output"; 
	}
	
	public BigrapherHandler(String BRSfileName, String outputFolder) {
		
		bigrapherFileName = BRSfileName;
		this.outputFolder = outputFolder;
	}
	
	/**
	 * Validates and then executes the Bigrapher file (*.big)
	 * @return output folder name if execution is successeful. Otherwise, it returns null
	 */
	public String execute() {
		
		if(isTesting) {
			return outputFolder;
		}
		
		if (validateBigraph()) {

			Process proc;
			String cmd = createDefaultBigrapherExecutionCmd();

			Runtime r = Runtime.getRuntime();
			try {
				r.exec("mkdir " + outputFolder);

				// for future development this could run in own thread for
				// multiprocessing
				proc = r.exec(cmd);

				// check the output of the command, if it has something then
				// there
				// are errors otherwise its ok
				Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
				String result = s.hasNext() ? s.next() : "";

				if (result != null) {
					if (!result.toLowerCase().isEmpty()) {
						System.out.println("Execution could not be completed. Please see possible issues below:");
						System.out.println(result);
					} else {
						System.out.println("Execution is Done");

						// should be a step taken by the main program
						// createDigraph();

					}
				}

				return outputFolder;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	private boolean validateBigraph() {
		boolean isValid = false;
		Process proc;
		Runtime r = Runtime.getRuntime();
		try {

			proc = r.exec(bigrapherValidateCmd + bigrapherFileName);
			
			Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";

			if (result != null) {
				if (result.toLowerCase().contains(validBigrapherString)) {
					System.out.println(bigrapherFileName + " is valid");
					isValid = true;
				} else {
					System.out.println(bigrapherFileName + " is not valid. Please see possible issues below:");
					System.out.println(result + "");
					isValid = false;

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isValid = false;
		}
		return isValid;
	}

	private String createDefaultBigrapherExecutionCmd() {
		StringBuilder res = new StringBuilder();
		res.append("bigrapher full -q -M ").append(maximumNumberOfStates).append(" -t ")
				.append(outputFolder).append("/transitionSystem -s ")
				.append(outputFolder).append(" -l ").append(outputFolder)
				.append("/predicates -p ").append(outputFolder).append("/transitions -f ")
				.append(bigrapherOutputFormat).append(" ").append(bigrapherFileName);

		return res.toString();
	}

	public int getMaximumNumberOfStates() {
		return maximumNumberOfStates;
	}

	public void setMaximumNumberOfStates(int maxStates) {
		maximumNumberOfStates = maxStates;
	}

	public String getBigrapherOutputFormat() {
		return bigrapherOutputFormat;
	}

	public void setBigrapherOutputFormat(String format) {
		bigrapherOutputFormat = format;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * creates a signature from the Bigrapher file provided (i.e. fileName set
	 * by method setFileName)
	 * 
	 * @return The signature of the Bigrapher as a Signature object from the
	 *         LibBig library
	 */
	public  Signature createSignatureFromBRS() {
		SignatureBuilder sigBuilder = new SignatureBuilder();

		String[] lines = FileManipulator.readFileNewLine(bigrapherFileName);
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

		return sigBuilder.makeSignature();

	}
	
	public TransitionSystem createTransitionSystem() {
		if (transitionSystem == null) {
			if (outputFolder != null) {
				TransitionSystem.setFileName(outputFolder + "/" + transitionFileName);
				transitionSystem = TransitionSystem.getTransitionSystemInstance();
			}
		}

		return transitionSystem;
	}

	/**
	 * creates a signature from the Bigrapher file provided (i.e. fileName set
	 * by method setFileName)
	 * 
	 * @return The signature of the Bigrapher as a Signature object from the
	 *         LibBig library
	 */
	public Signature createSignatureFromBRS(String fileName) {
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

		bigraphSignature = sigBuilder.makeSignature();
		
		return bigraphSignature;
	}

	/**
	 * creates a global signature by traversing through all states and
	 * extracting controls from the nodes
	 * 
	 * @return Signature object
	 */
	public  Signature createSignatureFromStates() {
		SignatureBuilder sigBuilder = new SignatureBuilder();
		JSONArray ary;
		Iterator<?> it;
		JSONObject tmpObj, tmpCtrl;
		String tmp, tmpArity;
		LinkedList<String> controls = new LinkedList<String>();
		int numOfStates = createTransitionSystem().getNumberOfStates();
		JSONParser parser = new JSONParser();
		JSONObject state;

		for (int i = 0; i < numOfStates; i++) {
			try {
				// read state from file
				state = (JSONObject) parser.parse(new FileReader(outputFolder + "/" + i + ".json"));
				ary = (JSONArray) state.get(JSONTerms.BIGRAPHER_NODES);
				it = ary.iterator();
				while (it.hasNext()) {
					tmpObj = (JSONObject) it.next(); // gets hold of node info

					tmpCtrl = (JSONObject) tmpObj.get(JSONTerms.BIGRAPHER_CONTROL);
					tmp = tmpCtrl.get(JSONTerms.BIGRAPHER_CONTROL_ID).toString();
					tmpArity = tmpCtrl.get(JSONTerms.BIGRAPHER_CONTROL_ARITY).toString();

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

		bigraphSignature = sigBuilder.makeSignature();
		
		return bigraphSignature;
	}


	@Override
	public TransitionSystem getTransitionSystem() {

		return createTransitionSystem();
	}

	@Override
	public Signature getBigraphSignature() {
		
		if(bigraphSignature != null) {
			return bigraphSignature;
		}
		
		bigraphSignature = createSignatureFromBRS();
		
		if(bigraphSignature != null) {
			return bigraphSignature;
		}
		
		return bigraphSignature  = createSignatureFromStates();
		
	}
	

	/**
	 * converts the states of a bigraph execution to bigraph objects then adds
	 * them to a hashmap
	 * 
	 * @return HashMap containing the Bigraphs keyed using their state number
	 *         (e.g., key 0, value Bigraph0)
	 */
	public  HashMap<Integer, Bigraph> loadStates() {

		states = new HashMap<Integer, Bigraph>();
		// should rethink how to know how many states are there/ Currently
		// depends on the transition file

		int numOfStates = transitionSystem.getNumberOfStates();
	
		JSONObject state;
		JSONParser parser = new JSONParser();

		if (bigraphSignature != null) {
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
		} else {
			return null;
		}
		
		return states;
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
	public Bigraph convertJSONtoBigraph(JSONObject state) {

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
		int numOfRoots = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_REGIONS).toString());
		int numOfSites = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_SITES).toString());
		int numOfNodes = Integer.parseInt(((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_NODES).toString());
		int edgeNumber = 0;
		
		// get controls & their arity [defines signature]. Controls are assumed
		// to be active (i.e. true)
		ary = (JSONArray) state.get(JSONTerms.BIGRAPHER_NODES);
		it = ary.iterator();
		while (it.hasNext()) {
			node = new BigraphNode();
			tmpObj = (JSONObject) it.next(); // gets hold of node info

			tmpCtrl = (JSONObject) tmpObj.get(JSONTerms.BIGRAPHER_CONTROL);
			tmp = tmpCtrl.get(JSONTerms.BIGRAPHER_CONTROL_ID).toString();
			tmpArity = tmpCtrl.get(JSONTerms.BIGRAPHER_CONTROL_ARITY).toString();

			// set node id
			node.setId(tmpObj.get(JSONTerms.BIGRAPHER_NODE_ID).toString());
			// set node control
			node.setControl(tmp);
			nodes.put(node.getId(), node);
		}

		// get parents for nodes from the place_graph=> dag. Caution using the
		// roots and sites numbers
		ary = (JSONArray) ((JSONObject) state.get(JSONTerms.BIGRAPHER_PLACE_GRAPH)).get(JSONTerms.BIGRAPHER_DAG);
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			src = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_SOURCE).toString());
			target = Integer.parseInt(tmpObj.get(JSONTerms.BIGRAPHER_TARGET).toString());

			if (src >= numOfRoots) {
				// set parent node in the target node
				nodes.get(Integer.toString(target)).setParent(nodes.get(Integer.toString(src - numOfRoots)));
				// add child node to source node
				nodes.get(Integer.toString(src - numOfRoots)).addChildNode(nodes.get(Integer.toString(target)));
			} else { // source is a root
				nodes.get(Integer.toString(target)).setParentRoot(src);

			}

		}

		// get outer names and inner names for the nodes. Currently, focus on
		// outer names
		// while inner names are extracted they are not updated in the nodes
		ary = (JSONArray) (state.get(JSONTerms.BIGRAPHER_LINK_GRAPH));
		it = ary.iterator();
		while (it.hasNext()) {
			tmpObj = (JSONObject) it.next(); // gets hold of node info
			outerNames.clear();
			innerNames.clear();

			// get outer names
			outerAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_OUTER));
			innerAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_INNER));
			portAry = (JSONArray) (tmpObj.get(JSONTerms.BIGRAPHER_PORTS));
			
			//get outernames
			for (int i=0; i<outerAry.size();i++) {
				JSONObject tmpOuter = (JSONObject)outerAry.get(i);
				
				outerNames.add(tmpOuter.get(JSONTerms.BIGRAPHER_NAME).toString());
			}
			
			// get inner names
			for (int i=0; i<innerAry.size();i++) {
				JSONObject tmpInner = (JSONObject)innerAry.get(i);
	
				innerNames.add(tmpInner.get(JSONTerms.BIGRAPHER_NAME).toString());
			}
						
			// get nodes connected to outer names. Inner names should be considered
			if (outerNames.size() > 0) {
				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());

					node.addOuterNames(outerNames);
					node.addInnerNames(innerNames);
				}
			} else { //if there are no outer names, then create edges by creating outernames, adding them to the nodes, then closing the outername

				for (int i = 0; i < portAry.size(); i++) {
					JSONObject tmpPort = (JSONObject) portAry.get(i);
					node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());

					node.addOuterName("edge"+edgeNumber, true);
				}
				edgeNumber++;
			}
			
			//add inner names to nodes
			if(innerNames.size()>0) {
			for (int i = 0; i < portAry.size(); i++) {
				JSONObject tmpPort = (JSONObject) portAry.get(i);
				node = nodes.get(tmpPort.get(JSONTerms.BIGRAPHER_NODE_ID).toString());;
				node.addInnerNames(innerNames);
			}
			}
		}
		
		outerNamesFull.addAll(outerNames);
		innerNamesFull.addAll(innerNames);
		
		//// Create Bigraph Object \\\\\
		
		Signature tmpSig = getBigraphSignature();
		
		if(tmpSig == null) {
			return null;
		}
		
		
		BigraphBuilder biBuilder = new BigraphBuilder(tmpSig);
	
		// create roots for the bigraph
		for (int i = 0; i < numOfRoots; i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}

		// create outer names		
		OuterName tmpNm;
		HashMap<String, Boolean> isClosedMap = new HashMap<String, Boolean>();
		
		for(BigraphNode nd : nodes.values()) {
			for(BigraphNode.OuterName nm : nd.getOuterNamesObjects()) {
				if(libBigOuterNames.get(nm.getName()) == null) {
					tmpNm = biBuilder.addOuterName(nm.getName());
					libBigOuterNames.put(nm.getName(), tmpNm);
					isClosedMap.put(nm.getName(), nm.isClosed());
				}
			}
		}
		
		// create inner names
		//consider closing iner names also (future work)
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
		

		//close outernames
		for(OuterName nm : libBigOuterNames.values()) {
			if(isClosedMap.get(nm.getName())) {
				biBuilder.closeOuterName(nm);
			}
		}
		
		
		// add sites to bigraph (probably for states they don't have sites)
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

	@Override
	public HashMap<Integer, Bigraph> getStates() {
		// TODO Auto-generated method stub
		return loadStates();
	}

}
