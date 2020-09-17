package it.polimi.algorithm.balancedpmedian.alns.recreate;

import it.polimi.algorithm.balancedpmedian.alns.BPMProblem;
import it.polimi.algorithm.balancedpmedian.alns.BPMSolution;

import java.util.Random;

public class BestPointInsertionWithNoise extends BestPointInsertion {

    private Random random;
    private double maxNoise;

    public BestPointInsertionWithNoise(BPMProblem problem, Random random, double maxNoise) {
        super(problem);
        this.random = random;
        this.maxNoise = maxNoise;
    }

    @Override
    protected double getInsertionCost(int point, int median, BPMSolution solution) {
        return super.getInsertionCost(point, median, solution) + getNoise();
    }

    private double getNoise() {
        return -maxNoise + random.nextDouble()*2*maxNoise;
    }
}
