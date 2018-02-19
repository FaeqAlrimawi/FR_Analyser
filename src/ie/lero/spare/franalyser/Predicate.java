package ie.lero.spare.franalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ie.lero.spare.franalyser.utility.BigraphNode;
import ie.lero.spare.franalyser.utility.PredicateType;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;
import it.uniud.mads.jlibbig.core.std.Site;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;

public class Predicate {
	
	private String predicate; //Bigrapher format
	private Bigraph bigraphPredicate;
	private PredicateType predicateType; //precondition, postcondition
	private String name;
	private ArrayList<Integer> bigraphStates; //what states from the execution of a bigrapher the pred satisfies
	private Predicate[] associatedPredicates; //to be implemented, those are linked predicates
	private LinkedList<GraphPath> paths;
	private IncidentActivity incidentActivity;
	private ArrayList<Integer> statesIntraSatisfied;
	private ArrayList<Integer> statesInterSatisfied;
	public Predicate(){
		predicate="";
		predicateType = PredicateType.Precondition;
		name="";
		bigraphStates = new ArrayList<Integer>();
		statesIntraSatisfied = new ArrayList<Integer>();
		statesInterSatisfied = new ArrayList<Integer>();
		paths = new LinkedList<GraphPath>();
		}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public PredicateType getPredicateType() {
		return predicateType;
	}

