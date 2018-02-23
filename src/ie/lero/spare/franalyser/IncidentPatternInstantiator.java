package ie.lero.spare.franalyser;

import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.Digraph;

public class IncidentPatternInstantiator {

	public void execute() {
		
		
			//handles system representation: analysing system output (states, transitions) to generate bigraphs
			initialiseSystem();
			
			Mapper m = new Mapper("match_query.xq");
			//finds components in a system representation (space.xml) that match the entities identified in an incident (incident.xml)
			AssetMap am = m.findMatches(); 

			// if there are incident assets with no matches from space model then exit
			if (am.hasAssetsWithNoMatch()) {
				System.out.println("Some incident Assets have no matches in the space asset, these are:");
				String[] asts = am.getIncidentAssetsWithNoMatch();
				for (String s : asts) {
					System.out.println(s);
				}
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
			
			PotentialIncidentInstance[] incidentInstances = new PotentialIncidentInstance[lst.size()];
			
			//create threads that handle each sequence generated from asset matching
			for(int i=0; i<2;i++) {
				incidentInstances[i] = new PotentialIncidentInstance(lst.get(i), am.getIncidentAssetNames(), i);
				incidentInstances[i].start();
			}
			
	}

	// this method can be later changed to another location since pattern
	// instantiation
	// does not need to execute a bigrapher file
	public void initialiseSystem() {
		
		// set the name of the output folder
		String BRSFileName = "actors.big";
		String outputFolder = "output";
		
		// execute BRS using Bigrapher tool as a systemExecutor
		// the default output folder is in the format: [fileName]_output e.g.,
		// sb3_output
		// output folder can be set in the executeBigraph method
		//BigrapherHandler bigrapher = new BigrapherHandler();
		
		SystemInstanceHandler.setFileName(BRSFileName);
		SystemInstanceHandler.setOutputFolder(outputFolder);
		SystemInstanceHandler.loadStates();
		//boolean issuccessful = SystemInstanceHandler.analyseSystem(BRSFileName, bigrapher);
		
		// load states (includes converting them into LibBig format for
		// matching)
		/** some method needed here */
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

	public static void main(String[] args) {
		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		ins.execute();
		//ins.test();
		//SystemInstanceHandler.loadStates();
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
