package ie.lero.spare.franalyser;

import java.util.LinkedList;

import org.apache.xmlgraphics.xmp.merge.ArrayAddPropertyMerger;

public class GraphPath {

	private Predicate predicateSrc;
	private Predicate predicateDes;
	LinkedList<Integer> stateTransitions;
	
	public GraphPath() {
		stateTransitions = new LinkedList<Integer>();
	}
	
	public GraphPath(Predicate predSrc, Predicate predDes, LinkedList<Integer> transition) {
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
		
		for(Integer state: stateTransitions) {
			res.append(state).append(",");
		}
		res.deleteCharAt(res.length()-1); //remove last added comma
		
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
		
		for(Integer state: stateTransitions) {
			res.append(state).append(" => ");
		}
		if(res.length() > 4)
		res.delete(res.length()-4, res.length()); //remove " => "
		
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
}
