package ie.lero.spare.franalyser;

import java.util.Arrays;
import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.Digraph;

public class IncidentPatternInstantiator {

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
		if(!SystemInstanceHandler.isSystemAnalysed()) {
			if(!SystemInstanceHandler.analyseSystem(BRSFileName)) {
				System.out.println("something went wrong executing the BRS system");
				return false;
			}
		}
		
		//if execution is already done then one can set the file name and the output folder instead of using the analyseSystem() method above
		SystemInstanceHandler.setFileName(BRSFileName);
		SystemInstanceHandler.setOutputFolder(outputFolder);
		
		//read states from the output folder then create Bigraph signature and convert states from JSON objects to Bigraph (from LibBig library) objects
		SystemInstanceHandler.loadStates();
	}
	
	public void test() {
		String BRSFileName = "sav/savannah-general.big";
		String outputFolder = "sav/output10000";
		
		// execute BRS using Bigrapher tool as a systemExecutor
		// the default output folder is in the format: [fileName]_output e.g.,
		// sb3_output
		// output folder can be set in the executeBigraph method
		//BigrapherHandler bigrapher = new BigrapherHandler();
		
		SystemInstanceHandler.setFileName(BRSFileName);
		SystemInstanceHandler.setOutputFolder(outputFolder);
		SystemInstanceHandler.loadStates();
	//	Mapper m = new Mapper("match_query.xq");
		//AssetMap am = m.findMatches();
		
		PotentialIncidentInstance incidentInstances = new PotentialIncidentInstance(null, null, 1);
		incidentInstances.start();
	}

	private void testNewIncident(){
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
		
		//print matched assets
		for(String n : am.getIncidentAssetNames()) {
			System.out.println(n+":"+Arrays.toString(am.getSpaceAssetMatched(n)));
		}
	
		//generate sequences
		LinkedList<String[]> lst = am.generateUniqueCombinations();
		
		//checks if there are sequences generated or not. if not, then execution is terminated
		if(lst == null) {
			System.out.println("no combinations found.... exisitng program");
			return;
		}
		
		//initialise BRS system. This includes: 
		//1- Executing the BRS file (currently done using Bigrapher tool), 
		//2- Loading states i.e. reading states from output folder, create Bigraph signature, and convert states into Bigraph objects for matching
		initialiseBigraphSystem("research_centre_system.big", "research_centre_output");
		//print sequences 
		System.out.println("Sequences ["+lst.size()+"]");
		for (String[] s : lst) {
			System.out.println(Arrays.toString(s));
		}
		
		//create threads that handle each sequence generated from asset matching
		PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
		String [] incidentAssetNames = am.getIncidentAssetNames();
		for(int i=0; i<incidentInstances.length;i++) {
			incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), incidentAssetNames, i);
			incidentInstances[i].start();
		}
	}

	public static void main(String[] args) {
		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		//ins.execute();
		//ins.test();
		//SystemInstanceHandler.loadStates();
		ins.testNewIncident();
	}

	
	class PotentialIncidentInstance implements Runnable {

		private String[] systemAssetNames;
		private String[] incidentAssetNames;
		private Thread t;
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
		
			//this object allows the conversion of incident activities conditions into bigraphs
			//which later can be matched against states of the system (also presented in Bigraph)
			PredicateGenerator predicateGenerator = new PredicateGenerator(systemAssetNames, incidentAssetNames);
			PredicateHandler predicateHandler = predicateGenerator.generatePredicates();

			//this object identifies states and state transitions that satisfy the conditions of activities
			//state transitions are updated in the predicates, which can be accessed through predicateHandler
			BigraphAnalyser analyser = new BigraphAnalyser(predicateHandler);
			analyser.analyse();
			
			 //could be done internally in the PredicateHandler class
			 Digraph<String> graph = predicateHandler.createActivitiesDigraph();
			 System.out.println(graph);			 
			 
			// hndlr.getActivitiesSequences();
			 //print all possible state transitions satisfying conditions
			/* if(!hndlr.areAllSatisfied()){
				 System.out.println("thread ["+threadID+"] activities are not satisfied:" + 
						 hndlr.getActivitiesNotSatisfied());
			 }*/
			 
			 //how to represent all possible paths to the given sequence of assets?
			 //incidentpath can be used to hold one path, but now it is holding everything
			IncidentPath inc = new IncidentPath(predicateHandler);
			inc.generateDistinctPaths();
			
			//System.out.println(predic.toString());
		}
		public void start() {
			System.out.println("Starting " + threadID);
			//System.out.println("system assets: " + Arrays.toString(systemAssetNames));
			if (t == null) {
				t = new Thread(this, "" + threadID);
				t.start();
			}
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
}