	public void setPredicateType(PredicateType predType) {
		this.predicateType = predType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public IncidentActivity getIncidentActivity() {
		return incidentActivity;
	}

	public void setIncidentActivity(IncidentActivity incidentActivity) {
		this.incidentActivity = incidentActivity;
	}

	public ArrayList<Integer> getBigraphStates() {
		return bigraphStates;
	}

	public void setBigraphStates(ArrayList<Integer> bigraphStates) {
		this.bigraphStates = bigraphStates;
	}

	public boolean addBigraphState(Integer state) {
		
		boolean isAdded= false;
		
		bigraphStates.add(state);
		
		return isAdded;
	}

	public ArrayList<Integer> getStatesIntraSatisfied() {
		return statesIntraSatisfied;
	}

	public void setStatesIntraSatisfied(ArrayList<Integer> statesIntraSatisfied) {
		this.statesIntraSatisfied = statesIntraSatisfied;
	}
	
	public void addIntraSatisfiedState(Integer state) {
		if(!statesIntraSatisfied.contains(state)) {
			statesIntraSatisfied.add(state);
		}
	}

	
	public ArrayList<Integer> getStatesInterSatisfied() {
		return statesInterSatisfied;
	}

	public void setStatesInterSatisfied(ArrayList<Integer> statesInterSatisfied) {
		this.statesInterSatisfied = statesInterSatisfied;
	}

	public void addInterSatisfiedState(Integer state) {
		if(!statesInterSatisfied.contains(state)) {
			statesInterSatisfied.add(state);
		}
	}
	
	public void removeAllBigraphStates() {
		bigraphStates.clear();
	}
	public boolean validatePredicate() {
		boolean isValid = true;
		//to be done...how to validate them? could be using the validate command in bigraph and output the errors
		return isValid;
	}

	
	public Predicate[] getAssociatedPredicates() {
		return associatedPredicates;
	}

	public void setAssociatedPredicates(Predicate[] associatedPredicates) {
		this.associatedPredicates = associatedPredicates;
	}

	
	public LinkedList<GraphPath> getPaths() {
		return paths;
	}
	
/*	public LinkedList<GraphPath> getPaths(Predicate pred) {
		LinkedList<GraphPath> list = new LinkedList<GraphPath>();
		
		for (GraphPath p : paths) {
			if (pred.getPredicateType() == PredicateType.Precondition){
				if(pred.getBigraphPredicateName()  //if pred is a precondition
						.contentEquals(p.getPredicateSrc().getBigraphPredicateName())) {
					list.add(p);
				}
			} else { //if pred is a postcondition
				if(pred.getBigraphPredicateName()
						.contentEquals(p.getPredicateDes().getBigraphPredicateName())) {
					list.add(p);
				}
			}
		}
		return list;
	}*/

	public void removeAllPaths() {
		paths.clear();
	}
	public void setPaths(LinkedList<GraphPath> paths) {
		this.paths = paths;
	}

	public void addPaths(LinkedList<GraphPath> paths) {
		this.paths.addAll(paths);
	}
	
	public String toString(){
		StringBuilder res = new StringBuilder();
		
		res.append("{Name:").append(getName()).append(", Type:").append(getPredicateType().toString()).
		append(", ActivityName:").append(incidentActivity.getName()).append(", Predicate:").
		append(getPredicate()).append("}\n");
		
	return res.toString();
	}
	
	public String toPrettyString(){
		StringBuilder res = new StringBuilder();
		
		res.append("\nName: ").append(getName()).
		append("\nType: ").append(getPredicateType().toString()).
		append("\nActivityName: ").append(incidentActivity.getName()).
		append("\nPredicate value: ").append(getPredicate()).
		
		append("\nStates Satisfying: ");
		for(Integer state : bigraphStates) {
			res.append(state).append(",");
		}
		res.deleteCharAt(res.length()-1); //delete ","
		
		res.append("\nPaths Satisfying: ");
		for(GraphPath path : paths) {
			if(predicateType == PredicateType.Precondition) {
				res.append(path.getPredicateDes().getBigraphPredicateName()).append(":").append(path.toPrettyString()).append("\n");
			} else {
				res.append(path.getPredicateSrc().getBigraphPredicateName()).append(":").append(path.toPrettyString()).append("\n");
			}
			
		}
		
	return res.toString();
	}
	
	public String getBigraphPredicateStatement() {
		StringBuilder res= new StringBuilder();
		
		res.append("big ").append(getName()).append("_").append(getPredicateType()).append("_")
		.append(incidentActivity.getName()).append(" = ").append(getPredicate()).append(";\r\n");
		
		return res.toString();
		
	}
	
	public String getBigraphPredicateName() {
		StringBuilder res = new StringBuilder();
		
		res.append(getName()).append("_").append(getPredicateType()).append("_").append(incidentActivity.getName());
		
		return res.toString();
	}
	
/*	public boolean isSatisfied() {
		
		if(predicateType == PredicateType.Precondition) {
			if(paths.size() > 0) { //this indicates that a predicate has at least one state and one path to a postcondition state
				return true;
			}
		}
		
		if(predicateType == PredicateType.Postcondition) {
			
		}
		
		return false;
	}*/

	public boolean hasStates() {
		
		if(bigraphStates != null && bigraphStates.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasPaths() {
		
		if(paths != null && paths.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	public boolean hasPathsTo(Predicate pred) {
		
		for(GraphPath path : paths) {
			if(pred.getPredicateType() == PredicateType.Postcondition) {
				if(path.getPredicateDes().getName().contentEquals(pred.getName())) {
					return true;
				}
			} else if(pred.getPredicateType() == PredicateType.Precondition) {
				if(path.getPredicateSrc().getName().contentEquals(pred.getName())) {
					return true;
				}
			}
			
		}
		
		return false;
	}
	
	public LinkedList<GraphPath> getPathsTo(Predicate pred) {
		LinkedList<GraphPath> ps = new LinkedList<GraphPath>();
		
		for(GraphPath path : paths) {
			if(pred.getPredicateType() == PredicateType.Postcondition) {
				if(path.getPredicateDes().getName().contentEquals(pred.getName())) {
					ps.add(path);
				}
			} else if(pred.getPredicateType() == PredicateType.Precondition) {
				if(path.getPredicateSrc().getName().contentEquals(pred.getName())) {
					ps.add(path);
				}
			}
			
		}
		
		return ps;
	}
	
	public Bigraph convertPredicateToBigraph() {
		
		
		//convert predicate to bigraph
		
		BigraphBuilder bigraphBuilder = new BigraphBuilder(SystemInstanceHandler.getBigraphSignature());
		
		return bigraphBuilder.makeBigraph();
	}
	
	public static Bigraph convertJSONtoBigraph(JSONObject redex){

		String tmp;
		String tmpArity;
		JSONObject tmpObj;
		JSONObject tmpCtrl;
		HashMap<Integer,BigraphNode> nodes = new HashMap<Integer, BigraphNode>();
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
		HashMap<Integer, Node> libBigNodes = new HashMap<Integer, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();
		LinkedList<Site> libBigSites = new LinkedList<Site>();
		
		// number of roots, sites, and nodes respectively
		//int numOfRoots = Integer.parseInt(((JSONObject)redex.get("place_graph")).get("regions").toString());
		int numOfSites = 0;//Integer.parseInt(((JSONObject)redex.get("place_graph")).get("sites").toString());
		int numOfNodes = 0;//Integer.parseInt(((JSONObject)redex.get("place_graph")).get("nodes").toString());
		
		//get all entities (they are divided by || as Bigraph)
		ary = (JSONArray) redex.get("entity");
		it = ary.iterator();
		while(it.hasNext()) {
			node = new BigraphNode();
			tmpObj = (JSONObject) it.next(); //gets hold of node info
			
			//set node id
			//node.setId(Integer.parseInt(tmpObj.get("node_id").toString()));
			//set node control
			node.setControl(tmpObj.get("control").toString());
			node.setControl(tmpObj.get("control").toString());
			nodes.put(node.getId(), node);
		}
		
		//get parents for nodes from the place_graph=> dag. Caution using the roots and sites numbers
		ary = (JSONArray)((JSONObject)redex.get("place_graph")).get("dag");
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
		
		//get outer names and inner names for the nodes. Currently, focus on outer names
		//while inner names are extracted they are not updated in the nodes
		ary = (JSONArray)(redex.get("link_graph"));
		it = ary.iterator();
		while(it.hasNext()) {
			tmpObj = (JSONObject) it.next(); //gets hold of node info
			outerNames.clear();
			innerNames.clear();
			
			//get inner names
			innerAry = (JSONArray)(tmpObj.get("inner"));
			itInner = innerAry.iterator();
			while(itInner.hasNext()) {
				innerNames.add(itInner.next().get("name").toString());
				innerNamesFull.addAll(innerNames);
			}
			
			//get outer names
			outerAry = (JSONArray)(tmpObj.get("outer"));
			itOuter = outerAry.iterator();
			while(itOuter.hasNext()) {
				outerNames.add(itOuter.next().get("name").toString());
				outerNamesFull.addAll(outerNames);
			}
			
			//get nodes connected to outer names. Inner names should be considered
			portAry = (JSONArray)(tmpObj.get("ports"));
			itPort = portAry.iterator();
			while(itPort.hasNext()) {
				node = nodes.get(Integer.parseInt(itPort.next().get("node_id").toString()));
				node.addOuterNames(outerNames);
				node.addInnerNames(innerNames);
			}
		}
		
		BigraphBuilder biBuilder = new BigraphBuilder(SystemInstanceHandler.getBigraphSignature());
		
		//create roots for the bigraph
		for(int i=0;i<numOfRoots;i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}
		
		//create outer names
		for(String outer : outerNamesFull) {
			libBigOuterNames.put(outer, biBuilder.addOuterName(outer));
		}
		
		//create inner names
		for(String inner : innerNamesFull) {
			libBigInnerNames.put(inner, biBuilder.addInnerName(inner));
		}
		
		//initial creation of nodes
		LinkedList<Integer> visited = new LinkedList<Integer>();
		
		for(BigraphNode nd : nodes.values()) {
			if(visited.contains(nd.getId())) {
				continue;
			}
			
			createNodeParent(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes, visited);	
		}
		
		return biBuilder.makeBigraph();
	}
	
	private static Node createNodeParent(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots, 
			HashMap<String, OuterName> outerNames, HashMap<Integer, Node> nodes, LinkedList<Integer> visitedNodes) {
		visitedNodes.add(node.getId());
		
		LinkedList<Handle> names = new LinkedList<Handle>();
		for(String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}
		
		//if the parent is a root
		if(node.getParent() == null) { //if the parent is a root	
			Node  n = biBuilder.addNode(node.getControl(), libBigRoots.get(node.getParentRoot()), names);
			nodes.put(node.getId(), n);
			return n;
		}
		
		
		//if the parent is already created as a node in the bigraph
		if(nodes.containsKey(node.getParent().getId())) {
			Node  n = biBuilder.addNode(node.getControl(), nodes.get(node.getParent().getId()), names);
			nodes.put(node.getId(), n);
			return n;
		}
		
		return biBuilder.addNode(node.getControl(), createNodeParent(node.getParent(), biBuilder, libBigRoots, outerNames, nodes, visitedNodes), names);
			
	}
}

