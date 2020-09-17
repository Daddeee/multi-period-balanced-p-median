package it.polimi.algorithm.balancedpmedian.alns.recreate;

import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class RandomMedianInsertion implements MedianInsertion {

    private BPMProblem problem;
    private Random random;

    public RandomMedianInsertion(BPMProblem problem, Random random) {
        this.problem = problem;
        this.random = random;
    }

    @Override
    public Solution recreate(Solution s) {
        BPMSolution sol = (BPMSolution) s;
        Set<Integer> points = sol.getPointsMedian().keySet();

        while (sol.getMedianPoints().keySet().size() < problem.getP()) {
            int toInsert = randomSampleFromSet(points);
            sol.insert(toInsert, problem);
            points.remove(toInsert);
        }

        return sol;
    }

    private int randomSampleFromSet(Set<Integer> set) {
        int index = random.nextInt(set.size());
        Iterator<Integer> iter = set.iterator();
        for (int i = 0; i < index; i++)
            iter.next();
        return iter.next();
    }
}
