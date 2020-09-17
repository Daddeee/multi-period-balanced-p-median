package it.polimi.algorithm.balancedpmedian.alns.ruin;

import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class WorstMedianRemoval implements MedianRemoval {

    private final BPMProblem problem;
    private final Random random;
    private final double eps;

    public WorstMedianRemoval(BPMProblem problem, Random random, double eps) {
        this.problem = problem;
        this.random = random;
        this.eps = eps;
    }

    @Override
    public Solution ruin(Solution s) {
        BPMSolution sol = (BPMSolution) s;
        List<Integer> medians = new ArrayList<>(sol.getMedianPoints().keySet());
        medians.sort(Comparator.comparingDouble(i -> getMedianCost(i, sol)));
        int q = 1 + random.nextInt(Math.min(100, (int) Math.ceil(eps*problem.getP())));
        for (int i=0; i<q; i++) {
            int toRemove = medians.get(i);
            sol.remove(toRemove, problem);
        }
        return sol;
    }

    private double getMedianCost(int median, BPMSolution solution) {
        double distCost = solution.getMedianPoints().get(median).stream().mapToDouble(p -> problem.getC()[p][median]).sum();
        double numCost = problem.getAlpha() * Math.abs(solution.getMedianPoints().get(median).size() - problem.getAvg());
        return distCost + numCost;
    }

}
