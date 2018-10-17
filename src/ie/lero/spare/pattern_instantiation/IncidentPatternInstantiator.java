
package ie.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.json.JSONObject;

import ie.lero.spare.franalyser.utility.BigrapherHandler;
import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.util.StopWatch;

public class IncidentPatternInstantiator {

	// private String xqueryFile = "etc/match_query.xq";

	// parallelism parameters
	private int threadPoolSize = 1; // sets how many asset sets can run in
									// parallel
	private int parallelActivities = 1; // sets how many activities can be
										// analysed in parallel
	private int matchingThreshold = 100; // set how many bigraph matching can be
											// done in parallel
	private ExecutorService executor;
	private ForkJoinPool mainPool = new ForkJoinPool();

	// waiting time for executor before firing an exception
	int maxWaitingTime = 48;
	TimeUnit timeUnit = TimeUnit.HOURS;

	// GUI
	IncidentPatternInstantiationListener listener;
	int incrementValue = 10; // for GUI progress bar
	boolean isSetsSelected = false;
	LinkedList<Integer> assetSetsSelected;

	// Logging
	private Logger logger;
	private boolean isPrintToScreen = true;
	private boolean isSaveLog = false;
	private boolean dummy = true;
	
	private String outputFolder = ".";

	private void runLogger() {

		logger = Logger.getInstance();

		logger.setListener(listener);
		logger.setPrintToScreen(isPrintToScreen);
		logger.setSaveLog(isSaveLog);
		logger.setLogFolder(outputFolder + "/log");
		
		logger.createLogFile(); // file name is created internally by the logger
								// (name: log[time_date].txt)

		logger.start();

	}

	/**
	 * Maps given incident pattern to the given system model. Bigraph
	 * representation of the system and generated states are considered to have
	 * the same name as the system model file name
	 * 
	 * @param incidentPatternFile
	 *            incident pattern file path
	 * @param systemModelFile
	 *            system model file path
	 * @param GUIlistener
	 *            a GUI listener used to send messages to the GUI
	 */
	public void execute(String incidentPatternFile, String systemModelFile,
			IncidentPatternInstantiationListener GUIlistener) {

		// brs output folder (containing states) has the same name as the system
		// model file name
		String BRS_outputFolder = systemModelFile.substring(0, systemModelFile.lastIndexOf("."));

		// brs file has the same name as the system model file name but with
		// .big extension instead of .cps
		String BRS_file = BRS_outputFolder + ".big";

		execute(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder, GUIlistener);
	}

