package i.e.lero.spare.pattern_instantiation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ie.lero.spare.franalyser.utility.PredicateType;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.util.StopWatch;

public class BigraphAnalyser {

	private PredicateHandler predicateHandler;
	private LinkedList<GraphPath> paths;
	private boolean isDebugging = true;
	private HashMap<Integer, Bigraph> states;
	private Matcher matcher;
	private int threadPoolSize = 100;
	private ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
	private double partitionSizePercentage = 0.02; //represents the size of the partiition as a percentage of the number of states
	private int partitionSize = 1;
	private int numberOfPartitions = 1;
	private boolean noThreading = false;
	private int minimumPartitionSize = 1;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
	private boolean isTestingTime = true;
	private LocalDateTime timeNow;
	private StopWatch timer = new StopWatch();
	
	public BigraphAnalyser() {
		predicateHandler = null;
		states = SystemInstanceHandler.getStates();
		matcher = new Matcher();
	
		setParitionSize();	
		
	}
	
	private void setParitionSize() {
		
		//set partition size
		//also can determine if partitioning is needed depending on the number of states
		if(states.size() < 100) {
			noThreading = true;
		} else {
			int tries = 0;
			partitionSize = (int)(states.size()*partitionSizePercentage);
			
			while (tries < 100 && partitionSize < minimumPartitionSize) {
				//percentage is increase by a certain number to increase the size of the partition (but nothing above 50%)
				partitionSizePercentage *= 1.5;
				
				if(partitionSizePercentage >0.5) {
					partitionSizePercentage = 0.5;
					partitionSize = (int)(states.size()*partitionSizePercentage);
					
					if(partitionSize < minimumPartitionSize) {
						tries = 100;
					}
					break;
				}
				
				partitionSize = (int)(states.size()*partitionSizePercentage);
				tries++;
			}
			
			//if the partition size is not increased after the number of tries then no threading is done
			if(tries == 100) {
				noThreading = true;
			}
			
			numberOfPartitions = states.size()/partitionSize;
			
		}
		
		if(isTestingTime) {
			if(noThreading) {
				print("number of states: "+states.size()+"\nNo threads");
			} else {
				print("number of states: "+states.size()+"\npartition size: "+partitionSize + "\nnumber of partitions: "+ numberOfPartitions+"\nthread pool size: " + threadPoolSize);
			}
					
		}
	
	}

	public BigraphAnalyser(PredicateHandler predHandler) {
		this();
		predicateHandler = predHandler;
	}

	public PredicateHandler analyse() {
		
		identifyRelevantStates();
		predicateHandler =  identifyStateTransitions();
		
		executor.shutdown();
		
		return predicateHandler;

	}

	public PredicateHandler getPredicateHandler() {
		return predicateHandler;
	}

	public void setPredicateHandler(PredicateHandler predicateHandler) {
		
		this.predicateHandler = predicateHandler;
	}

	public PredicateHandler identifyRelevantStates() {
		
		ArrayList<String> activitiesName = predicateHandler.getActivitNames();
		
		for (String nm : activitiesName) {
			identifyRelevantStates(nm);
		}
		
		return predicateHandler;
	}

	// could be transfered to predicateHandler class
	public ArrayList<Predicate> identifyRelevantStates(String activityName) {
		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		for (Predicate p : preds) {
			identifyRelevantStates(p);
		}

		return preds;
	}
	
