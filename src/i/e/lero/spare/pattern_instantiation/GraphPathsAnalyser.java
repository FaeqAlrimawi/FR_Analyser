package i.e.lero.spare.pattern_instantiation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GraphPathsAnalyser {
	
	private LinkedList<GraphPath> paths;
	private int topPercent = 10;
	private HashMap<String, Integer> commonActions; //string is action name, and integer is the frequency
	private LinkedList<String> commonAssets;
 	private LinkedList<Integer> topPaths;
 	private LinkedList<Integer> shortestPaths;
 	private LinkedList<Integer> longestPaths;
 	
	public GraphPathsAnalyser(LinkedList<GraphPath> paths) {
		this.paths = paths;
	}
	
	public void analyse() {
		
		getCommonActions();
		getTopPaths();
		getShortestPaths();
		getLongestPaths();

	}

	public LinkedList<GraphPath> getPaths() {
		return paths;
	}

	public void setPaths(LinkedList<GraphPath> paths) {
		this.paths = paths;
	}

	public int getTopPercent() {
		return topPercent;
	}

	public void setTopPercent(int topPercent) {
		this.topPercent = topPercent;
	}

	/**
	 * Returns all actions with their frequency i.e. how many times they appeared in the different paths
	 * @return
	 */
	public HashMap<String, Integer> getCommonActions() {
		
		commonActions = new HashMap<String, Integer>();
		LinkedList<String> actions;
		int cnt;
		
		for(GraphPath path : paths) {
			actions = path.getPathActions();
			for(String action : actions) {
				//if the action exists in the hashmap, then add one to its counter
				if(commonActions.containsKey(action)) {
					cnt = commonActions.get(action);
					cnt++;
					commonActions.put(action, cnt);
				} else {//if it is a new action
					commonActions.put(action, 1);
				}
			}
		}
		
		//sort the map from the most frequent to the least
		commonActions = (HashMap<String,Integer>)sortByComparator(commonActions, false);
		
		return commonActions;
	}
	
	 private  static Map<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order)
	    {

	        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

	        // Sorting the list based on values
	        Collections.sort(list, new Comparator<Entry<String, Integer>>()
	        {
	            public int compare(Entry<String, Integer> o1,
	                    Entry<String, Integer> o2)
	            {
	                if (order)
	                {
	                	System.out.println(o1.getValue()+" "+o2.getValue());
	                    return o1.getValue().compareTo(o2.getValue());
	                }
	                else
	                {
	                    return o2.getValue().compareTo(o1.getValue());

	                }
	            }
	        });

	        // Maintaining insertion order with the help of LinkedList
	        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	        for (Entry<String, Integer> entry : list)
	        {
	        	
	            sortedMap.put(entry.getKey(), entry.getValue());
	        }

	     
	        return sortedMap;
	    }

	public LinkedList<String> getCommonAssets() {
		return commonAssets;
	}

	/**
	 * Returns all paths that contain all common actions
	 * @return
	 */
	public LinkedList<Integer> getTopPaths() {
		
		topPaths = new LinkedList<Integer>();
		LinkedList<String> pathActions;
		
		for(int i=0;i< paths.size();i++) {
			pathActions = paths.get(i).getPathActions();
			
			//if the path contains all common actions then the path is considered a top one
			if(pathActions.containsAll(commonActions.keySet())) {
				topPaths.add(i);
			}
		}
		
		return topPaths;
	}
	
	/**
	 * Returns the shortest paths i.e. paths that has the minimum number of states/actions
	 * @return IDs of the shortest paths
	 */
	public LinkedList<Integer> getShortestPaths() {
		shortestPaths = new LinkedList<Integer>();
		
		//initial smallest size
		
		if(paths == null || paths.size() == 0) {
			return shortestPaths;
		}
		
		int numOfStates = paths.get(0).getStateTransitions().size();
		int size;
		
		shortestPaths.add(0);
		
		for(int i=1;i<paths.size();i++) {
			//if current path has a number of states smaller than the set one then the set one is changed
			size = paths.get(i).getStateTransitions().size();
			if(size<numOfStates) {
				numOfStates = size;
				shortestPaths.clear();
				shortestPaths.add(i);
			} else if(size == numOfStates) {
				shortestPaths.add(i);
			}
		}
		
		//this sets the number of actions for the shortest paths
		shortestPaths.add(numOfStates-1);
		
		return shortestPaths;
	}
	
	public LinkedList<Integer> getLongestPaths() {
		longestPaths = new LinkedList<Integer>();
		
		if(paths == null || paths.size() == 0) {
			return shortestPaths;
		}
		
		
		//initial smallest size
		int numOfStates = paths.get(0).getStateTransitions().size();
		int size;
		
		longestPaths.add(0);
		
		for(int i=1;i<paths.size();i++) {
			//if current path has a number of states smaller than the set one then the set one is changed
			size = paths.get(i).getStateTransitions().size();
			if(size>numOfStates) {
				numOfStates = size;
				longestPaths.clear();
				longestPaths.add(i);
			} else if(size == numOfStates) {
				longestPaths.add(i);
			}
		}
		
		//this sets the number of actions for the longest paths
		longestPaths.add(numOfStates-1);
				
		return longestPaths;
	}
	
	public String print() {
		
		StringBuilder str = new StringBuilder();
		String newLine = "\n";
		int actionsNum = 0;
		
		//get common paths
		str.append(newLine).append("common actions:").append(commonActions).append(newLine);
		
		//get top paths based on the common actions i.e. paths that contain all common actions
		str.append(newLine).append("top paths (based on common actions): ").append(topPaths).append(newLine);
		
		//get shortest paths
		if(shortestPaths.size() >0) {
			actionsNum = shortestPaths.getLast();
			shortestPaths.removeLast();
			str.append(newLine).append("Shortest Paths (").append(actionsNum).append(" actions): ").append(shortestPaths).append(newLine);
			shortestPaths.add(actionsNum);
		} else {
			str.append(newLine).append("Shortest Paths: [NONE]");
		}
		
		//get longest paths
		if(longestPaths.size() >0) {
			actionsNum = longestPaths.getLast();
			longestPaths.removeLast();
			str.append(newLine).append("Longest Paths (").append(actionsNum).append(" actions): ").append(longestPaths).append(newLine);	
			longestPaths.add(actionsNum);
		} else {
			str.append(newLine).append("Longest Paths: [NONE]");
		}
		
		return str.toString();
	}

	
}
