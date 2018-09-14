package ie.lero.spare.pattern_extraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

public class PatternMappingSolver {

	// result containing integer which indicates solution id and list of
	// integers that
	// are the pattern maps
	protected Map<Integer, List<int[]>> allSolutions;

	// list that contains the patterns mapped in each solution
	// list index is a solution id and the integer array are the patterns
	protected List<int[]> patternIDs;
	List<int[]> mapIDs;

	// list that contains the sum severity for each solution
	// index is a solution id and the integer value is the severity sum
	protected List<Integer> allSolutionsSeverity;

	protected List<int[]> optimalSolution;
	protected int[] optimalSolutionPatternsID;
	protected int[] optimalSolutionMapsID;
	protected int optimalSolutionSeverity;

	// given
	protected Map<Integer, List<int[]>> patternMaps;

	static boolean MAXIMISE = false;

	public PatternMappingSolver() {
		allSolutions = new HashMap<Integer, List<int[]>>();
		patternIDs = new LinkedList<int[]>();
		allSolutionsSeverity = new LinkedList<Integer>();
		mapIDs = new LinkedList<int[]>();
	}

	public Map<Integer, List<int[]>> findSolutions(Map<Integer, List<int[]>> patternMaps) {

		int NumOfActions = findMaxNumber(patternMaps);
		return findSolutions(patternMaps, NumOfActions);
	}

	public Map<Integer, List<int[]>> findSolutions(Map<Integer, List<int[]>> patternMaps, boolean maximise) {

		int NumOfActions = findMaxNumber(patternMaps);
		return findSolutions(patternMaps, NumOfActions, maximise);
	}

	protected int findMaxNumber(Map<Integer, List<int[]>> patternMaps) {

		// finds the maximum action number specified in the given map
		int result = 0;

		for (List<int[]> list : patternMaps.values()) {
			for (int[] ary : list) {
				if (ary[ary.length - 1] > result) {
					result = ary[ary.length - 1];
				}
			}
		}

		// add one as the num found is an index
		result++;

		return result;
	}

	public Map<Integer, List<int[]>> findSolutions(Map<Integer, List<int[]>> patternMaps, int numberOfActions,
			boolean maximise) {

		PatternMappingSolver.MAXIMISE = maximise;

		return findSolutions(patternMaps, numberOfActions);
	}

