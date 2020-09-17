package it.polimi.algorithm.balancedpmedian.alns.ruin;

import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.*;

public class RandomPointRemoval implements PointRemoval {

    private final BPMProblem problem;
    private final Random random;
    private final double eps;

    public RandomPointRemoval(BPMProblem problem, Random random, double eps) {
        this.problem = problem;
        this.random = random;
        this.eps = eps;
    }

    @Override
    public Solution ruin(Solution s) {
        BPMSolution sol = (BPMSolution) s;
        List<Integer> points = new ArrayList<>(sol.getPointsMedian().keySet());
        Collections.shuffle(points, random);
        int q = 2 + random.nextInt(Math.min(100, (int) Math.ceil(eps*points.size())));
        for (int i=0; i<q; i++) {
            int toRemove = points.get(i);
            sol.unassign(toRemove, problem);
        }
        return sol;
    }
}
