package Testing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.testng.internal.collections.Ints;

import choco.cp.solver.search.task.profile.ProbabilisticProfile;
import ie.lero.spare.franalyser.utility.BigrapherHandler;
import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.Digraph.Edge;
import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import ie.lero.spare.pattern_instantiation.GraphPath;
import ie.lero.spare.pattern_instantiation.SystemInstanceHandler;
import ie.lero.spare.pattern_instantiation.SystemsHandler;

public class TransitionFinderAlgorithmsTester {

	Digraph<Integer> transitionDigraph;
	Integer preState;
	static int V = 4; // Number of vertices

	private void depthFirst(Integer endState, LinkedList<Integer> transition, List<Integer> allVisited) {

		List<Integer> nodes = transitionDigraph.outboundNeighborsForTransitionGeneration(transition.getLast());

		// boolean isAdded = false;

		for (Integer node : nodes) {

			if (transition.contains(node)) {
				continue;
			}

			/**
			 * Check if current node has a transition to the start node
			 * 
			 */
			// if (preIntraStates != null && preIntraStates.contains(node)) {
			// continue;
			// }

			/*****************************/

			if (node.equals(endState)) {

				transition.add(node);
				print(transition);
				transition.removeLast();

				continue;
			}

			transition.addLast(node);

			// allVisited.addAll(nodes);

			depthFirst(endState, transition, allVisited);

			transition.removeLast();

			// allVisited.removeAll(nodes);

		}

	}

	private void breadthFirst(Integer endState) {

		LinkedList<List<Integer>> queue = new LinkedList<List<Integer>>();
		LinkedList<Integer> visited = new LinkedList<Integer>();

		List<Integer> transition = new LinkedList<Integer>();

		transition.add(preState);
		queue.add(transition);
		visited.add(preState);

		// add all precondition states that the preState has a transition to
		// visited.addAll(preIntraStates);

		while (!queue.isEmpty()) {

			List<Integer> trans = queue.poll();

			// System.out.println(trans);
			Integer state = trans.get(trans.size() - 1);

			// check if its the endState
			if (state.equals(endState)) {

				// add found transitions to the list
				print(trans);
				// visited.addAll(trans);

				// not interested in any neighbours if final reached
				continue;
			}

			List<Integer> states = transitionDigraph.outboundNeighborsForTransitionGeneration(state);

			if (states != null && !states.isEmpty()) {

				for (Integer neighborState : states) {

					// if the neighbour state has a transition to the pre
					// state
					// if (preIntraStates != null &&
					// preIntraStates.contains(neighborState)) {
					// continue;
					// }

					if (!visited.contains(neighborState)) {
						List<Integer> newTrans = new LinkedList<Integer>(trans);
						newTrans.add(neighborState);
						queue.add(newTrans);

						if (!neighborState.equals(endState)) {
							visited.add(neighborState);
						}

					}
				}
			}
		}

	}

	private void print(List<Integer> transition) {

		// if the transition does not contain states from the other
		// activities, then it is ignored
		// if (!analyseTransition(transition)) {
		// return;
		// }

		System.out.println(transition.toString());

		// LinkedList<Integer> newList = new LinkedList<Integer>(transition);
		// GraphPath path = new GraphPath(transitionSystem);
		//
		// // newList.addAll(transition);
		//
		// // path.setPredicateSrc(null);
		// // path.setPredicateDes(null);
		// path.setStateTransitions(newList);
		// localResult.add(path);

	}

	int countwalks(int graph[][], int u, int v, int k) {
		// Table to be filled up using DP. The value count[i][j][e]
		// will/ store count of possible walks from i to j with
		// exactly k edges
		int count[][][] = new int[V][V][k + 1];

		// Loop for number of edges from 0 to k
		for (int e = 0; e <= k; e++) {
			for (int i = 0; i < V; i++) // for source
			{
				for (int j = 0; j < V; j++) // for destination
				{
					// initialize value
					count[i][j][e] = 0;

					// from base cases
					if (e == 0 && i == j)
						count[i][j][e] = 1;
					if (e == 1 && graph[i][j] != 0)
						count[i][j][e] = 1;

					// go to adjacent only when number of edges
					// is more than 1
					if (e > 1) {
						for (int a = 0; a < V; a++) // adjacent of i
							if (graph[i][a] != 0)
								count[i][j][e] += count[a][j][e - 1];
					}
				}
			}
		}

		for (int i = 0; i < count[0][v].length; i++) {
			System.out.println(count[0][v][i]);
		}

		return count[u][v][k];
	}

