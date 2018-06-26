package i.e.lero.spare.pattern_instantiation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.PredicateType;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.util.StopWatch;

public class BigraphAnalyser {

	private PredicateHandler predicateHandler;
	private LinkedList<GraphPath> paths;
//	private boolean isDebugging = true;
	private HashMap<Integer, Bigraph> states;
	private Matcher matcher;
//	private int threadPoolSize = 1;
//	private double partitionSizePercentage = 0.0645; //represents the size of the partiition as a percentage of the number of states
//	private int partitionSize = 1;
	private int numberOfPartitions = 1;
	private boolean isThreading = true; //this is for threading bigraph matching in the identifyRelevantStates function
//	private ExecutorService executor; //= Executors.newFixedThreadPool(threadPoolSize);
	private boolean isPredicateThreading = true; //this is to thread the conditions of the activities (this is separate from threading bigraph matching)
	private int numberofActivityParallelExecution = 1; //determines how many activities should be threaded
	private ExecutorService predicateExecutor = Executors.newFixedThreadPool(numberofActivityParallelExecution*2); //pool size for parallel running
//	private int minimumPartitionSize = 1;
	private BlockingQueue<String> msgQ;
	private int threadID;
	private int maxWaitingTime = 24;
	private TimeUnit timeUnit = TimeUnit.HOURS;
//	private LinkedList<Integer> allMatchedStates = new LinkedList<Integer>();
	
	//using forkjoin threading
	private ForkJoinPool mainPool;
	
	//for testing
//	private boolean isLite = false;
	private int numberOfConditions = 0;
//	private boolean isLastCondition = false;
//	private boolean isLastActivity = false;
	private boolean isTestingTime = true;
//	private int timeIndex = 0;
//	private int averageTime = 0;
//	private LocalDateTime timeNow;
	//private it.uniud.mads.jlibbig.core.std.BigraphMatcher bigraphMatcher;
	private StopWatch timer = new StopWatch();
//	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
	private LinkedList<Future<Integer>> predicateResults = new LinkedList<Future<Integer>>();
	
	public BigraphAnalyser() {
		
		this(null, -1);
	}
	
	public BigraphAnalyser(PredicateHandler predHandler) {
		this(predHandler, -1);
		//predicateHandler = predHandler;
	}

	public BigraphAnalyser(PredicateHandler predHandler, int threadID) {
		
		states = SystemInstanceHandler.getStates();
		matcher = new Matcher();
		//bigraphMatcher = new it.uniud.mads.jlibbig.core.std.BigraphMatcher();
		msgQ = Logger.getInstance().getMsgQ();
		predicateHandler = predHandler;
		this.threadID=  threadID;
		mainPool = new ForkJoinPool();
	//	setParitionSize();
	}
	
	/*private void setParitionSize() {
		
		//set partition size
		//also can determine if partitioning is needed depending on the number of states
		
		//set thread pool size to be the number of available processors
		//seems half of the processors is a good number
		
		try {
		if(states.size() < 100) {
			isThreading = false;
		} else {
			int tries = 0;
			
			threadPoolSize = Runtime.getRuntime().availableProcessors()+2;
			mainPool = new ForkJoinPool();
			
			executor = Executors.newFixedThreadPool(threadPoolSize);
			//probalby will be fixed to 100 (maybe more, more testing is needed)
			partitionSize = 100;//(int)(states.size()*partitionSizePercentage);
			
			numberOfPartitions = states.size()/partitionSize;
			
		}
		
		if(isTestingTime) {
			
			if(isThreading) {
			
					msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>number of states: "+states.size()+", partition size: "+partitionSize + " ("+ (int)((partitionSize*1.0/states.size())*10000)/100.0+ "%)"+
							", number of partitions: "+ numberOfPartitions+", thread pool size: " + threadPoolSize);
				
			} else {
				msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>number of states: "+states.size()+", No threads");
			}
					
		}
			} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
*/
	
