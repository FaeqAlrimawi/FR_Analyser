package i.e.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import org.apache.batik.parser.PathArrayProducer;
import org.json.JSONObject;

import ie.lero.spare.franalyser.utility.BigrapherHandler;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.FileManipulator;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.util.StopWatch;

public class IncidentPatternInstantiator {
	
	private String xqueryFile = "match_query.xq";
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
	CountDownLatch latch;
	private File scenario1File;
	BufferedWriter bufferWriter;
	
	public void execute() {
		
			//handles system representation: analysing system output (states, transitions) to generate bigraphs
			initialiseBigraphSystem("BRS-fileName","outputFolder");
			
			Mapper m = new Mapper("match_query.xq");
			//finds components in a system representation (space.xml) that match the entities identified in an incident (incident.xml)
			AssetMap am = m.findMatches(); 

			// if there are incident assets with no matches from space model then exit
			if (am.hasAssetsWithNoMatch()) {
				System.out.println("Some incident Assets have no matches in the space asset, these are:");
				String[] asts = am.getIncidentAssetsWithNoMatch();
				System.out.println(Arrays.toString(asts));
				return; // execution stops if there are incident entities with
						// no matching
			}
 
			//generate all possible unique combinations of system assets 
			//the generation might take a while! scalability issue with this!
			//temporary storage could be used, which then can be processed
			LinkedList<String[]> lst = am.generateUniqueCombinations();
			
			if(lst != null) {
				System.out.println(lst.size());
			} else {
				System.out.println("no combinations found.... exisitng program");
				return;
			}
			
			//create threads that handle each sequence generated from asset matching
			PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
			String [] incidentAssetNames = am.getIncidentAssetNames();
			for(int i=0; i<incidentInstances.length;i++) {
				incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
				incidentInstances[i].start();
			}
			
	}

	/**
	 * This method intialises the BRS system by executing the BRS file, then loading states as Bigraph objects 
	 * @param BRSFileName The BRS file describing the system components and its evolution
	 * @param outputFolder Output folder
	 * @return
	 */
	public boolean initialiseBigraphSystem(String BRSFileName, String outputFolder) {
		
		//first execute the system. If the execution is not successful then return false
		//a pre-requiste for this is to have the BRS tool installed on the system
		/*if(!SystemInstanceHandler.isSystemAnalysed()) {
			if(!SystemInstanceHandler.analyseSystem(BRSFileName)) {
				return false;
			}
		}*/
		
		//create a handler for bigrapher tool
		BigrapherHandler bigrapherHandler = new BigrapherHandler(BRSFileName, outputFolder);
		
		//read states from the output folder then create Bigraph signature and convert states from JSON objects to Bigraph (from LibBig library) objects
		
		SystemInstanceHandler.setExecutor(bigrapherHandler);
		
		return SystemInstanceHandler.analyseSystem();

	}
	
