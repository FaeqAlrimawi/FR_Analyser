package i.e.lero.spare.pattern_instantiation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.PredicateType;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.util.StopWatch;
import net.sf.saxon.expr.IsLastExpression;

public class BigraphAnalyser {

	private PredicateHandler predicateHandler;
	private LinkedList<GraphPath> paths;
	private boolean isDebugging = true;
	private HashMap<Integer, Bigraph> states;
	private Matcher matcher;
	private int threadPoolSize = 1;
	private double partitionSizePercentage = 0.0645; //represents the size of the partiition as a percentage of the number of states
	private int partitionSize = 1;
	private int numberOfPartitions = 1;
	private boolean isThreading = true; //this is for threading bigraph matching in the identifyRelevantStates function
	private ExecutorService executor; //= Executors.newFixedThreadPool(threadPoolSize);
	private boolean isPredicateThreading = true; //this is to thread the conditions of the activities (this is separate from threading bigraph matching)
	private int numberofActivityParallelExecution = 1; //determines how many activities should be threaded
	private ExecutorService activityExecutor = Executors.newFixedThreadPool(numberofActivityParallelExecution*2); //pool size for parallel running
	private int minimumPartitionSize = 1;
	private BlockingQueue<String> msgQ;
	private int threadID;
	private int maxWaitingTime = 24;
	private TimeUnit timeUnit = TimeUnit.HOURS;
	
	//for testing
	private boolean isLite = false;
	private int numberOfConditions = 0;
	private boolean isLastCondition = false;
	private boolean isLastActivity = false;
	private boolean isTestingTime = true;
	private int timeIndex = 0;
	private int averageTime = 0;
	private LocalDateTime timeNow;
	private it.uniud.mads.jlibbig.core.std.BigraphMatcher bigraphMatcher;
	private StopWatch timer = new StopWatch();
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
	private LinkedList<Future<Integer>> predicateResults = new LinkedList<Future<Integer>>();
	
	
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
			