	public boolean identifyRelevantStates(Predicate pred) {
		
		
		boolean areStatesIdentified = false;
		
		if(isTestingTime) {
			timeNow =  LocalDateTime.now();
			String startTime = "Start time: " + dtf.format(timeNow);
			//print(startTime);
			timer.reset();
			timer.start();
		}
		
		if(pred == null) {
			return false;
		}
		
		Bigraph redex = pred.getBigraphPredicate();
		
		if(redex == null) {
			return false;
		}		
		
		//divide the states array for mult-threading
		if(!noThreading) {	
		try {
			
		
		LinkedList<Future<LinkedList<Integer>>> results = new LinkedList<Future<LinkedList<Integer>>>();
		
		LinkedList<Integer> statesResults = new LinkedList<Integer>();
		
		//run tasks
		int index=0;
		
		for(index=0;index<numberOfPartitions;index++) {
			results.add(executor.submit(new BigraphMatcher(partitionSize*index, partitionSize*(index+1), redex.clone())));
		}
		
		//last partition takes the residue as well
		if(index*partitionSize < states.size()) {
			results.add(executor.submit(new BigraphMatcher(partitionSize*index, states.size(), redex.clone())));	
		}
		
		
		//get results
		for(int i=0;i<results.size();i++) {
			LinkedList<Integer> res = results.get(i).get();
			
			if(res != null && !res.isEmpty())
			statesResults.addAll(res);
		}
			
		//set the predicate states
		pred.setBigraphStates(statesResults);
		
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} else {
			for(int i =0; i<states.size();i++) {	
				if(matcher.match(states.get(i), redex).iterator().hasNext()){
					pred.addBigraphState(i);
					areStatesIdentified = true;
					//print("state " + i%length + " matched");		
				}
			}
		}
		
	
		if(isTestingTime) {
			timer.stop();
			timeNow = LocalDateTime.now();	
			//print("\n[End time: " + dtf.format(EndingTime) +"]");
			
			long timePassed = timer.getEllapsedMillis();
			
			int hours = (int)(timePassed/3600000)%60;
			int mins = (int)(timePassed/60000)%60;
			int secs = (int)(timePassed/1000)%60;
			int secMils = (int)timePassed%1000;
			
			//execution time
			print("\nExecution time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
		}

		System.out.println(pred.getName()+"-states: "+pred.getBigraphStates());
		return areStatesIdentified;
	}

	public PredicateHandler identifyStateTransitions() {

		for (String activityName : predicateHandler.getActivitNames()) {
			identifyStateTransitions(activityName);
		}

		return predicateHandler;
	}

	public void identifyStateTransitions(String activityName) {

		// identifyRelevantStates(activityName);
		ArrayList<Predicate> preconditions = predicateHandler.getPredicates(activityName, PredicateType.Precondition);
		ArrayList<Predicate> postconditions = predicateHandler.getPredicates(activityName, PredicateType.Postcondition);

		for (Predicate pre : preconditions) {
			pre.removeAllPaths();
			for (Predicate post : postconditions) { // this can be limited to
													// conditions that are
													// associated with each
													// other
				post.removeAllPaths();
				paths = SystemInstanceHandler.getTransitionSystem().getPaths(pre, post);
				pre.addPaths(paths);
				post.addPaths(paths);
			}
		}

	}
	
	private void print(String msg) {
		if(isDebugging) {
			System.out.println(msg);
		}
	}
	/*public static void main(String[] args){
		BigraphAnalyser a = new BigraphAnalyser();
		Predicate p = null;
		a.identifyRelevantStates(p);
	}*/

	class BigraphMatcher implements Callable<LinkedList<Integer>> {

		int indexStart;
		int indexEnd;
		Bigraph redex;
		LinkedList<Integer> matchedStates;
		
		public BigraphMatcher(int indexStart, int indexEnd, Bigraph redex){
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redex = redex;
			matchedStates = new LinkedList<Integer>();
		}
		
		@Override
		public LinkedList<Integer> call() throws Exception {			
			for(int i = indexStart; i<indexEnd; i++) {
				if(matcher.match(states.get(i), redex).iterator().hasNext()){
					matchedStates.add(i);
				}
			}
			
			return matchedStates;
		}
		
	}
	
	/*class ActivityMatcher implements Callable<Integer>{

		private String activityName;
		
		public ActivityMatcher(String activityName) {
			this.activityName = activityName;
			System.out.println(activityName);
		}
		
		@Override
		public Integer call() throws Exception {
			// TODO Auto-generated method stub
			ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

			for (Predicate p : preds) {
				identifyRelevantStates(p);
			}
			
			return null;
		}
		
		public boolean identifyRelevantStates(Predicate pred) {
			
			boolean areStatesIdentified = false;
				
			if(pred == null) {
				return false;
			}
			
			Bigraph redex = pred.getBigraphPredicate();
			
			if(redex == null) {
				return false;
			}		
			
			//divid the states array for mult-threading
			try {
				
			LinkedList<Future<LinkedList<Integer>>> results = new LinkedList<Future<LinkedList<Integer>>>();
			
			LinkedList<Integer> statesResults = new LinkedList<Integer>();
			
			//run tasks
			int index=0;
			int size = states.size()/partitionSize;
			
			for(index=0;index<size-1;index++) {
				results.add(executor.submit(new BigraphMatcher(partitionSize*index, partitionSize*(index+1), redex.clone())));
			}
			
			//last partition takes the residue as well
			results.add(executor.submit(new BigraphMatcher(partitionSize*index, states.size(), redex.clone())));
			
			//get results
			for(int i=0;i<size;i++) {
				statesResults.addAll(results.get(i).get());
			}
				
			//set the predicate states
			pred.setBigraphStates(statesResults);
			
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//should be kept in case number of states is not high
			for(int i =0; i<states.size();i++) {	
				if(matcher.match(states.get(i), redex).iterator().hasNext()){
					pred.addBigraphState(i);
					areStatesIdentified = true;
					//print("state " + i%length + " matched");		
				}
			}
			
			System.out.println(pred.getName()+"-states: "+pred.getBigraphStates());
			return areStatesIdentified;
		}
		
	}*/
}
