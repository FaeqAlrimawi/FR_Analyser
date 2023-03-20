package ie.lero.spare.pattern_instantiation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import cyberPhysical_Incident.Connection;
import cyberPhysical_Incident.ConnectionState;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.IncidentEntity;
import cyberPhysical_Incident.Mobility;
import environment.Asset;

public class IncidentEntitytoAssetSetSolver {

	public IncidentEntitytoAssetSetSolver() {
		convertedMap = new HashMap<Integer, List<Integer>>();
		assetToIDMap = new HashMap<Asset, Integer>();
		assetToIDMapReverse = new HashMap<Integer, Asset>();
		allSolutions = new HashMap<Integer, List<Integer>>();
//	solutionsFound = new LinkedList<MonitorSolution>();
		entityToIDMap = new HashMap<Integer, String>();
		entitySequence = new LinkedList<String>();
//	monitorsCosts = new HashMap<Integer, Integer>();
//	allSolutionsCost = new LinkedList<Integer>();
	}

	// input
	Map<String, List<Asset>> assets;

	// convert the given map into a map that can be used by the solver
	// key is an integer indicating the position (index) referring to action
	// the value is a list containing monitor indices referring to all monitor that
	// can monitor the given action index
	Map<Integer, List<Integer>> convertedMap;

	// the first index indicates an action and the second is the monitor. The value
	// held is a monitor ID
	int[][] entityAssetMatrix;
//		int[][] actionMonitorCostMatrix;

	// key is a monitor and value is its id
	Map<Asset, Integer> assetToIDMap;
	int[] assetIDs;

	Map<Integer, Asset> assetToIDMapReverse;

	// result containing integer which indicates solution id and list of
	// integers that
	// are the pattern maps
	protected Map<Integer, List<Integer>> allSolutions;

	// list that contains the sum of cost for each solution
	// index is a solution id and the integer value is the cost sum
//		protected List<Integer> allSolutionsCost;

	// all solutions. Same as allSolutions map but converted into MonitorSolution
	// list
//		List<String> solutionsFound;

	// key is an action and the value is its id
	Map<Integer, String> entityToIDMap;

	// entity sequence that correspond to the sequence of assets in a solution
	List<String> entitySequence;

	// key is monitor id and value is cost
//		Map<Integer, Integer> monitorsCosts;

	// isOptimal
	boolean isOptimal = false;

	// if true then it minimises the cost
	boolean isMinimal = true;

	// if true then it finds different monitors for different actions
	boolean isAllDifferent = true;

	// variables used for finding a solution
	// sum of cost of a solution
//		IntVar costSum = null;

	// monitors variables
	IntVar[] assetsVars = null;
	Map<Integer, IntVar> assetParentVars = null;

	// maximum number of solutions
	int maxNumOfSolutions = 1;

	/**
	 * Finds ALL solutions for the given actions and their monitors
	 * 
	 * @param actionsMonitorsMap A map in which the key is action name and the value
	 *                           is a list of monitors that can monitor the action
	 * @param isOptimal          If true, then an optimal solution is found. If
	 *                           false then it finds all solutions
	 * @param allDifferent       if true, then a solution should contain unique
	 *                           monitors for actions. If false, then a solution can
	 *                           use a monitor for more than one action
	 * @param isMinimum          if true, then a minimum cost for a solution is
	 *                           searched. If false, then cost will be ignored
	 * @return A List of MonitorSolution objects, in which each object contains
	 *         information about the solution (e.g., id, a monitor for each action,
	 *         and cost for the solution)
	 */
	public Map<Integer, List<Integer>> solve(Map<String, List<Asset>> incidentAssetMap, boolean allDifferent,
			int maxSolutions) {

		if (incidentAssetMap == null || incidentAssetMap.isEmpty()) {
			return null;
		}

		// reset variables
		reset();

//		this.isOptimal = isOptimal;
		this.isAllDifferent = allDifferent;
//		this.isMinimal = isMinimum;

		this.assets = incidentAssetMap;

		this.maxNumOfSolutions = maxSolutions;

		// ====create ids for actions and monitors
		int entityID = 0;
		int assetID = 0;

		for (Entry<String, List<Asset>> entry : assets.entrySet()) {

			String incidentEntity = entry.getKey();
			List<Asset> entityAssets = entry.getValue();

			List<Integer> entityAssetIDs = new LinkedList<Integer>();

			entityToIDMap.put(entityID, incidentEntity);

			convertedMap.put(entityID, entityAssetIDs);

			entityID++;

			// monitor id and cost
			for (Asset ast : entityAssets) {
				// if the monitor has no id, then create one and then add the id to the list of
				// monitor ids for the current action
				if (!assetToIDMap.containsKey(ast)) {
					assetToIDMap.put(ast, assetID);
					entityAssetIDs.add(assetID);

					// used for reversed access
					assetToIDMapReverse.put(assetID, ast);

					// cost
//					monitorsCosts.put(monitorID, (int) mon.getCost());

					assetID++;
					// if the monitor id already exists, then just add it to the list
				} else {
					entityAssetIDs.add(assetToIDMap.get(ast));

					// cost
//					monitorsCosts.put(monitorID, (int) mon.getCost());
				}

			}

		}

		// create entity sequence corresponding to the sequence of assets in a solution
		for (Integer entID : convertedMap.keySet()) {
			entitySequence.add(entityToIDMap.get(entID));
		}

		// get action-monitor matrix
		generateEntityAssetMatrix();

		// find solutions
		// key is solution id, value is the id of the monitor

		printConvertedMap();

		if (isOptimal) {
			// optimal solution
//			findOptimalSolution();
		} else {
			// all possible solutions
			findSolutions();
		}

//		getFoundSolutions();

		return allSolutions;
	}