	int countwalksModified(Map<Integer, List<Integer>> neighbours, int src, int des, int length) {
		// Table to be filled up using DP. The value count[i][j][e]
		// will/ store count of possible walks from i to j with
		// exactly k edges
		// int count[][][] = new int[V][V][length + 1];
		int cnt[] = new int[length + 1];

		// Loop for number of edges from 0 to k
		for (int e = 0; e <= length; e++) {
			// for (int i = 0; i < V; i++) // for source
			// {
			// for (int j = 0; j < V; j++) // for destination
			// {
			// initialize value
			cnt[e] = 0;

			// from base cases
			if (e == 0 && src == des)
				cnt[e] = 1;
			if (e == 1 && neighbours.get(src).contains(des))
				cnt[e] = 1;

			// go to adjacent only when number of edges
			// is more than 1
			if (e > 1) {
				// for (int a = 0; a < V; a++) // adjacent of i
				// if (graph[i][a] != 0)
				// count[i][j][e] += count[a][j][e - 1];
				cnt[e] *= cnt[e - 1] * neighbours.get(src).size();
			}
			// }
			// }
		}

		// for(int i = 0;i<count[0][v].length;i++) {
		// System.out.println(count[0][v][i]);
		// }

		return cnt[length];
	}

	public static void main(String[] args) {

		testDigraphWalksGeneration();
		
		TransitionFinderAlgorithmsTester tester = new TransitionFinderAlgorithmsTester();

		Digraph<Integer> g = new Digraph<Integer>();

		// add states
		int numOfStates = 10;

		g.add(0, 1, -1);
		g.add(0, 2, -1);
		g.add(1, 2, -1);
		g.add(1, 3, -1);
		g.add(2, 1, -1);
		g.add(2, 3, -1);
		g.add(2, 4, -1);
		g.add(2, 0, -1);
		g.add(2, 5, -1);
		g.add(2, 6, -1);
		g.add(4, 2, -1);
		g.add(4, 6, -1);
		g.add(6, 3, -1);
		g.add(6, 1, -1);
		g.add(3, 0, -1);
		g.add(3, 2, -1);

		// System.out.println(g);
//		tester.transitionDigraph = g;
//		g.generateNeighborNodesMap();
//
//		tester.preState = 0;
//		Integer endState = 3;
//
//		// test BFS
//		System.out.println("BFS");
//		// tester.breadthFirst(endState);
//
//		// test DFS
//		LinkedList<Integer> v = new LinkedList<Integer>();
//		v.add(tester.preState);
//		System.out.println("\nDFS");
//
//		// tester.depthFirst(endState, v, new LinkedList<Integer>());
//
//		int length = 10;
//		//testing walks
////		int walks[][][] = g.countwalks(length);
////		int walks2[][][] = g.countwalksList(length);
//		
//		int src = 0;
//		int des = 6;
//		int len = 10;
//		
//		System.out.println(walks[src][des][len]);
//		System.out.println(walks2[src][des][len]);
//		
//		int graph[][] = new int[][] { { 0, 0, 1, 1, 1 }, { 0, 0, 0, 0, 1 }, { 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0 },
//				{ 0, 0, 0, 1, 0 } };
//		Map<Integer, List<Integer>> ne = new HashMap<Integer, List<Integer>>();
//
//		ne.put(0, new LinkedList<Integer>(Ints.asList(new int[] { 2, 3, 4 })));
//		ne.put(1, new LinkedList<Integer>(Ints.asList(new int[] { 4 })));
//		ne.put(2, new LinkedList<Integer>(Ints.asList(new int[] { 3 })));
//		ne.put(3, new LinkedList<Integer>(Ints.asList(new int[] {})));
//		ne.put(4, new LinkedList<Integer>(Ints.asList(new int[] { 3 })));
//		int u = 1;
//		int vi = 3;
//		int k = 2;
//		V = 5;

		// KPaths p = new KPaths();
//		System.out.println("tester: " + tester.countwalks(graph, u, vi, k));
//		System.out.println("tester-modeified: " + tester.countwalksModified(ne, u, vi, k));
		
//		testJGraphT();
	}