	public Map<Integer, List<int[]>> findSolutions(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		// numberOfActions could be defined as the max number of the maps

		this.patternMaps = patternMaps;

		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		int numOfAllMaps = 0;

		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfPatterns = numOfAllMaps;
		Model model = null;
		List<Solution> solutions = null;
		Solver solver = null;
		IntVar severitySum = null;
		int maxSeverity = 20;
		int minSeverity = 1;
		SetVar[] possiblePatternsMaps = new SetVar[numOfAllMaps];
		SetVar[] patterns = null;
		boolean isSolutionfound = false;

		// actual severity array, assuming its embedded in the argument
		// variable
		int[] severityValuesForMaps = new int[numOfAllMaps];

		for (int i = 0; i < numOfAllMaps; i++) {
			severityValuesForMaps[i] = maxSeverity - minSeverity;
		}

		// =============look for
		// solution==========================================
		while (currentNumOfPatterns > 0) {

			model = new Model("Pattern-Map Model");

			// ============Defining Variables======================//
			patterns = new SetVar[currentNumOfPatterns];
			IntVar[] patternseverity = new IntVar[currentNumOfPatterns];

			// used to update severity values
			int[] coeffs = new int[currentNumOfPatterns];
			Arrays.fill(coeffs, 1); // coeff is 1

			// defines severity. Currently it is considered from 1 to 10
			severitySum = model.intVar("max_severity", 0, 99999);

			// each pattern has as domain values the range from {} to
			// {0,1,2,..,N-1}, where N is number of actions
			for (int i = 0; i < currentNumOfPatterns; i++) {
				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
				patternseverity[i] = model.intVar("pattern_" + i + "_severity", minSeverity, maxSeverity);
			}

			int index = 0;
			for (List<int[]> list : patternMaps.values()) {
				for (int[] ary : list) {
					possiblePatternsMaps[index] = model.setVar("map" + index, ary);
					index++;
				}
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(patterns).post();

			// essential: at least 1 map for each pattern
			for (int i = 0; i < patterns.length; i++) {
				model.member(possiblePatternsMaps, patterns[i]).post();
			}

			// create constraints over the value of severity to be one in the
			// defined pattern severity array
			for (int i = 0; i < patterns.length; i++) {
				for (int j = 0; j < numOfAllMaps; j++) {
					model.ifThen(model.allEqual(patterns[i], possiblePatternsMaps[j]),
							model.element(patternseverity[i], severityValuesForMaps, model.intVar(j)));
				}
			}

			// defines the maximum severity for a solution
			model.scalar(patternseverity, coeffs, "=", severitySum).post();

			if (PatternMappingSolver.MAXIMISE) {
				model.setObjective(Model.MAXIMIZE, severitySum);
			}

			// ============Finding solutions======================//
			solver = model.getSolver();
			SetVar uniq;
			solutions = new LinkedList<Solution>();
			List<Integer> vals = new LinkedList<Integer>();

			while (solver.solve()) {

				vals.clear();

				// get the new solution
				for (int i = 0; i < currentNumOfPatterns; i++) {
					vals.addAll(Arrays.stream(patterns[i].getValue().toArray()).boxed().collect(Collectors.toList()));
				}

				// create a setVar of the new solution
				uniq = model.setVar(vals.stream().mapToInt(i -> i).toArray());

				// add a constraint that next solution should be different from
				// this
				model.not(model.union(patterns, uniq)).post();

				// add a constraint that next solution should have equal or more
				// actions
				// could be implemented..?

				// add the current solution to the solutions list
				solutions.add(new Solution(model).record());

				isSolutionfound = true;
				// break;
			}

			if (isSolutionfound) {
				break;
			}

			currentNumOfPatterns--;
		}

		analyseSolutions(solutions, patterns, severitySum);

		return this.allSolutions;
	}

	public List<int[]> findOptimalSolution(Map<Integer, List<int[]>> patternMaps) {

		// numberOfActions could be defined as the max number of the maps

		this.patternMaps = patternMaps;
		Solution optimalSolution = null;

		int numberOfActions = findMaxNumber(patternMaps);
		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		int numOfAllMaps = 0;

		for (List<int[]> list : patternMaps.values()) {
			numOfAllMaps += list.size();
		}

		int currentNumOfPatterns = numOfAllMaps;
		Model model = null;
		List<Solution> solutions = null;
		Solver solver = null;
		IntVar severitySum = null;
		int maxSeverity = 20;
		int minSeverity = 1;
		SetVar[] possiblePatternsMaps = new SetVar[numOfAllMaps];
		SetVar[] patterns = null;
		boolean isSolutionfound = false;

		// actual severity array, assuming its embedded in the argument
		// variable
		int[] severityValuesForMaps = new int[numOfAllMaps];

		for (int i = 0; i < numOfAllMaps; i++) {
			severityValuesForMaps[i] = maxSeverity - minSeverity;
		}

		// =============look for
		// solution==========================================
		while (currentNumOfPatterns > 0) {

			model = new Model("Pattern-Map Model");

			// ============Defining Variables======================//
			patterns = new SetVar[currentNumOfPatterns];
			IntVar[] patternseverity = new IntVar[currentNumOfPatterns];

			// used to update severity values
			int[] coeffs = new int[currentNumOfPatterns];
			Arrays.fill(coeffs, 1); // coeff is 1

			// defines severity. Currently it is considered from 1 to 10
			severitySum = model.intVar("max_severity", 0, 99999);

			// each pattern has as domain values the range from {} to
			// {0,1,2,..,N-1}, where N is number of actions
			for (int i = 0; i < currentNumOfPatterns; i++) {
				patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
				patternseverity[i] = model.intVar("pattern_" + i + "_severity", minSeverity, maxSeverity);
			}

			int index = 0;
			for (List<int[]> list : patternMaps.values()) {
				for (int[] ary : list) {
					possiblePatternsMaps[index] = model.setVar("map" + index, ary);
					index++;
				}
			}

			// ============Defining Constraints======================//
			// ===1-No overlapping between maps
			// ===2-A map should be one of the defined maps by the variable
			// possiblePatternMaps
			// ===3-at least 1 map for each pattern

			// 1-no overlapping
			model.allDisjoint(patterns).post();

			// essential: at least 1 map for each pattern
			for (int i = 0; i < patterns.length; i++) {
				model.member(possiblePatternsMaps, patterns[i]).post();
			}

			// create constraints over the value of severity to be one in the
			// defined pattern severity array
			for (int i = 0; i < patterns.length; i++) {
				for (int j = 0; j < numOfAllMaps; j++) {
					model.ifThen(model.allEqual(patterns[i], possiblePatternsMaps[j]),
							model.element(patternseverity[i], severityValuesForMaps, model.intVar(j)));
				}
			}

			// defines the maximum severity for a solution
			model.scalar(patternseverity, coeffs, "=", severitySum).post();
			model.setObjective(Model.MAXIMIZE, severitySum);

			// ============Finding solutions======================//
			solver = model.getSolver();
			SetVar uniq;
			solutions = new LinkedList<Solution>();
			List<Integer> vals = new LinkedList<Integer>();

			optimalSolution = solver.findOptimalSolution(severitySum, Model.MAXIMIZE);
			if (optimalSolution != null) {

				break;
			}

			currentNumOfPatterns--;
		}

		return analyseOptimalSolution(optimalSolution, patterns, severitySum);

	}

	protected Map<Integer, List<int[]>> analyseSolutions(List<Solution> solutions, SetVar[] patterns,
			IntVar severitySum) {

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);

			List<int[]> solVals = new LinkedList<int[]>();

			for (int i = 0; i < patterns.length; i++) {
				solVals.add(sol.getSetVal(patterns[i]));
			}

			// get patterns and maps ids used in this solution
			getPatternAndMapIDs(solVals);

			// add to solutions
			this.allSolutions.put(j, solVals);

			// add severity
			allSolutionsSeverity.add(sol.getIntVal(severitySum));
		}

