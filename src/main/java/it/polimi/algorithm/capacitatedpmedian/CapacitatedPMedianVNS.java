package it.polimi.algorithm.capacitatedpmedian;

import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.util.Pair;
import it.polimi.util.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CapacitatedPMedianVNS {
    private final Logger LOGGER = LoggerFactory.getLogger(CapacitatedPMedianVNS.class);
    private final Random random;
    protected final int MAX_SOLUTION_CHANGES = 20;

    public CapacitatedPMedianVNS() {
        this(new Random());
    }

    public CapacitatedPMedianVNS(int seed) {
        this(new Random(seed));
    }

    public CapacitatedPMedianVNS(Random random) {
        this.random = random;
    }

    public CapacitatedPMedianSolution run(CapacitatedPMedianProblem prob) {
        int n = prob.getN();
        int p = prob.getP();
        int kmax = prob.getKmax();
        int r = prob.getR();
        float[][] d = prob.getC();
        int[] q = prob.getQs();
        int Q = prob.getQ();

        LOGGER.info("Computing a random initial solution.");

        int[] randX = Rand.permutateRange(0, n);
        Set<Integer> medians = new HashSet<>();
        for (int i=0; i<p; i++)
            medians.add(randX[i]);
        CapacitatedPMedianSolution initial = new CapacitatedPMedianSolution(medians, randX, prob);
        initial.computeLB2(prob);
        initial.computeF(prob);

        LOGGER.info("Done. Now running VNSLB for a good initial solution.");

        double start = System.nanoTime();

        prob.setR(1);
        CapacitatedPMedianSolution sol = vnslb(initial, prob);

        double end = System.nanoTime();
        double elapsedTime =  (end - start) / 1e6;

        LOGGER.info("VNSLB Done. obj=" + sol.getF() + " time=" + elapsedTime);

        sol.setElapsedTime(elapsedTime);

        return sol;
    }

    private CapacitatedPMedianSolution vnslb(CapacitatedPMedianSolution initial, CapacitatedPMedianProblem prob) {
        LOGGER.info("Running initial NSLB. obj=" + initial.getF() + " lb2=" + initial.getLb2());
        CapacitatedPMedianSolution a = nslb(initial, -Double.MAX_VALUE, prob);
        LOGGER.info("Initial NSLB done. Starting iterations.");
        for (int k=1; k<=prob.getKmax(); k++) {
            for (int r=0; r < prob.getR(); r++) {
                LOGGER.info("Starting iteration. k=" + k + " r=" + r);
                CapacitatedPMedianSolution a1 = shake(a, k, prob);
                LOGGER.info("Done shake. obj=" + a1.getF() + " lb2=" + a1.getLb2() + " lb1=" + a1.getLb1());

                a1 = nslb(a1, -Double.MAX_VALUE, prob);
                LOGGER.info("Done NSLB. obj=" + a1.getF() + " lb2=" + a1.getLb2() + " lb1=" + a1.getLb1());
                if (a1.getLb2() < a.getLb2()) {
                    LOGGER.info("Improvement! Replacing solution and restarting loop.");
                    a = a1;
                    k = 1;
                    break;
                }
            }
        }
        return a;
    }

    private CapacitatedPMedianSolution shake(CapacitatedPMedianSolution a, int k, CapacitatedPMedianProblem prob) {
        CapacitatedPMedianSolution a1 = a.clone();
        for (int i=0; i<k; i++) {
            int goout = a1.getX()[random.nextInt(prob.getP())];
            int goin = a1.getX()[prob.getP() + random.nextInt(prob.getN() - prob.getP())];
            a1.update(goout, goin, prob);
        }
        a1.computeLB2(prob);
        a1.computeF(prob);
        return a1;
    }

    private CapacitatedPMedianSolution nslb(CapacitatedPMedianSolution initial, double fbound,
                                            CapacitatedPMedianProblem prob) {
        LOGGER.info("Starting NSLB. lb2=" + initial.getLb2());
        int[] randMedians = Rand.permutateRange(0, prob.getP());
        int[] randNodes = Rand.permutateRange(prob.getP(), prob.getN());
        CapacitatedPMedianSolution a = initial;
        for (int i=0; i<randMedians.length; i++) {
            int gooutidx = randMedians[i];
            int goout = a.getX()[gooutidx];
            for (int goinidx : randNodes) {
                int goin = a.getX()[goinidx];
                CapacitatedPMedianSolution a1 = a.clone();
                a1.update(goout, goin, prob);
                a1.computeLB2(prob);
                a1.computeF(prob);
                if (a1.isBetterLB(a)) {
                    a = a1;
                    LOGGER.info("NSLB new improvement found. lb2=" + a.getLb2());
                    if (a.getLb2() < fbound) {
                        LOGGER.info("NSLB met early termination. Exiting.");
                        return a;
                    }
                    i = 0;
                    break;
                }
            }
        }
        a.computeLB2(prob);
        LOGGER.info("NSLB terminated. Exiting.");
        return a;
    }
}