	/**
	 * Maps given incident pattern to the given system model.
	 * 
	 * @param incidentPatternFile
	 *            incident pattern file path
	 * @param systemModelFile
	 *            system model file path
	 * @param BRS_file
	 *            Bigraphical Reactive System representation file path
	 * @param BRS_statesFolder
	 *            folder containing generated states using BRS
	 * @param GUIlistener
	 *            a GUI listener used to send messages to the GUI
	 */
	public void execute(String incidentPatternFile, String systemModelFile, String BRS_file, String BRS_statesFolder,
			IncidentPatternInstantiationListener GUIlistener) {

		listener = GUIlistener;

		// String xQueryMatcherFile = xqueryFile;

		try {

			// currently creates a folder named "log" where the states folder is
			outputFolder = BRS_statesFolder.substring(0, BRS_statesFolder.lastIndexOf("/"));

			runLogger();

			XqueryExecuter.SPACE_DOC = systemModelFile;
			XqueryExecuter.INCIDENT_DOC = incidentPatternFile;

			// add the models to the ModelsHandler class (which can be used by
			// other objects like the Mapper to
			// access the models
			ModelsHandler.addIncidentModel(incidentPatternFile);
			ModelsHandler.addSystemModel(systemModelFile);

			StopWatch timer = new StopWatch();

			logger.putMessage("////Executing Scenario1\\\\\\\\");
			logger.putMessage("*Incident pattern file \"" + incidentPatternFile + "\"");
			logger.putMessage("*System model file \"" + systemModelFile + "\"");
			logger.putMessage("*BRS file \"" + BRS_file + "\" & states folder \"" + BRS_statesFolder + "\"");

			// start a timer
			timer.start();

			//// start executing the scenario \\\\
			Mapper m = new Mapper();

			logger.putMessage(">>Matching incident pattern entities to system assets");

			AssetMap am = m.findMatches();

			// if there are incident assets with no matches from space model
			// then exit
			if (am.hasEntitiesWithNoMatch()) {
				logger.putMessage(">>Some incident entities have no matches in the system assets. These are:");
				// getIncidetnAssetWithNoMatch method has some issues
				List<String> asts = am.getIncidentAssetsWithNoMatch();
				logger.putMessage(asts.toString());
				return; // execution stops if there are incident entities with
						// no matching
			}

			// print matched assets
			logger.putMessage(">>Number of Assets (also entities) =  " + am.getIncidentEntityNames().length);
			logger.putMessage(">>Incident entities order: " + Arrays.toString(am.getIncidentEntityNames()));
			logger.putMessage(">>Entity-Asset map:");
			logger.putMessage(am.toString());
			logger.putMessage(">>Generating asset sets..");

			listener.updateAssetMapInfo(am.toStringCompact());

			// generate sequences
			boolean isStrict = true;
			LinkedList<String[]> lst = am.generateUniqueCombinations(isStrict);

			listener.updateProgress(10);
			listener.updateAssetSetInfo(lst);

			// checks if there are sequences generated or not. if not, then
			// execution is terminated
			// this can be loosened to allow same asset to be mapped to two
			// entities
			if (lst == null || lst.isEmpty()) {
				logger.putMessage(">>No combinations found. Terminating execution");
				return;
			}

			logger.putMessage(">>Asset sets (" + lst.size() + "):");

			// print the sets only if there are less than 200. Else, print a 100
			// but save the rest to a file
			boolean oldIsPrintToScreen = isPrintToScreen;

			// print the sets only if there are less than 200. Else, print a 100
			// but save the rest to a file
			for (int i = 0; i < lst.size(); i++) {// adjust the length
				if (isPrintToScreen && i >= 100) {
					isPrintToScreen = false;
					System.out.println("-... [See log file (" + Logger.getInstance().getLogFolder() + "/"
							+ Logger.getInstance().getLogFileName() + ") for the rest]");
				}
				logger.putMessage("-Set[" + i + "]: " + Arrays.toString(lst.get(i)));
			}

			isPrintToScreen = oldIsPrintToScreen;

			logger.putMessage(
					">>Initialising the Bigraphical Reactive System (Loading states & creating the state transition graph)...");

			// initialise the system (load states and transition system)
			boolean isInitialised = initialiseBigraphSystem(BRS_file, BRS_statesFolder);

			if (isInitialised) {
				logger.putMessage(">>Initialisation completed successfully");
			} else {
				logger.putMessage(">>Initialisation was NOT completed successfully. Execution is terminated");
			}

			logger.putMessage(
					">>Number of States= " + TransitionSystem.getTransitionSystemInstance().getNumberOfStates());
			// logger.putMessage(">>State Transitions:");
			// logger.putMessage(TransitionSystem.getTransitionSystemInstance().getDigraph().toString());

			PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];

			String[] incidentAssetNames = am.getIncidentEntityNames();

			while (!isSetsSelected) {
				// wait user input
				Thread.sleep(100);
			}

			if (assetSetsSelected.size() > 0) {
				incrementValue = (int) Math.ceil(90.0 / assetSetsSelected.size());
			}

			// create threads that handle each sequence generated from asset
			// matching
			executor = Executors.newFixedThreadPool(threadPoolSize);

			logger.putMessage(">>Creating [" + assetSetsSelected.size() + "] threads for asset sets. [" + threadPoolSize
					+ "] thread(s) are running in parallel.");

			for (int i = 0; i < assetSetsSelected.size(); i++) {// adjust the
																// length
				incidentInstances[i] = new PotentialIncidentInstance(lst.get(assetSetsSelected.get(i)),
						incidentAssetNames, i);
				executor.submit(incidentInstances[i]);
			}

			try {
				executor.shutdown();

				// if it returns false then maximum waiting time is reached
				if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
					logger.putMessage("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " "
							+ timeUnit + "]");
				}

				mainPool.shutdown();

				// if it returns false then maximum waiting time is reached
				if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
					logger.putMessage("Time out! saving instances took more than specified maximum time ["
							+ maxWaitingTime + " " + timeUnit + "]");
				}

			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			timer.stop();

