package it.polimi.algorithm.balancedpmedian.alns.ruin;

import it.polimi.algorithm.alns.RuinOperator;
import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.*;

public class WorstPointRemoval implements PointRemoval {

    private final BPMProblem problem;
    private final Random random;
    private final double eps;

    public WorstPointRemoval(BPMProblem problem, Random random, double eps) {
        this.problem = problem;
        this.random = random;
        this.eps = eps;
    }

    @Override
    public Solution ruin(Solution s) {
        BPMSolution sol = (BPMSolution) s;
        List<Integer> points = new ArrayList<>(sol.getPointsMedian().keySet());
        points.sort(Comparator.comparingDouble(i -> getDistanceCost(i, sol)));
        int q = 2 + random.nextInt(Math.min(100, (int) Math.ceil(eps*points.size())));
        for (int i=0; i<q; i++) {
            int toRemove = points.get(i);
            sol.unassign(toRemove, problem);
        }
        return sol;
    }

    private double getDistanceCost(int point, BPMSolution solution) {
        return problem.getC()[point][solution.getPointsMedian().get(point)];
    }
}
