package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.alns.OperatorsSelector;
import it.polimi.algorithm.alns.RecreateOperator;
import it.polimi.algorithm.alns.RuinOperator;
import it.polimi.algorithm.alns.Solution;
import it.polimi.algorithm.balancedpmedian.alns.recreate.MedianInsertion;
import it.polimi.algorithm.balancedpmedian.alns.recreate.PointInsertion;
import it.polimi.algorithm.balancedpmedian.alns.ruin.MedianRemoval;
import it.polimi.algorithm.balancedpmedian.alns.ruin.PointRemoval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BalancedPMedianOperatorSelector extends OperatorsSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalancedPMedianOperatorSelector.class);
    private List<MedianInsertion> medianRecreateOperators;
    private List<PointInsertion> pointRecreateOperators;
    private Selector medianRecreateSelector;
    private Selector pointRecreateSelector;
    private String lastSelectedRuinType;

    public BalancedPMedianOperatorSelector(List<RuinOperator> ruinOperators, List<RecreateOperator> recreateOperators,
                                       double d1, double d2, double d3, double r) {
        super(ruinOperators, recreateOperators, d1, d2, d3, r);
        initDividedRecreate(recreateOperators);
    }

    public BalancedPMedianOperatorSelector(List<RuinOperator> ruinOperators, List<RecreateOperator> recreateOperators,
                                       double d1, double d2, double d3, double r, int seed) {
        super(ruinOperators, recreateOperators, d1, d2, d3, r, seed);
        initDividedRecreate(recreateOperators);
    }

    public BalancedPMedianOperatorSelector(List<RuinOperator> ruinOperators, List<RecreateOperator> recreateOperators,
                                       double d1, double d2, double d3, double r, Random random) {
        super(ruinOperators, recreateOperators, d1, d2, d3, r, random);
        initDividedRecreate(recreateOperators);
    }

    private void initDividedRecreate(List<RecreateOperator> recreateOperators) {
        this.medianRecreateOperators = recreateOperators.stream().filter(ro -> ro instanceof MedianInsertion)
                .map(ro -> (MedianInsertion) ro).collect(Collectors.toList());
        this.pointRecreateOperators = recreateOperators.stream().filter(ro -> ro instanceof PointInsertion)
                .map(ro -> (PointInsertion) ro).collect(Collectors.toList());
        this.medianRecreateSelector = new Selector(medianRecreateOperators.size());
        this.pointRecreateSelector = new Selector(pointRecreateOperators.size());
    }

    @Override
    public Solution search(Solution s) {
        lastSelectedRuinIdx = ruinSelector.select();
        RuinOperator d = ruinOperators.get(lastSelectedRuinIdx);
        BalancedPMedianSolution sol;
        if (d instanceof PointRemoval) {
            lastSelectedRuinType = "point";
            lastSelectedRecreateIdx = pointRecreateSelector.select();
            RecreateOperator r = pointRecreateOperators.get(lastSelectedRecreateIdx);
            LOGGER.info("selected operators. ruin=" + d.getClass().getSimpleName() + " recreate=" + r.getClass().getSimpleName());
            sol = (BalancedPMedianSolution) r.recreate(d.ruin(s));
        } else if (d instanceof MedianRemoval) {
            lastSelectedRuinType = "median";
            lastSelectedRecreateIdx = medianRecreateSelector.select();
            RecreateOperator r = medianRecreateOperators.get(lastSelectedRecreateIdx);
            LOGGER.info("selected operators. ruin=" + d.getClass().getSimpleName() + " recreate=" + r.getClass().getSimpleName());
            sol = (BalancedPMedianSolution) r.recreate(d.ruin(s));
        } else {
            throw new IllegalStateException("operator not a point removal neither a median removal");
        }
        return sol;
    }

    @Override
    public void updateWeights() {
        this.ruinSelector.updateWeights();
        this.pointRecreateSelector.updateWeights();
        this.medianRecreateSelector.updateWeights();
    }

    @Override
    public void update(Solution accepted, Solution current, Solution best) {
        this.ruinSelector.update(lastSelectedRuinIdx, accepted, current, best);
        if (lastSelectedRuinType.equals("point")) {
            this.pointRecreateSelector.update(lastSelectedRecreateIdx, accepted, current, best);
        } else if (lastSelectedRuinType.equals("median")) {
            this.medianRecreateSelector.update(lastSelectedRecreateIdx, accepted, current, best);
        } else {
            throw new IllegalStateException("unknown last selected type");
        }
    }
}