			long timePassed = timer.getEllapsedMillis();
			int hours = (int) (timePassed / 3600000) % 60;
			int mins = (int) (timePassed / 60000) % 60;
			int secs = (int) (timePassed / 1000) % 60;
			int secMils = (int) timePassed % 1000;

			logger.putMessage("////Execution finished\\\\\\\\");
			// execution time
			logger.putMessage("Execution time: " + timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:"
					+ secMils + "ms]");

			// logger.putMessage(Logger.terminatingString);
			logger.terminateLogging();

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method intialises the BRS system by executing the BRS file, then
	 * loading states as Bigraph objects
	 * 
	 * @param BRSFileName
	 *            The BRS file describing the system components and its
	 *            evolution
	 * @param outputFolder
	 *            Output folder
	 * @return
	 */
	private boolean initialiseBigraphSystem(String BRSFileName, String outputFolder) {

		// create a handler for bigrapher tool
		BigrapherHandler bigrapherHandler = new BigrapherHandler(BRSFileName, outputFolder);

		// read states from the output folder then create Bigraph signature and
		// convert states from JSON objects to Bigraph (from LibBig library)
		// objects

		SystemInstanceHandler.setExecutor(bigrapherHandler);

		return SystemInstanceHandler.analyseBRS();

	}

	private void executeStealScenario() {

		String BRS_file = "etc/steal_scenario/research_centre_system.big";
		String BRS_outputFolder = "etc/steal_scenario/research_centre_output_500";
		String systemModelFile = "etc/steal_scenario/research_centre_model.cps";
		String incidentPatternFile = "etc/steal_scenario/incidentInstance_steal.cpi";

		executeScenario(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder);
	}

	private void executeScenario1() {

		String BRS_file = "etc/scenario1/research_centre_system.big";
		String BRS_outputFolder = "etc/scenario1/research_centre_output_10000";
		String systemModelFile = "etc/scenario1/research_centre_model.cps";
		String incidentPatternFile = "etc/scenario1/interruption_incident-pattern.cpi";

		executeScenario(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder);
	}

	private void executeLeroScenario() {

		// String BRS_file = "D:/Bigrapher data/scenario2/lero_BRS.big";
		// String BRS_outputFolder = "D:/Bigrapher data/scenario2/output-10000";
		String interruptionPattern = "interruption_incident-pattern_modified.cpi";
		String dataCollectionPattern = "dataCollection_incident-pattern.cpi";
		
		String systemModelFile = "D:/Bigrapher data/scenario2/lero.cps";
		String incidentPatternFile = "D:/Bigrapher data/scenario2/"+interruptionPattern;

		executeScenario(incidentPatternFile, systemModelFile);
	}

	private void executeScenario(String incidentPatternFile, String systemModelFile) {

		// brs output folder (containing states) has the same name as the system
		// model file name
		String BRS_outputFolder = systemModelFile.substring(0, systemModelFile.lastIndexOf("."));

		// brs file has the same name as the system model file name but with
		// .big extension instead of .cps
		String BRS_file = BRS_outputFolder + ".big";
		
		executeScenario(incidentPatternFile, systemModelFile, BRS_file, BRS_outputFolder);
	}

