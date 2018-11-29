package ie.lero.spare.pattern_instantiation;

import java.util.ArrayList;
import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.TransitionSystem;

public class GraphPath {

	private Predicate predicateSrc;
	private Predicate predicateDes;
	private LinkedList<Integer> stateTransitions;
	private SystemInstanceHandler systemHandler;
	private TransitionSystem transitionSystem;
	
	
	public GraphPath() {
		stateTransitions = new LinkedList<Integer>();
		systemHandler = SystemHandlers.getCurrentSystemHandler();
		transitionSystem = systemHandler!=null?systemHandler.getTransitionSystem():null;
	}
	
	public GraphPath(TransitionSystem transitionSystem) {
		stateTransitions = new LinkedList<Integer>();
//		systemHandler = sysHandler;
		transitionSystem = systemHandler!=null?systemHandler.getTransitionSystem():null;
		this.transitionSystem = transitionSystem;
	}
	
	public GraphPath(Predicate predSrc, Predicate predDes, LinkedList<Integer> transition) {
		this();
		predicateSrc = predSrc;
		predicateDes = predDes;
		stateTransitions = transition;
	}

	public Predicate getPredicateSrc() {
		return predicateSrc;
	}

	public void setPredicateSrc(Predicate predicateSrc) {
		this.predicateSrc = predicateSrc;
	}

	public Predicate getPredicateDes() {
		return predicateDes;
	}

	public void setPredicateDes(Predicate predicateDes) {
		this.predicateDes = predicateDes;
	}

	public LinkedList<Integer> getStateTransitions() {
		return stateTransitions;
	}

	public void setStateTransitions(LinkedList<Integer> stateTransition) {
		this.stateTransitions = stateTransition;
	}

	public Integer getStartState() {
		return stateTransitions.getFirst();
	}


	public Integer getEndState() {
		return stateTransitions.getLast();
	}

	public String toString() {
		StringBuilder res = new StringBuilder();
		
		//format: predicate source name:predicate destination name=state transitions where first state comes from src and last is from des
		//e.g., pre1_Precondition_activity1:post1_Postcondition_activity1=0,1,2
		res.append(predicateSrc.getBigraphPredicateName()).append(":");
		res.append(predicateDes.getBigraphPredicateName()).append("=");
//		TransitionSystem t = transitionSystem.getTransitionSystemInstance();
		String label;
		
		for(int i=0;i<stateTransitions.size();i++) {
			if(i<stateTransitions.size()-1) {
				if((label = transitionSystem.getLabel(stateTransitions.get(i), stateTransitions.get(i+1))) != null) {
					res.append(stateTransitions.get(i)).append("-["+label+"]>");
				} else {
					res.append(stateTransitions.get(i)).append("->");
				}
			} else {
				res.append(stateTransitions.get(i));
			}
			
			
		}
		/*res.deleteCharAt(res.length()-1); //remove last added comma
		res.deleteCharAt(res.length()-1);*/
		return res.toString();
		
	}
	
	public String toSimpleString() {
		StringBuilder res = new StringBuilder();
		
		for(Integer state: stateTransitions) {
			res.append(state).append(",");
		}
		
		if(res.length() > 1) {
			res.deleteCharAt(res.length()-1); //remove last added comma
		}

		return res.toString();
	}
	
	public String toPrettyString() {
		
		StringBuilder res = new StringBuilder();
//		TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
		String label;
		
		for(int i=0;i<stateTransitions.size();i++) {
			if(i<stateTransitions.size()-1) {
				if((label = transitionSystem.getLabel(stateTransitions.get(i), stateTransitions.get(i+1))) != null) {
					res.append(stateTransitions.get(i)).append("=["+label+"]=>");
				} else {
					res.append(stateTransitions.get(i)).append("==>");
				}
			} else {
				res.append(stateTransitions.get(i));
			}
		}
	
		return res.toString();
	}
	
