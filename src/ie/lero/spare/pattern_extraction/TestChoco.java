package ie.lero.spare.pattern_extraction;

import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;

public class TestChoco {

	public static void main(String[] args) {

		// example1();
		ex2();

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
		IntVar num2 = mod.intVar("num1", 5, 10, false);
		IntVar num3 = mod.intVar("num1", -2, 20, false);
		IntVar[] nms = mod.intVarArray(5, new int[] { 1, 2, 3, 4, 5 });

		// my own constraint
		Constraint testConstraint = new Constraint("TestConstraint", new TestPropagator(nms, 5));

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

}
