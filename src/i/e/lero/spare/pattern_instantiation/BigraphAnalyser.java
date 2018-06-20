package i.e.lero.spare.pattern_instantiation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ie.lero.spare.franalyser.utility.Logger;
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
	private it.uniud.mads.jlibbig.core.std.BigraphMatcher bigraphMatcher;
	private int threadPoolSize = 1;
	private ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
	private double partitionSizePercentage = 0.0645; //represents the size of the partiition as a percentage of the number of states
	private int partitionSize = 1;
	private int numberOfPartitions = 1;
	private boolean isThreading = false;
	private int minimumPartitionSize = 1;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
	private boolean isTestingTime = true;
	private LocalDateTime timeNow;
	private StopWatch timer = new StopWatch();
	private long times [];
	private int timeIndex = 0;
	private int averageTime = 0;
	private BlockingQueue<String> msgQ;
	private int threadID;
	private boolean isLite = true;
	
	public BigraphAnalyser() {
		
		predicateHandler = null;
		states = SystemInstanceHandler.getStates();
		matcher = new Matcher();
		bigraphMatcher = new it.uniud.mads.jlibbig.core.std.BigraphMatcher();
		msgQ = Logger.getInstance().getMsgQ();
		threadID = -1;
		
		setParitionSize();	
		
	}
	
	public BigraphAnalyser(PredicateHandler predHandler) {
		this();
		predicateHandler = predHandler;
	}

	public BigraphAnalyser(PredicateHandler predHandler, int threadID) {
		
		states = SystemInstanceHandler.getStates();
		matcher = new Matcher();
		bigraphMatcher = new it.uniud.mads.jlibbig.core.std.BigraphMatcher();
		msgQ = Logger.getInstance().getMsgQ();
		predicateHandler = predHandler;
		this.threadID=  threadID;
		setParitionSize();
	}
	
	private void setParitionSize() {
		
		//set partition size
		//also can determine if partitioning is needed depending on the number of states
		
		//set thread pool size to be the number of available processors
		//seems half of the processors is a good number
		
		try {
		if(states.size() < 100) {
			isThreading = false;
		} else {
			int tries = 0;
			
			threadPoolSize = Runtime.getRuntime().availableProcessors()/2;
			//probalby will be fixed to 100 (maybe more, more testing is needed)
			partitionSize = 500;//(int)(states.size()*partitionSizePercentage);
			
			/*while (tries < 100 && partitionSize < minimumPartitionSize) {
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
			}*/
			
			numberOfPartitions = states.size()/partitionSize;
			
		}
		
		if(isTestingTime) {
			times = new long[6];
			
			if(isThreading) {
			
					msgQ.put("Thread["+threadID+"]>>number of states: "+states.size()+", partition size: "+partitionSize + " ("+ (int)((partitionSize*1.0/states.size())*10000)/100.0+ "%)"+
							", number of partitions: "+ numberOfPartitions+", thread pool size: " + threadPoolSize+", Lite matching is " + isLite);
				
			} else {
				msgQ.put("Thread["+threadID+"]>>number of states: "+states.size()+"\nNo threads"+", Lite matching is " + isLite);
			}
					
		}
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
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
		
		try {
			
		if(pred == null) {
			return false;
		}
		
		Bigraph redex = pred.getBigraphPredicate();
		
		if(redex == null) {
			return false;
		}		
		
		//divide the states array for mult-threading
		if(isThreading) {	

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
		
		} else {
			/*for(int i =0; i<states.size();i++) {
				int cnt = 0;
				Iterator res = matcher.match(states.get(i), redex).iterator();
				//proabably getes stuck here
				while(res.hasNext()){
					//pred.addBigraphState(i);
					res.next();
					cnt++;
					areStatesIdentified = true;
					//print("state " + i%length + " matched");		
				}
				
				System.out.println("matches for state ["+i+"] = " + cnt);*/
			if(isLite) {
			for(int i =0; i<states.size();i++) {		
				if(bigraphMatcher.matchBigraph(states.get(i), redex)) {
					pred.addBigraphState(i);
				} 
			}
			} else {
				for(int i =0; i<states.size();i++) {	
				if(matcher.match(states.get(i), redex).iterator().hasNext()){
					pred.addBigraphState(i);
					//areStatesIdentified = true;
					//print("state " + i%length + " matched");		
				}
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
			msgQ.put("Thread["+threadID+"]>>Execution time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
			times[timeIndex] = timePassed;
			timeIndex++;
			
			if(timeIndex == 6) {
				for(long time : times) {
					averageTime+=time;
				}
				averageTime /=6;
				msgQ.put("Thread["+threadID+"]>>Average execution time = "+ averageTime+"ms");
			}
			
		}

		msgQ.put("Thread["+threadID+"]>>"+pred.getName()+"-states: "+pred.getBigraphStates());
		
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