	public String toJSON() {
		StringBuilder res = new StringBuilder();
//		TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
		String action;
		
		res.append("\"transitions\":[");
		
		if(stateTransitions.size() == 1) {
			res.append("{\"source\":").append(stateTransitions.get(0)).append(",")
			.append("\"target\":").append(stateTransitions.get(0)).append(",")
			.append("\"action\":\"\"}");
			
			return res.toString();
		}
		
		for(int i=0;i<stateTransitions.size()-1;i++) {
			action = transitionSystem.getLabel(stateTransitions.get(i), stateTransitions.get(i+1));
			res.append("{\"source\":").append(stateTransitions.get(i)).append(",")
			.append("\"target\":").append(stateTransitions.get(i+1)).append(",")
			.append("\"action\":\"").append(action).append("\"},");
			
//			//if not last transitions
//			if(i < stateTransitions.size()-2) {
//				res.append(",");
//			}
		}
		
		res.deleteCharAt(res.length()-1);
		res.append("]");
		return res.toString();
	}
	
	public boolean isSubPath(GraphPath path) {
		
		boolean isSubpath = false;
		
		LinkedList<Integer> transitions = path.getStateTransitions();
		LinkedList<Integer> indexOccurences = new LinkedList<Integer>();
		int index = 0;
		int indexSrc = 1;

		//if the source transitions are more than that of the given parameter
		if(stateTransitions.size() > transitions.size()) {
			return false;
		}
	
		//find the first occurrence where the first state of the source transition happens
		for(;index<transitions.size();index++) {
			if(stateTransitions.get(0).compareTo(transitions.get(index)) == 0) {
				indexOccurences.add(new Integer(index));
			}
		}
		
	
		if(indexOccurences.isEmpty()) { //there are no states that match the first state of the source
			return false;
		}
		
		if(stateTransitions.size() == 1) { // if the source has only one state, then return true as occurrences will be more than zero
			return true;
		}
		
	
		for(Integer ind : indexOccurences) {
			indexSrc = 1;
			index = ind.intValue()+1;
			isSubpath = true;
			// if the number of states in the parameter transition are less than that for the source
			if ((transitions.size()-index) < (stateTransitions.size()-1)) {
				isSubpath = false;
				continue;
			}
			
			for(;indexSrc<stateTransitions.size() && index <transitions.size();indexSrc++,index++) {
				if(stateTransitions.get(indexSrc).compareTo(transitions.get(index)) != 0) {
					isSubpath = false;
					break;
				}
			}
			
			if(isSubpath) {
				return true;
			}
		}
		
	/*		if((index == transitions.size()) && (indexSrc < stateTransitions.size()) ) {
				return false;
			}*/
		
			return false;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!GraphPath.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final GraphPath other = (GraphPath) obj;
	    
	    if((this.predicateSrc == null && other.getPredicateSrc() != null)
	    		|| (this.predicateSrc != null && other.getPredicateSrc() == null)) {
	    	return false;
	    }
	    	
	    if((this.predicateDes == null && other.getPredicateDes() != null)
	    		|| (this.predicateDes != null && other.getPredicateDes() == null)) {
	    	return false;
	    }
	    
	    if((this.predicateSrc != null && other.getPredicateSrc() != null)
	    		&& !this.predicateSrc.getName().contentEquals(other.getPredicateSrc().getName())) {
	    	return false;
	    }
	    
	    if((this.predicateDes != null && other.getPredicateDes() != null)
	    		&& !this.predicateDes.getName().contentEquals(other.getPredicateDes().getName())) {
	    	return false;
	    }
	    
	    if(this.stateTransitions.size() != other.getStateTransitions().size()) {
	    	return false;
	    }
	    
	    for(int i=0;i<stateTransitions.size();i++) {
	    	if(stateTransitions.get(i).compareTo(other.getStateTransitions().get(i)) != 0) {
	    		return false;
	    	}
	    }
	    
	    return true;
	}
	