	protected Map<Integer, List<Integer>> findSolutions() {

		// ============Finding solutions======================//
		List<Solution> solutions = null;
		Solver solver = null;
//		IntVar costSum = null;
//		IntVar[] monitors = null;

		Model model = createSolverModel();

		solver = model.getSolver();
		solutions = new LinkedList<Solution>();

		int numOfSolutions = 0;
		while (solver.solve() && numOfSolutions < maxNumOfSolutions) {

			// add the current solution to the solutions list
			solutions.add(new Solution(model).record());
			numOfSolutions++;
		}

		analyseSolutions(solutions);

		return allSolutions;
	}

	protected Model createSolverModel() {

		Model model = null;
		assetsVars = null;
		assetParentVars = null;
//		costSum = null;

		int numOfEntities = entitySequence.size();

		// actual severity array, assuming its embedded in the argument
		// variable
//		int sumCost = 0;

//		for (Integer monitorCost : monitorsCosts.values()) {
//			sumCost += monitorCost;
//		}
//
//		if (sumCost == 0) {
//			sumCost = 1;
//		}

		// =============look for
		// solution==========================================
//		while (currentNumOfMonitors > 0) {

		model = new Model("Entity-Asset Model");

		// ============Defining Variables======================//
		assetsVars = new IntVar[numOfEntities];
		assetParentVars = new HashMap<Integer, IntVar>();

//		System.out.println("ARrray " + Arrays.toString(getAssetIDsAsArray()));

		int[] potentialParents = getAssetIDsAsArray();
		// create monitor variables
		for (int i = 0; i < numOfEntities; i++) {
			assetsVars[i] = model.intVar("asset-" + i, entityAssetMatrix[i]);
			
//			System.out.println("ARrray " + Arrays.toString(getAssetIDsAsArray()));
//			assetParentVars[i] = model.intVar("assetParent-"+i, potentialParents);

		}

		List<Constraint> consList = new LinkedList<Constraint>();

		for (IntVar assetVar : assetsVars) {
//			assetsVars[i] = model.intVar("asset-" + i, entityAssetMatrix[i]);
			Asset currentAsset = assetToIDMapReverse.get(assetVar.getValue());
			Asset currentAssetParent = currentAsset != null ? currentAsset.getParentAsset() : null;

			if (currentAssetParent != null) {
				for (IntVar potentialParentVar : assetsVars) {
//			System.out.println("ARrray " + Arrays.toString(getAssetIDsAsArray()));

					Asset potentialAssetParent = assetToIDMapReverse.get(potentialParentVar.getValue());

					if (potentialAssetParent == currentAssetParent
							&& currentAsset.getMobility() == environment.Mobility.FIXED) {
						int currentAssetParentID = assetToIDMap.get(potentialAssetParent);

						String id = assetVar.getName().split("-")[1];
//						System.out.println("IDDD: " + id);

						int index = Integer.parseInt(id);

//						System.out.println("astParentID: " + currentAssetParentID + " parentAssetName: " + currentAssetParent.getName() + " current Asset Name: "+currentAsset.getName());
						IntVar parentAsset = model.intVar("assetParent-" + index, currentAssetParentID);
						assetParentVars.put(index, parentAsset);
//
//						// Create parent constraint
						Constraint nxtAssetParentCons = model.allEqual(parentAsset, potentialParentVar);
						consList.add(nxtAssetParentCons);
					

						break;
					}
				}
			}

		}

		Constraint[] res = new Constraint[consList.size()];
		consList.toArray(res);
//		System.out.println("Constraints #: " + res.length);
		
//		for(int i=0;i<res.length;i++) {
//			System.out.println("con[" + i+"]: " + res[i]);
//		}
		
//		Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
		model.and(res).post();
		consList.clear();

		// ============Defining Constraints======================//
		// ===1- All different (if Alldifferent is true)
		// ===2- A monitor in position X should match to a monitor that already can
		// monitor the action in position X
		// ===3- Cost is set (if minimise is true)

		// 1- all different
		if (isAllDifferent) {
			model.allDifferent(assetsVars).post();
		}

		// 2- A monitor in position X should match to a monitor that already can monitor
		// the action in position X
//		List<Constraint> consList = new LinkedList<Constraint>();
		// essential: at least 1 map for each pattern

		// CONTinment constraint: if the next asset is the parent of the previous then
		// put this constraint
//		for (int i = 0; i < assetsVars.length - 1; i++) {
//			Asset currentAsset = assetToIDMapReverse.get(assetsVars[i].getValue());
//			Asset currentAssetParent = currentAsset != null ? currentAsset.getParentAsset() : null;
//
//			if (currentAssetParent != null) {
//				Asset nextAsset = assetToIDMapReverse.get(assetsVars[i + 1].getValue());
//
//				if (nextAsset == currentAssetParent) {
//					Constraint nxtAssetParentCons = model.allEqual(assetParentVars.get(i), assetsVars[i + 1]);
//					consList.add(nxtAssetParentCons);
////					model.and(nxtAssetParentCons).post();
//				}
//			}
//		}
//		int size = consList.size();	
//		Constraint[] res = new Constraint[consList.size()];
//		consList.toArray(res);
//		System.out.println("LEEEEEE " + res.length + " EEE " + res[0]);
////		Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
//		model.and(res).post();
//		consList.clear();

//		model.and(model.allDifferent(assetsVars)).post();
//		for (int i = 0; i < assetsVars.length; i++) {
//			for (int j = 0; j < entityAssetMatrix[i].length; j++) {
//
//				// pattern map should be a one of the found maps
//
//				Constraint correctActionMonitor = model.element(assetsVars[i], entityAssetMatrix[i],
//						model.intVar(j));
//
//				consList.add(correctActionMonitor);
//
//				// the severity of the pattern should equal to the pattern
//				// severity specified in the argument
////				if (isMinimal) {
////					model.ifThen(correctActionMonitor,
////							model.arithm(monitorCost[i], "=", monitorsCosts.get(actionMonitorMatrix[i][j])));
////				}
//			}
////
//			Constraint[] res = consList.stream().toArray(size -> new Constraint[size]);
//			model.or(res).post();
//			consList.clear();
//		}

		// 3- cost
//		if (isMinimal) {
//			model.scalar(monitorCost, coeffs, "=", costSum).post();
//			model.setObjective(Model.MINIMIZE, costSum);
//		}

		return model;
	}

