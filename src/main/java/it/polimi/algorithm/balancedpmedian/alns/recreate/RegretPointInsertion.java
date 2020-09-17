package it.polimi.algorithm.balancedpmedian.alns.recreate;

import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.Set;

public class RegretPointInsertion implements PointInsertion {

    protected BPMProblem problem;

    public RegretPointInsertion(BPMProblem problem) {
        this.problem = problem;
    }

    @Override
    public Solution recreate(Solution s) {
        BPMSolution sol = (BPMSolution) s;

        Set<Integer> unassignedPoints = sol.getUnassignedPointsTabu().keySet();

        while (unassignedPoints.size() > 0) {
            int bestUnassigned = -1, bestMedian = -1;
            double bestRegret = Double.MIN_VALUE;
            for (int unassigned : unassignedPoints) {
                int tabu = sol.getUnassignedPointsTabu().get(unassigned);
                double bestCost = Double.MAX_VALUE, secondBestCost = Double.MAX_VALUE;
                int toInsertMedian = -1;

                for (int median : sol.getMedianPoints().keySet()) {
                    if (median == tabu) continue;
                    double cost = getInsertionCost(unassigned, median, sol);
                    if (cost < bestCost) {
                        secondBestCost = bestCost;
                        bestCost = cost;
                        toInsertMedian = median;
                    } else if (cost < secondBestCost) {
                        secondBestCost = cost;
                    }
                }

                double regret = secondBestCost - bestCost;
                if (regret > bestRegret) {
                    bestRegret = regret;
                    bestUnassigned = unassigned;
                    bestMedian = toInsertMedian;
                }
            }
            sol.assign(bestUnassigned, bestMedian, problem);
        }

        return sol;
    }

    protected double getInsertionCost(int point, int median, BPMSolution solution) {
        int c1 = solution.getMedianPoints().get(median).size();
        int c2 = c1 + 1;
        double numcost = problem.getAlpha()*(Math.abs(c2 - problem.getAvg()) - Math.abs(c1 - problem.getAvg()));
        double distcost = problem.getC()[point][median];
        return distcost + numcost;
    }
}
