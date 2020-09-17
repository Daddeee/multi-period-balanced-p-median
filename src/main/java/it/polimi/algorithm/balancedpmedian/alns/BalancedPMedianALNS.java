package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.Solver;
import it.polimi.algorithm.alns.*;
import it.polimi.algorithm.balancedpmedian.alns.recreate.*;
import it.polimi.algorithm.balancedpmedian.alns.ruin.*;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BalancedPMedianALNS implements Solver {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalancedPMedianALNS.class);

    @Override
    public Solution run(Problem problem) {
        BPMProblem prob = new BPMProblem(problem.getN(), problem.getP(), problem.getC());
        Random random = new Random(1337);
        BPMSolution initial = getInitial(prob, random);

        LOGGER.info("initial solution obj=" + initial.getCost());

        int segmentSize = 100;
        StoppingCondition stoppingCondition = new MultipleStopping(Arrays.asList(
                new MaxIterationStopping(200), new EarlyStopping(20)));
        PointMedianOperatorSelector selector = new PointMedianOperatorSelector(getRuinOperators(prob, random),
                getRecreateOperators(prob, random), 33, 9, 13, 0.1, random);
        AcceptanceCriterion acceptance = new TemperatureAcceptance(getTStart(initial), 0.99, random);

        ALNS alns = new ALNS(segmentSize, stoppingCondition, selector, acceptance);

        BPMSolution sol = (BPMSolution) alns.run(initial);

        return parseSolution(prob, sol);
    }

    private BPMSolution getInitial(BPMProblem problem, Random random) {
        List<Integer> unassigneds = IntStream.range(0, problem.getN()).boxed().collect(Collectors.toList());
        Collections.shuffle(unassigneds, random);
        BPMSolution solution = BPMSolution.empty(unassigneds);
        solution.setCost(solution.computeCost(problem));

        int firstMedian = unassigneds.get(0);
        solution.insert(firstMedian, problem);
        for (int i=1; i<unassigneds.size(); i++)
            solution.assign(unassigneds.get(i), firstMedian, problem);

        for (int i=1; i<problem.getP(); i++)
            solution.insert(unassigneds.get(i), problem);

        double testcost = solution.computeCost(problem);
        if (solution.getCost() == testcost)
            throw new IllegalStateException("wrong cost calculation. auto=" + solution.getCost() + " calc=" + testcost);

        return solution;
    }

    private Solution parseSolution(BPMProblem problem, BPMSolution solution) {
        int[] medians = new int[problem.getN()];
        int[] supermedians = Collections.nCopies(problem.getN(), -1).stream().mapToInt(s -> s).toArray();
        Solution sol = new Solution(Collections.nCopies(problem.getN(), 0).stream().mapToInt(i -> i).toArray(), medians,
                supermedians, 0, 0);
        double obj = solution.computeCost(problem);
        sol.setObjective(obj);
        return sol;
    }

    private List<RuinOperator> getRuinOperators(BPMProblem prob, Random random) {
        return Arrays.asList(
                new FromMaxRemoval(prob),
                new RandomMedianRemoval(prob, random, 0.4),
                new RandomPointRemoval(prob, random, 0.4),
                new WorstPointRemoval(prob, random, 0.4),
                new WorstMedianRemoval(prob, random, 0.4)
        );
    }

    private List<RecreateOperator> getRecreateOperators(BPMProblem prob, Random random) {
        double maxN = getMaxN(prob);
        return Arrays.asList(
                new BestPointInsertion(prob),
                new BestPointInsertionWithNoise(prob, random, maxN),
                new RandomMedianInsertion(prob, random),
                new RegretPointInsertion(prob),
                new RegretPointInsertionWithNoise(prob, random, maxN)
        );
    }

    private static double getMaxN(BPMProblem prob) {
        double eta = 0.025;
        double maxDist = Double.MIN_VALUE;
        float[][] dists = prob.getC();
        for (int i=0; i<dists.length; i++) {
            for (int j=0; j<dists[i].length; j++) {
                if (dists[i][j] > maxDist)
                    maxDist = dists[i][j];
            }
        }
        return eta*maxDist;
    }

    private static double getTStart(BPMSolution sol) {
        double w = 0.05;
        double cost = sol.getCost();
        return 0; //return w*cost / Math.log(2);
    }
}