	/**
	 * Generates a two-dimensional array that the first index indicates an actionID
	 * (or its position in the sequence of actions, while the second index indicates
	 * the monitor ID. The value given for a particular position is a monitor ID
	 */
	protected void generateEntityAssetMatrix() {

		int numOfActions = convertedMap.size();

		// monitors
		entityAssetMatrix = new int[numOfActions][];

		// monitors cost
//		actionMonitorCostMatrix = new int[numOfActions][];

		int indexEntity = 0;

		for (Entry<Integer, List<Integer>> entry : convertedMap.entrySet()) {

			List<Integer> list = entry.getValue();

			// monitor
			entityAssetMatrix[indexEntity] = new int[list.size()];

			// cost
//			actionMonitorCostMatrix[indexAction] = new int[list.size()];

			for (int indexAsset = 0; indexAsset < list.size(); indexAsset++) {

				int astID = list.get(indexAsset);

				entityAssetMatrix[indexEntity][indexAsset] = astID;
//				actionMonitorCostMatrix[indexAction][indexMonitor] = monitorsCosts.get(monID);
			}

			indexEntity++;
		}

	}

	private int[] getAssetIDsAsArray() {

		if (assetIDs != null && assetIDs.length > 0) {
			return assetIDs;
		}

		if (assetToIDMap == null || assetToIDMap.isEmpty()) {
			return null;
		}

		assetIDs = new int[assetToIDMap.size()];

		int index = 0;
		for (Integer value : assetToIDMap.values()) {
			assetIDs[index] = value;
			index++;
		}

		return assetIDs;

	}
//	private void createIncidentEntitiesRules(IncidentDiagram incidentModel) {
//
////		IncidentDiagram incidentModel = ModelsHandler.getCurrentIncidentModel();
//
//		List<IncidentEntity> generalEntities = new LinkedList<IncidentEntity>();
//
//		for (String entityName : incidentEntityNames) {
//			IncidentEntity ent = incidentModel.getEntity(entityName);
//			generalEntities.add(ent);
//		}
//
//		// rules for each asset are 4 currently: isSame? 0 or 1,
//		// hasConnectivity? 0 or 1, isParent? 0 or 1, isChild? 0 or 1
//		// the size of the rules array = rulesNum*neighbourhood*size of
//		// generalAsset array (i.e. number of general assets)
//		int numOfEntities = generalEntities.size();
//
//		IncidentEntity src, des;
//
//		rulesList = new LinkedList<int[]>();
//
//		for (int i = 0; i < numOfEntities - 1; i++) {
//			int size = (numOfEntities - 1 - i) * rulesNum;
//			rulesList.add(new int[size]);
//		}
//
//		int index = 0;
//
//		// determine properties
//		for (int i = 0; i < rulesList.size(); i++) {
//			src = generalEntities.get(i);
//
//			index = i + 1;
//			int[] tmpAry = rulesList.get(i);
//			for (int j = 0; j < tmpAry.length;) {
//				des = generalEntities.get(index);
//
////				System.out.println("check..." + src.getName() + " & " + des.getName());
//
//				// [1] isConnected
//				for (Connection con : src.getConnections()) {
//
//					if(con.getState() == ConnectionState.TEMPORARY
//							|| con.getState() == ConnectionState.UNKNOWN) {
//						continue;
//					}
//					
//					IncidentEntity ent1 = (cyberPhysical_Incident.IncidentEntity) con.getEntity1();
//					IncidentEntity ent2 = (cyberPhysical_Incident.IncidentEntity) con.getEntity2();
//
//					if ((ent1 != null && ent1.getName().equals(des.getName()))
//							|| (ent2 != null && ent2.getName().equals(des.getName()))) {
//						tmpAry[j] = 1;// con.getType().ordinal();
//						break;
//					}
//				}
//
//				if (tmpAry[j] != 1) {
//					tmpAry[j] = 0;
//				}
//
//				//next property to check
//				j++;
//				
//				// [2] is the destination parent of source
//				IncidentEntity srcParent = (IncidentEntity) src.getParentEntity();
//				if (src.getMobility() == Mobility.FIXED && 
//						srcParent != null && srcParent.getName().equals(des.getName())) {
//					tmpAry[j] = 1;
//				} else {
//					tmpAry[j] = 0;
//				}
//
//				//next property to check
//				j++;
//				
//				// [3] is destination a child in source
//				if (des.getMobility() == Mobility.FIXED &&
//						src.getContainedEntities().contains(des)) {
//					tmpAry[j] = 1;
//				} else {
//					tmpAry[j] = 0;
//				}
//
//				//next property to check
//				j++;
//				
//				index++;
//				
//				
//			}
//		}
//	}

