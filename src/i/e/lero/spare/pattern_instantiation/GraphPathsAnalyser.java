package i.e.lero.spare.pattern_instantiation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class GraphPathsAnalyser {
	
	private LinkedList<GraphPath> paths;
	private int topPercent = 10;
	private HashMap<String, Integer> actionsFrequency; //string is action name, and integer is the frequency
	private LinkedList<String> commonAssets;
 	private LinkedList<Integer> topPaths;
//	private LinkedList<Integer> allShortestPaths;
 	private LinkedList<Integer> shortestPaths;
 	private LinkedList<Integer> longestPaths;
 	private ForkJoinPool mainPool;
	private int maxWaitingTime = 24;
	private TimeUnit timeUnit = TimeUnit.HOURS;
	private final static int THRESHOLD = 100; //threshold for the number of states on which task is further subdivided into halfs
	private boolean isAll = true;
	private double percentageFrequency = 0.5;
	
	public GraphPathsAnalyser(LinkedList<GraphPath> paths) {
		this.paths = paths;
	//	allShortestPaths = new LinkedList<Integer>();
	}
	
	public String analyse() {
		
		mainPool = new ForkJoinPool();
		
		
		//getActionsFrequencyOriginal();
		//getTopPathsOriginal();
		
		//returns the topPaths that has actions with at least frequencey percentage more than or equal to [e.g., 50%]. If true it will return only the paths that contain actions with
		//frequency more than [e.g., 50%]
		
		//parallelism based functionalities
		
		getShortestPaths();
		getLongestPaths();
		
		//sets the percentage of the frequency that the actio
		percentageFrequency = 0.9;
		//sets if all actions in the path has the sepcified frequency in percentageFrequency variable
		isAll = true;
		
		getTopPaths(0.9, isAll);
		
		mainPool.shutdown();
		
		try {
			if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
				//msgQ.put("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " " + timeUnit + "]");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return print();
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
	public HashMap<String, Integer> getActionsFrequency() {
		
		
		actionsFrequency = mainPool.invoke(new ActionsFrequencyAnalyser(0, paths.size()));
		
		return actionsFrequency;
	}
	
	/**
	 * Returns all actions with their frequency i.e. how many times they appeared in the different paths
	 * @return
	 */
	public HashMap<String, Integer> getActionsFrequencyOriginal() {
		
		actionsFrequency = new HashMap<String, Integer>();
		LinkedList<String> actions;
		int cnt;
		
		for(GraphPath path : paths) {
			actions = path.getPathActions();
			for(String action : actions) {
				//if the action exists in the hashmap, then add one to its counter
				if(actionsFrequency.containsKey(action)) {
					cnt = actionsFrequency.get(action);
					cnt++;
					actionsFrequency.put(action, cnt);
				} else {//if it is a new action
					actionsFrequency.put(action, 1);
				}
			}
		}
		
		//sort the map from the most frequent to the least
		actionsFrequency = (HashMap<String,Integer>)sortByComparator(actionsFrequency, false);
		
		return actionsFrequency;
	}
	
	 private   Map<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order)
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
	                	//System.out.println(o1.getValue()+" "+o2.getValue());
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
		
		//to be implemented
		//
		return commonAssets;
	}

	/**
	 * Returns all paths that contain the given actions
	 * @param actions the list of actions that paths should have
	 * @param isExact if true it returns all paths that have exactly the given actions in any order. If false it returns all paths that have at least the actions given 
	 * @return List of path IDs
	 */
	public LinkedList<Integer> getTopPaths(LinkedList<String> actions, boolean isExact) {
		
		topPaths = mainPool.invoke(new TopPathsAnalyser(0, paths.size(), actions, isExact));
		
		return topPaths;
	}
	
	/**
	 * Returns all paths that contain actions which has a frequency percentage >= to the given frequencey percentage (percentage = action_frequency/num_of_paths)
	 * @param actionsFrequencyPercentage the percentage that actions should have at least
	 * @param isExact if true it returns all paths that have exactly the actions with a percentage >= given percentage. If false it returns all paths that have at least the actions with percentage >= given percentage
	 * @return List of path IDs
	 */
	public LinkedList<Integer> getTopPaths(double actionsFrequencyPercentage, boolean isExact) {
		
		LinkedList<String> actions = new LinkedList<String>();
		
		//if actions frequency are not calculated then calculate them
		if(actionsFrequency == null || actionsFrequency.isEmpty()) {
			actionsFrequency = mainPool.invoke(new ActionsFrequencyAnalyser(0, paths.size()));
		}
		
		int numOfPaths = paths.size();
		
		//find actions with percentage >= given percentage
		for(Entry<String, Integer> set: actionsFrequency.entrySet()) {
			if((double)set.getValue()/(double)numOfPaths >= actionsFrequencyPercentage) {
				actions.add(set.getKey());
			}
		}
		
		//System.out.println("actions with >= 0.5 : "+ actions);
		
		topPaths = mainPool.invoke(new TopPathsAnalyser(0, paths.size(), actions, isExact));
		
		return topPaths;
	}
	
	/**
	 * Returns all paths that contain all common actions
	 * @return
	 */
	public LinkedList<Integer> getTopPathsOriginal() {
		
		topPaths = new LinkedList<Integer>();
		LinkedList<String> pathActions;
		LinkedList<String> actions = new LinkedList<String>();
		
		//for testing
		double perc = 0.85;
		int numOfPaths = paths.size();
		
		for(Entry<String, Integer> set : actionsFrequency.entrySet()) {
			if((double)set.getValue()/(double)numOfPaths >= perc) {
				actions.add(set.getKey());
			}
		}
		
		System.out.println("actions with >= 0.8 : "+ actions);
		
		for(int i=0;i< paths.size();i++) {
			pathActions = paths.get(i).getPathActions();
			
			//if the path contains all common actions then the path is considered a top one
			if(pathActions.containsAll(actions)) {
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
		
		//shortestPaths = new LinkedList<Integer>();
		
		if(paths == null || paths.size() == 0) {
			return shortestPaths;
		}
		
		shortestPaths = mainPool.invoke(new PathsAnalyserParallelism(0, paths.size(), PathsAnalyserParallelism.SHORTEST));
		
		return shortestPaths;
	}
	
	/**
	 * Returns the shortest paths i.e. paths that has the minimum number of states/actions
	 * @return IDs of the shortest paths
	 */
	public LinkedList<Integer> getShortestPathsOriginal() {
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
		//shortestPaths.add(numOfStates-1);
		
		return shortestPaths;
	}
	
	public LinkedList<Integer> getLongestPaths() {
		longestPaths = new LinkedList<Integer>();
		
		if(paths == null || paths.size() == 0) {
			return shortestPaths;
		}
		
		
		longestPaths = mainPool.invoke(new PathsAnalyserParallelism(0, paths.size(), PathsAnalyserParallelism.LONGEST));
				
		return longestPaths;
	}
	
	public LinkedList<Integer> getLongestPathsOriginal() {
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
		//longestPaths.add(numOfStates-1);
				
		return longestPaths;
	}
	public String print() {
		
		StringBuilder str = new StringBuilder();
		String newLine = "\n";
		int actionsNum = 0;
		
		if(paths == null || paths.size() == 0) {
			return null;
		}
		
		//get common paths
		if(actionsFrequency != null) {
		str.append(newLine).append("-Actions Frequency: [");
		int size = paths.size();
		for(Entry<String, Integer> freq : actionsFrequency.entrySet()) {
			double perc = (int)(((double)freq.getValue()/(double)size)*10000)/100;
			str.append(freq.getKey()).append("=").append(freq.getValue()).append(" (").append(perc).append("%), ");
		}
		
		//removes the last comma and space
		str.replace(str.length()-2, str.length(), "");
		str.append("]").append(newLine);
		}
		
		//get top paths based on the common actions i.e. paths that contain all common actions
		if(topPaths != null) {
		str.append("-top paths ["+topPaths.size()+"] (based on actions Frequency >= ").append(percentageFrequency).append("):").append(topPaths).append(newLine);
		}
		
		//get shortest paths
		if(shortestPaths != null && shortestPaths.size() >0) {
			actionsNum = paths.get(shortestPaths.getFirst()).getStateTransitions().size()-1;
			str.append("-Shortest Paths [").append(shortestPaths.size()).append("] (").append(actionsNum).append(" actions): ").append(shortestPaths).append(newLine);
		} else {
			str.append("-Shortest Paths: [NONE]");
		}
		
		//get longest paths
		if(longestPaths != null && longestPaths.size() >0) {
			actionsNum = paths.get(longestPaths.getFirst()).getStateTransitions().size()-1;
			str.append("-Longest Paths [").append(longestPaths.size()).append("] (").append(actionsNum).append(" actions): ").append(longestPaths).append(newLine);	
		} else {
			str.append("-Longest Paths: [NONE]");
		}
		
		return str.toString();
	}

	class PathsAnalyserParallelism extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private LinkedList<Integer> sPaths;
		protected static final int SHORTEST = 1;
		protected static final int LONGEST = 2;
		private int operation = SHORTEST;
		//for testing
		//protected int numOfParts = 0;
		
		public PathsAnalyserParallelism(int indexStart, int indexEnd, int operation){
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			sPaths = new LinkedList<Integer>();
			this.operation = operation;
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub
			
			if((indexEnd-indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks())
						.stream()
						.map(new Function<PathsAnalyserParallelism, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(PathsAnalyserParallelism arg0) {
								return arg0.sPaths;
							}
							
						}).reduce(sPaths, new BinaryOperator<LinkedList<Integer>>() {

							
							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {
								
								//finds all shortest paths
								if(operation == PathsAnalyserParallelism.SHORTEST) {
								
								//if the first list (arg0) has less number of transtions than that of the 2nd list (arg1)
								if(arg0.size()>0 && arg1.size()>0) {
								if(paths.get(arg0.getFirst()).getStateTransitions().size() < paths.get(arg1.getFirst()).getStateTransitions().size()) {
									return arg0;
								} else if(paths.get(arg0.getFirst()).getStateTransitions().size() > paths.get(arg1.getFirst()).getStateTransitions().size()) {
									arg0.clear();
									arg0.addAll(arg1);
									return arg0;
								} 
								//if they both equal then merge them and return their merge
								else {
									arg0.addAll(arg1);
									return arg0;	
								}
								
							}  else if(arg0.size()>0) {
								return arg0;
							} else {
								arg0.clear();
								arg0.addAll(arg1);
								return arg0;
							}
								
						//finds all longest paths		
						} else if(operation == PathsAnalyserParallelism.LONGEST){
							if(arg0.size()>0 && arg1.size()>0) {
								if(paths.get(arg0.getFirst()).getStateTransitions().size() > paths.get(arg1.getFirst()).getStateTransitions().size()) {
									return arg0;
								} else if(paths.get(arg0.getFirst()).getStateTransitions().size() < paths.get(arg1.getFirst()).getStateTransitions().size()) {
									
									arg0.clear();
									arg0.addAll(arg1);
									return arg0;
								} 
								//if they both equal then merge them and return their merge
								else {
									arg0.addAll(arg1);
									return arg0;	
								}
								
							}  else if(arg0.size()>0) {
								return arg0;
							} else {
								arg0.clear();
								arg0.addAll(arg1);
								return arg0;
							}
							
						} 
						//other than the longest or shortest it returns the seed element
						else {
							return arg0;
						}
						}});
						
			} else {
							
				int numOfStates = paths.get(indexStart).getStateTransitions().size();
				int size;
				
				sPaths.add(indexStart);
				
				if(operation == SHORTEST) {
				for(int i=indexStart+1;i<indexEnd;i++) {
					//if current path has a number of states smaller than the set one then the set one is changed
					size = paths.get(i).getStateTransitions().size();
					if(size<numOfStates) {
						numOfStates = size;
						sPaths.clear();
						sPaths.add(i);
					} else if(size == numOfStates) {
						sPaths.add(i);
					}
				}
				} else if(operation == LONGEST) {
					for(int i=indexStart+1;i<indexEnd;i++) {
						//if current path has a number of states smaller than the set one then the set one is changed
						size = paths.get(i).getStateTransitions().size();
						if(size>numOfStates) {
							numOfStates = size;
							sPaths.clear();
							sPaths.add(i);
						} else if(size == numOfStates) {
							sPaths.add(i);
						}
					}
				}
				return sPaths;
			
			}
			
		}
		
		private Collection<PathsAnalyserParallelism> createSubTasks() {
			List<PathsAnalyserParallelism> dividedTasks = new LinkedList<PathsAnalyserParallelism>();
			
			int mid = (indexStart+indexEnd)/2;
			//int startInd = indexEnd - endInd1;
			
			dividedTasks.add(new PathsAnalyserParallelism(indexStart, mid, operation));
			dividedTasks.add(new PathsAnalyserParallelism(mid, indexEnd, operation));
			
			return dividedTasks;
		}
	}

	class ActionsFrequencyAnalyser extends RecursiveTask<HashMap<String, Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private HashMap<String, Integer> actionsFrequency;
		//for testing
		//protected int numOfParts = 0;
		
		public ActionsFrequencyAnalyser(int indexStart, int indexEnd){
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			actionsFrequency = new HashMap<String, Integer>();
		}

		@Override
		protected HashMap<String, Integer> compute() {
			
			if((indexEnd-indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks())
						.stream()
						.map(new Function<ActionsFrequencyAnalyser, HashMap<String, Integer>>() {

							@Override
							public HashMap<String, Integer> apply(ActionsFrequencyAnalyser arg0) {
								return arg0.actionsFrequency;
							}
							
						}).reduce(actionsFrequency, new BinaryOperator<HashMap<String, Integer>>() {

							
							@Override
							public HashMap<String, Integer> apply(HashMap<String, Integer> arg0, HashMap<String, Integer> arg1) {
								
								for(Entry<String, Integer> set: arg1.entrySet()) {
									//if the action is already contained in the result (arg0) then add the count to the current count else add a new action with the arg1 count
									String action = set.getKey();
									int freq = set.getValue();
									
									if(arg0.containsKey(action)) {
										int oldValue = arg0.get(action);
										arg0.put(action, oldValue+freq);
									} else {
										arg0.put(action, freq);	
									}
									
								}
								
								return arg0;
						}});
						
			} else {
				
				//get actions frequency
				LinkedList<String> actions;
				
				int cnt = 0;
				
				for(int i=indexStart;i<indexEnd;i++) {
					actions = paths.get(i).getPathActions();
					for(String action : actions) {
						//if the action exists in the hashmap, then add one to its counter
						if(actionsFrequency.containsKey(action)) {
							cnt = actionsFrequency.get(action);
							cnt++;
							actionsFrequency.put(action, cnt);
						} else {//if it is a new action
							actionsFrequency.put(action, 1);
						}
					}
				}
				
				//sort the map from the most frequent to the least
				actionsFrequency = (HashMap<String,Integer>)sortByComparator(actionsFrequency, false);
				
				return actionsFrequency;
			}
			}
			
		
		private Collection<ActionsFrequencyAnalyser> createSubTasks() {
			List<ActionsFrequencyAnalyser> dividedTasks = new LinkedList<ActionsFrequencyAnalyser>();
			
			int mid = (indexStart+indexEnd)/2;
			//int startInd = indexEnd - endInd1;
			
			dividedTasks.add(new ActionsFrequencyAnalyser(indexStart, mid));
			dividedTasks.add(new ActionsFrequencyAnalyser(mid, indexEnd));
			
			return dividedTasks;
		}
		
		 private   Map<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order)
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
	}

	//returns all paths that contain the actions given in the constructor
	//the actions can be set by a percentage of frequencey
	
	class TopPathsAnalyser extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private LinkedList<Integer> topPaths;
		private boolean isExact = false;
		private LinkedList<String> actions;
		
		//private double frequenceyPercentage = 0.10;
		
		public TopPathsAnalyser(int indexStart, int indexEnd, LinkedList<String> actions, boolean isExact){
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			topPaths = new LinkedList<Integer>();
			//this.frequenceyPercentage = frequenceyPercentage;
			this.actions = actions;
			this.isExact = isExact;
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub
			
			if((indexEnd-indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks())
						.stream()
						.map(new Function<TopPathsAnalyser, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(TopPathsAnalyser arg0) {
								return arg0.topPaths;
							}
							
						}).reduce(topPaths, new BinaryOperator<LinkedList<Integer>>() {

							
							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {
								
								arg0.addAll(arg1);
								return arg0;
						}});
						
			} else {
							
				LinkedList<String> pathActions;
			
				//has exactly the specified actions in any order
				if(isExact) {
					for(int i=indexStart;i< indexEnd;i++) {
						pathActions = paths.get(i).getPathActions();
						
						if(pathActions.size() != actions.size()) {
							continue;
						}
						
						//compares the two list independent of order
						if(new HashSet<>(pathActions).equals(new HashSet<>(actions))) {
							topPaths.add(i);
						}
					}
					//contains the given action (could have more actions
				} else {
				for(int i=indexStart;i< indexEnd;i++) {
					pathActions = paths.get(i).getPathActions();
					
					//if the path contains all common actions then the path is considered a top one
					if(pathActions.containsAll(actions)) {
						topPaths.add(i);
					}
				}
				}
				
				return topPaths;
			
			}
			
		}
		
		private Collection<TopPathsAnalyser> createSubTasks() {
			List<TopPathsAnalyser> dividedTasks = new LinkedList<TopPathsAnalyser>();
			
			int mid = (indexStart+indexEnd)/2;
			//int startInd = indexEnd - endInd1;
			
			dividedTasks.add(new TopPathsAnalyser(indexStart, mid, actions, isExact));
			dividedTasks.add(new TopPathsAnalyser(mid, indexEnd, actions, isExact));
			
			return dividedTasks;
		}
	}

	
}
