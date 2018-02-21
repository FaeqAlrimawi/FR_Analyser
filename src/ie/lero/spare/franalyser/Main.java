package ie.lero.spare.franalyser;

import java.util.Arrays;
import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.TransitionSystem;

public class Main {
	
	public static void main(String[] args) {
		try {
			
			//######### basic execution ########################################################################################
			Mapper m = new Mapper("match_query.xq"); 
			AssetMap am = m.findMatches(); //finds components in a system representation (space.xml) that match the entities identified in an incident
			System.out.println("Asset map=======");
			System.out.println(am.toString());
			
			//LinkedList<String[]> lst = am.getUniqueCombinations(); //generates unique combinations of system assets
			
			PredicateGenerator pred = new PredicateGenerator(am); 
			PredicateHandler predic = pred.generatePredicates();//convert entities in the pre-/post-conditions of an activity into components matched from the previous step
			
			//if there are incident assets with no matches from space model then exit
			if(predic == null) {
				System.out.println("Some incident Assets have no matches in the space asset, these are:");
				String [] asts = am.getIncidentAssetsWithNoMatch(); 
				for(String s: asts) {
					System.out.println(s);
				}
				return;
			}
			predic.insertPredicatesIntoBigraphFile("sb3.big");
			predic.updateNextPreviousActivities();	
			//BigraphAnalyser analyser = new BigraphAnalyser(predic, "sb3.big");
			//analyser.setBigrapherExecutionOutputFolder("sb3_"+BigraphAnalyser.getBigrapherExecutionOutputFolder());
			//TransitionSystem.setFileName(BigraphAnalyser.getBigrapherExecutionOutputFolder() + "/transitions");
			//analyser.analyse(false); //set to true to execute the bigrapher file or use the function without parameters
			IncidentPath inc = new IncidentPath(predic);
			inc.generateDistinctPaths();
			//######### basic execution ########################################################################################
			
			if(predic != null) {
				System.out.println(predic.toString());
			}
			//predic.insertPredicatesIntoBigraphFile("sb3.big");
/*			IncidentActivity act1 = predic.getIncidentActivities().get("activity1");
			
			IncidentActivity act2 = predic.getIncidentActivities().get("activity2");
			IncidentActivity act3 = predic.getIncidentActivities().get("activity3");
			*/
			//System.out.println(inc.getRandomPath());
		/*	GraphPath pr = inc.getPath(predic.getInitialActivity().
					getPathsToNextActivity(predic.getInitialActivity().getNextActivities().get(0)).get(0));
			
			System.out.println("pr: "+pr);
			System.out.println(inc.getRandomPath());*/
			/*LinkedList<GraphPath> ps = inc.getAllPaths();
			
			for(GraphPath p : ps) {
				System.out.println(p);
			}*/
		/*	IncidentActivity t = predic.getIncidentActivities().get("activity1");
			
			if(t != null) {
				for(GraphPath p : t.getPathsBetweenPredicates()) {
					System.out.println(p);
				}
			}*/
			//TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
			//predic.createActivitiesDigraph();
			//predic.printAll();
			//predic.getPathsForIncident();
			/*getCombinedPaths(act1, act2);
			getCombinedPaths(act2, act3);
			LinkedList<HashMap<String, LinkedList<GraphPath>>> paths = predic.getPathsForIncident();
			
			for(HashMap<String, LinkedList<GraphPath>> ls : paths) {
				System.out.println("\nBegin of hashmap");
				for(LinkedList<GraphPath> pa : ls.values()) {
					System.out.println("\nBegin of linkedlist");
					for(GraphPath gp : pa) {
						System.out.println(gp);
					}
					
				}
			}*/
			//predic.findAllPossiblePaths();
		//	System.out.println(paths);
		/*	HashMap<String, LinkedList<GraphPath>> pa = act1.getIntraInterPaths();
			
			for(LinkedList<GraphPath> pl : pa.values()) {
				for(GraphPath p: pl) {
					System.out.println(p);
				}
			}*/
			//getCombinedPaths(act3);
			//LinkedList<GraphPath> ps = act2.getPathsBetweenPredicates();
			//LinkedList<GraphPath> ps2 = act2.getPathsToNextActivities();
			//LinkedList<GraphPath> ps3 = act2.getPathsFromPreviousActivities();
			/*for(Predicate s : act.getPredicates()) {
				System.out.println(s.getName()+"\nstates: "+s.getBigraphStates());
				System.out.println("intra states: "+s.getStatesIntraSatisfied());
				System.out.println("inter states: "+s.getStatesInterSatisfied());
			}
			
			for(Predicate p : act.getPredicates()) {
				System.out.println(p.getPaths());
			}
			System.out.println("intra paths");
			for(GraphPath p : ps)
			System.out.println(p);
			
			System.out.println("inter paths");
			for(GraphPath p : ps2)
			System.out.println(p);*/
/*			for(Predicate s : act2.getPredicates()) {
			System.out.println(s.getName()+"\nstates: "+s.getBigraphStates());
			System.out.println("intra states: "+s.getStatesIntraSatisfied());
			System.out.println("inter states: "+s.getStatesInterSatisfied());
			}
		
			System.out.println("intra paths");
			for(GraphPath p : ps)
			System.out.println(p);
			
			System.out.println("\nfiltered intra paths");
			act2.getIntraInterPaths();
			
			System.out.println("\ninter to next activity paths");
			for(GraphPath p : ps2)
				System.out.println(p);
			*/
		/*	GraphPath p1 = ps.get(0).combine(ps2.get(1));
			System.out.println(ps.get(0));
			System.out.println(ps2.get(1));
			System.out.println(p1);*/
			/*System.out.println("\ninter to previous activity paths");
			for(GraphPath p : ps3)
				System.out.println(p);*/
			//ArrayList<Predicate> pr = act.getConditionsWithPaths(PredicateType.Precondition);
			//System.out.println(pr);
			//analyser.checkIncidentActivitiesSatisfaction();
		/*	IncidentActivity act = predic.getIncidentActivities().get("activity1");
			
			if (act != null) {
				for(IncidentActivity actNxt : act.getNextActivities()) {
					System.out.println(actNxt.getName());
					for(Predicate p : actNxt.getPredicates(PredicateType.Precondition)) {
						System.out.println(p.toPrettyString());
					}
				}
				for(Predicate p : act.getPredicates(PredicateType.Postcondition)) {
					System.out.println(p.toPrettyString());
				}
				
				System.out.println(act.hasPathsToNextActivities());
				for(GraphPath p : act.getPathsToNextActivities()) {
				System.out.println(p.toPrettyString());
			}
			}*/
			/*TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
			LinkedList<GraphPath> p = t.getPaths(predic.getPredicates().get("pre1"), predic.getPredicates().get("post3"));
			if(!p.isEmpty()) {
				for(GraphPath a :  p) {
					System.out.println(a.toString());
				}
			}
			*/
		/*	LinkedList<Integer> ls1 = new LinkedList<Integer>();
			LinkedList<Integer> ls2 = new LinkedList<Integer>();
			for(int i=2;i<4;i++) {
				ls1.add(new Integer(i));
			}
			
			for(int i=0;i<10;i++) {
				if(i==3)
				ls2.add(new Integer(i*6));
				else
					ls2.add(new Integer(i));
			}
			ls2.add(new Integer(2)); ls2.add(new Integer(3));
			GraphPath p1 = new GraphPath();
			p1.setStateTransitions(ls1);
			
			GraphPath p2 = new GraphPath();
			p2.setStateTransitions(ls2);
			
			System.out.println(ls1);
			System.out.println(ls2);
			System.out.println(p1.isSubPath(p2));*/
/*		
			HashMap<String, Predicate> preds = predic.getPredicates();
			System.out.println("pre1: "+preds.get("pre1").getPaths().get(2).toPrettyString());
			System.out.println("pre1: "+preds.get("pre3").getPaths().get(0).toPrettyString());
			System.out.println(preds.get("pre1").getPaths().get(2)
					.equals(preds.get("pre3").getPaths().get(0)));*/
			/*for (IncidentActivity act : predic.getIncidentActivities().values()) {
				System.out.println(act.getName()+" "+act.isActivitySatisfied());
			}*/
			
			/*for(Predicate p : predic.getPredicates().values()) {
				System.out.println(p.toPrettyString());
			}*/
/*			TransitionSystem t = TransitionSystem.getTransitionSystemInstance();
				LinkedList<GraphPath> pt = t.getPaths(new Integer(0), new Integer(12));
			
			for(GraphPath p : pt) {
				System.out.println(p.toPrettyString());
			}*/
			//System.out.println(XqueryExecuter.executeQueryFromFile("tst_match.xq"));
			//System.out.println(XqueryExecuter.executeQueryFromFile("match_query.xq"));
			//String [] res = am.getRandomSpaceAssetMatches();
			
//			if (res != null)
//			for(String s: res) {
//				
//				System.out.print("$$$: "+s);
//			}
				
			//System.out.println(am.toString());
			
//			String[] ar = am.getIncidentAssetsWithNoMatch();
//			
//			if(ar != null) {
//				for(String el : ar) 
//					System.out.print(el+" ");
//			}
			
			//System.out.println(am.getCombinations());
			
	
	/*		int i = 0;
			String [] res = FileManipulator.readFile("incident.xml");
			for(String s: res){
				i++;
				System.out.print("\n-------------------------------------\n"+s + "---Line number " + i);
				
				
			}*/
			
			//System.out.println(i);
			//String outputFileName = predic.insertPredicatesIntoBigraphFile("sb3.big");
	
			/*analyser.setBigrapherExecutionOutputFolder("sb3_"+analyser.getBigrapherExecutionOutputFolder());
			PredicateHandler p = analyser.analyse(false);
			
			if(p != null) {
				System.out.println("Analysis is done");
				for(Predicate pr : p.getPredicates()) {
					System.out.println(pr.toPrettyString());
				}
			} else {
				System.out.println("Analysis is not performed");
			}*/
			/*String [] t = XqueryExecuter.returnNextPreviousActivities();
			for(String r : t) {
				System.out.println(r);
			}*/
			/*predic.updateNextPreviousActivities();
			HashMap<String, IncidentActivity> tst = predic.getIncidentActivities();
			
			for(IncidentActivity act : tst.values()) {
				System.out.println("\n\n"+act.getName());
				System.out.println("next activities");
				for( IncidentActivity act2 : act.getNextActivities()){
					if(act2 != null) {
						System.out.print(act2.getName() + " ");
						if(act2.getPredicates() != null && !act2.getPredicates().isEmpty())
						System.out.print(act2.getPredicates().get(0).getName() + " ");
					}
					
				}
				System.out.println("\nprevious activities");
				for( IncidentActivity act2 : act.getPreviousActivities()){
					if(act2 != null)
					System.out.println(act2.getName() + " ");
				}
			}*/
	/*		Predicate p1 = predic.getPredicates().get("pre1");
			Predicate p2 = predic.getPredicates().get("post1");
			for(Predicate p : predic.getPredicates().values()) {
				System.out.println(p.getName()+"   ");
			}
			*/
			/*for(GraphPath p : p1.getPaths()) {
				System.out.print(p.getPredicateDes().getName()+"   ");
			}*/
		/*	LinkedList<GraphPath> p = p1.hasPathTo(p2);
			if(p != null && !p.isEmpty()) {
				for(GraphPath pa : p) {
					System.out.println("has path:"+ pa.toSimpleString());
				}
				
			} else {
				System.out.println("No paths...");
			}*/
			//System.out.println(analyser.createDefaultBigrapherExecutionCmd());
			/*analyser.identifyRelevantStates("activity1");
			for(Predicate p : analyser.getPredicateHandler().getActivityPredicates("activity1")) {
				System.out.println(p.toString()+" states="+ p.getBigraphStates().toString());
			}*/
			/*ArrayList<Predicate> preds = analyser.identifyRelevantStates("activity1");
			Predicate pre;
			Predicate post;
			if (preds.get(0).getPredicateType() == PredicateType.Precondition) {
				pre = preds.get(0);
				post = preds.get(1);
			} else {
				pre = preds.get(1);
				post = preds.get(0);
			}
			
			analyser.identifyStateTransitions(pre, post);*/
			/*analyser.identifyStateTransitions("activity1");
			ArrayList<Predicate> preds = predic.getActivityPredicates("activity1");
			if(preds != null) {
				for (Predicate p : preds) {
					System.out.println(p.toPrettyString());
				}
			}
			
			LinkedList<GraphPath> p = preds.get(0).getPaths(preds.get(2));
			System.out.println(p.size());*/
			/*SVGFileHandler h = new SVGFileHandler();
			h.updateSVGFileStates("", null);*/
			/*for(GraphPath p : analyser.getPaths()) {
				System.out.println(p.toString());
			}*/
			//analyser.identifyStateTransitions("activity1");
			//analyser.identifyStateTransitions("activity1");
			//analyser.setBigrapherFileName("sb3.big");
			/*if(analyser.validateBigraph()) {
				analyser.executeBigraph(); 
			}
			
		/*	String [] nm = {"entrance","mainLocation", "tst"};
			String [][] res = am.getSpaceAssetsMatched(nm);
			
			for(String [] s: res) {
				if (s != null)
			for(String r: s) {
				System.out.print(r+", ");
			}
			System.out.println();
			}*/
		//	System.out.println(XqueryExecuter.executeQueryFromFile("tst.xq"));
			//String [] names = {"location_SD1", "tradeSecrets", "s"};
		//System.out.println(am.getIncidentAssetInfo(names));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void getCombinedPaths(IncidentActivity activity, IncidentActivity nextActivity) {
//		LinkedList<GraphPath> ps2 = activity.findPathsToNextActivities();
//		LinkedList<GraphPath> ps3 = activity.getPathsBetweenPredicates();
//		
//		LinkedList<GraphPath> ps = activity.getIntraInterPaths(nextActivity);
//		
//		System.out.println("\n\nActivity: "+ activity.getName());
//		System.out.println("\nPredicates");
//		for(Predicate p : activity.getPredicates()) {
//			System.out.println(p.getName());
//			System.out.println("states: "+p.getBigraphStates());
//			System.out.println("intra states: " + p.getStatesIntraSatisfied());
//			System.out.println("inter states: " + p.getStatesInterSatisfied());
//		}
//		
//		System.out.println("\nintra paths");
//		for(GraphPath p : ps3) {
//			System.out.println(p);
//		}
//		
//		System.out.println("\ninter next paths");
//		for(GraphPath p : ps2) {
//			System.out.println(p);
//		}
//		
//		System.out.println("\ncombined paths");
//		for(GraphPath p : ps) {
//			System.out.println(p);
//		}
	}
	
}