		return this.allSolutions;
	}

	protected List<int[]> analyseOptimalSolution(Solution optimalSolution, SetVar[] patterns, IntVar severitySum) {

		if (optimalSolution == null) {
			return null;
		}

		this.optimalSolution = new LinkedList<int[]>();
		List<Integer> tmpPatternIDs = new LinkedList<Integer>();
		List<Integer> tmpMapIDs = new LinkedList<Integer>();
		
		for (int i = 0; i < patterns.length; i++) {
			this.optimalSolution.add(optimalSolution.getSetVal(patterns[i]));
		}

		// get patterns and maps ids used in this solution
		for (int[] map : this.optimalSolution) {
			for (int i = 0; i < patternMaps.size(); i++) {
				for (int j = 0; j < patternMaps.get(i).size(); j++) {
					if (Arrays.equals(patternMaps.get(i).get(j), map)) {
						tmpPatternIDs.add(i);
						tmpMapIDs.add(j);
					}
				}
			}
		}

		//add patterns ids
		optimalSolutionPatternsID = tmpPatternIDs.stream().mapToInt(i->i).toArray();
		
		//add maps ids
		optimalSolutionMapsID = tmpMapIDs.stream().mapToInt(i->i).toArray();
		
		// add severity
		optimalSolutionSeverity = optimalSolution.getIntVal(severitySum);

		return this.optimalSolution;
	}

	protected void getPatternAndMapIDs(List<int[]> maps) {

		List<Integer> tmpPatternIDs = new LinkedList<Integer>();
		List<Integer> tmpMapIDs = new LinkedList<Integer>();

		for (int[] map : maps) {
			for (int i = 0; i < patternMaps.size(); i++) {
				for (int j = 0; j < patternMaps.get(i).size(); j++) {
					if (Arrays.equals(patternMaps.get(i).get(j), map)) {
						tmpPatternIDs.add(i);
						tmpMapIDs.add(j);
					}
				}
			}
		}

		patternIDs.add(tmpPatternIDs.stream().mapToInt(i -> i).toArray());
		mapIDs.add(tmpMapIDs.stream().mapToInt(i -> i).toArray());
	}

	public void printAllSolutions() {

		printInputPatternMap();
		System.out.println("==============Solutions Summary===================");

		System.out.println("Number of Solutions found:" + allSolutions.size());
		// print solutions
		for (int i = 0; i < allSolutions.size(); i++) {
			System.out.println("Solution ["+i+"]:");
			printSolution(allSolutions.get(i), i);
			System.out.println();
		}

		System.out.println("==================================================");
	}

	protected void printSolution(List<int[]> solution, int index) {
		
		// print maps
		System.out.print("Maps:");
		for (int j = 0; j < allSolutions.get(index).size(); j++) {
			System.out.print(Arrays.toString(allSolutions.get(index).get(j)) + ",");
		}
		System.out.println();

		// print patterns ids used
		System.out.println("Pattern IDs:" + Arrays.toString(patternIDs.get(index)));

		// print maps ids used
		System.out.println("Map IDs:" + Arrays.toString(mapIDs.get(index)));

		// print each solution severity sum
		System.out.println("severity:" + allSolutionsSeverity.get(index));
	}
	
	public void printOptimalSolution() {
		
		System.out.println("==============Optimal Solution===================");
		// print maps
		System.out.print("Maps:");
		for (int j = 0; j < optimalSolution.size(); j++) {
			System.out.print(Arrays.toString(optimalSolution.get(j)) + ",");
		}
		System.out.println();

		// print patterns ids used
		System.out.println("Pattern IDs:" + Arrays.toString(optimalSolutionPatternsID));

		// print maps ids used
		System.out.println("Map IDs:" + Arrays.toString(optimalSolutionMapsID));

		// print each solution severity sum
		System.out.println("severity:" + optimalSolutionSeverity);

		System.out.println("=================================================");
	}
	
	public void printInputPatternMap() {
		
		System.out.println("==============Input Patterns Maps===================");
		
		for(Entry<Integer, List<int[]>> entry : patternMaps.entrySet()) {
			System.out.print("Pattern["+entry.getKey()+"] = ");
			for(int [] ary : entry.getValue()) {
				System.out.print(Arrays.toString(ary)+",");
			}
			System.out.println();
		}

		System.out.println("=================================================");
	}

	public static void main(String[] args) {

		System.out.println("=======================================");
		Map<Integer, List<int[]>> maps = new HashMap<Integer, List<int[]>>();

		int[][] allPossiblePatternsMapsInt = new int[5][];

		allPossiblePatternsMapsInt[0] = new int[] { 1, 2 }; // sequence should
															// be ordered in //
															// // ascending
															// order
		allPossiblePatternsMapsInt[1] = new int[] { 3, 4 };
		allPossiblePatternsMapsInt[2] = new int[] { 6, 8, 9 };
		allPossiblePatternsMapsInt[3] = new int[] { 6, 7, 8, 9 };
		allPossiblePatternsMapsInt[4] = new int[] { 9, 10, 12, 13, 14 };

		int numOfPatterns = 2;
		LinkedList<int[]> pattern_1_maps = new LinkedList<int[]>();
		pattern_1_maps.add(allPossiblePatternsMapsInt[0]);
		pattern_1_maps.add(allPossiblePatternsMapsInt[1]);

		LinkedList<int[]> pattern_2_maps = new LinkedList<int[]>();
		pattern_2_maps.add(allPossiblePatternsMapsInt[2]);
		pattern_2_maps.add(allPossiblePatternsMapsInt[3]);
		pattern_2_maps.add(allPossiblePatternsMapsInt[4]);

		maps.put(0, pattern_1_maps);
		maps.put(1, pattern_2_maps);

		System.out.println("==Map used===");
		for (Entry<Integer, List<int[]>> entry : maps.entrySet()) {
			System.out.print("Pattern [" + entry.getKey() + "]:");
			for (int[] ary : entry.getValue()) {
				System.out.print(Arrays.toString(ary) + ",");
			}
			System.out.println();
		}
		System.out.println("=============");

		PatternMappingSolver solver = new PatternMappingSolver();

		solver.findSolutions(maps);

		solver.printAllSolutions();
		
		solver.findOptimalSolution(maps);
		solver.printOptimalSolution();
		
	}

	public Map<Integer, List<int[]>> getAllSolutions() {
		return allSolutions;
	}

	public List<int[]> getPatternIDs() {
		return patternIDs;
	}

	public List<int[]> getMapIDs() {
		return mapIDs;
	}

	public List<Integer> getAllSolutionsSeverity() {
		return allSolutionsSeverity;
	}

	public List<int[]> getOptimalSolution() {
		return optimalSolution;
	}

	public int[] getOptimalSolutionPatternsID() {
		return optimalSolutionPatternsID;
	}

	public int[] getOptimalSolutionMapsID() {
		return optimalSolutionMapsID;
	}

	public int getOptimalSolutionSeverity() {
		return optimalSolutionSeverity;
	}

	public static boolean isMAXIMISE() {
		return MAXIMISE;
	}
	
	

}
