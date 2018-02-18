package ie.lero.spare.franalyser;

import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.BigrapherHandler;
import ie.lero.spare.franalyser.utility.TransitionSystem;

public class IncidentPatternInstantiator {

	public void execute() {
		
		try {
			//should be done before that for all incident patterns that apply to the system
			initializeSystem();
			
			Mapper m = new Mapper("match_query.xq");
			//finds components in a system representation (space.xml) that
			//match the entities identified in an incident (incident.xml)
			AssetMap am = m.findMatches(); 
								
		/*	System.out.println("Asset map=======");
			System.out.println(am.toString());*/

			//generate all possible unique combinations of system assets
			LinkedList<String[]> lst = am.getUniqueCombinations();
		
			// if there are incident assets with no matches from space model
			// then exit
			if (am.hasAssetsWithNoMatch()) {
				System.out.println("Some incident Assets have no matches in the space asset, these are:");
				String[] asts = am.getIncidentAssetsWithNoMatch();
				for (String s : asts) {
					System.out.println(s);
				}
				return; // execution stops if there are incident entities with
						// no matching
			}

			// execute, as threads, all possible unique combinations of system
			// assets


		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	// this method can be later changed to another location since pattern
	// instantiation
	// does not need to execute a bigrapher file
	public void initializeSystem() {
		
		// set the name of the output folder
		String BRSFileName = "sb3.big";

		// execute BRS using Bigrapher tool as a systemExecutor
		// the default output folder is in the format: [fileName]_output e.g.,
		// sb3_output
		// output folder can be set in the executeBigraph method
		BigrapherHandler bigrapher = new BigrapherHandler();
		
		boolean issuccessful = SystemInstanceHandler.analyseSystem(BRSFileName, bigrapher);

		if(issuccessful) {
			System.out.println(SystemInstanceHandler.getTransitionSystem().toString());
		}
		
		// load states (includes converting them into LibBig format for
		// matching)
		/** some method needed here */
	}

	public static void main(String[] args) {
		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();

		ins.execute();
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
			// TODO Auto-generated method stub
			PredicateGenerator pred = new PredicateGenerator(systemAssetNames, incidentAssetNames);
			PredicateHandler predic = pred.generatePredicates();// convert
																// entities in
																// the
																// pre-/post-conditions
																// of an
																// activity into
																// components
																// matched from
																// the previous
																// step
			// String outputFolder =
			// BRSFileName.split("\\.")[0]+"_"+threadID+"_output";
			predic.insertPredicatesIntoBigraphFile(BRSFileName);
			predic.updateNextPreviousActivities();
			BigraphAnalyser analyser = new BigraphAnalyser(predic, BRSFileName);
			TransitionSystem.setFileName(outputFolder + "/transitions");
			analyser.setBigrapherExecutionOutputFolder(outputFolder);

			// in the execution of the bigrapher file there is NO need to create
			// the states and the transtion
			// files again..only needed is the new predicates file containing
			// the states that satisfy a predicate
			//
			analyser.analyse(false); // set to true to execute the bigrapher
										// file or use the function without
										// parameters

			IncidentPath inc = new IncidentPath(predic);
			inc.generateDistinctPaths();

			System.out.println(predic.toString());
		}

		public void start() {
			System.out.println("Starting " + threadID);
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
