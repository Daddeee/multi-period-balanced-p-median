package it.polimi.algorithm.balancedpmedian;

import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.util.Pair;
import it.polimi.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BalancedPMedianVNS implements Solver {
    public static int MAX_RESTART_WITHOUT_IMPROVEMENTS = 0;
    private final Logger LOGGER = LoggerFactory.getLogger(BalancedPMedianRVNS.class);
    private final Random random;
    private final BalancedAssignmentSolver balancedAssignmentSolver = new BalancedAssignmentSolver();

    public BalancedPMedianVNS() {
        this(new Random());
    }

    public BalancedPMedianVNS(int seed) {
        this(new Random(seed));
    }

    public BalancedPMedianVNS(Random random) {
        this.random = random;
    }

    @Override
    public Solution run(Problem problem) {
        double start = System.nanoTime();
        int n = problem.getN();
        int p = problem.getP();
        float[][] d = problem.getC();
        double avg = problem.getAvg();
        double alpha = problem.getAlpha();
        int kmax = problem.getKmax();

        BalancedPMedianSolution opt = new BalancedPMedianSolution(n, p, d, alpha, avg, random);

        int count = 0;
        do {
            //BalancedPMedianSolution acc = opt.clone();
            BalancedPMedianSolution cur = opt.clone();

            //double temperature = getInitialTemperature(cur.getF());
            //double cooling = 0.995;

            int k = 1;
            while (k <= kmax) {
                // shaking
                shake(cur, k, n, p, d, avg, alpha);

                LOGGER.info("Starting local search. k=" + k + " cur=" + cur.getF() + " opt=" + opt.getF());
                localSearch(cur, n, p, d, avg, alpha);
                LOGGER.info("Completed local search. k=" + k + " cur=" + cur.getF() + " opt=" + opt.getF());

                if (cur.getF() < opt.getF()) {
                    opt = cur.clone();
                    k = 1;
                } else {
                    cur = opt.clone();
                    k = k + 1;
                }

                // Move or not
                //if (accept(acc.getF(), cur.getF(), temperature)) {
                //    acc = cur.clone();
                //    k = 1;
                //    if (acc.getF() < opt.getF()) {
                //        opt = acc.clone();
                //    }
                //} else {
                //    cur = acc.clone();
                //    k = k + 1;
                //}

                //temperature = temperature*cooling;
            }
            count++;
            //LOGGER.info("Restarting. best=" + fopt + " count=" + count);
        } while (count < MAX_RESTART_WITHOUT_IMPROVEMENTS);

        double end = System.nanoTime();
        double time = (end - start) / 1e6;
        int[] periods = new int[n];
        int[] supermedians = new int[n];
        for (int i=0; i<n; i++)
            supermedians[i] = (opt.getC1()[i] == i) ? i : Solution.NO_SUPERMEDIAN;

        //LOGGER.info("Completed! Solution cost=" + fopt + "\n");

        return new Solution(periods, opt.getAx(), supermedians, opt.getF(), time);
    }

    private void shake(BalancedPMedianSolution cur, int k, int n, int p, float[][] d, double avg, double alpha) {
        for (int j = 1; j <= k; j++) {
            int goin = cur.getX()[random.nextInt(n - p) + p];
            int goout = cur.getX()[random.nextInt(p)];
            cur.swap(goin, goout, n, p, d, alpha, avg);
        }
        cur.setAx(cur.getC1().clone());
        cur.setF(cur.objectiveFunction(n, p, d, alpha, avg));
    }

    private void localSearch(BalancedPMedianSolution sol, int n, int p, float[][] d, double avg, double alpha) {
        for (int i=p; i<n; i++) {
            for (int j=0; j<p; j++) {
                BalancedPMedianSolution incumbent = sol.clone();
                int goin = incumbent.getX()[i];
                int goout = incumbent.getX()[j];
                incumbent.swap(goin, goout, n, p, d, alpha, avg);
                if (isBetter(incumbent, sol, n, p, d, alpha, avg)) {
                    sol.swap(goin, goout, n, p, d, alpha, avg);
                    sol.setF(incumbent.getF());
                    sol.setAx(incumbent.getAx());
                    i = p;
                    break;
                }
            }
        }
    }

    private boolean isBetter(BalancedPMedianSolution incumbent, BalancedPMedianSolution opt, int n, int p, float[][] d,
                             double alpha, double avg) {
        //if (incumbent.getLb1() > opt.getF()) return false;
        Pair<Double, int[]> optAx = balancedAssignmentSolver.solve(n, p, d, avg, alpha, incumbent.getX());
        if (optAx.getFirst() < opt.getF()) {
            incumbent.setAx(optAx.getSecond());
            incumbent.setF(optAx.getFirst());
            return true;
        }
        return false;
    }

    private boolean accept(double opt, double cur, double temperature) {
        double prob = Math.exp(-(cur - opt)/temperature);
        return opt != cur && random.nextDouble() < prob;
    }

    private double getInitialTemperature(double obj) {
        double w = 0.05;
        return w*obj / Math.log(2);
    }

    
}