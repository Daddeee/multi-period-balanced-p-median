package it.polimi.algorithm.balancedpmedian.alns.ruin;

import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.*;

public class RandomMedianRemoval implements MedianRemoval {

    private final BPMProblem problem;
    private final Random random;
    private final double eps;

    public RandomMedianRemoval(BPMProblem problem, Random random, double eps) {
        this.problem = problem;
        this.random = random;
        this.eps = eps;
    }

    @Override
    public Solution ruin(Solution s) {
        BPMSolution sol = (BPMSolution) s;
        List<Integer> medians = new ArrayList<>(sol.getMedianPoints().keySet());
        Collections.shuffle(medians);
        int q = 1 + random.nextInt(Math.min(100, (int) Math.ceil(eps*problem.getP())));
        for (int i=0; i<q; i++) {
            int toRemove = medians.get(i);
            sol.remove(toRemove, problem);
        }
        return sol;
    }
}
