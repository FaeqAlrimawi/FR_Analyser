package ie.lero.spare.pattern_extraction;

import java.util.List;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.SetVar;

public class PatternMappingSolver {

	
	
	public List<Solution> findSolutions(Map<Integer, List<int[]>> patternMaps, int numberOfActions) {

		//numberOfActions could be defined as the max number of the maps

		int[] actionsArray = new int[numberOfActions];
		
		//used as an upper bound for the set variables (i.e. patterns variables)
		// 0,1,2,...N-1 where N is the number of actions
		for(int i=0;i<actionsArray.length;i++) {
			actionsArray[i] = i;
		}
		
		//============Creating model========================//
		
		Model model = new Model("Pattern-Map Model");
		
		//============Defining Variables======================//
		
		SetVar[] patterns = new SetVar[patternMaps.keySet().size()];
	
		SetVar[][] possiblePatternsMaps = new SetVar[patterns.length][];
		
		//each pattern has as domain values the range from {} to {0,1,2,..,N-1}, where N is number of actions
		for(int i=0;i<patterns.length;i++) {
			patterns[i] = model.setVar("pattern-"+i, new int[]{}, actionsArray);
		}
		
		for(int i=0;i<patterns.length;i++) {
		
			//variables which represent the sets that a generated set by a pattern should belong to
			possiblePatternsMaps[i] = new SetVar[patternMaps.get(i).size()];
			
			for(int j=0;j<possiblePatternsMaps[i].length;j++) {
				possiblePatternsMaps[i][j] = model.setVar("map"+i+""+j,patternMaps.get(i).get(j)); 
			}
		}
		
		//============Defining Constraints======================//
		//===1-No overlapping between maps
		//===2-A map should be one of the defined maps by the variable possiblePatternMaps
		//===3-at least 1 map for each pattern
		
		//1-no overlapping
		model.allDisjoint(patterns).post();
	
		for(int i =0;i < patterns.length;i++) {
			//2 & 3- a map should belong to one of the identified maps and each pattern should have 
			//a map in the solution
			model.member(possiblePatternsMaps[i], patterns[i]).post();
			
		}
		
		//============Finding solutions======================//
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();
		
		//optimisation
		
		return solutions;
	}
	
	public List<Solution> findSolutions(Map<Integer, List<int[]>> patternMaps) {

		//numberOfActions could be defined as the max number of the maps
		int numberOfActions = findMaxNumber(patternMaps);
		int[] actionsArray = new int[numberOfActions];
		
		//used as an upper bound for the set variables (i.e. patterns variables)
		// 0,1,2,...N-1 where N is the number of actions
		for(int i=0;i<actionsArray.length;i++) {
			actionsArray[i] = i;
		}
		
		//============Creating model========================//
		
		Model model = new Model("Pattern-Map Model");
		
		//============Defining Variables======================//
		
		SetVar[] patterns = new SetVar[patternMaps.keySet().size()];
	
		SetVar[][] possiblePatternsMaps = new SetVar[patterns.length][];
		
		//each pattern has as domain values the range from {} to {0,1,2,..,N-1}, where N is number of actions
		for(int i=0;i<patterns.length;i++) {
			patterns[i] = model.setVar("pattern-"+i, new int[]{}, actionsArray);
		}
		
		for(int i=0;i<patterns.length;i++) {
		
			//variables which represent the sets that a generated set by a pattern should belong to
			possiblePatternsMaps[i] = new SetVar[patternMaps.get(i).size()];
			
			for(int j=0;j<possiblePatternsMaps[i].length;j++) {
				possiblePatternsMaps[i][j] = model.setVar("map"+i+""+j,patternMaps.get(i).get(j)); 
			}
		}
		
		//============Defining Constraints======================//
		//===1-No overlapping between maps
		//===2-A map should be one of the defined maps by the variable possiblePatternMaps
		//===3-at least 1 map for each pattern
		
		//1-no overlapping
		model.allDisjoint(patterns).post();
	
		for(int i =0;i < patterns.length;i++) {
			//2 & 3- a map should belong to one of the identified maps and each pattern should have 
			//a map in the solution
			model.member(possiblePatternsMaps[i], patterns[i]).post();
			
		}
		
		//============Finding solutions======================//
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();
		
//		int cnt = 0;
		
//		for(Solution so  : solutions) {
//			System.out.println(cnt+":"+so);
//			cnt++;
//			
//			if(cnt == 100) {
//				break;
//			}
//		}
//		
//		System.out.println(solutions.size());
		
		return solutions;
	}
	
	protected int findMaxNumber(Map<Integer, List<int[]>> patternMaps) {
		
		//finds the maximum action number specified in the given map
		int result = 0;
		
		for(List<int[]> list : patternMaps.values()) {
			for(int [] ary : list) {
				if(ary[ary.length-1] > result) {
					result = ary[ary.length-1];
				}
			}
		}
		
		return result;
	}
	
}
