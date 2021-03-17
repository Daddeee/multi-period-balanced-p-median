package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.alns.ConstructionHeuristic;
import it.polimi.algorithm.alns.Problem;
import it.polimi.algorithm.alns.Solution;
import it.polimi.util.Rand;

import java.util.*;
import java.util.stream.Collectors;

public class BalancedPMedianConstructionHeuristic implements ConstructionHeuristic {

    @Override
    public Solution build(Problem p) {
        BalancedPMedianProblem problem = (BalancedPMedianProblem) p;
        Set<Integer> medians = Arrays.stream(Rand.permutateRange(0, problem.getN())).boxed().limit(problem.getP())
                .collect(Collectors.toSet());
        Map<Integer,Integer> counts = new HashMap<>();
        int[] assignment = new int[problem.getN()];
        Arrays.fill(assignment, -1);
        for (int i=0; i<problem.getN(); i++) {
            for (int median : medians) {
                if (assignment[i] == -1 || problem.getC()[i][median] < problem.getC()[i][assignment[i]]) {
                    assignment[i] = median;
                    counts.put(median, counts.getOrDefault(median, 0) + 1);
                }
            }
        }
        BalancedPMedianSolution solution = new BalancedPMedianSolution(problem, assignment, medians, counts);
        double obj = BalancedPMedianSolution.computeObjective(problem, solution);
        solution.setObjective(obj);
        return solution;
    }
}