	private void executeScenario(String incidentPatternFile, String systemModelFile, String BRS_file,
			String BRS_outputFolder) {

		// String xQueryMatcherFile = xqueryFile;

		// set the model file paths for the xquery as it is used by other
		// objects (e.g., predicate generator) to retrieve some data such as
		// controls of system assets
		XqueryExecuter.SPACE_DOC = systemModelFile;
		XqueryExecuter.INCIDENT_DOC = incidentPatternFile;

		try {

			// currently creates a folder named "log" where the states folder is
			outputFolder = BRS_outputFolder.substring(0, BRS_outputFolder.lastIndexOf("/"));

			runLogger();

			StopWatch timer = new StopWatch();

			logger.putMessage("////Executing Scenario\\\\\\\\");
			logger.putMessage("*Incident pattern file \"" + incidentPatternFile + "\"");
			logger.putMessage("*System model file \"" + systemModelFile + "\"");
			logger.putMessage("*BRS file \"" + BRS_file + "\" & states folder \"" + BRS_outputFolder + "\"");

			// start a timer to see how long it takes for the whole execution to
			// finish
			timer.start();

			//// start executing the scenario \\\\
			Mapper m = new Mapper();

			// finds components in a system representation (space.xml) that
			// match the entities identified in an incident (incident.xml)
			logger.putMessage(">>Matching incident pattern entities to system assets");

			// add the models to the ModelsHandler class (which can be used by
			// other objects like the Mapper to
			// access the models
			ModelsHandler.addIncidentModel(incidentPatternFile);
			ModelsHandler.addSystemModel(systemModelFile);

			AssetMap am = m.findMatches();
			// finding matches also can be accomplished using Xquery (but more
			// strict criteria is applied)
			// AssetMap am = m.findMatchesUsingXquery(xqueryFilePath);

			// if there are incident assets with no matches from space model
			// then exit
			if (am.hasEntitiesWithNoMatch()) {
				logger.putError(">>Some incident entities have no matches in the system assets. These are:");
				// getIncidetnAssetWithNoMatch method has some issues
				List<String> asts = am.getIncidentAssetsWithNoMatch();
				logger.putError(asts.toString());
				return; // execution stops if there are incident entities with
						// no matching
			}

			// print matched assets
			logger.putMessage(">>Incident Entities =  " + am.getIncidentEntityNames().length);
			logger.putMessage(">>Incident entities order: " + Arrays.toString(am.getIncidentEntityNames()));
			logger.putMessage(">>Entity-Asset map:");
			logger.putMessage(am.toString());
			logger.putMessage(">>Generating asset sets..");

			
			// generate sequences
			boolean isStrict = true;
			LinkedList<String[]> lst = am.generateUniqueCombinations(isStrict);

			// checks if there are sequences generated or not. if not, then
			// execution is terminated
			// this can be loosened to allow same asset to be mapped to two
			// entities
			if (lst == null || lst.isEmpty()) {
				logger.putMessage(">>No combinations found. Terminating execution");
				return;
			}

			
			logger.putMessage(">>Number of Asset Sets generated = " + lst.size() + " Sets");
			logger.putMessage("Incident entity set:" + Arrays.toString(am.getIncidentEntityNames()));
			for (int i = 0; i < lst.size(); i++) {
				logger.putMessage("-Set[" + i + "]: " + Arrays.toString(lst.get(i)));
			}
//			// boolean oldIsPrintToScreen = isPrintToScreen;
//
//			// print the sets only if there are less than 200. Else, print a 100
//			// but save the rest to a file
//			int maxNum = 200;
//
//			if (isSaveLog && lst.size() > maxNum) {
//				if (isPrintToScreen) {
//					System.out.println("*See log file (" + Logger.getInstance().getLogFolder() + "/"
//							+ Logger.getInstance().getLogFileName() + ") for All generated sets]");
//				}
//
//				int index = 0;
//
//				for (; index < 20; index++) {
//					logger.putMessage("-Set[" + index + "]: " + Arrays.toString(lst.get(index)));
//				}
//
//				logger.setPrintToScreen(false);
//				for (; index < lst.size(); index++) {
//					logger.putMessage("-Set[" + index + "]: " + Arrays.toString(lst.get(index)));
//				}
//				logger.setPrintToScreen(true);
//
//			} else {
//				for (int i = 0; i < lst.size(); i++) {
//					logger.putMessage("-Set[" + i + "]: " + Arrays.toString(lst.get(i)));
//				}
//			}
//
//			// isPrintToScreen = oldIsPrintToScreen;

			if(dummy) {
				return;
			}
			
			logger.putMessage(
					">>Initialising the Bigraphical Reactive System (Loading states & creating the state transition graph)...");

			// initialise BRS system
			boolean isInitialised = initialiseBigraphSystem(BRS_file, BRS_outputFolder);

			if (isInitialised) {
				logger.putMessage(">>Initialisation completed successfully");
			} else {
				logger.putMessage(">>Initialisation was NOT completed successfully. Execution is terminated");
			}

			logger.putMessage(
					">>Number of States= " + TransitionSystem.getTransitionSystemInstance().getNumberOfStates());
			// logger.putMessage(">>State Transitions:");
			// logger.putMessage(TransitionSystem.getTransitionSystemInstance().getDigraph().toString());

			// create threads that handle each sequence generated from asset
			// matching
			executor = Executors.newFixedThreadPool(threadPoolSize);

			PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];

			String[] incidentAssetNames = am.getIncidentEntityNames();

			logger.putMessage(
					">>Creating threads for asset sets. [" + threadPoolSize + "] thread(s) are running in parallel.");

			for (int i = 0; i < lst.size(); i++) {// adjust the length
				incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
				executor.submit(incidentInstances[i]);
			}

			// no more tasks will be added so it will execute the submitted ones
			// and then terminate
			executor.shutdown();

			// if it returns false then maximum waiting time is reached
			if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
				logger.putMessage("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " "
						+ timeUnit + "]");
			}

