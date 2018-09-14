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
	Map<Integer, List<int[]>> solutions;

	// list that contains the patterns mapped in each solution
	// list index is a solution id and the integer array are the patterns
	List<int[]> mappedPatternsID;
	List<int[]> mapsID;

	// list that contains the sum severity for each solution
	// index is a solution id and the integer value is the severity sum
	List<Integer> solutionSeverity;

	// given
	Map<Integer, List<int[]>> patternMaps;

	public PatternMappingSolver() {
		solutions = new HashMap<Integer, List<int[]>>();
		mappedPatternsID = new LinkedList<int[]>();
		solutionSeverity = new LinkedList<Integer>();
		mapsID = new LinkedList<int[]>();
	}

	public List<Solution> findSolutions(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		// numberOfActions could be defined as the max number of the map

		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		// ============Creating model========================//

		Model model = new Model("Pattern-Map Model");

		// ============Defining Variables======================//

		SetVar[] patterns = new SetVar[patternMaps.keySet().size()];

		SetVar[][] possiblePatternsMaps = new SetVar[patterns.length][];

		// each pattern has as domain values the range from {} to
		// {0,1,2,..,N-1}, where N is number of actions
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
		}

		for (int i = 0; i < patterns.length; i++) {

			// variables which represent the sets that a generated set by a
			// pattern should belong to
			possiblePatternsMaps[i] = new SetVar[patternMaps.get(i).size()];

			for (int j = 0; j < possiblePatternsMaps[i].length; j++) {
				possiblePatternsMaps[i][j] = model.setVar("map" + i + "" + j, patternMaps.get(i).get(j));
			}
		}

		// ============Defining Constraints======================//
		// ===1-No overlapping between maps
		// ===2-A map should be one of the defined maps by the variable
		// possiblePatternMaps
		// ===3-at least 1 map for each pattern

		// 1-no overlapping
		model.allDisjoint(patterns).post();

		for (int i = 0; i < patterns.length; i++) {
			// 2 & 3- a map should belong to one of the identified maps and each
			// pattern should have
			// a map in the solution
			model.member(possiblePatternsMaps[i], patterns[i]).post();

		}

		// ============Finding solutions======================//
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();

		// optimisation

		return solutions;
	}

	public List<Solution> findSolutions(Map<Integer, List<int[]>> patternMaps) {

		// numberOfActions could be defined as the max number of the maps
		int numberOfActions = findMaxNumber(patternMaps);
		int[] actionsArray = new int[numberOfActions];

		// used as an upper bound for the set variables (i.e. patterns
		// variables)
		// 0,1,2,...N-1 where N is the number of actions
		for (int i = 0; i < actionsArray.length; i++) {
			actionsArray[i] = i;
		}

		// ============Creating model========================//

		Model model = new Model("Pattern-Map Model");

		// ============Defining Variables======================//

		SetVar[] patterns = new SetVar[patternMaps.keySet().size()];

		SetVar[][] possiblePatternsMaps = new SetVar[patterns.length][];

		// each pattern has as domain values the range from {} to
		// {0,1,2,..,N-1}, where N is number of actions
		for (int i = 0; i < patterns.length; i++) {
			patterns[i] = model.setVar("pattern-" + i, new int[] {}, actionsArray);
		}

		for (int i = 0; i < patterns.length; i++) {

			// variables which represent the sets that a generated set by a
			// pattern should belong to
			possiblePatternsMaps[i] = new SetVar[patternMaps.get(i).size()];

			for (int j = 0; j < possiblePatternsMaps[i].length; j++) {
				possiblePatternsMaps[i][j] = model.setVar("map" + i + "" + j, patternMaps.get(i).get(j));
			}
		}

		// ============Defining Constraints======================//
		// ===1-No overlapping between maps
		// ===2-A map should be one of the defined maps by the variable
		// possiblePatternMaps
		// ===3-at least 1 map for each pattern

		// 1-no overlapping
		model.allDisjoint(patterns).post();

		for (int i = 0; i < patterns.length; i++) {
			// 2 & 3- a map should belong to one of the identified maps and each
			// pattern should have
			// a map in the solution
			model.member(possiblePatternsMaps[i], patterns[i]).post();

		}

		// ============Finding solutions======================//
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();

		// int cnt = 0;

		// for(Solution so : solutions) {
		// System.out.println(cnt+":"+so);
		// cnt++;
		//
		// if(cnt == 100) {
		// break;
		// }
		// }
		//
		// System.out.println(solutions.size());

		return solutions;
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

		return result;
	}

	public List<Solution> findSolutions4(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

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
			// model.setObjective(Model.MAXIMIZE, severitySum);

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

		return solutions;
	}

	protected Map<Integer, List<int[]>> analyseSolutions(List<Solution> solutions, SetVar[] patterns,
			IntVar severitySum) {

		List<int[]> solVals = new LinkedList<int[]>();

		for (int j = 0; j < solutions.size(); j++) {

			Solution sol = solutions.get(j);
			solVals.clear();
			
			for (int i = 0; i < patterns.length; i++) {
				solVals.add(sol.getSetVal(patterns[i]));
			}
			
			// get patterns and maps ids used in this solution
			getPatternAndMapIDs(solVals);
			
			//add to solutions
			this.solutions.put(j, solVals);
			
			//add severity
			solutionSeverity.add(sol.getIntVal(severitySum));
		}

		return this.solutions;
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
		
		mappedPatternsID.add(tmpPatternIDs.stream().mapToInt(i -> i).toArray());
		mapsID.add(tmpMapIDs.stream().mapToInt(i -> i).toArray());
	}
	
	protected void print() {
		
		for(Integer solID : solutions.keySet()) {
			
		}
	}

}
