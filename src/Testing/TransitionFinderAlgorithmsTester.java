package Testing;

import java.util.LinkedList;
import java.util.List;

import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.pattern_instantiation.GraphPath;

public class TransitionFinderAlgorithmsTester {

	Digraph<Integer> transitionDigraph;
	Integer preState;
	static final int V = 4; // Number of vertices

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
		return count[u][v][k];
	}

	public static void main(String[] args) {

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

//		System.out.println(g);
		tester.transitionDigraph = g;
		g.generateNeighborNodesMap();

		tester.preState = 0;
		Integer endState = 3;

		// test BFS
		System.out.println("BFS");
//		tester.breadthFirst(endState);

		// test DFS
		LinkedList<Integer> v = new LinkedList<Integer>();
		v.add(tester.preState);
		System.out.println("\nDFS");

//		tester.depthFirst(endState, v, new LinkedList<Integer>());

		int graph[][] = new int[][] { { 0,0, 1, 1, 1 }, { 0,0, 0, 0, 1 }, { 0,0, 0, 1, 0 }, { 0,0, 0, 0, 0 }, { 0,0, 0, 1, 0 } };
		int u = 0;
		int vi = 3;
		int k = 2;
//		KPaths p = new KPaths();
		System.out.println(tester.countwalks(graph, u, vi, k));
	}

}