			mainPool.shutdown();
			// logger.putMessage(">>Instantiation is completed. Still saving
			// generated instances...");

			// if it returns false then maximum waiting time is reached
			if (!mainPool.awaitTermination(maxWaitingTime, timeUnit)) {
				logger.putMessage("Time out! saving instances took more than specified maximum time [" + maxWaitingTime
						+ " " + timeUnit + "]");
			}

			// calculate execution time
			timer.stop();
			// LocalDateTime EndingTime = LocalDateTime.now();

			// logger.putMessage("[End time: " + dtf.format(EndingTime) +"]");

			long timePassed = timer.getEllapsedMillis();

			int hours = (int) (timePassed / 3600000) % 60;
			int mins = (int) (timePassed / 60000) % 60;
			int secs = (int) (timePassed / 1000) % 60;
			int secMils = (int) timePassed % 1000;

			// execution time
			logger.putMessage("////Execution finished\\\\\\\\");
			logger.putMessage("Execution time: " + timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:"
					+ secMils + "ms]");

			// stop logging after all finished
			// logger.putMessage(Logger.terminatingString);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			logger.terminateLogging();
		}

	}

	class PotentialIncidentInstance implements Runnable {

		private String[] systemAssetNames;
		private String[] incidentEntityNames;
		private int threadID;
		private String outputFileName;

		public PotentialIncidentInstance(String[] sa, String[] ie, int id) {

			systemAssetNames = Arrays.copyOf(sa, sa.length);
			incidentEntityNames = Arrays.copyOf(ie, ie.length);
			;
			threadID = id;

			// default output
			setOutputFileName(outputFolder + "/output/" + threadID + ".json");
		}

		public PotentialIncidentInstance(String[] sa, String[] ia, int id, String outputFileName) {

			this(sa, ia, id);
			setOutputFileName(outputFileName);
		}

		@Override
		public void run() {

			GraphPathsAnalyser pathsAnalyser = null;

			StopWatch timer = new StopWatch();

			timer.start();

			// msgQ = Logger.getInstance().getMsgQ();

			try {

				// this object allows the conversion of incident activities
				// conditions into bigraphs
				// which later can be matched against states of the system (also
				// presented in Bigraph)

				PredicateGenerator predicateGenerator = new PredicateGenerator(systemAssetNames, incidentEntityNames);
				PredicateHandler predicateHandler = predicateGenerator.generatePredicates();

				// this object identifies states and state transitions that
				// satisfy the conditions of activities
				// state transitions are updated in the predicates, which can be
				// accessed through predicateHandler
				BigraphAnalyser analyser = new BigraphAnalyser(predicateHandler, threadID);
				analyser.setNumberofActivityParallelExecution(parallelActivities);
				analyser.setThreshold(matchingThreshold);

				logger.putMessage("Thread[" + threadID + "]>>Identifying states and their transitions...");

				// identify states that satisfy the pre-/post-conditions of each
				// activity
				analyser.analyse();

				// for GUI
				if (listener != null) {
					listener.updateProgress(incrementValue / 3);
				}

				// print all possible state transitions satisfying conditions
				/*
				 * if(!predicateHandler.areAllSatisfied()){
				 * logger.putMessage("Thread["+
				 * threadID+"]>>Activities are not satisfied:" +
				 * predicateHandler.getActivitiesNotSatisfied());
				 * logger.putMessage("Thread["+threadID+"]>>Terminating thread"
				 * ); threadWriter.close(); return; }
				 */

				// how to represent all possible paths to the given sequence of
				// assets?
				// incidentpath can be used to hold one path, but now it is
				// holding everything
				// IncidentPath inc = new IncidentPath(predicateHandler);
				// inc.generateDistinctPaths();

				// this gives details about the states and their transitions
				// that satisfy the conditions of each activity
				// it prints transitions between pre and post within one
				// activity, post of current to pre of next activity, pre of
				// current to pre of next
				// predicateHandler.printAll();

				// one way to find all possible paths between activities is to
				// find all transitions from the precondition of the initial
				// activity to the postconditions of the final activity
				logger.putMessage("Thread[" + threadID + "]>>Generating potential incident instances...");
				// LinkedList<GraphPath> paths =
				// predicateHandler.getPathsBetweenActivities(predicateHandler.getInitialActivity(),
				// predicateHandler.getFinalActivity());
				LinkedList<GraphPath> paths = predicateHandler.getPaths();

				// updated gui
				if (listener != null) {
					listener.updateProgress(incrementValue / 3);
				}

				// save and analyse generated paths there are any
				if (paths.size() > 0) {

					// create and run an instance saver to store instances to a
					// file
					InstancesSaver saver = new InstancesSaver(threadID, outputFileName, incidentEntityNames,
							systemAssetNames, paths);
					mainPool.submit(saver);

					logger.putMessage("Thread[" + threadID + "]>>Analysing [" + paths.size()
							+ "] of generated potential incident instances...");

					// create an analysis object for the identified paths
					pathsAnalyser = new GraphPathsAnalyser(paths);
					// String result = pathsAnalyser.analyse();

					// if (result != null) {
					// logger.putMessage(result);
					// }

				} else {
					logger.putMessage("Thread[" + threadID + "]>>NO potential incident instances generated");
				}

				// print(pathsAnalyser.print());
				// another way is to combine the transitions found for each
				// activity from the initial one to the final one
				// predicateHandler.printAll();

				// System.out.println("\nThread["+threadID+"]>>Summary of the
				// incident pattern activities");
				// System.out.println(predicateHandler.getSummary());

				long timePassed = timer.getEllapsedMillis();

				int hours = (int) (timePassed / 3600000) % 60;
				int mins = (int) (timePassed / 60000) % 60;
				int secs = (int) (timePassed / 1000) % 60;
				int secMils = (int) timePassed % 1000;
				String strTime = timePassed + "ms [" + hours + "h:" + mins + "m:" + secs + "s:" + secMils + "ms]";
				// execution time
				logger.putMessage("Thread[" + threadID + "]>>Execution time: " + strTime);

				logger.putMessage("Thread[" + threadID + "]>>Finished Successfully");
				logger.putMessage("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n\n");

				if (listener != null) {
					listener.updateProgress(incrementValue / 3 + incrementValue % 3);
				}

				listener.updateResult(threadID, pathsAnalyser, getOutputFileName(), strTime);
				/*
				 * inc.generateDistinctPaths(); LinkedList<GraphPath> paths =
				 * inc.getAllPaths();
				 * 
				 * for(GraphPath p : paths) {
				 * System.out.println(p.toSimpleString()); }
				 */
				// System.out.println(predic.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * public void start() { print(">>Thread [" + threadID
		 * +"] is starting...\n"); //System.out.println("system assets: " +
		 * Arrays.toString(systemAssetNames)); if (t == null) { t = new
		 * Thread(this, "" + threadID); t.start(); }
		 * 
		 * }
		 */

		public String[] getSystemAssetNames() {
			return systemAssetNames;
		}

		public void setSystemAssetNames(String[] systemAssetNames) {
			this.systemAssetNames = systemAssetNames;
		}

		public String[] getIncidentAssetNames() {
			return incidentEntityNames;
		}

		public void setIncidentAssetNames(String[] incidentAssetNames) {
			this.incidentEntityNames = incidentAssetNames;
		}

		public int getThreadID() {
			return threadID;
		}

		public void setThreadID(int threadID) {
			this.threadID = threadID;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

	}

	public boolean isSetsSelected() {
		return isSetsSelected;
	}

	public void setSetsSelected(boolean isSetsSelected) {
		this.isSetsSelected = isSetsSelected;
	}

	public LinkedList<Integer> getAssetSetsSelected() {
		return assetSetsSelected;
	}

	public void setAssetSetsSelected(LinkedList<Integer> assetSetsSelected) {
		this.assetSetsSelected = assetSetsSelected;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public int getParallelActivities() {
		return parallelActivities;
	}

	public void setParallelActivities(int parallelActivities) {
		this.parallelActivities = parallelActivities;
	}

	public int getMatchingThreshold() {
		return matchingThreshold;
	}

	public void setMatchingThreshold(int matchingThreshold) {
		this.matchingThreshold = matchingThreshold;
	}

	class InstancesSaver implements Runnable {

		private String outputFileName;
		private String[] systemAssetNames;
		private String[] incidentEntityNames;
		private LinkedList<GraphPath> paths;
		private int threadID;
		// private BlockingQueue<String> msgQ = Logger.getInstance().getMsgQ();

		public InstancesSaver(int threadID, String file, String[] entityNames, String[] astNames,
				LinkedList<GraphPath> paths) {

			this.threadID = threadID;
			outputFileName = file;
			incidentEntityNames = entityNames;
			systemAssetNames = astNames;
			this.paths = paths;
		}

		@Override
		public void run() {

			File threadFile = new File(outputFileName);
			// BufferedWriter threadWriter;
			// FileWriter fw;
			StringBuilder jsonStr = new StringBuilder();

			try {

				logger.putMessage("Thread[" + threadID + "]>>InstanceSaver>>Storing generated instances...");

				// fw = new FileWriter(threadFile.getAbsoluteFile());

				// threadWriter = new BufferedWriter(fw);

				// add the map between incident entities to system assets

				jsonStr.append("{\"map\":[");

				for (int i = 0; i < systemAssetNames.length; i++) {
					jsonStr.append("{\"incident_entity_name\":\"").append(incidentEntityNames[i]).append("\",")
							.append("\"system_asset_name\":\"").append(systemAssetNames[i]).append("\"}");

					if (i < systemAssetNames.length - 1) {
						jsonStr.append(",");
					}

				}
				jsonStr.append("],");

				int size = paths.size();

				// add instances generated. Format:
				// {instance_id:1,transitions:[{source:3,target:4,action:"enter"},...,{...}]}
				jsonStr.append("\"potential_incident_instances\":{").append("\"num\":").append(size).append(",")
						.append("\"instances\":[");

				/*
				 * for(int i=0; i<size;i++) {
				 * jsonStr.append("{\"instance_id\":").append(i).append(",")
				 * .append(paths.get(i).toJSON()) .append("}"); if(i < size-1) {
				 * jsonStr.append(","); }
				 * 
				 * }
				 */

				String result = mainPool.invoke(new GraphPathsToStringConverter(0, size, paths));

				// System.out.println("result string generated");
				logger.putMessage("Thread[" + threadID + "]>>InstanceSave>>JSON string is generated");
				jsonStr.append(result);

				// remove the last comma at the end of the string
				jsonStr.deleteCharAt(jsonStr.length() - 1);

				jsonStr.append("]}}");

				JSONObject obj = new JSONObject(jsonStr.toString());

				logger.putMessage("Thread[" + threadID + "]>>InstanceSave>>JSON Object is created");

				if (!threadFile.exists()) {
					threadFile.createNewFile();
				}

				// write paths to a file
				try (final BufferedWriter writer = Files.newBufferedWriter(threadFile.toPath())) {
					writer.write(obj.toString(4));
				}

				// threadWriter.write(obj.toString(4));
				// threadWriter.close();

				logger.putMessage("Thread[" + threadID + "]>>InstanceSave>>Instances are stored in file: "
						+ threadFile.getAbsolutePath());

				obj = null;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	class GraphPathsToStringConverter extends RecursiveTask<String> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private static final int THRESHOLD = 100;
		private LinkedList<GraphPath> paths;
		private StringBuilder result;

		public GraphPathsToStringConverter(int start, int end, LinkedList<GraphPath> paths) {
			this.paths = paths;
			this.indexStart = start;
			this.indexEnd = end;
			result = new StringBuilder();
		}

		@Override
		protected String compute() {

			if ((indexEnd - indexStart) > THRESHOLD) {

				result = ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<GraphPathsToStringConverter, StringBuilder>() {

							@Override
							public StringBuilder apply(GraphPathsToStringConverter arg0) {
								return arg0.result;
							}

						}).reduce(result, new BinaryOperator<StringBuilder>() {

							@Override
							public StringBuilder apply(StringBuilder arg0, StringBuilder arg1) {

								return arg0.append(arg1.toString());
							}
						});

				return result.toString();

			} else {
				// StringBuilder jsonStr = new StringBuilder();

				for (int i = indexStart; i < indexEnd; i++) {
					result.append("{\"instance_id\":").append(i).append(",").append(paths.get(i).toJSON()).append("}");
					result.append(",");
					/*
					 * if(i < size-1) { jsonStr.append(","); }
					 */

				}

				// result = jsonStr;
				return result.toString();
			}

		}

		private Collection<GraphPathsToStringConverter> createSubTasks() {

			List<GraphPathsToStringConverter> dividedTasks = new LinkedList<GraphPathsToStringConverter>();

			int mid = (indexStart + indexEnd) / 2;

			dividedTasks.add(new GraphPathsToStringConverter(indexStart, mid, paths));
			dividedTasks.add(new GraphPathsToStringConverter(mid, indexEnd, paths));

			return dividedTasks;
		}
	}

	public static void main(String[] args) {

		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		// ins.executeExample();

		ins.executeLeroScenario();
		// ins.executeScenario1();
		// ins.executeStealScenario();
		// ins.test1();
	}

}