	public void test() {
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
		
		String xQueryMatcherFile = xqueryFile;//in the xquery file the incident and system model paths should be adjusted if changed from current location
		String BRS_file = "etc/scenario1/research_centre_system.big";
		String BRS_outputFolder = "etc/scenario1/research_centre_output";
		String systemModelFile = "etc/scenario1/research_centre_model.cps";
		String incidentPatternFile = "etc/scenario1/interruption_incident-pattern.cpi";
		String outputFileName = "etc/scenario1/output1559.txt";
		
		try {
			
		//output file
		scenario1File = new File(outputFileName);
		
		if (!scenario1File.exists()) {
			scenario1File.createNewFile();
        }
		
		FileWriter fw = new FileWriter(scenario1File.getAbsoluteFile());
		bufferWriter = new BufferedWriter(fw);
		        
		XqueryExecuter.SPACE_DOC = systemModelFile;
		XqueryExecuter.INCIDENT_DOC = incidentPatternFile;
		
		StopWatch timer = new StopWatch();
		
		LocalDateTime startingTime = LocalDateTime.now();
		
		String startTime = "Start time: " + dtf.format(startingTime);
		
		print(startTime);
		
		//start a timer
		timer.start();
		
		////start executing the scenario \\\\
		Mapper m = new Mapper(xQueryMatcherFile);
		//finds components in a system representation (space.xml) that match the entities identified in an incident (incident.xml)
		print(">>Matching incident pattern entities to system assets");
		AssetMap am = m.findMatches(); 
		
		// if there are incident assets with no matches from space model then exit
		if (am.hasAssetsWithNoMatch()) {
			print(">>Some incident entities have no matches in the system assets. These are:");
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
		print(">>Entity-Asset map:");
		print(am.toString());
		
		//generate sequences
		LinkedList<String[]> lst = am.generateUniqueCombinations();
		
		//checks if there are sequences generated or not. if not, then execution is terminated
		//this can be loosened to allow same asset to be mapped to two entities
		if(lst == null || lst.isEmpty()) {
			print(">>No combinations found.... exisitng program");
			return;
		}
		
		//print sequences 
	/*	System.out.println("Sequences ["+lst.size()+"]");
		for (String[] s : lst) {
			System.out.println(Arrays.toString(s));
		}*/
		
		print(">>Initialise the System");
		//initialise BRS system 
		boolean isInitialised = initialiseBigraphSystem( BRS_file, BRS_outputFolder); 
		if (!isInitialised) {
			print(">>System could not be initialised....execution is terminated");
		}
		
		//create threads that handle each sequence generated from asset matching
		PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
		String [] incidentAssetNames = am.getIncidentAssetNames();
		
		//create a latch to let the main thread wait for the other threads to finish execute
		latch = new CountDownLatch(1);
		
		for(int i=0; i<1;i++) {//adjust the length
			incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
			print(">>Asset set["+i+"]: "+ Arrays.toString(lst.get(i)));
	
			incidentInstances[i].start();
		}
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LocalDateTime EndingTime = LocalDateTime.now();
		print("End time: " + dtf.format(EndingTime));
		
		timer.stop();
		
		long timePassed = timer.getEllapsedMillis();
		
		int hours = (int)(timePassed/3600000)%60;
		int mins = (int)(timePassed/60000)%60;
		int secs = (int)(timePassed/1000)%60;
		int secMils = (int)timePassed%1000;
		
		//if time passed is more than 1 minute
		print("time ellapsed: "+timePassed+" ms");
		print("Execution time: " +  hours+"h:"+mins+"m:"+secs+"s:"+secMils+"ms");
		
		bufferWriter.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void generateSequences(String incidentPatternFile, String systemModelFile, String BRSFile) {
		String outputFolder = BRSFile.split("\\.")[0]+"_output";
		System.out.println(outputFolder);
		
		//generateSequences(incidentPatternFile, systemModelFile, BRSFile, outputFolder);
	}
	
	public void generateSequences(String incidentPatternFile, String systemModelFile, String BRSFile, String BRSoutputFolder) {
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
	
	public static void main(String[] args) {
		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		//ins.execute();
		//ins.test();
		//SystemInstanceHandler.loadStates();
		//ins.executeExample();
		
		ins.executeScenario1();
		String BRS_file = "etc/scenario1/research_centre_system.big";
		String BRS_outputFolder = "etc/scenario1/research_centre_output"; //could be derived i.e. the name of the BRS_output
		String systemModelFile = "etc/scenario1/research_centre_model.cps";
		String incidentPatternFile = "etc/scenario1/interruption_incident-pattern.cpi";
		
		//ins.generateSequences(incidentPatternFile, systemModelFile, BRS_file);
	}

	
	class PotentialIncidentInstance extends Thread {

		private String[] systemAssetNames;
		private String[] incidentAssetNames;
		//private Thread t;
		private long threadID;
		private String BRSFileName;
		private String outputFolder;

		public PotentialIncidentInstance(String[] sa, String[] ia, long id) {
			// TODO Auto-generated constructor stub
			systemAssetNames = sa;
			incidentAssetNames = ia;
			threadID = id;
		}

		// could be removed as this class will be handling states from the
		// memory
		public PotentialIncidentInstance(String[] sa, String[] ia, long id, String fileName) {
			// TODO Auto-generated constructor stub
			systemAssetNames = sa;
			incidentAssetNames = ia;
			threadID = id;
			BRSFileName = fileName;
		}

		@Override
		public void run() {
		
			StringBuilder jsonStr = new StringBuilder();
			String fileName = "etc/scenario1/output/"+threadID+".json";
			File threadFile = new File(fileName);
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
			PredicateGenerator predicateGenerator = new PredicateGenerator(systemAssetNames, incidentAssetNames);
			PredicateHandler predicateHandler = predicateGenerator.generatePredicates();

			//this object identifies states and state transitions that satisfy the conditions of activities
		 	//state transitions are updated in the predicates, which can be accessed through predicateHandler
			BigraphAnalyser analyser = new BigraphAnalyser(predicateHandler);
			
			print("\nThread["+threadID+"]>>Identifying states and their transitions that satisfy the pattern activities...");

			//identify states and transitions that satisfy the pre-/post-conditions of each activity
			analyser.analyse();
			//System.out.println("\nThread["+threadID+"]>>Identification is completed");
			
			 //creating activities diagraph could be done internally in the PredicateHandler class
			/* Digraph<String> graph = predicateHandler.createActivitiesDigraph();
			 System.out.println("activities graph: "+graph);*/			 
			 
			 //dpredicateHandler.getActivitiesSequences();
			 
			 //print all possible state transitions satisfying conditions
				
			 if(!predicateHandler.areAllSatisfied()){
				 print("\nThread["+threadID+"]>>Activities are not satisfied:" + 
						 predicateHandler.getActivitiesNotSatisfied());
				 print("\nThread["+threadID+"]>>Terminating thread");
				 latch.countDown();
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
			LinkedList<GraphPath> paths = predicateHandler.getPathsBetweenActivities(predicateHandler.getInitialActivity(), predicateHandler.getFinalActivity());
			
			
			jsonStr.append("{\"paths\":[");
			print("\nThread["+threadID+"]>>State transitions that satisfy the incident:");
			for(int i=0; i<paths.size();i++) {
				jsonStr.append("{\"path_id\":").append(i).append(",")
				.append(paths.get(i).toJSON())
				.append("}");
				if(i < paths.size()-1) {
					jsonStr.append(",");
				}
			}
			jsonStr.append("]}");
			
			JSONObject obj = new JSONObject(jsonStr.toString());
			
			//write paths to a file
			threadWriter.write(obj.toString(4));
			threadWriter.close();
			obj = null;
			
			print(paths.size()+" Potential incident instances were generated. Please see details in:");
			print("File: "+ threadFile.getAbsolutePath());
			//create an analysis object for the identified paths
			GraphPathsAnalyser pathsAnalyser = new GraphPathsAnalyser(paths);
			pathsAnalyser.analyse();
			
			print(pathsAnalyser.print());
			//another way is to combine the transitions found for each activity from the initial one to the final one
			//predicateHandler.printAll();
			
//			System.out.println("\nThread["+threadID+"]>>Summary of the incident pattern activities");
//			System.out.println(predicateHandler.getSummary());
			
			print("Thread ["+threadID+"]>>Terminated");
			print("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n\n");
			
			latch.countDown();
			/*inc.generateDistinctPaths();
			LinkedList<GraphPath> paths = inc.getAllPaths();
			
			for(GraphPath p : paths) {
				System.out.println(p.toSimpleString());
			}*/
			//System.out.println(predic.toString());
			
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void start() {
			print(">>Thread [" + threadID +"] is starting...\n");
			//System.out.println("system assets: " + Arrays.toString(systemAssetNames));
			/*if (t == null) {
				t = new Thread(this, "" + threadID);
				t.start();
			}*/
			run();
		}

		public String[] getSystemAssetNames() {
			return systemAssetNames;
		}

		public void setSystemAssetNames(String[] systemAssetNames) {
			this.systemAssetNames = systemAssetNames;
		}

		public String[] getIncidentAssetNames() {
			return incidentAssetNames;
		}

		public void setIncidentAssetNames(String[] incidentAssetNames) {
			this.incidentAssetNames = incidentAssetNames;
		}

		public long getThreadID() {
			return threadID;
		}

		public void setThreadID(long threadID) {
			this.threadID = threadID;
		}

		public String getBRSFileName() {
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
		}

	}
	
	public void print(String msg) {
		
		System.out.println(msg);
		
		try {
			bufferWriter.write(msg);
			bufferWriter.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
