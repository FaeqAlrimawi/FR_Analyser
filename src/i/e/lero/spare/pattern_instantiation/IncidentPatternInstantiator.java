package i.e.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import ie.lero.spare.franalyser.GUI.IncidentPatternInstantiationListener;
import ie.lero.spare.franalyser.utility.BigrapherHandler;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.util.StopWatch;

public class IncidentPatternInstantiator {
	
	private String xqueryFile = "etc/match_query.xq";
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
	BufferedWriter bufferWriter;
	int threadPoolSize = 1;
	int maxWaitingTime = 48;
	TimeUnit timeUnit = TimeUnit.HOURS;
	IncidentPatternInstantiationListener listener;
	int incrementValue = 10;
	boolean isSetsSelected = false;
	LinkedList<Integer> assetSetsSelected;
	private String logFileName;
	private String logFolder;	
	private boolean isSaveLog = false;
	private boolean isPrintToScreen = true;
	
	public BufferedWriter createLogFile(String logFileName) {
		
		this.logFileName = logFileName;
		return createLogFile();
	}
	
	public BufferedWriter createLogFile() {
		
		BufferedWriter bufferWriter = null;
		
		logFolder = "etc/scenario1/log";
		
		boolean isFolderCreated = true;
		File file = null;
		
		try {
		File folder = new File(logFolder);
		
		
		
		if(!folder.exists()) {
			isFolderCreated = folder.mkdir();
		}
		
		if(isFolderCreated) {
			if(!logFileName.endsWith(".txt")) {
				logFileName = logFileName+".txt";
			}
			
			file = new File(logFolder+"/"+logFileName);
			if (!file.exists()) {
				file.createNewFile();	
	        }	
		}
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		bufferWriter = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bufferWriter;
	}
	
	public void execute(String incidentPatternFile, String systemModelFile, int threadPoolSiz, IncidentPatternInstantiationListener listen) {
		this.threadPoolSize = threadPoolSiz;
		//this.logger = logger;
		listener = listen;
		
		String xQueryMatcherFile = xqueryFile;
		String BRS_file = "etc/scenario1/research_centre_system.big";
		String BRS_outputFolder = "etc/scenario1/research_centre_output_100";
		//String systemModelFile = "etc/scenario1/research_centre_model.cps";
		//String incidentPatternFile = "etc/scenario1/interruption_incident-pattern.cpi";
		String outputFileName = "etc/scenario1/log_test1.txt";
		
		try {
		        
		XqueryExecuter.SPACE_DOC = systemModelFile;
		XqueryExecuter.INCIDENT_DOC = incidentPatternFile;
		
		StopWatch timer = new StopWatch();
		
		LocalDateTime startingTime = LocalDateTime.now();
		
		String startTime = "Start time: " + dtf.format(startingTime);
		
		//set log file name
		if(isSaveLog) {
			logFileName = "log"+startingTime.getHour()+startingTime.getMinute()+startingTime.getSecond()+"_"+startingTime.toLocalDate()+".txt";
			bufferWriter = createLogFile();	
		}
		
		print(startTime);
		
		//start a timer
		timer.start();
		
		////start executing the scenario \\\\
		Mapper m = new Mapper(xQueryMatcherFile);
		
		print(">>Matching incident pattern entities to system assets");
		
		AssetMap am = m.findMatches(); 
		
		// if there are incident assets with no matches from space model then exit
		if (am.hasAssetsWithNoMatch()) {
			print(">>Some incident entities have no matches in the system assets. These are:");
			//getIncidetnAssetWithNoMatch method has some issues
			String[] asts = am.getIncidentAssetsWithNoMatch();
				print(Arrays.toString(asts));
				listener.updateAssetMapInfo(">>Some incident entities have no matches in the system assets. These are:\n"+Arrays.toString(asts));
				listener.updateAssetMapInfo("Execution is terminated");
				print("Execution is terminated");
			return; // execution stops if there are incident entities with
					// no matching
		}
		
		//print matched assets
		print("\n>>Number of Assets (also entities) =  "+am.getIncidentAssetNames().length);
		print("\n>>Incident entities order: " + Arrays.toString(am.getIncidentAssetNames()));
		print("\n>>Entity-Asset map:");
		print(am.toString());
		print("\n>>Generating asset sets..");
		
		listener.updateAssetMapInfo(am.toString());
		
		//generate sequences
		LinkedList<String[]> lst = am.generateUniqueCombinations();
		
		listener.updateProgress(10);
		listener.updateAssetSetInfo(lst);
		
		//checks if there are sequences generated or not. if not, then execution is terminated
		//this can be loosened to allow same asset to be mapped to two entities
		if(lst == null || lst.isEmpty()) {
			print(">>No combinations found. Execution is terminated");
			return;
		}
		
		print("\n>>Asset sets ("+lst.size()+"):");
		
		//print the sets only if there are less than 200. Else, print a 100 but save the rest to a file
		for (int i = 0; i < lst.size(); i++) {// adjust the length
			if (isPrintToScreen && i >= 100) {
				isPrintToScreen = false;
				System.out.println("-... [See log file (" + logFolder + "/" + logFileName + ") for the rest]");
			}
			print("-Set[" + i + "]: " + Arrays.toString(lst.get(i)));
		}
		isPrintToScreen = true;
		
		print("\n>>Initialising the Bigraphical Reactive System (Loading states & creating the state transition graph)...");
		
		//initialise the system (load states and transition system)
		boolean isInitialised = initialiseBigraphSystem( BRS_file, BRS_outputFolder); 
		
		if (isInitialised) {
			print(">>Initialisation completed successfully");
		} else {
			print(">>Initialisation was NOT completed successfully. Execution is terminated");
		}
		
		print("\n>>Number of States= "+ TransitionSystem.getTransitionSystemInstance().getNumberOfStates());
		print(">>State Transitions:");
		print(TransitionSystem.getTransitionSystemInstance().getDigraph().toString());
		
		//create threads that handle each sequence generated from asset matching
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		
		PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
		
		String [] incidentAssetNames = am.getIncidentAssetNames();
		
		
		while(!isSetsSelected) {
			//wait user input
			Thread.sleep(500);
		}
		
		if(assetSetsSelected.size()>0) {
			incrementValue = (int)Math.ceil(90.0/assetSetsSelected.size());	
		}
		
		for(int i=0; i<assetSetsSelected.size();i++) {//adjust the length
			incidentInstances[i] = new PotentialIncidentInstance(lst.get(assetSetsSelected.get(i)), incidentAssetNames, i);
			print("\n>>Thread ["+i+"] is submitted for executing asset set ["+assetSetsSelected.get(i)+"]");
			executor.submit(incidentInstances[i]);
		}
		
		try {
			executor.shutdown();
			
			//if it returns false then maximum waiting time is reached
			if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
				print("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " " + timeUnit + "]");
			}
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		timer.stop();
		LocalDateTime EndingTime = LocalDateTime.now();
		
		print("\n[End time: " + dtf.format(EndingTime) +"]");
		
		long timePassed = timer.getEllapsedMillis();
		
		int hours = (int)(timePassed/3600000)%60;
		int mins = (int)(timePassed/60000)%60;
		int secs = (int)(timePassed/1000)%60;
		int secMils = (int)timePassed%1000;
		
		//execution time
		print("Execution time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
			
		if(isSaveLog) {
			bufferWriter.close();	
		}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void execute(int threadPoolSiz, IncidentPatternInstantiationListener listen) {
		
		String systemModelFile = "etc/scenario1/research_centre_model.cps";
		String incidentPatternFile = "etc/scenario1/interruption_incident-pattern.cpi";
		
		execute(incidentPatternFile,  systemModelFile, threadPoolSiz, listen);
			
	}

	public void execute(String incidentPatternFile, String systemModelFile, IncidentPatternInstantiationListener listen) {
		
		execute(incidentPatternFile,  systemModelFile, 4, listen);
			
	}
	
	
	/**
	 * This method intialises the BRS system by executing the BRS file, then loading states as Bigraph objects 
	 * @param BRSFileName The BRS file describing the system components and its evolution
	 * @param outputFolder Output folder
	 * @return
	 */
	private boolean initialiseBigraphSystem(String BRSFileName, String outputFolder) {
		
		//create a handler for bigrapher tool
		BigrapherHandler bigrapherHandler = new BigrapherHandler(BRSFileName, outputFolder);
		
		//read states from the output folder then create Bigraph signature and convert states from JSON objects to Bigraph (from LibBig library) objects
		
		SystemInstanceHandler.setExecutor(bigrapherHandler);
		
		return SystemInstanceHandler.analyseBRS(this);

	}
	
/*	public void test() {
		String BRSFileName = "savannah_BigrapherExample/savannah-general.big";
		String outputFolder = "savannah_BigrapherExample/output10000";
		
		// execute BRS using Bigrapher tool as a systemExecutor
		// the default output folder is in the format: [fileName]_output e.g.,
		// sb3_output
		// output folder can be set in the executeBigraph method
		//BigrapherHandler bigrapher = new BigrapherHandler();
		BigrapherHandler bigrapherHandler = new BigrapherHandler(BRSFileName, outputFolder);
		
		SystemInstanceHandler.setExecutor(bigrapherHandler);
		
		SystemInstanceHandler.analyseSystem();
	//	Mapper m = new Mapper("match_query.xq");
		//AssetMap am = m.findMatches();
		
		PotentialIncidentInstance incidentInstances = new PotentialIncidentInstance(null, null, 1);
		incidentInstances.start();
	}
*/
	
	/**
	 * This method details the steps for mapping an incident pattern to a system representation
	 * it requires: 1-incident pattern file (i.e. *.cpi), 2-system model (i.e. *.environment), and 3-Bigraph representation of the system (i.e. *.big)
	 */
	private void executeExample(){
		
		String xQueryMatcherFile = xqueryFile;//in the xquery file the incident and system model paths should be adjusted if changed from current location
		String BRS_file = "etc/example/research_centre_system.big";
		String BRS_outputFolder = "etc/example/research_centre_output";
		String systemModelFile = "etc/example/research_centre_model.cps";
		String incidentPatternFile = "etc/example/interruption_incident-pattern.cpi";
		
		XqueryExecuter.SPACE_DOC = systemModelFile;
		XqueryExecuter.INCIDENT_DOC = incidentPatternFile;
		
		Mapper m = new Mapper(xQueryMatcherFile);
		//finds components in a system representation (space.xml) that match the entities identified in an incident (incident.xml)
		System.out.println(">>Matching incident pattern entities to system assets");
		AssetMap am = m.findMatches(); 
		
		// if there are incident assets with no matches from space model then exit
		if (am.hasAssetsWithNoMatch()) {
			System.out.println(">>Some incident entities have no matches in the system assets. These are:");
			//getIncidetnAssetWithNoMatch method has some issues
			String[] asts = am.getIncidentAssetsWithNoMatch();
				System.out.println(Arrays.toString(asts));
			return; // execution stops if there are incident entities with
					// no matching
		}
		
		//print matched assets
		/*for(String n : am.getIncidentAssetNames()) {
			//getIncidetnAssetWithNoMatch method has some issues
			System.out.println(n+":"+Arrays.toString(am.getSpaceAssetMatched(n)));
		}*/
		
		//print matched assets
		System.out.println(">>Entity-Asset map:");
		System.out.println(am.toString());
		
		//generate sequences
		LinkedList<String[]> lst = am.generateUniqueCombinations();
		
		//checks if there are sequences generated or not. if not, then execution is terminated
		//this can be loosened to allow same asset to be mapped to two entities
		if(lst == null || lst.isEmpty()) {
			System.out.println(">>No combinations found.... exisitng program");
			return;
		}
		
		//print sequences 
	/*	System.out.println("Sequences ["+lst.size()+"]");
		for (String[] s : lst) {
			System.out.println(Arrays.toString(s));
		}*/
		
		System.out.println(">>Initialise the System");
		//initialise BRS system 
		boolean isInitialised = initialiseBigraphSystem( BRS_file, BRS_outputFolder); 
		if (!isInitialised) {
			System.out.println(">>System could not be initialised....execution is terminated");
		}
		
		//create threads that handle each sequence generated from asset matching
		PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
		String [] incidentAssetNames = am.getIncidentAssetNames();
		
		
		for(int i=0; i<lst.size();i++) {//adjust the length
			incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
			System.out.println(">>Asset set["+i+"]: "+ Arrays.toString(lst.get(i)));
			incidentInstances[i].start();
		}	
	}
	
	private void executeScenario1(){
		
		String xQueryMatcherFile = xqueryFile;
		String BRS_file = "etc/scenario1/research_centre_system.big";
		String BRS_outputFolder = "etc/scenario1/research_centre_output_100";
		String systemModelFile = "etc/scenario1/research_centre_model.cps";
		String incidentPatternFile = "etc/scenario1/interruption_incident-pattern.cpi";
		//String logFileName = "etc/scenario1/log.txt";

		XqueryExecuter.SPACE_DOC = systemModelFile;
		XqueryExecuter.INCIDENT_DOC = incidentPatternFile;
	
		try {
		
		StopWatch timer = new StopWatch();
			
		LocalDateTime startingTime = LocalDateTime.now();
			
		String startTime = "[Start time: " + dtf.format(startingTime)+"]";
		
		if(isSaveLog) {
			logFileName = "log"+startingTime.getHour()+startingTime.getMinute()+startingTime.getSecond()+"_"+startingTime.toLocalDate()+".txt";
			bufferWriter = createLogFile();
		}
		
		print(startTime);
		
		//start a timer
		timer.start();
		
		////start executing the scenario \\\\
		Mapper m = new Mapper(xQueryMatcherFile);
		//finds components in a system representation (space.xml) that match the entities identified in an incident (incident.xml)
		print("\n>>Matching incident pattern entities to system assets");
		AssetMap am = m.findMatches(); 
		
		// if there are incident assets with no matches from space model then exit
		if (am.hasAssetsWithNoMatch()) {
			print("\n>>Some incident entities have no matches in the system assets. These are:");
			//getIncidetnAssetWithNoMatch method has some issues
			String[] asts = am.getIncidentAssetsWithNoMatch();
				print(Arrays.toString(asts));
			return; // execution stops if there are incident entities with
					// no matching
		}
		
		//print matched assets
		print("\n>>Number of Assets (also entities) =  "+am.getIncidentAssetNames().length);
		print("\n>>Incident entities order: " + Arrays.toString(am.getIncidentAssetNames()));
		print("\n>>Entity-Asset map:");
		print(am.toString());
		print("\n>>Generating asset sets..");
		
		//generate sequences
		LinkedList<String[]> lst = am.generateUniqueCombinations();
		
		//checks if there are sequences generated or not. if not, then execution is terminated
		//this can be loosened to allow same asset to be mapped to two entities
		if(lst == null || lst.isEmpty()) {
			print("\n>>No combinations found. Terminating execution");
			return;
		 }
		
		print("\n>>Asset sets ("+lst.size()+"):");
		
		boolean oldIsPrintToScreen = isPrintToScreen;
		
		//print the sets only if there are less than 200. Else, print a 100 but save the rest to a file
		for(int i=0; i<lst.size();i++) {//adjust the length
			if(isPrintToScreen && i>=100) {
				isPrintToScreen = false;
				System.out.println("-... [See log file ("+logFolder+"/"+logFileName+") for the rest]");
			}
			print("-Set["+i+"]: "+ Arrays.toString(lst.get(i)));
		}
		
		isPrintToScreen = oldIsPrintToScreen;
		
		print("\n>>Initialising the Bigraphical Reactive System (Loading states & creating the state transition graph)...");
		
		//initialise BRS system 
		boolean isInitialised = initialiseBigraphSystem( BRS_file, BRS_outputFolder); 
		
		if (isInitialised) {
			print(">>Initialisation completed successfully");
		} else {
			print(">>Initialisation was NOT completed successfully. Execution is terminated");
		}
		
		print(">>Number of States= "+ TransitionSystem.getTransitionSystemInstance().getNumberOfStates());
		print(">>State Transitions:");
		print(TransitionSystem.getTransitionSystemInstance().getDigraph().toString());
		
		//create threads that handle each sequence generated from asset matching
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		
		PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
		
		String [] incidentAssetNames = am.getIncidentAssetNames();
		
		print("\n>>Creating threads for asset sets. ["+threadPoolSize+"] thread(s) are running in parallel.");
		
		for(int i=0; i<lst.size();i++) {//adjust the length
			incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
			executor.submit(incidentInstances[i]);
		}
			
		
		//no more tasks will be added so it will execute the submitted ones and then terminate
		executor.shutdown();
			
		//if it returns false then maximum waiting time is reached
		if (!executor.awaitTermination(maxWaitingTime, timeUnit)) {
			print("Time out! tasks took more than specified maximum time [" + maxWaitingTime + " " + timeUnit + "]");
		}
		
		//calculate execution time
		timer.stop();
		LocalDateTime EndingTime = LocalDateTime.now();
		
		print("\n[End time: " + dtf.format(EndingTime) +"]");
		
		long timePassed = timer.getEllapsedMillis();
		
		int hours = (int)(timePassed/3600000)%60;
		int mins = (int)(timePassed/60000)%60;
		int secs = (int)(timePassed/1000)%60;
		int secMils = (int)timePassed%1000;
		
		//execution time
		print("Execution time: " +  timePassed+"ms ["+ hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms]");
	
		if(isSaveLog) {
			bufferWriter.close();	
		}
		
		} catch (IOException e) {
			print(e.getMessage());
		}catch (InterruptedException e) {
			print(e.getMessage());
		} 
		
	}

	/*public void generateSequences(String incidentPatternFile, String systemModelFile, String BRSFile) {
		String outputFolder = BRSFile.split("\\.")[0]+"_output";
		System.out.println(outputFolder);
		
		//generateSequences(incidentPatternFile, systemModelFile, BRSFile, outputFolder);
	}*/
	
	/*public void generateSequences(String incidentPatternFile, String systemModelFile, String BRSFile, String BRSoutputFolder) {
		String xQueryMatcherFile = xqueryFile;//in the xquery file the incident and system model paths should be adjusted if changed from current location
		String BRS_file = BRSFile;
		String BRS_outputFolder = BRSoutputFolder;
		
		
		XqueryExecuter.SPACE_DOC = systemModelFile;
		XqueryExecuter.INCIDENT_DOC = incidentPatternFile;
		
		////start executing the scenario \\\\
		Mapper m = new Mapper(xQueryMatcherFile);
		//finds components in a system representation (space.xml) that match the entities identified in an incident (incident.xml)
		System.out.println(">>Matching incident pattern entities to system assets");
		AssetMap am = m.findMatches(); 
		
		// if there are incident assets with no matches from space model then exit
		if (am.hasAssetsWithNoMatch()) {
			System.out.println(">>Some incident entities have no matches in the system assets. These are:");
			//getIncidetnAssetWithNoMatch method has some issues
			String[] asts = am.getIncidentAssetsWithNoMatch();
				System.out.println(Arrays.toString(asts));
			return; // execution stops if there are incident entities with
					// no matching
		}
		
		//print matched assets
		for(String n : am.getIncidentAssetNames()) {
			//getIncidetnAssetWithNoMatch method has some issues
			System.out.println(n+":"+Arrays.toString(am.getSpaceAssetMatched(n)));
		}
		
		//print matched assets
		System.out.println(">>Entity-Asset map:");
		System.out.println(am.toString());
		
		//generate sequences
		LinkedList<String[]> lst = am.generateUniqueCombinations();
		
		//checks if there are sequences generated or not. if not, then execution is terminated
		//this can be loosened to allow same asset to be mapped to two entities
		if(lst == null || lst.isEmpty()) {
			System.out.println(">>No combinations found.... exisitng program");
			return;
		}
		
		//print sequences 
		System.out.println("Sequences ["+lst.size()+"]");
		for (String[] s : lst) {
			System.out.println(Arrays.toString(s));
		}
		
		System.out.println(">>Initialise the System");
		//initialise BRS system 
		boolean isInitialised = initialiseBigraphSystem( BRS_file, BRS_outputFolder); 
		if (!isInitialised) {
			System.out.println(">>System could not be initialised....execution is terminated");
		}
		
		//create threads that handle each sequence generated from asset matching
		PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
		String [] incidentAssetNames = am.getIncidentAssetNames();
		
		
		for(int i=0; i<lst.size();i++) {//adjust the length
			incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
			System.out.println(">>Asset set["+i+"]: "+ Arrays.toString(lst.get(i)));
			incidentInstances[i].start();
		}	
	}*/
	
	public static void main(String[] args) {
		
		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();
		
		//ins.executeExample();
		
		ins.executeScenario1();
	}

	
	class PotentialIncidentInstance implements Runnable {

		private String[] systemAssetNames;
		private String[] incidentEntityNames;
		//private Thread t;
		private long threadID;
		//private String BRSFileName;
		//private String outputFolder;
		private String outputFileName;
		
		public PotentialIncidentInstance(String[] sa, String[] ie, long id) {
			
			systemAssetNames = Arrays.copyOf(sa, sa.length);
			incidentEntityNames = Arrays.copyOf(ie, ie.length);;
			threadID = id;
			
			//default output
			setOutputFileName("etc/scenario1/output/"+threadID+".json");
		}

		
		public PotentialIncidentInstance(String[] sa, String[] ia, long id, String outputFileName) {
	
			this(sa, ia, id);
			setOutputFileName(outputFileName);
		}
		
		@Override
		public void run() {
		
			StringBuilder jsonStr = new StringBuilder();
			
			File threadFile = new File(outputFileName);
			BufferedWriter threadWriter;
			FileWriter fw;
			
			try {
				
				if (!threadFile.exists()) {
					threadFile.createNewFile();
		        }
				
			fw = new FileWriter(threadFile.getAbsoluteFile());
			
			threadWriter = new BufferedWriter(fw);
			
			//this object allows the conversion of incident activities conditions into bigraphs
			//which later can be matched against states of the system (also presented in Bigraph)
			
			PredicateGenerator predicateGenerator = new PredicateGenerator(systemAssetNames, incidentEntityNames);
			PredicateHandler predicateHandler = predicateGenerator.generatePredicates();

			//this object identifies states and state transitions that satisfy the conditions of activities
		 	//state transitions are updated in the predicates, which can be accessed through predicateHandler
			BigraphAnalyser analyser = new BigraphAnalyser(predicateHandler);
			
			print("\nThread["+threadID+"]>>Identifying states and their transitions...");

			//identify states and transitions that satisfy the pre-/post-conditions of each activity
			analyser.analyse();
			
			//for GUI
			if(listener != null) {
				listener.updateProgress(incrementValue/3);	
			}
			
			 //print all possible state transitions satisfying conditions	
			 if(!predicateHandler.areAllSatisfied()){
				 print("\nThread["+threadID+"]>>Activities are not satisfied:" + 
						 predicateHandler.getActivitiesNotSatisfied());
				 print("\nThread["+threadID+"]>>Terminating thread");
				 threadWriter.close();
				 return;
			 }
			 
			 //how to represent all possible paths to the given sequence of assets?
			 //incidentpath can be used to hold one path, but now it is holding everything
			//IncidentPath inc = new IncidentPath(predicateHandler);
			//inc.generateDistinctPaths();
			
			//this gives details about the states and their transitions that satisfy the conditions of each activity
			//it prints transitions between pre and post within one activity, post of current to pre of next activity, pre of current to pre of next 
			//predicateHandler.printAll();
			
			//one way to find all possible paths between activities is to find all transitions from the precondition of the initial activity to the postconditions of the final activity
			 print("\nThread ["+threadID+"]>>Generating potential incident instances...");
			LinkedList<GraphPath> paths = predicateHandler.getPathsBetweenActivities(predicateHandler.getInitialActivity(), predicateHandler.getFinalActivity());
			
			//store system assets and incident entities
			jsonStr.append("{\"map\":[");
			
			for(int i =0;i<systemAssetNames.length;i++) {
				jsonStr.append("{\"incident_entity_name\":\"").append(incidentEntityNames[i]).append("\",")
					.append("\"system_asset_name\":\"").append(systemAssetNames[i]).append("\"}");
				
				if(i<systemAssetNames.length-1) {
					jsonStr.append(",");
				}
				
			}
			jsonStr.append("],");
			
			int size = paths.size();
			
			jsonStr.append("\"potential_incident_instances\":{")
			.append("\"num\":").append(size).append(",")
			.append("\"instances\":[");
	
			for(int i=0; i<size;i++) {
				jsonStr.append("{\"instance_id\":").append(i).append(",")
				.append(paths.get(i).toJSON())
				.append("}");
				if(i < size-1) {
					jsonStr.append(",");
				}
			}
			jsonStr.append("]}}");
			
			
			JSONObject obj = new JSONObject(jsonStr.toString());
			
			//write paths to a file
			threadWriter.write(obj.toString(4));
			threadWriter.close();
			obj = null;
			
			print("\nThread ["+threadID+"]>>" + paths.size()+" Potential incident instances were generated. Please see details in:");
			
			print("File: "+ threadFile.getAbsolutePath());
			
			if(listener != null) {
				listener.updateProgress(incrementValue/3);	
			}
			
			print("\nThread ["+threadID+"]>>Analysing generated potential incident instances...");
			
			//create an analysis object for the identified paths
			GraphPathsAnalyser pathsAnalyser = new GraphPathsAnalyser(paths);
			print(pathsAnalyser.analyse());
			
			//print(pathsAnalyser.print());
			//another way is to combine the transitions found for each activity from the initial one to the final one
			//predicateHandler.printAll();
			
//			System.out.println("\nThread["+threadID+"]>>Summary of the incident pattern activities");
//			System.out.println(predicateHandler.getSummary());
			
			print("\nThread ["+threadID+"]>>Finished Succefully");
			print("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n\n");
			
			if(listener != null) {
				listener.updateProgress(incrementValue/3 + incrementValue%3);
			}
			
			/*inc.generateDistinctPaths();
			LinkedList<GraphPath> paths = inc.getAllPaths();
			
			for(GraphPath p : paths) {
				System.out.println(p.toSimpleString());
			}*/
			//System.out.println(predic.toString());
			
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				print(e.getMessage());
			} 
 		}
		
		public void start() {
			print(">>Thread [" + threadID +"] is starting...\n");
			//System.out.println("system assets: " + Arrays.toString(systemAssetNames));
			/*if (t == null) {
				t = new Thread(this, "" + threadID);
				t.start();
			}*/
			
		}

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

		public long getThreadID() {
			return threadID;
		}

		public void setThreadID(long threadID) {
			this.threadID = threadID;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

		/*public String getBRSFileName() {
			return BRSFileName;
		}

		public void setBRSFileName(String bRSFileName) {
			BRSFileName = bRSFileName;
		}

		public String getOutputFolder() {
			return outputFolder;
		}

		public void setOutputFolder(String outputFolder) {
			this.outputFolder = outputFolder;
		}*/

		
	}
	
	public void print(String msg) {
		
		if(isPrintToScreen) {
			System.out.println(msg);
		}
		
		if(listener != null) {
		listener.updateLogger(msg);
		}
		
		try {
			if(isSaveLog) {
				bufferWriter.write(msg);
				bufferWriter.newLine();	
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			print(e.getMessage());
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
	
	
}