	protected Map<Integer, List<Integer>> analyseSolutions(List<Solution> solutions) {

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);

			if (sol == null) {
				continue;
			}

			List<Integer> solVals = new LinkedList<Integer>();

			for (int i = 0; i < assetsVars.length; i++) {
				solVals.add(sol.getIntVal(assetsVars[i]));
			}

			// add to solutions
			this.allSolutions.put(j, solVals);

			// add severity
//			if (costSum != null) {
//				allSolutionsCost.add(sol.getIntVal(costSum));
//			}

		}

		return allSolutions;
	}

	protected void reset() {
		allSolutions.clear();
//		allSolutionsCost.clear();
//		solutionsFound.clear();
		convertedMap.clear();
		assetToIDMap.clear();
		entityToIDMap.clear();
		entitySequence.clear();
//		monitorsCosts.clear();
		entityAssetMatrix = null;

	}

	public void printConvertedMap() {

		for (Entry<Integer, List<Integer>> entry : convertedMap.entrySet()) {
			System.out.println(entry.getKey() + " >> " + Arrays.toString(entry.getValue().toArray()));
		}
	}

	public LinkedList<String[]> convertSolutionsToList() {

		if (allSolutions == null) {
			return null;
		}

		LinkedList<String[]> convertedSolutions = new LinkedList<String[]>();

		for (Entry<Integer, List<Integer>> entry : allSolutions.entrySet()) {
			int arySize = entry.getValue().size();
			String[] assetAry = new String[arySize];
			int index = 0;
			for (Integer astId : entry.getValue()) {
				Asset astObj = assetToIDMapReverse.get(astId);
				String astName = astObj != null ? astObj.getName() : null;
				assetAry[index] = astName;
				index++;
			}

			convertedSolutions.add(assetAry);
		}

		return convertedSolutions;

	}
}
