package ie.lero.spare.pattern_extraction;

import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

public class TestChoco {

	public static void main(String[] args) {

		// example1();
//		ex2();
//		patternBasedExample();
		test2();

	}

	protected static void example1() {
		int N = 100;
		// 1. Modelling part
		Model model = new Model("all-interval series of size " + N);
		// 1.a declare the variables
		IntVar[] S = model.intVarArray("s", N, 0, N - 1, false);
		IntVar[] V = model.intVarArray("V", N - 1, 1, N - 1, false);
		// 1.b post the constraints
		for (int i = 0; i < N - 1; i++) {
			model.distance(S[i + 1], S[i], "=", V[i]).post();
		}
		model.allDifferent(S).post();
		model.allDifferent(V).post();

		S[1].gt(S[0]).post();
		V[1].gt(V[N - 2]).post();

		// 2. Solving part
		Solver solver = model.getSolver();
		// 2.a define a search strategy
		solver.setSearch(Search.minDomLBSearch(S));
		if (solver.solve()) {
			System.out.printf("All interval series of size %d%n", N);
			for (int i = 0; i < N - 1; i++) {
				System.out.printf("%d <%d> ", S[i].getValue(), V[i].getValue());
			}
			System.out.printf("%d", S[N - 1].getValue());
		}

	}

	static void ex2() {

		Model mod = new Model("Ex2 model");

		IntVar num1 = mod.intVar("num1", 0, 10, false);
		IntVar num2 = mod.intVar("num2", 5, 10, false);
		IntVar num3 = mod.intVar("num3", 11, 20, false);
		IntVar[] nms = mod.intVarArray(5, new int[] { 1, 2, 3, 4, 5 });

		// my own constraint
		// Constraint testConstraint = new Constraint("TestConstraint", new
		// TestPropagator(nms, 5));

		mod.arithm(num1, "<", num2).post();
		mod.arithm(num1, "=", num3).post();

		Solver solver = mod.getSolver();
		int cnt = 100;
		int i = 1;

		List<Solution> solutions = solver.findAllSolutions();

		System.out.println(solutions.get(0));

		/*
		 * while(solver.solve() && cnt > 0) { System.out.println(i+": "+num1 +
		 * " " + num2); // System.out.println(num2); cnt--;i++; }
		 */
		System.out.println(solver.getSolutionCount());
	}

	protected static void patternBasedExample() {

		/*//pattern 0 maps (3 maps)
		pattern1MapsArray[0][0] = new int[]{1,2};
		pattern1MapsArray[0][1] = new int[]{1,2,3};
		pattern1MapsArray[0][2] = new int[]{1,2,3,4};
		
		//pattern 1 maps (2 maps
		pattern1MapsArray[1][0] = new int[]{3,5};
		pattern1MapsArray[1][1] = new int[]{5,7};
*/
//		IntVar [] patternMaps = new IntVar[actualNumberOfPatterns];
		
		//variables for pattern maps
		/*pattern1Maps[0] = model.setVar("ptr1-1", 1,2);
		pattern1Maps[1] = model.setVar("ptr1-2", 1,3);
		pattern1Maps[2] = model.setVar("ptr1-3", 2,5);*/
/*		
		int patternIndex = 0;
		int cntMap = 0;*/
		//variables for all patterns
//		patternMaps[cntMap] = model.intVar("ptr0", patternIndex, patternIndex+numOfpattern0Maps-1,false);
		
		
//		int numOfpattern1Maps = 1;
//		int numOfpattern0Maps = 2;
//		SetVar[] patterns = new SetVar[actualNumberOfPatterns];
		
//		SetVar[] pattern1Maps = new SetVar[numOfpattern1Maps];
		
//		int[][][] pattern1MapsArray = new int[numOfpatterns][numOfpattern1Maps][];
		
//		int numOfpatterns = 3;
		/*patterns[patternIndex] = model.intVar("ptr0-0", 0, numOfPossiblePatterns-1);
		patternIndex++;
		patterns[patternIndex] = model.intVar("ptr0-1", 0, numOfPossiblePatterns-1);
		
		patternIndex++;
		cntMap++;
		
		patternMaps[cntMap] = model.intVar("ptr1", patternIndex, patternIndex+numOfpattern1Maps-1,false);
		
		patterns[patternIndex] = model.setVar("ptr1-1", 4,5);*/
		
//		patterns[0] = model.setVar("ptr1-1", 1, 2); // actions 1, 2, 3
//		patterns[1] = model.setVar("ptr1-2", 1, 2, 3); // actions 1, 2, 3
//		
//		patterns[2] = model.setVar("ptr2-1", 4, 5); // actions 3, 4, 5
//		patterns[3] = model.setVar("ptr2-2", 3, 4, 5); // actions 3, 4, 5
//		
//		patterns[4] = model.setVar("ptr3-1", 6, 7); // assuming it maps to actions 7, 8

		// define constraints
		//1st constraint: all patterns should be in a solution
		//2nd constraint: no overlapping between patterns
		
//		model.or(model.element(model.intVar(0), patterns, patterns[0]), 
//				model.element(model.intVar(1), patterns, patterns[1])).post();
		
		//patterns[0].
		
		/*for(int i = 0;i<numOfpatterns-1;i++) {
			//Constraint samePattern = model.
			model.disjoint(patterns[i], patterns[i+1]).post();
		}*/
		
		
		Model model = new Model("Pattern model");
		
		int actualNumberOfPatterns = 2;//numer of patterns matched
		int numOfPossiblePatterns = 5; //all found matches of patterns
		IntVar[] patterns = new IntVar[actualNumberOfPatterns];

		
		//represents which possible patterns are mapped to the same pattern (i.e. same number same pattern)
		/*IntVar[] possiblePatternsAssociation = new IntVar[numOfPossiblePatterns];
		
		possiblePatternsAssociation[0] = model.intVar(0); //pattern 0
		possiblePatternsAssociation[1] = model.intVar(0); //pattern 0
		possiblePatternsAssociation[2] = model.intVar(1); //pattern 1
		possiblePatternsAssociation[3] = model.intVar(1); //pattern 1
		possiblePatternsAssociation[4] = model.intVar(1); //pattern 1
*/		
		//=========Variables=====================
		
		//represents the actions that map to a pattern. Actions are represented as numbers in the sequence
	/*	SetVar[] possiblePatternsSets = new SetVar[numOfPossiblePatterns];
		
		possiblePatternsSets[0] = model.setVar("ptr0",1,2); //pattern 0
		possiblePatternsSets[1] = model.setVar("ptr1",2,3); //pattern 0
		possiblePatternsSets[2] = model.setVar("ptr2",80,100); //pattern 1
		possiblePatternsSets[3] = model.setVar("ptr3",4,6); //pattern 1
		possiblePatternsSets[4] = model.setVar("ptr4",7,8); //pattern 1
*/		
//		int[] tst = new int[] {1,2};
//		model.setVar(tst);
		//create pattern variables
		for(int i=0;i<patterns.length;i++) {
			patterns[i] = model.intVar("pattern-"+i, 0, numOfPossiblePatterns-1);
		}

		//=========Constraints=====================
		//each pattern should be different
		model.allDifferent(patterns).post();
//		Constraint myCons = new Constraint("Cons1", new TestPropagator(patterns,4));
//		
//		model.and(myCons).post();
	
		//no overlapping
		for(int i=0;i<actualNumberOfPatterns-1;i++) {

//			for(int j=i+1; j<actualNumberOfPatterns;j++) {
//				Constraint con = new Constraint("Cons1", new TestPropagator(patterns[i], patterns[j],4));
//				model.and(con).post();
//		}
		}
////		model.allDisjoint(pattern1Maps).post(); //no overlap constraint
//		//model.addClauses(LogOp.or(null));

		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();
		
		for(Solution so  : solutions) {
			System.out.println(so);
		}
	
		System.out.println(solutions.size());
		
	}
	