	public boolean equalsIgnorePredicates(GraphPath path) {
		
		if(this.stateTransitions.size() != path.getStateTransitions().size()) {
	    	return false;
	    }
	    
	    for(int i=0;i<stateTransitions.size();i++) {
	    	if(stateTransitions.get(i).compareTo(path.getStateTransitions().get(i)) != 0) {
	    		return false;
	    	}
	    }
	    
	    return true;
	}
	
	public GraphPath combine(GraphPath path) {
		GraphPath result = new GraphPath();
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		int index = 1;
		LinkedList<Integer> ls;
		
		result.setPredicateSrc(this.predicateSrc);
		result.setPredicateDes(path.getPredicateDes());
		
		for(Integer state : stateTransitions) {
			tmp.add(state);
		}
		
		if(getEndState().compareTo(path.getStartState()) != 0) {
			index = 0;
		}
			
		ls = path.getStateTransitions();
		
			for(; index<ls.size(); index++) {
				tmp.add(ls.get(index));
			}
		
		result.setStateTransitions(tmp);
		
		return result;
	}
	
	public boolean contains(Integer state) {
		
		return stateTransitions.contains(state);
	}
	
	public boolean containsAtLeastOne(LinkedList<Integer> states) {
		
		for( Integer st : states) {
			if (stateTransitions.contains(st)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean satisfiesActivity(IncidentActivity activity) {
		ArrayList<Predicate> predsPre = activity.getPredicates(PredicateType.Precondition);
		ArrayList<Predicate> predsPost = activity.getPredicates(PredicateType.Postcondition);
		
	/*	//checks whether the both precondition and postcondition have at least one state in each that is in the graph transition
		if (!containsAtLeastOne(predsPre.get(0).getBigraphStates()) ||  
				!containsAtLeastOne(predsPost.get(0).getBigraphStates())) {
			return false;
			
		}*/
		
		LinkedList<Integer> preStates = predsPre.get(0).getBigraphStates();
		LinkedList<Integer> postStates = predsPost.get(0).getBigraphStates();
		
		for(int i=0;i<stateTransitions.size();i++) {
			if(preStates.contains(stateTransitions.get(i))) {
				for(int j = i; j<stateTransitions.size();j++) {
					if(postStates.contains(stateTransitions.get(j))) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public LinkedList<String> getPathActions(){
		
		LinkedList<String> actions = new LinkedList<String>();
//		TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
		
		for(int i=0;i<stateTransitions.size()-1;i++){
			actions.add(transitionSystem.getLabel(stateTransitions.get(i), stateTransitions.get(i+1)));
		}
		
		return actions;
	}
	
	public double getPathProbability() {
		
		double prob = -1;
//		TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
		
		if(stateTransitions.size()>1) {
			prob = transitionSystem.getProbability(stateTransitions.get(0), stateTransitions.get(1));
		}
		for(int i=2;i<stateTransitions.size()-1;i++){
			prob *= transitionSystem.getProbability(stateTransitions.get(i), stateTransitions.get(i+1));
		}
		
		return prob;
	}
	
	/*public static void main(String[] args) {
		
		GraphPath p = new GraphPath();
		LinkedList<Integer> t = new LinkedList<Integer>();
		LinkedList<Integer> t1 = new LinkedList<Integer>();
		LinkedList<Integer> t2 = new LinkedList<Integer>();
		Predicate pre = new Predicate();
		Predicate post = new Predicate();
		IncidentActivity a = new IncidentActivity();
		
		t1.add(5);
		t2.add(9);
		pre.setPredicateType(PredicateType.Precondition);
		pre.setBigraphStates(t1);
		
		post.setPredicateType(PredicateType.Postcondition);;
		post.setBigraphStates(t2);
		
		a.addPredicate(pre);
		a.addPredicate(post);
		
		for(int i=0;i<10;i++) {
			t.add(i);
		}
		p.setStateTransitions(t);
		
		System.out.println(p.satisfiesActivity(a));
	}*/
}
