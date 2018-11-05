package ie.lero.spare.pattern_instantiation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	// private boolean isDebugging = true;
	private HashMap<Integer, Bigraph> states;
	private Matcher matcher;

	/** Threading parameters **/
	// determines if activities should run in parallel
	private boolean isThreading = true;

	// determine if predicates should run in parallel
	private boolean isPredicateThreading = true;

	// determines how many activities should be threaded
	private int numberofActivityParallelExecution = 1;

	private ExecutorService predicateExecutor = Executors.newCachedThreadPool();
	/************************/

	// private int minimumPartitionSize = 1;
	// private BlockingQueue<String> msgQ;
	private int threadID;
	private int maxWaitingTime = 24;
	private TimeUnit timeUnit = TimeUnit.HOURS;

	// used to calculate average time of execution
	private int numberOfConditions = 0;

	// threshold for the number of states on which task is further subdivided
	// into halfs
	private int threshold = 200;
	private static final int Adjust_Threshold_State_Number = 1000;
	private static final double PERCENTAGE__OF_STATES = 0.10;
	private static final int DEFAULT_THRESHOLD = 100;
	// using forkjoin threading for dividing the matching process of states to a condition
	private ForkJoinPool mainPool;

	private boolean isTestingTime = true;

	private StopWatch timer = new StopWatch();

	// private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd
	// HH:mm:ss");

	private LinkedList<Future<Integer>> predicateResults = new LinkedList<Future<Integer>>();
	private Logger logger;

	public BigraphAnalyser() {

		this(null, -1);
	}

	public BigraphAnalyser(PredicateHandler predHandler) {
		this(predHandler, -1);
		// predicateHandler = predHandler;
	}

	public BigraphAnalyser(PredicateHandler predHandler, int threadID) {

		states = SystemInstanceHandler.getStates();
		matcher = new Matcher();
		// bigraphMatcher = new it.uniud.mads.jlibbig.core.std.BigraphMatcher();
		// msgQ = Logger.getInstance().getMsgQ();
		logger = Logger.getInstance();
		predicateHandler = predHandler;
		this.threadID = threadID;
		mainPool = new ForkJoinPool();
		threshold = DEFAULT_THRESHOLD;
		// setParitionSize();
	}

	/*
	 * private void setParitionSize() {
	 * 
	 * //set partition size //also can determine if partitioning is needed
	 * depending on the number of states
	 * 
	 * //set thread pool size to be the number of available processors //seems
	 * half of the processors is a good number
	 * 
	 * try { if(states.size() < 100) { isThreading = false; } else { int tries =
	 * 0;
	 * 
	 * threadPoolSize = Runtime.getRuntime().availableProcessors()+2; mainPool =
	 * new ForkJoinPool();
	 * 
	 * executor = Executors.newFixedThreadPool(threadPoolSize); //probalby will
	 * be fixed to 100 (maybe more, more testing is needed) partitionSize =
	 * 100;//(int)(states.size()*partitionSizePercentage);
	 * 
	 * numberOfPartitions = states.size()/partitionSize;
	 * 
	 * }
	 * 
	 * if(isTestingTime) {
	 * 
	 * if(isThreading) {
	 * 
	 * logger.putMessage("Thread["+
	 * threadID+"]>>BigraphAnalyser>>number of states: "+states.size()
	 * +", partition size: "+partitionSize + " ("+
	 * (int)((partitionSize*1.0/states.size())*10000)/100.0+ "%)"+
	 * ", number of partitions: "+ numberOfPartitions+", thread pool size: " +
	 * threadPoolSize);
	 * 
	 * } else { logger.putMessage("Thread["+
	 * threadID+"]>>BigraphAnalyser>>number of states: "+states.size()
	 * +", No threads"); }
	 * 
	 * } } catch (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * }
	 */

	public int getNumberofActivityParallelExecution() {
		return numberofActivityParallelExecution;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setNumberofActivityParallelExecution(int numberofActivityParallelExecution) {
		this.numberofActivityParallelExecution = numberofActivityParallelExecution;
	}

	public PredicateHandler analyse() {

		try {

			StopWatch timer = new StopWatch();

			// start time
			timer.start();

			if (isThreading) {

				int numberOfPartitions = (int) Math.ceil(states.size()/(1.0*threshold));

				if (numberOfPartitions < mainPool.getParallelism()) {
					
					numberOfPartitions = mainPool.getParallelism();
					
					threshold = (int)Math.ceil(states.size()/(1.0*numberOfPartitions));
				}

				if (states.size() > threshold) {
					numberOfPartitions = (int) Math.pow(2,
							Math.ceil(32 - Integer.numberOfLeadingZeros(states.size() / threshold)));
				} else {
					numberOfPartitions = 1;
				}

				if (isTestingTime) {
					logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>number of states: " + states.size()
							+ ", partition size <= threshold [" + threshold + "] ("
							+ (int) ((threshold * 1.0 / states.size()) * 10000) / 100.0 + "%)"
							+ ", number of partitions: " + numberOfPartitions + ", Number of parallel activities = "
							+ numberofActivityParallelExecution + ", Parallelism for matching (= num of processors): "
							+ mainPool.getParallelism());
				}

				logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>identifying states...");

				// this method runs predicate in parallel if
				// isPredicateThreading is set to true
				identifyRelevantStatesWithThreading();

				// this method runs the predicates in series
				// identifyRelevantStatesWithThreadingArray();

				predicateExecutor.shutdown();
				mainPool.shutdown();

				if (!predicateExecutor.awaitTermination(maxWaitingTime, timeUnit)) {
					logger.putMessage("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " "
							+ timeUnit + "]");
				}

				if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
					logger.putMessage("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " "
							+ timeUnit + "]");
				}

			} else {
				logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>identifying states...");
				identifyRelevantStates();
			}

			// calculate time requried for finding matching states to the
			// activities conditions

			if (isTestingTime) {
				// averageTime /=numberOfConditions;
				timer.stop();
				long totalTime = timer.getEllapsedMillis();
				int maxHours = (int) (totalTime / 3600000) % 60;
				int maxMins = (int) (totalTime / 60000) % 60;
				int maxSecs = (int) (totalTime / 1000) % 60;
				int maxMils = (int) totalTime % 1000;

				long avgTime = 0;

				if (numberOfConditions > 0) {
					avgTime = totalTime / numberOfConditions;
					;
				}

				int avgHours = (int) (avgTime / 3600000) % 60;
				int avgMins = (int) (avgTime / 60000) % 60;
				int avgSecs = (int) (avgTime / 1000) % 60;
				int avgMils = (int) avgTime % 1000;

				logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>Total matching time of conditions = "
						+ totalTime + "ms [" + maxHours + "h:" + maxMins + "m:" + maxSecs + "s:" + maxMils + "ms]");
				logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>Average matching time of conditions = "
						+ avgTime + "ms [" + avgHours + "h:" + avgMins + "m:" + avgSecs + "s:" + avgMils + "ms]");

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

		for (int i = 0; i < activitiesName.size(); i++) {

			identifyRelevantStates(activitiesName.get(i));

		}

		return predicateHandler;
	}

	public PredicateHandler identifyRelevantStatesWithThreading() {

		ArrayList<String> activitiesName = predicateHandler.getActivitNames();

		try {

			int cnt = 0;
			for (int i = 0; i < activitiesName.size(); i++) {

				identifyRelevantStatesWithThreading(activitiesName.get(i));

				// used to determine when to stop parallelism of activities and
				// wait for current tasks to finish (determined by the number of
				// activities to parallel)
				cnt++;

				if ((cnt % numberofActivityParallelExecution == 0) || (i == activitiesName.size() - 1)) {
					for (int j = 0; j < predicateResults.size(); j++) {
						Future<Integer> future = predicateResults.get(j);
						if (!future.isDone()) {
							future.get();
						}

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

	public PredicateHandler identifyRelevantStatesWithThreadingArray() {

		ArrayList<String> activitiesName = predicateHandler.getActivitNames();

		try {

			int cnt = 0;
			for (int i = 0; i < activitiesName.size(); i++) {

				identifyRelevantStatesWithThreadingArray(activitiesName.get(i));

				// used to determine when to stop parallelism of activities and
				// wait for current tasks to finish (determined by the number of
				// activities to parallel)
				cnt++;

				if ((cnt % numberofActivityParallelExecution == 0) || (i == activitiesName.size() - 1)) {
					for (int j = 0; j < predicateResults.size(); j++) {
						Future<Integer> future = predicateResults.get(j);
						if (!future.isDone()) {
							future.get();
						}

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
	 * Identifies the states of the system that matches the conditions (pre &
	 * post) of the given activity name
	 * 
	 * @param activityName
	 * @return
	 */
	public void identifyRelevantStates(String activityName) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		// LinkedList<Future<Integer>> futurePreds = new
		// LinkedList<Future<Integer>>();
		for (int i = 0; i < preds.size(); i++) {

			logger.putMessage(
					"Thread[" + threadID + "]>>BigraphAnalyser>>Executing condition: " + preds.get(i).getName());

			identifyRelevantStates(preds.get(i));
		}

	}

	public void identifyRelevantStatesWithThreading(String activityName) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		// LinkedList<Future<Integer>> futurePreds = new
		// LinkedList<Future<Integer>>();
		for (int i = 0; i < preds.size(); i++) {
			// futurePreds.clear();
			/*
			 * if(isLastActivity && i == preds.size()-1) { isLastCondition =
			 * true; }
			 */

			logger.putMessage(
					"Thread[" + threadID + "]>>BigraphAnalyser>>Executing condition: " + preds.get(i).getName());

			if (isPredicateThreading) {

				// parallelism
				predicateResults.add(predicateExecutor.submit(new PredicateMatcher(preds.get(i))));

			} else {

				// series
				// identifyRelevantStates(preds.get(i));
				try {
					predicateExecutor.submit(new PredicateMatcher(preds.get(i))).get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	public void identifyRelevantStatesWithThreadingArray(String activityName) {

		ArrayList<Predicate> preds = predicateHandler.getActivityPredicates(activityName);

		// identification is done in series
		predicateResults.add(predicateExecutor.submit(new PredicateMatcherArray(preds)));

	}

	/**
	 * Identifies the states of the system that matches the give condition
	 * 
	 * @param pred
	 *            the condition
	 * @return true if there is at least one state that matches it
	 */
	public boolean identifyRelevantStates(Predicate pred) {

		boolean areStatesIdentified = false;

		if (isTestingTime) {
			timer.reset();
			timer.start();
		}

		if (pred == null) {
			return false;
		}

		Bigraph redex = pred.getBigraphPredicate();

		if (redex == null) {
			return false;
		}

		// divide the states array for mult-threading
		LinkedList<Integer> statesResults;

		// if (isThreading) {
		//
		// // use the ForkJoin to multi-thread the matching. The states are
		// // divided in half as long as its size above THRESHOLD set in the
		// // ForkJoin class extended here
		// statesResults = mainPool.invoke(new BigraphMatcher(0, states.size(),
		// redex));
		//
		// }
		// // this else can be removed as the fork class can decide to create
		// only
		// // 1 task if size is less than threshold
		// else {
		statesResults = new LinkedList<Integer>();

		for (int i = 0; i < states.size(); i++) {
			if (matcher.match(states.get(i), redex).iterator().hasNext()) {
				statesResults.add(i);
			}

			// }

		}

		// set bigraph states in the conditions
		pred.setBigraphStates(statesResults);

		logger.putMessage(
				"Thread[" + threadID + "]>>BigraphAnalyser>>" + pred.getName() + "-states: " + pred.getBigraphStates());

		if (isTestingTime) {
			timer.stop();

			long timePassed = timer.getEllapsedMillis();

			int hours = (int) (timePassed / 3600000) % 60;
			int mins = (int) (timePassed / 60000) % 60;
			int secs = (int) (timePassed / 1000) % 60;
			int secMils = (int) timePassed % 1000;

			// execution time
			logger.putMessage(
					"Thread[" + threadID + "]>>BigraphAnalyser>>Condition [" + pred.getName() + "] matching time: "
							+ timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");
			// averageTime += timePassed;
			numberOfConditions++;
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

	/*
	 * private void print(String msg) { if(isDebugging) {
	 * System.out.println(msg); } }
	 */
	/*
	 * public static void main(String[] args){ BigraphAnalyser a = new
	 * BigraphAnalyser(); Predicate p = null; a.identifyRelevantStates(p); }
	 */

	/*
	 * class BigraphMatcherExecutor implements Callable<LinkedList<Integer>> {
	 * 
	 * int indexStart; int indexEnd; Bigraph redex; LinkedList<Integer>
	 * matchedStates;
	 * 
	 * public BigraphMatcherExecutor(int indexStart, int indexEnd, Bigraph
	 * redex){ this.indexStart = indexStart; this.indexEnd = indexEnd;
	 * this.redex = redex; matchedStates = new LinkedList<Integer>(); }
	 * 
	 * @Override public LinkedList<Integer> call() throws Exception { for(int i
	 * = indexStart; i<indexEnd; i++) { if(matcher.match(states.get(i),
	 * redex).iterator().hasNext()){ matchedStates.add(i); } }
	 * 
	 * return matchedStates; }
	 * 
	 * }
	 */

	class BigraphMatcher extends RecursiveTask<LinkedList<Integer>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		// private LinkedList<Bigraph> states;
		private Bigraph redex;
		private LinkedList<Integer> matchedStates;

		// for testing
		// protected int numOfParts = 0;

		public BigraphMatcher(int indexStart, int indexEnd, Bigraph redex) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redex = redex;
			matchedStates = new LinkedList<Integer>();
		}

		@Override
		protected LinkedList<Integer> compute() {
			// TODO Auto-generated method stub

			if ((indexEnd - indexStart) > threshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
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
				for (int i = indexStart; i < indexEnd; i++) {
					if (matcher.match(states.get(i), redex).iterator().hasNext()) {
						matchedStates.add(i);
					}
				}

				return matchedStates;
			}

		}

		private Collection<BigraphMatcher> createSubTasks() {
			List<BigraphMatcher> dividedTasks = new LinkedList<BigraphMatcher>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new BigraphMatcher(indexStart, mid, redex));
			dividedTasks.add(new BigraphMatcher(mid, indexEnd, redex));

			return dividedTasks;
		}
	}

	class PredicateMatcher implements Callable<Integer> {

		private Predicate pred;
		private Matcher matcher;

		public PredicateMatcher(Predicate pred) {
			this.pred = pred;
			matcher = new Matcher();
		}

		@Override
		public Integer call() throws Exception {

			StopWatch timer = null;

			if (isTestingTime) {
				timer = new StopWatch();
				// timer.reset();
				timer.start();
			}

			if (pred == null) {
				return -1;
			}

			Bigraph redex = pred.getBigraphPredicate();

			if (redex == null) {
				return -1;
			}

			LinkedList<Integer> statesResults;

			// divide the states array for mult-threading
			if (isThreading) {

				BigraphMatcher bigraphMatcher = new BigraphMatcher(0, states.size(), redex);
				statesResults = mainPool.invoke(bigraphMatcher);

			}
			// this else can be removed as the fork class can decide to create
			// only 1 task if size is less than threshold
			else {
				statesResults = new LinkedList<Integer>();
				for (int i = 0; i < states.size(); i++) {
					if (matcher.match(states.get(i), redex).iterator().hasNext()) {
						statesResults.add(i);
					}
				}
			}

			// set result
			pred.setBigraphStates(statesResults);

			logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>" + pred.getName() + "-states: "
					+ pred.getBigraphStates());

			if (isTestingTime) {
				timer.stop();

				long timePassed = timer.getEllapsedMillis();

				int hours = (int) (timePassed / 3600000) % 60;
				int mins = (int) (timePassed / 60000) % 60;
				int secs = (int) (timePassed / 1000) % 60;
				int secMils = (int) timePassed % 1000;

				// execution time
				logger.putMessage(
						"Thread[" + threadID + "]>>BigraphAnalyser>>Condition [" + pred.getName() + "] matching time: "
								+ timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");
				// averageTime += timePassed;
				numberOfConditions++;

			}

			return 1;
		}

	}

	/**
	 * TESTING
	 */
	class BigraphMatcherArray extends RecursiveTask<Map<Integer, LinkedList<Integer>>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		// private LinkedList<Bigraph> states;
		private List<Bigraph> redexes;

		// the key is an integer that is the index of the bigraph in the given
		// list of bigraph (i.e. redexes)
		private Map<Integer, LinkedList<Integer>> matchedStates;

		// for testing
		// protected int numOfParts = 0;

		public BigraphMatcherArray(int indexStart, int indexEnd, List<Bigraph> redex) {
			this.indexStart = indexStart;
			this.indexEnd = indexEnd;
			this.redexes = redex;
			matchedStates = new HashMap<Integer, LinkedList<Integer>>();

			for (int i = 0; i < redex.size(); i++) {
				matchedStates.put(i, new LinkedList<Integer>());
			}
		}

		@Override
		protected Map<Integer, LinkedList<Integer>> compute() {
			// TODO Auto-generated method stub

			if ((indexEnd - indexStart) > threshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<BigraphMatcherArray, Map<Integer, LinkedList<Integer>>>() {

							@Override
							public Map<Integer, LinkedList<Integer>> apply(BigraphMatcherArray arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedStates;
							}

						}).reduce(matchedStates, new BinaryOperator<Map<Integer, LinkedList<Integer>>>() {

							@Override
							public Map<Integer, LinkedList<Integer>> apply(Map<Integer, LinkedList<Integer>> arg0,
									Map<Integer, LinkedList<Integer>> arg1) {
								// TODO Auto-generated method stub
								for (Entry<Integer, LinkedList<Integer>> entry : arg1.entrySet()) {
									if (arg0.containsKey(entry.getKey())) {
										arg0.get(entry.getKey()).addAll(entry.getValue());
									} else {
										arg0.put(entry.getKey(), entry.getValue());
									}
								}
								// arg0.putAll(arg1);
								return arg0;
							}

						});

			} else {
				for (int i = indexStart; i < indexEnd; i++) {
					for (int j = 0; j < redexes.size(); j++) {
						if (matcher.match(states.get(i), redexes.get(j)).iterator().hasNext()) {
							// if (matchedStates.containsKey(j)) {
							// matchedStates.get(j).add(i);
							// } else {
							// LinkedList<Integer> newList = new
							// LinkedList<Integer>();
							//
							// newList.add(i);
							// matchedStates.put(j, newList);
							// }
							matchedStates.get(j).add(i);
						}
					}

				}

				return matchedStates;
			}

		}

		private Collection<BigraphMatcherArray> createSubTasks() {
			List<BigraphMatcherArray> dividedTasks = new LinkedList<BigraphMatcherArray>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new BigraphMatcherArray(indexStart, mid, redexes));
			dividedTasks.add(new BigraphMatcherArray(mid, indexEnd, redexes));

			return dividedTasks;
		}
	}

	class PredicateMatcherArray implements Callable<Integer> {

		private List<Predicate> predicates;

		public PredicateMatcherArray(List<Predicate> preds) {
			predicates = preds;
			matcher = new Matcher();
		}

		@Override
		public Integer call() throws Exception {

			StopWatch timer = null;

			if (isTestingTime) {
				timer = new StopWatch();
				// timer.reset();
				timer.start();
			}

			LinkedList<Bigraph> bigraphs = new LinkedList<Bigraph>();

			for (Predicate pred : predicates) {
				Bigraph redex = pred.getBigraphPredicate();

				if (redex == null) {
					logger.putError("Redex for condition [" + pred.getName() + "] is NULL");
					continue;
				}

				bigraphs.add(redex);
			}

			Map<Integer, LinkedList<Integer>> statesResults;

			BigraphMatcherArray bigraphMatcherArray = new BigraphMatcherArray(0, states.size(), bigraphs);
			statesResults = mainPool.invoke(bigraphMatcherArray);

			// this else can be removed as the fork class can decide to create
			// only 1 task if size is less than threshold
			// else {
			// statesResults = new LinkedList<Integer>();
			// for (int i = 0; i < states.size(); i++) {
			// if (matcher.match(states.get(i), redex).iterator().hasNext()) {
			// statesResults.add(i);
			// }
			// }
			// }

			StringBuilder str = new StringBuilder();

			// set result
			for (int i = 0; i < predicates.size(); i++) {

				Predicate pred = predicates.get(i);

				pred.setBigraphStates(statesResults.get(i));

				logger.putMessage("Thread[" + threadID + "]>>BigraphAnalyser>>" + pred.getName() + "-states: "
						+ pred.getBigraphStates());

				str.append(pred.getName()).append(", ");
			}

			if (isTestingTime) {
				timer.stop();

				long timePassed = timer.getEllapsedMillis();

				int hours = (int) (timePassed / 3600000) % 60;
				int mins = (int) (timePassed / 60000) % 60;
				int secs = (int) (timePassed / 1000) % 60;
				int secMils = (int) timePassed % 1000;

				// execution time
				logger.putMessage(
						"Thread[" + threadID + "]>>BigraphAnalyser>>Conditions [" + str.toString() + "] matching time: "
								+ timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]");
				// averageTime += timePassed;
				numberOfConditions++;

			}

			return 1;
		}

	}
}