			threadPoolSize = Runtime.getRuntime().availableProcessors()+2;
			executor = Executors.newFixedThreadPool(threadPoolSize);
			//probalby will be fixed to 100 (maybe more, more testing is needed)
			partitionSize = 100;//(int)(states.size()*partitionSizePercentage);
			
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

	
	public PredicateHandler analyse() {
		
		try {
		
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>identifying states...");
			
			identifyRelevantStates();
	
			//msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>identifying state transitions...");
			
			//predicateHandler =  identifyStateTransitions();
		
			//msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>transitions identified");
		 executor.shutdown();
		 activityExecutor.shutdown();
		
		/*if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
			msgQ.put("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " " + timeUnit + "]");
		}*/
		
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
		
		try {
		
			int cnt = 0;
		for (int i =0;i<activitiesName.size(); i++) {
			
			identifyRelevantStates(activitiesName.get(i));
			
			cnt++;
			
			if(i == activitiesName.size()-1) {
				for(int j = 0; j< predicateResults.size();j++) {
					predicateResults.get(j).get();	
				}
				cnt = 0;
				predicateResults.clear();
			
				isLastActivity = true;
			} else {
			
			
			if(isPredicateThreading && (cnt%numberofActivityParallelExecution == 0)) {
				for(int j = 0; j< predicateResults.size();j++) {
						predicateResults.get(j).get();
					
				}
				cnt = 0;
				predicateResults.clear();
			}
			}
		
		}
		
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		
		if(isLastCondition) {
			averageTime /=numberOfConditions;
			int avgHours = (int)(averageTime/3600000)%60;
			int avgMins = (int)(averageTime/60000)%60;
			int avgSecs = (int)(averageTime/1000)%60;
			int avgMils = (int)averageTime%1000;
			try {
				msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Average matching time of predicates = "+ averageTime+"ms ["+ avgHours+"h:"+ avgMins+"m:"+ avgSecs+"s:"+ avgMils+"ms]");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			if(isLastActivity && i == preds.size()-1) {
				isLastCondition = true;
			}
			
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>Executing predicate "+ preds.get(i).getName());
			
			if(isPredicateThreading) {
				predicateResults.add(activityExecutor.submit(new PredicateMatcher(preds.get(i))));
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
			if(isLite) {
			long avr = 0;
			long timePassed = 0;
			StopWatch timer = new StopWatch();
			for(int i =0; i<states.size();i++) {
				timer.reset();
				timer.start();
				if(bigraphMatcher.matchBigraph(states.get(i), redex)) {
					pred.addBigraphState(i);
				} 
				timer.stop();
				timePassed = timer.getEllapsedMillis();
				avr+=timePassed;

				//execution time
			//	msgQ.put("Lite Bigraph state-redex matching time =  " +  timePassed+"ms");
			}
			//msgQ.put("Lite Average time for matching operation = " + avr/states.size()+"ms");
				
			} else {
				long avr = 0;
				long timePassed = 0;
				StopWatch timer = new StopWatch();
				for(int i =0; i<states.size();i++) {
				timer.reset();
				timer.start();
				if(matcher.match(states.get(i), redex).iterator().hasNext()){
					pred.addBigraphState(i);
					//areStatesIdentified = true;
					//print("state " + i%length + " matched");		
				}
				timer.stop();
				timePassed = timer.getEllapsedMillis();
				avr+=timePassed;
				
				//execution time
				//msgQ.put("Normal Bigraph state-redex matching time =  " +  timePassed+"ms");
			}
				//msgQ.put("Normal Average time for matching operation = " + avr/states.size()+"ms");
			}
			
		}
		
		msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>"+pred.getName()+"-states: "+pred.getBigraphStates());
		
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
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>predicate matching time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
			averageTime += timePassed;
			numberOfConditions++;
		}

		
		
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

	//identi
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
		
		public PredicateMatcher(Predicate pred) {
			this.pred = pred;
		}
		
		@Override
		public Integer call() throws Exception {
			
			//this represents the identifyRelevantStates(Predicate pred) function
			boolean areStatesIdentified = false;
			StopWatch timer = new StopWatch();
			
			if(isTestingTime) {
				timeNow =  LocalDateTime.now();
				String startTime = "Start time: " + dtf.format(timeNow);
				//print(startTime);
				timer.reset();
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
				if(isLite) {
				long avr = 0;
				long timePassed = 0;
				StopWatch timer2 = new StopWatch();
				for(int i =0; i<states.size();i++) {
					timer2.reset();
					timer2.start();
					if(bigraphMatcher.matchBigraph(states.get(i), redex)) {
						pred.addBigraphState(i);
					} 
					timer2.stop();
					timePassed = timer2.getEllapsedMillis();
					avr+=timePassed;

					//execution time
					//msgQ.put("Lite Bigraph state-redex matching time =  " +  timePassed+"ms");
				}
				//msgQ.put("Lite Average time for matching operation = " + avr/states.size()+"ms");
					
				} else {
					long avr = 0;
					long timePassed = 0;
					StopWatch timer2 = new StopWatch();
					for(int i =0; i<states.size();i++) {
					timer2.reset();
					timer2.start();
					if(matcher.match(states.get(i), redex).iterator().hasNext()){
						pred.addBigraphState(i);
						//areStatesIdentified = true;
						//print("state " + i%length + " matched");		
					}
					timer2.stop();
					timePassed = timer2.getEllapsedMillis();
					avr+=timePassed;
					
					//execution time
				//	msgQ.put("Normal Bigraph state-redex matching time =  " +  timePassed+"ms");
				}
				//	msgQ.put("Normal Average time for matching operation = " + avr/states.size()+"ms");
				}
				
			}
			
			msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>"+pred.getName()+"-states: "+pred.getBigraphStates());
			
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
				msgQ.put("Thread["+threadID+"]>>BigraphAnalyser>>predicate matching time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
				averageTime += timePassed;
				numberOfConditions++;
				
			}

			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return 1;
		}
		
	}
}
