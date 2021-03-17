package it.polimi.algorithm.alns;

public class MaxIterationStopping implements StoppingCondition {

    private final int maxNumSegments;
    private int segmentsCount;

    public MaxIterationStopping(final int maxNumSegments) {
        this.maxNumSegments = maxNumSegments;
        this.segmentsCount = 0;
    }

    @Override
    public boolean isStopping(Solution s) {
        segmentsCount++;
        return segmentsCount > maxNumSegments;
    }
}
