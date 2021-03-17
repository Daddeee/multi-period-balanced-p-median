package it.polimi.algorithm.alns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ALNS {
    private static final Logger LOGGER = LoggerFactory.getLogger(ALNS.class);
    private final int segmentSize;
    private final StoppingCondition stoppingCondition;
    private final OperatorsSelector operatorsSelector;
    private final AcceptanceCriterion acceptanceCriterion;
    private final HashSet<Solution> accepted;

    public ALNS(final int segmentSize, final StoppingCondition stoppingCondition,
                final OperatorsSelector operatorsSelector, final AcceptanceCriterion acceptanceCriterion) {
        this.segmentSize = segmentSize;
        this.stoppingCondition = stoppingCondition;
        this.operatorsSelector = operatorsSelector;
        this.acceptanceCriterion = acceptanceCriterion;
        this.accepted = new HashSet<>();
    }

    public Solution run(final Solution initialSolution) {
        Solution s = initialSolution;
        Solution smin = s;
        int iterations = 0;
        int segmentCount = 0;
        while(!stoppingCondition.isStopping(s)) {
            segmentCount++;
            LOGGER.info("Starting segment " + segmentCount);
            for (int i=0; i<segmentSize; i++) {
                iterations++;

                Solution s1 = operatorsSelector.search(s);

                if (acceptanceCriterion.accept(s1, s)) {
                    LOGGER.info("Accepted solution at iteration " + iterations + ", obj=" + s1.getCost());
                    if (!accepted.contains(s1))
                        operatorsSelector.update(s1, s, smin);
                    accepted.add(s1);
                    s = s1;
                    if (s.getCost() < smin.getCost()) {
                        LOGGER.info("Best solution so far.");
                        smin = s;
                    }
                }
            }
            operatorsSelector.updateWeights();
        }
        return smin;
    }
}
