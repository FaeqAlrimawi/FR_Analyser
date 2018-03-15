package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.xquery.XQException;

import org.json.JSONArray;
import org.json.JSONObject;

import ie.lero.spare.franalyser.utility.BigraphNode;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Handle;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Site;

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
	private boolean isDebugging = true;
	
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

	public Bigraph getBigraphPredicate() {
		return bigraphPredicate;
	}

	public void setBigraphPredicate(Bigraph bigraphPredicate) {
		this.bigraphPredicate = bigraphPredicate;
	}
	
	public void setBigraphPredicate(JSONObject JSONPredicate) {
		this.bigraphPredicate = Predicate.convertJSONtoBigraph(JSONPredicate);
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
		
		BigraphBuilder bigraphBuilder = new BigraphBuilder(SystemInstanceHandler.getGlobalBigraphSignature());
		
		return bigraphBuilder.makeBigraph();
	}
	
	public static 	Bigraph convertJSONtoBigraph(JSONObject redex){

		JSONObject tmpObj;
		HashMap<String,BigraphNode> nodes = new HashMap<String, BigraphNode>();
		BigraphNode node;
		JSONArray ary;
		LinkedList<BigraphNode.OuterName> outerNames = new LinkedList<BigraphNode.OuterName>();
		LinkedList<BigraphNode.InnerName> innerNames = new LinkedList<BigraphNode.InnerName>();
		
		HashMap<String, OuterName> libBigOuterNames = new HashMap<String, OuterName>();
		HashMap<String, InnerName> libBigInnerNames = new HashMap<String, InnerName>();
		HashMap<String, Node> libBigNodes = new HashMap<String, Node>();
		LinkedList<Root> libBigRoots = new LinkedList<Root>();
		LinkedList<Site> libBigSites = new LinkedList<Site>();
		
		// number of roots
		int numOfRoots = 0;
		
		if(redex.isNull("entity")) {
			return null;
		}
		
		//get all entities (they are divided by || as Bigraph)
		if (JSONArray.class.isAssignableFrom(redex.get("entity").getClass())){
			
		ary = (JSONArray) redex.get("entity");

		for(int i=0;i<ary.length();i++) {

			node = new BigraphNode();
			tmpObj = ary.getJSONObject(i); //gets hold of node info
			
			//set node id
			//node.setId(Integer.parseInt(tmpObj.get("node_id").toString()));
			//set node control

			node.setControl(tmpObj.get("control").toString());
			node.setId(tmpObj.get("name").toString());
			node.setParentRoot(numOfRoots);
			numOfRoots++;
			nodes.put(node.getId(), node);
			
			//get outer names
			if(!tmpObj.isNull("outername")) {	
			if (JSONArray.class.isAssignableFrom(tmpObj.get("outername").getClass())){
				JSONArray tmpAry = tmpObj.getJSONArray("outername");
				for(int j = 0;j<tmpAry.length();j++) {
					String name = ((JSONObject)tmpAry.get(j)).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpAry.get(j)).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpAry.get(j)).get("isClosed").toString().equals("true");
					}
					node.addOuterName(name, isClosed);
				}
			} else {
				String name = ((JSONObject)tmpObj.get("outername")).get("name").toString();
				boolean isClosed = false;
				if(!((JSONObject)tmpObj.get("outername")).isNull("isClosed")) {
					isClosed = ((JSONObject)tmpObj.get("outername")).get("isClosed").toString().equals("true");
				}
				node.addOuterName(name, isClosed);
			}
			}
			
			//get inner names
			if(!tmpObj.isNull("innername")) {	
				if (JSONArray.class.isAssignableFrom(tmpObj.get("innername").getClass())){
					JSONArray tmpAry = tmpObj.getJSONArray("innername");
					for(int j = 0;j<tmpAry.length();j++) {
						String name = ((JSONObject)tmpAry.get(j)).get("name").toString();
						boolean isClosed = false;
						if(!((JSONObject)tmpAry.get(j)).isNull("isClosed")) {
							isClosed = ((JSONObject)tmpAry.get(j)).get("isClosed").toString().equals("true");
						}
						node.addInnerName(name, isClosed);
					}
				} else {
					String name = ((JSONObject)tmpObj.get("innername")).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpObj.get("innername")).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpObj.get("innername")).get("isClosed").toString().equals("true");
					}
					node.addInnerName(name, isClosed);
				}
			}
			
			//get sites
			if(!tmpObj.isNull("site")) {	
					node.setSite(true);
			}
			
			//get childern
			if(!tmpObj.isNull("entity")) {
				getChildren(tmpObj, nodes);
			}
			
		}
		} else { //if there is only one entity
			node = new BigraphNode();
			tmpObj = (JSONObject)redex.get("entity");
			
			node.setControl(tmpObj.get("control").toString());
			node.setId(tmpObj.get("name").toString());
			node.setParentRoot(numOfRoots);
			numOfRoots++;
			nodes.put(node.getId(), node);
			
			//get outer names
			if(!tmpObj.isNull("outername")) {	
			if (JSONArray.class.isAssignableFrom(tmpObj.get("outername").getClass())){
				JSONArray tmpAry = tmpObj.getJSONArray("outername");
				for(int j = 0;j<tmpAry.length();j++) {
					String name = ((JSONObject)tmpAry.get(j)).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpAry.get(j)).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpAry.get(j)).get("isClosed").toString().equals("true");
					}
					node.addOuterName(name, isClosed);
				}
			} else {
				String name = ((JSONObject)tmpObj.get("outername")).get("name").toString();
				boolean isClosed = false;
				if(!((JSONObject)tmpObj.get("outername")).isNull("isClosed")) {
					isClosed = ((JSONObject)tmpObj.get("outername")).get("isClosed").toString().equals("true");
				}
				node.addOuterName(name, isClosed);
			}
			}
			
			//get inner names
			if(!tmpObj.isNull("innername")) {	
				if (JSONArray.class.isAssignableFrom(tmpObj.get("innername").getClass())){
					JSONArray tmpAry = tmpObj.getJSONArray("innername");
					for(int j = 0;j<tmpAry.length();j++) {
						String name = ((JSONObject)tmpAry.get(j)).get("name").toString();
						boolean isClosed = false;
						if(!((JSONObject)tmpAry.get(j)).isNull("isClosed")) {
							isClosed = ((JSONObject)tmpAry.get(j)).get("isClosed").toString().equals("true");
						}
						node.addInnerName(name, isClosed);
					}
				} else {
					String name = ((JSONObject)tmpObj.get("innername")).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpObj.get("innername")).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpObj.get("innername")).get("isClosed").toString().equals("true");
					}
					node.addInnerName(name, isClosed);
				}
			}
			
			//get sites
			if(!tmpObj.isNull("site")) {	
					node.setSite(true);
			}
			
			//get childern
			if(!tmpObj.isNull("entity")) {
				getChildren(tmpObj, nodes);
			}
			
		}
		
		BigraphBuilder biBuilder = new BigraphBuilder(SystemInstanceHandler.getGlobalBigraphSignature());
		
		//create roots for the bigraph
		for(int i=0;i<numOfRoots;i++) {
			libBigRoots.add(biBuilder.addRoot(i));
		}
		
		for(BigraphNode n : nodes.values()) {
			
			//create bigraph outernames
			for(BigraphNode.OuterName out : n.getOuterNamesObjects()) {
				if(!outerNames.contains(out)) {
					libBigOuterNames.put(out.getName(), biBuilder.addOuterName(out.getName()));
					outerNames.add(out);
				}	
			}
			
			//create bigraph inner names
			for(BigraphNode.InnerName in : n.getInnerNamesObjects()) {
				if(!innerNames.contains(in)) {
					libBigInnerNames.put(in.getName(), biBuilder.addInnerName(in.getName()));
					innerNames.add(in);
				}	
			}
		}
	
		//initial creation of bigraph nodes
		for(BigraphNode nd : nodes.values()) {
			if(libBigNodes.containsKey(nd.getId())) {
				continue;
			}
			createNode(nd, biBuilder, libBigRoots, libBigOuterNames, libBigNodes);	
		}
		
		//close outernames after creating nodes of the Bigraph
		for(BigraphNode.OuterName out : outerNames) {
			if(out.isClosed()) {
				biBuilder.closeOuterName(out.getName());
			}
		}
		
		//close innernames after creating nodes of the Bigraph
		for(BigraphNode.InnerName in : innerNames) {
			if(in.isClosed()) {
				biBuilder.closeInnerName(in.getName());
			}
		}		
		
		//add sites to bigraph
		for(BigraphNode n : nodes.values()) {
			if(n.hasSite()) {
				biBuilder.addSite(libBigNodes.get(n.getId()));
			}
		}
		
		return biBuilder.makeBigraph();
	}
	
	/**
	 * loops the given json object to return internal tags (children) info
	 * @param obj JSONObject
	 * @param nodes BigraphNode objects holding the inner tags info
	 */
	private static void getChildren(JSONObject obj, HashMap<String,BigraphNode> nodes) {
		
		if (JSONArray.class.isAssignableFrom(obj.get("entity").getClass())){
			JSONArray tmpAry = (JSONArray)obj.get("entity");
			for(int j=0;j<tmpAry.length();j++) {
				BigraphNode nodeTmp = new BigraphNode();
				JSONObject tmpObj2 = (JSONObject)tmpAry.getJSONObject(j);
				nodeTmp.setControl(tmpObj2.get("control").toString());
				nodeTmp.setId(tmpObj2.get("name").toString());
				nodeTmp.setParent(nodes.get(obj.get("name")));
				
				nodes.put(nodeTmp.getId(), nodeTmp);
				
				//get outer names	
				//check if there are any outernames
				if(!tmpObj2.isNull("outername")) {
				//check if there are more than one outername	
				if (JSONArray.class.isAssignableFrom(tmpObj2.get("outername").getClass())){
					JSONArray tmpAry2 = tmpObj2.getJSONArray("outername");

					for(int k = 0;k<tmpAry2.length();k++) {
						String name = ((JSONObject)tmpAry2.get(k)).get("name").toString();
						boolean isClosed = false;
						if(!((JSONObject)tmpAry2.get(k)).isNull("isClosed")) {
							isClosed = ((JSONObject)tmpAry2.get(k)).get("isClosed").toString().equals("true");
						}
						nodeTmp.addOuterName(name, isClosed);
					}
				} else {
					String name = ((JSONObject)tmpObj2.get("outername")).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpObj2.get("outername")).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpObj2.get("outername")).get("isClosed").toString().equals("true");
					}
					nodeTmp.addOuterName(name, isClosed);
				}
				}
				
				//get inner names
				if(!tmpObj2.isNull("innername")) {	
					if (JSONArray.class.isAssignableFrom(tmpObj2.get("innername").getClass())){
						JSONArray tmpAry2 = tmpObj2.getJSONArray("innername");
						for(int k = 0;k<tmpAry2.length();k++) {
							String name = ((JSONObject)tmpAry.get(k)).get("name").toString();
							boolean isClosed = false;
							if(!((JSONObject)tmpAry.get(k)).isNull("isClosed")) {
								isClosed = ((JSONObject)tmpAry.get(k)).get("isClosed").toString().equals("true");
							}
							nodeTmp.addInnerName(name, isClosed);
						}
					} else {
						String name = ((JSONObject)tmpObj2.get("innername")).get("name").toString();
						boolean isClosed = false;
						if(!((JSONObject)tmpObj2.get("innername")).isNull("isClosed")) {
							isClosed = ((JSONObject)tmpObj2.get("innername")).get("isClosed").toString().equals("true");
						}
						nodeTmp.addInnerName(name, isClosed);
					}
				}
				
				//get sites
				if(!tmpObj2.isNull("site")) {	
						nodeTmp.setSite(true);
				}
				
				//iterate over other children
				if (!tmpObj2.isNull("entity")){
					getChildren( tmpObj2, nodes);
				}

			}
		} else {
			BigraphNode nodeTmp = new BigraphNode();
			JSONObject tmpObj2 = (JSONObject)obj.get("entity");
			nodeTmp.setControl(tmpObj2.get("control").toString());
			nodeTmp.setId(tmpObj2.get("name").toString());
			nodeTmp.setParent(nodes.get(obj.get("name")));
			nodes.put(nodeTmp.getId(), nodeTmp);

			//get outer names	
			if(!tmpObj2.isNull("outername")) {
			if (JSONArray.class.isAssignableFrom(tmpObj2.get("outername").getClass())){
				JSONArray tmpAry2 = tmpObj2.getJSONArray("outername");
				for(int k = 0;k<tmpAry2.length();k++) {
					String name = ((JSONObject)tmpAry2.get(k)).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpAry2.get(k)).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpAry2.get(k)).get("isClosed").toString().equals("true");
					}
					nodeTmp.addOuterName(name, isClosed);
				}
			} else {
				String name = ((JSONObject)tmpObj2.get("outername")).get("name").toString();
				boolean isClosed = false;
				if(!((JSONObject)tmpObj2.get("outername")).isNull("isClosed")) {
					isClosed = ((JSONObject)tmpObj2.get("outername")).get("isClosed").toString().equals("true");
				}
				nodeTmp.addOuterName(name, isClosed);
			}
			}
			
			//get inner names
			if(!tmpObj2.isNull("innername")) {	
				if (JSONArray.class.isAssignableFrom(tmpObj2.get("innername").getClass())){
					JSONArray tmpAry2 = tmpObj2.getJSONArray("innername");
					for(int k = 0;k<tmpAry2.length();k++) {
						String name = ((JSONObject)tmpAry2.get(k)).get("name").toString();
						boolean isClosed = false;
						if(!((JSONObject)tmpAry2.get(k)).isNull("isClosed")) {
							isClosed = ((JSONObject)tmpAry2.get(k)).get("isClosed").toString().equals("true");
						}
						nodeTmp.addInnerName(name, isClosed);
					}
				} else {
					String name = ((JSONObject)tmpObj2.get("innername")).get("name").toString();
					boolean isClosed = false;
					if(!((JSONObject)tmpObj2.get("innername")).isNull("isClosed")) {
						isClosed = ((JSONObject)tmpObj2.get("innername")).get("isClosed").toString().equals("true");
					}
					nodeTmp.addInnerName(name, isClosed);
				}
			}
			
			///get sites
			if(!tmpObj2.isNull("site")) {	
				nodeTmp.setSite(true);
			}
			
			//iterate over other children
			if (!tmpObj2.isNull("entity")){
				getChildren(tmpObj2, nodes);
			}
		}
	}
	
	

	private static Node createNode(BigraphNode node, BigraphBuilder biBuilder, LinkedList<Root> libBigRoots, 
			HashMap<String, OuterName> outerNames, HashMap<String, Node> nodes) {
		
		LinkedList<Handle> names = new LinkedList<Handle>();
		
		//get outernames
		for(String n : node.getOuterNames()) {
			names.add(outerNames.get(n));
		}
		
		//if the parent is a root
		if(node.isParentRoot()) { //if the parent is a root	
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
		
		//a node will take as outernames only the number specified in the bigraph signature
		//for example, if a node has arity 2, then it will take only two outernames (the first two) and ignore any other that might exist in the names variable
		//if the number of outernames defined are less than in the signature, then the rest of outernames will be defined as links (i.e. XX:e)
		Node n = biBuilder.addNode(node.getControl(), createNode(node.getParent(), biBuilder, libBigRoots, outerNames, nodes), names);
		nodes.put(node.getId(), n);
		return n;
			
	}
	
	public static void main(String[] args){
		Predicate p = new Predicate();
		Matcher matcher = new Matcher();
		
		try {
			JSONObject o = XqueryExecuter.getBigraphConditions("activity2", PredicateType.Precondition);
			SystemInstanceHandler.setFileName("actors.big");
			SystemInstanceHandler.setOutputFolder("output");
			//SystemInstanceHandler.createSignatureFromBRS();
			SystemInstanceHandler.loadStates();
			Bigraph redex = p.convertJSONtoBigraph(o);
			p.print(redex.toString());
			for (int i = 0; i < SystemInstanceHandler.getStates().size(); i++) {
				if (matcher.match(SystemInstanceHandler.getStates().get(i), redex).iterator().hasNext()) {
					System.out.println("state " + i + " matched");
				}
			}
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void print(String msg) {
		if(isDebugging) {
			System.out.println("Predicate: "+msg);
		}
	}
}

