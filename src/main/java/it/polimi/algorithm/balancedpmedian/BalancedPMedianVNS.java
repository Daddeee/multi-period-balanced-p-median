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
            BalancedPMedianSolution acc = opt.clone();
            BalancedPMedianSolution cur = opt.clone();

            double temperature = getInitialTemperature(cur.getF());
            double cooling = 0.995;

            int k = 1;
            while (k <= kmax) {
                // shaking
                shake(cur, k, n, p, d, avg, alpha);

                localSearch(cur, n, p, d, avg, alpha);

                if (accept(acc.getF(), cur.getF(), temperature)) {
                    acc = cur.clone();
                    k = 1;
                    if (acc.getF() < opt.getF()) {
                        opt = acc.clone();
                    }
                } else {
                    cur = acc.clone();
                    k = k + 1;
                }

                temperature = temperature*cooling;
            }
            count++;
            LOGGER.info("Restarting. best=" + opt.getF() + " count=" + count);
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
        while(true) {
            // find optimal goin and gout
            double wopt = Float.MAX_VALUE;
            int goinopt = -1, gooutopt = -1;
            int[] axopt = new int[0];
            for (int i=p; i < n; i++) {
                int goin = sol.getX()[i];
                Triple<Integer, Double, int[]> triple = move(sol, goin, wopt, n, p, d, alpha, avg);
                if (triple != null) {
                    double w = triple.getSecond();
                    if (w < wopt) {
                        wopt = w;
                        goinopt = goin;
                        gooutopt = triple.getFirst();
                        axopt = triple.getThird();
                    }
                }
            }

            // no improvement found
            if (wopt - sol.getF() >= 0) {
                sol.setAx(axopt);
                return;
            }

            // update obj function
            sol.setF(wopt);

            sol.swap(goinopt, gooutopt, n, p, d, alpha, avg);
        }
    }

    private boolean accept(double opt, double cur, double temperature) {
        double prob = Math.exp(-(cur - opt)/temperature);
        return opt != cur && random.nextDouble() < prob;
    }

    private double getInitialTemperature(double obj) {
        double w = 0.05;
        return w*obj / Math.log(2);
    }


    public Triple<Integer, Double, int[]> move(BalancedPMedianSolution sol, int goin, double fopt, int n, int p,
                                               float[][] d, double alpha, double avg) {
        int[] x = sol.getX();
        int[] c1 = sol.getC1();
        int[] c2 = sol.getC2();
        int[] xidx = sol.getXidx();

        int bestGoout = -1;
        int[] bestAx = null;
        double bestZ = Double.POSITIVE_INFINITY;

        for (int k=0; k<p; k++) {
            int goout = x[k];
            int[] nc1 = c1.clone();
            int[] nc2 = c2.clone();
            int[] counts = new int[p];
            int goincount = 0;

            double lb1 = 0;

            // update with new medians
            for (int i=0; i<n; i++) {
                if (nc1[i] == goout) {
                    if (d[i][goin] <= d[i][c2[i]]) {
                        nc1[i] = goin;
                        lb1 += d[i][goin];
                    } else {
                        nc1[i] = nc2[i];
                        lb1 += d[i][nc2[i]];
                    }
                } else {
                    if (d[i][goin] < d[i][nc1[i]]) {
                        lb1 += d[i][goin];
                        nc2[i] = nc1[i];
                        nc1[i] = goin;
                    } else if (d[i][goin] < d[i][nc2[i]]) {
                        nc2[i] = goin;
                        lb1 += d[i][nc1[i]];
                    } else if (nc2[i] == goout) {
                        nc2[i] = searchSecondMedian(i, x, nc1, goin, goout, p, d);
                    }
                }
            }

            // SKIP IF LB1 > FOPT
            if (lb1 > fopt) continue;

            int[] newx = x.clone();
            int goinidx = xidx[goin], gooutidx = k;
            newx[goinidx] = goout;
            newx[gooutidx] = goin;



            Pair<Double, int[]> optAx = balancedAssignmentSolver.solve(n, p, d, alpha, avg, newx, fopt);

            if (optAx == null) continue;

            if (optAx.getFirst() < bestZ) {
                bestGoout = k;
                bestZ = optAx.getFirst();
                bestAx = optAx.getSecond();
            }
        }

        if (bestGoout == -1) return null;

        return new Triple<>(x[bestGoout], bestZ, bestAx);
    }

    private int searchSecondMedian(int i, int[] x, int[] c1, int goin, int goout, int p, float[][] d) {
        float newMin = Float.MAX_VALUE;
        int secondMedian = -1;
        for (int j=0; j<p; j++) {
            if (x[j] != c1[i] && x[j] != goout && d[i][x[j]] < newMin) {
                newMin = d[i][x[j]];
                secondMedian = x[j];
            }
        }
        if (d[i][goin] < newMin && c1[i] != goin)
            secondMedian = goin;

        return secondMedian;
    }
    
}