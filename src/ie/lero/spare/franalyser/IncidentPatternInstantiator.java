package ie.lero.spare.franalyser;

import ie.lero.spare.franalyser.utility.TransitionSystem;

public class IncidentPatternInstantiator {
	
	public void execute() {
		try {
		Mapper m = new Mapper("match_query.xq"); 
		AssetMap am = m.findMatches(); //finds components in a system representation (space.xml) that match the entities identified in an incident
		System.out.println("Asset map=======");
		System.out.println(am.toString());
		
		//LinkedList<String[]> lst = am.getUniqueCombinations(); //generates unique combinations of system assets
		
		//if there are incident assets with no matches from space model then exit
		if(am.hasAssetsWithNoMatch()) {
			System.out.println("Some incident Assets have no matches in the space asset, these are:");
			String [] asts = am.getIncidentAssetsWithNoMatch(); 
			for(String s: asts) {
				System.out.println(s);
			}
			return; //execution stops if there are incident enitties with no matching
		}
		
		//execute, as threads, all possible unique combinations of system assets
		PotentialIncidentInstance ins = new PotentialIncidentInstance(
				am.getRandomSpaceAssetMatches(), am.getIncidentAssetNames(),1, "sb3.big");
		ins.start();
		/*PredicateGenerator pred = new PredicateGenerator(am.getRandomSpaceAssetMatches(), am.getIncidentAssetNames()); 
		PredicateHandler predic = pred.generatePredicates();//convert entities in the pre-/post-conditions of an activity into components matched from the previous step
	
		predic.insertPredicatesIntoBigraphFile("sb3.big");
		predic.updateNextPreviousActivities();	
		BigraphAnalyser analyser = new BigraphAnalyser(predic, "sb3.big");
		analyser.setBigrapherExecutionOutputFolder("sb3_"+BigraphAnalyser.getBigrapherExecutionOutputFolder());
		TransitionSystem.setFileName(BigraphAnalyser.getBigrapherExecutionOutputFolder() + "/transitions");
		analyser.analyse(false); //set to true to execute the bigrapher file or use the function without parameters
		IncidentPath inc = new IncidentPath(predic);
		inc.generateDistinctPaths();
		
		System.out.println(predic.toString());*/
		
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public static void main(String[] args) {
		IncidentPatternInstantiator ins = new IncidentPatternInstantiator();
		
		ins.execute();
	}

	class PotentialIncidentInstance implements Runnable {
		
		private String [] systemAssetNames;
		private String [] incidentAssetNames;
		private Thread t;
		private long threadID;
		private String BRSFileName;
		public PotentialIncidentInstance(String [] sa, String [] ia, long id) {
			// TODO Auto-generated constructor stub
			systemAssetNames = sa;
			incidentAssetNames = ia;
			threadID = id;
		}
		
		public PotentialIncidentInstance(String [] sa, String [] ia, long id, String fileName) {
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
			PredicateHandler predic = pred.generatePredicates();//convert entities in the pre-/post-conditions of an activity into components matched from the previous step
			String outputFolder = BRSFileName.split("\\.")[0]+"_"+threadID+"_output";
			predic.insertPredicatesIntoBigraphFile(BRSFileName);
			predic.updateNextPreviousActivities();	
			BigraphAnalyser analyser = new BigraphAnalyser(predic, BRSFileName);
			TransitionSystem.setFileName(outputFolder+"/transitions");
			analyser.setBigrapherExecutionOutputFolder(outputFolder);
			analyser.analyse(true); //set to true to execute the bigrapher file or use the function without parameters
			
			IncidentPath inc = new IncidentPath(predic);
			inc.generateDistinctPaths();
			
			System.out.println(predic.toString());
		}
		
		public void start () {
		      System.out.println("Starting " +  threadID );
		      if (t == null) {
		         t = new Thread (this, ""+threadID);
		         t.start ();
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
		
		
	}
}
