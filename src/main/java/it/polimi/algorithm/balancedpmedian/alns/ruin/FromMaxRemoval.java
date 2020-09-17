package it.polimi.algorithm.balancedpmedian.alns.ruin;

import it.polimi.algorithm.alns.RuinOperator;
import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FromMaxRemoval implements PointRemoval {

    private final BPMProblem problem;

    public FromMaxRemoval(BPMProblem problem) {
        this.problem = problem;
    }

    @Override
    public Solution ruin(Solution s) {
        BPMSolution sol = (BPMSolution) s;

        int maxMedian = sol.getMedianPoints().entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey).orElse(-1);

        if (maxMedian == -1)
            return sol;

        int numRemoved = (int) Math.floor(sol.getMedianPoints().get(maxMedian).size() - problem.getAvg());

        if (numRemoved <= 0)
            return sol;

        for (int i=0; i<numRemoved; i++) {
            int toRemove = sol.getMedianPoints().get(maxMedian).poll();
            sol.unassign(toRemove, problem);
        }

        return sol;
    }
}