	private static void test2() {
	
		Model model = new Model("Pattern-Map model");
		
		int actualNumberOfPatterns = 2;//numer of patterns matched
		int numOfPossiblePatterns = 5; //all found matches of patterns
		int numOfActions = 10;
		SetVar[] patterns = new SetVar[actualNumberOfPatterns];
		int[] actionsArray = new int[numOfActions];
		
		//used as an upper bound for the set variables (i.e. patterns variables)
		// 0,1,2,...N-1 where N is the number of actions
		for(int i=0;i<actionsArray.length;i++) {
			actionsArray[i] = i;
		}
		
		//represents pattern maps found from matching all patterns to an incident model
		int [][] possiblePatternsMapsInt = new int[numOfPossiblePatterns][];
		possiblePatternsMapsInt[0] = new int[] { 1, 2 }; // pattern 0
		possiblePatternsMapsInt[1] = new int[] { 2, 3 }; // pattern 0
		possiblePatternsMapsInt[2] = new int[] { 5, 6 }; // pattern 1
		possiblePatternsMapsInt[3] = new int[] { 4, 6 }; // pattern 1
		possiblePatternsMapsInt[4] = new int[] { 9, 11 }; // pattern 1
		
		//============Model Variables======================//
		
		SetVar[] possiblePatternsMaps = new SetVar[possiblePatternsMapsInt.length];
		
		//variables which represent the sets that a generated set by a pattern should belong to
		for(int i=0;i<possiblePatternsMapsInt.length;i++) {
			possiblePatternsMaps[i] = model.setVar("map"+i,possiblePatternsMapsInt[i]); 
		}
		
		//each pattern has as domain values the range from {} to {0,1,2,..,N-1}, where N is number of actions
		for(int i=0;i<patterns.length;i++) {
			patterns[i] = model.setVar("pattern-"+i, new int[]{}, actionsArray);
		}
		
		//============Constraints======================//
		//=======1-No overlapping between maps
		//=======2-A map should be oen of the defined maps by the variable possiblePatternSets
		//no overlapping
		model.allDisjoint(patterns).post();
	
		for(SetVar var : patterns) {
			
			//avoid empty sets
//			model.notEmpty(var).post();
			model.member(possiblePatternsMaps, var).post();
//			model.and(new Constraint("mycons", new TestPropagator(var, 3, dummy))).post();
		}
		
		Solver solver = model.getSolver();
		List<Solution> solutions = solver.findAllSolutions();
		
		int cnt = 0;
		
		for(Solution so  : solutions) {
			System.out.println(cnt+":"+so);
			cnt++;
			
			if(cnt == 100) {
				break;
			}
		}
		
		System.out.println(solutions.size());
	}
}