	public PredicateHandler analyse() {
		
		try {
			
			StopWatch timer = new StopWatch();
			
			//testing timing
			if(isTestingTime) {
				
				if(isThreading) {
				
					if(states.size() > BigraphMatcher.THRESHOLD) {
					numberOfPartitions = (int)Math.pow(2, Math.ceil(32 - Integer.numberOfLeadingZeros( states.size()/BigraphMatcher.THRESHOLD )));
					} else {
						numberOfPartitions = 1;
					}
					
						msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>number of states: "+states.size()+", partition size <= "+BigraphMatcher.THRESHOLD + " ("+ 
						(int)((BigraphMatcher.THRESHOLD*1.0/states.size())*10000)/100.0+ "%)"+
								", number of partitions: "+ numberOfPartitions+", Number of parallel activities = "+numberofActivityParallelExecution+", Parallelism for matching (= num of processors): " + mainPool.getParallelism());
					
				} else {
					msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>number of states: "+states.size()+", No threads");
				}
						
			}
		
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>identifying states...");
			
			timer.start();
			if(isPredicateThreading) {
				identifyRelevantStatesWithThreading();
				predicateExecutor.shutdown();	
				
				if (!predicateExecutor.awaitTermination(maxWaitingTime, timeUnit)) {
					msgQ.put("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " " + timeUnit + "]");
				}
			} else {
				identifyRelevantStates();	
			}
			
	
			mainPool.shutdown();
			if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
				msgQ.put("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " " + timeUnit + "]");
			}
		

			//calculate time requried for finding matching states to the activities conditions
			
			if(isTestingTime) {
				//averageTime /=numberOfConditions;
				timer.stop();
				long totalTime = timer.getEllapsedMillis();
				int maxHours = (int)(totalTime/3600000)%60;
				int maxMins = (int)(totalTime/60000)%60;
				int maxSecs = (int)(totalTime/1000)%60;
				int maxMils = (int)totalTime%1000;
			
				long avgTime = totalTime/numberOfConditions;
				
				int avgHours = (int)(avgTime/3600000)%60;
				int avgMins = (int)(avgTime/60000)%60;
				int avgSecs = (int)(avgTime/1000)%60;
				int avgMils = (int)avgTime%1000;
				
				msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Total matching time of conditions = "+ totalTime+"ms ["+ maxHours+"h:"+ maxMins+"m:"+ maxSecs+"s:"+ maxMils+"ms]");
				msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Average matching time of conditions = "+ avgTime +"ms ["+ avgHours+"h:"+ avgMins+"m:"+ avgSecs+"s:"+ avgMils+"ms]");
				
			}
		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
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
		
		for (int i =0;i<activitiesName.size(); i++) {
			
			identifyRelevantStates(activitiesName.get(i));
			
		}

		return predicateHandler;
	}
	
public PredicateHandler identifyRelevantStatesWithThreading() {
		
		ArrayList<String> activitiesName = predicateHandler.getActivitNames();
		
		try {
		
			int cnt = 0;
		for (int i =0;i<activitiesName.size(); i++) {
			
			identifyRelevantStates(activitiesName.get(i));
			
			//used to determine when to stop parallelism of activities and wait for current tasks to finish (determined by the number of activities to parallel)
			cnt++;

			if((cnt%numberofActivityParallelExecution == 0) || (i == activitiesName.size()-1)) {
				for(int j = 0; j< predicateResults.size();j++) {
						predicateResults.get(j).get();
					
				}
				cnt = 0;
				predicateResults.clear();
			}

		}
		
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return predicateHandler;
	}

	/**
	 * Identifies the states of the system that matches the conditions (pre & post) of the given activity name
	 * @param activityName
	 * @return
	 */
	public void identifyRelevantStates(String activityName) {
		
		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);
		
		//LinkedList<Future<Integer>> futurePreds = new LinkedList<Future<Integer>>();
		try {
			
		for (int i = 0;i< preds.size();i++) {
			//futurePreds.clear();
			/*if(isLastActivity && i == preds.size()-1) {
				isLastCondition = true;
			}*/
			
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Executing condition: "+ preds.get(i).getName());
			
			if(isPredicateThreading) {
				predicateResults.add(predicateExecutor.submit(new PredicateMatcher(preds.get(i))));
			} else {
				identifyRelevantStates(preds.get(i));	
			}
			
		}

		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//return preds;
	}
	
	/**
	 * Identifies the states of the system that matches the give condition
	 * @param pred the condition
	 * @return true if there is at least one state that matches it
	 */
	public boolean identifyRelevantStates(Predicate pred) {
		
		boolean areStatesIdentified = false;
		
		if(isTestingTime) {
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
		LinkedList<Integer> statesResults;
		
		if(isThreading) {	
			 
			//use the ForkJoin to multi-thread the matching. The states are divided in half as long as its size above THRESHOLD set in the ForkJoin class extended here
			statesResults = mainPool.invoke(new BigraphMatcher(0, states.size(), redex));
			
		}
		//this else can be removed as the fork class can decide to create only 1 task if size is less than threshold
		else {
			statesResults = new LinkedList<Integer>();
			
				for(int i =0; i<states.size();i++) {
				if(matcher.match(states.get(i), redex).iterator().hasNext()){
					statesResults.add(i);	
				}
	
			}
			
		}
		
		//set bigraph states in the conditions
		pred.setBigraphStates(statesResults);
		
		msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>"+pred.getName()+"-states: "+pred.getBigraphStates());
		
		if(isTestingTime) {
			timer.stop();
	
			long timePassed = timer.getEllapsedMillis();
			
			int hours = (int)(timePassed/3600000)%60;
			int mins = (int)(timePassed/60000)%60;
			int secs = (int)(timePassed/1000)%60;
			int secMils = (int)timePassed%1000;
			
			//execution time
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Condition ["+pred.getName()+"] matching time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
			//averageTime += timePassed;
			numberOfConditions++;
		}

		
		
		} catch (InterruptedException e) {
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
	
/*	private void print(String msg) {
		if(isDebugging) {
			System.out.println(msg);
		}
	}*/
	/*public static void main(String[] args){
		BigraphAnalyser a = new BigraphAnalyser();
		Predicate p = null;
		a.identifyRelevantStates(p);
	}*/

	/*class BigraphMatcherExecutor implements Callable<LinkedList<Integer>> {

		int indexStart;
		int indexEnd;
		Bigraph redex;
		LinkedList<Integer> matchedStates;
		
		public BigraphMatcherExecutor(int indexStart, int indexEnd, Bigraph redex){
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
		
	}*/
	
	class BigraphMatcher extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		//private LinkedList<Bigraph> states;
		private Bigraph redex;
		private LinkedList<Integer> matchedStates;
		private final static int THRESHOLD = 50; //threshold for the number of states on which task is further subdivided into halfs
		
		//for testing
		//protected int numOfParts = 0;
		
		public BigraphMatcher(int indexStart, int indexEnd, Bigraph redex){
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redex = redex;
			matchedStates = new LinkedList<Integer>();
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub
			
			if((indexEnd-indexStart) > THRESHOLD) {
				return ForkJoinTask.invokeAll(createSubTasks())
						.stream()
						.map(new Function<BigraphMatcher, LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(BigraphMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedStates;
							}
							
						}).reduce(matchedStates, new BinaryOperator<LinkedList<Integer>>() {

							@Override
							public LinkedList<Integer> apply(LinkedList<Integer> arg0, LinkedList<Integer> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}
							
						});
						
			} else {
				for(int i =indexStart; i<indexEnd;i++) {
					if(matcher.match(states.get(i), redex).iterator().hasNext()){
						matchedStates.add(i);	
					}
				}
				
				return matchedStates;
			}
			
		}
		
		private Collection<BigraphMatcher> createSubTasks() {
			List<BigraphMatcher> dividedTasks = new LinkedList<BigraphMatcher>();
			
			int mid = (indexStart+indexEnd)/2;
			//int startInd = indexEnd - endInd1;
			
			dividedTasks.add(new BigraphMatcher(indexStart, mid, redex));
			dividedTasks.add(new BigraphMatcher(mid, indexEnd, redex));
			
			return dividedTasks;
		}
	}
	
	/*class ActivityMatcher implements Callable<Integer>{

		private String activityName;
		
		public ActivityMatcher(String activityName) {
			this.activityName = activityName;
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
	
	class PredicateMatcher implements Callable<Integer>{

		private Predicate pred;
		private Matcher matcher;
		
		public PredicateMatcher(Predicate pred) {
			this.pred = pred;
			matcher = new Matcher();
		}
		
		@Override
		public Integer call() throws Exception {
	
			StopWatch timer = null; 
			
			if(isTestingTime) {
				timer = new StopWatch();
				//timer.reset();
				timer.start();
			}
			
			try {
				
			if(pred == null) {
				return -1;
			}
			
			Bigraph redex = pred.getBigraphPredicate();
			
			if(redex == null) {
				return -1;
			}		
			
			LinkedList<Integer> statesResults; 
			
			//divide the states array for mult-threading
			if(isThreading) {	

			BigraphMatcher bigraphMatcher = new BigraphMatcher(0, states.size(), redex);
			statesResults = mainPool.invoke(bigraphMatcher);
			
			} 
			//this else can be removed as the fork class can decide to create only 1 task if size is less than threshold
			else {
					statesResults = new LinkedList<Integer>();
					for (int i = 0; i < states.size(); i++) {
						if (matcher.match(states.get(i), redex).iterator().hasNext()) {
							statesResults.add(i);
						}
					}
				}
			
			//set result
			pred.setBigraphStates(statesResults);
			
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>"+pred.getName()+"-states: "+pred.getBigraphStates());
			
			if(isTestingTime) {
				timer.stop();
				
				long timePassed = timer.getEllapsedMillis();
				
				int hours = (int)(timePassed/3600000)%60;
				int mins = (int)(timePassed/60000)%60;
				int secs = (int)(timePassed/1000)%60;
				int secMils = (int)timePassed%1000;
				
				//execution time
				msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Condition ["+pred.getName()+"] matching time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
				//averageTime += timePassed;
				numberOfConditions++;
				
			}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return 1;
		}
		
	}
}