	 static void testJGraphT() {

		 TransitionFinderAlgorithmsTester tester = new TransitionFinderAlgorithmsTester();
		 DefaultDirectedGraph<Integer, DefaultEdge> newGraph = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
		// newGraph.add

		DefaultEdge edge1 = new DefaultEdge();//tester.new MyEdge(-1, "enter");
		DefaultEdge edge2 = new DefaultEdge();//tester.new MyEdge(-1, "exit");
		newGraph.addVertex(0);
		newGraph.addVertex(1);
		newGraph.addVertex(2);
		newGraph.addEdge(0, 1, edge1);
//		newGraph.setEdgeWeight(edge1, 0.5);
		newGraph.addEdge(1, 2, edge2);
//		newGraph.setEdgeWeight(edge2, 0.4);
		
		//some issue in getting type???
		newGraph.getType();
		
		AllDirectedPaths<Integer, DefaultEdge> paths = new AllDirectedPaths<>(newGraph);
		
		List<org.jgrapht.GraphPath<Integer, DefaultEdge>> result = paths.getAllPaths(0,2, true, null);
		
		for(org.jgrapht.GraphPath<Integer, DefaultEdge> res : result) {
			System.out.println(res.toString());
		}
		
		System.out.println(newGraph.toString());
//		System.out.println(paths.getAllPaths(0, 2, true, null));
		
	}

	class MyEdge extends DefaultWeightedEdge{

		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double probability;
		String name;

		public MyEdge(double prob, String name) {
		
			probability = prob;
			this.name = name;
		}
		
		public String toString() {
			
			return "Edge["+probability+","+name+"]";
		}
	}
	
	static void testDigraphWalksGeneration() {
		
//		String interruptionPatternWin = "D:/Bigrapher data/incident patterns/collectData-pattern.cpi";

//		String leroSystemModelWin = "D:/Bigrapher data/lero/lero.cps";

		String BRS_fileWin = "D:/Bigrapher data/lero/lero.big";

		String[] states = new String[10];

		for (int i = 0; i < states.length; i++) {

			states[i] = "/D:/Bigrapher data/lero/lero" + (i + 1);
		}

		String folder = states[4];
		
		Logger logger =  runLogger(true, false, folder);
		
		SystemInstanceHandler sysHandler = new SystemInstanceHandler();
		
		BigrapherHandler brsExecutor = new BigrapherHandler(BRS_fileWin, folder);

		// read states from the output folder then create Bigraph signature and
		// convert states from JSON objects to Bigraph (from LibBig library)
		// objects
		sysHandler.setLogger(logger);
		sysHandler.setExecutor(brsExecutor);

		boolean isDone = sysHandler.analyseBRS();

		if (isDone) {
			TransitionSystem transitionSystem = sysHandler.getTransitionSystem();
			Digraph<Integer> digraph = transitionSystem.getDigraph();
			
			int numOfNodes = digraph.getNumberOfNodes();
			
			logger.putMessage("Testing walk functionality for "+ numOfNodes);
			
			Random rand = new Random();
			int len = 10;
			int src = rand.nextInt(numOfNodes);
			int des = rand.nextInt(numOfNodes);
			int length = rand.nextInt(len);
			
			//too long!
//			int walksMap[][][] = digraph.countwalksList(len);
			
			int walks[][][] = digraph.countwalks(len);
			
			logger.putMessage("Matrix: walks from ("+src+") to (" + des + ") of length ["+ length +"] = "+ walks[src][des][length]);
			
			
//			logger.putMessage("Map: walks from ("+src+") to (" + des + ") of length ["+ length +"] = "+ walksMap[src][des][length]);
			// add to the list of system handlers for other objects to access
//			SystemsHandler.addSystemHandler(systemHandler);
		}
		
		logger.putMessage("Done!");
		logger.terminateLogging();
	}

	static Logger runLogger(boolean isPrintToScreen, boolean isSaveLog, String outputFolder) {

		Logger logger = new Logger();

		logger.setListener(null);
		logger.setPrintToScreen(isPrintToScreen);
		logger.setSaveLog(isSaveLog);
		logger.setLogFolder(outputFolder + "/log");

		logger.createLogFile();

		logger.start();

		return logger;
	}

}
