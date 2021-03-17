package it.polimi.algorithm.balancedpmedian.alns.ruin;

import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BalancedPMedianProblem;
import it.polimi.algorithm.balancedpmedian.alns.BalancedPMedianSolution;

import java.util.*;

public class RandomMedianRemoval implements MedianRemoval {

    private final BalancedPMedianProblem problem;
    private final Random random;
    private final double eps;

    public RandomMedianRemoval(BalancedPMedianProblem problem, Random random, double eps) {
        this.problem = problem;
        this.random = random;
        this.eps = eps;
    }

    @Override
    public Solution ruin(Solution s) {
        BalancedPMedianSolution sol = (BalancedPMedianSolution) s;
        List<Integer> medians = new ArrayList<>(sol.getMedians());
        int q = 1 + random.nextInt(Math.min(100, (int) Math.ceil(eps*problem.getP())));
        for (int i=0; i<q; i++) {
            int toRemove = medians.get(i);
            sol.remove(toRemove, problem);
        }
        return sol;
    }
}
