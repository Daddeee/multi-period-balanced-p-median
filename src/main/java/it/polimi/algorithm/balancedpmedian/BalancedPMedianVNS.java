package it.polimi.algorithm.balancedpmedian;

import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BalancedPMedianVNS implements Solver {
    public static int MAX_RESTART_WITHOUT_IMPROVEMENTS = 0;
    private final Logger LOGGER = LoggerFactory.getLogger(BalancedPMedianRVNS.class);
    private final Random random;

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

                // Move or not
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
        cur.setF(cur.objectiveFunction(n, d, alpha, avg));
    }

    private void localSearch(BalancedPMedianSolution sol, int n, int p, float[][] d, double avg, double alpha) {
        while(true) {
            // find optimal goin and gout
            double wopt = Float.MAX_VALUE;
            int goinopt = -1, gooutopt = -1;
            int[] axopt = new int[0];
            for (int i=p; i < n; i++) {
                int goin = sol.getX()[i];
                Triple<Integer, Double, int[]> triple = move(sol, goin, n, p, d, alpha, avg);
                double w = triple.getSecond();
                if (w < wopt) {
                    wopt = w;
                    goinopt = goin;
                    gooutopt = triple.getFirst();
                    axopt = triple.getThird();
                }
            }

            // no improvement found
            if (wopt - sol.getF() >= 0) {
                for (int i=0; i<axopt.length; i++)
                    sol.getAx()[i] = axopt[i];
                return;
            }

            sol.setF(wopt);
            sol.swap(goinopt, gooutopt, n, p, d, alpha, avg);
        }
    }

    public Triple<Integer, Double, int[]> move(BalancedPMedianSolution sol, int goin, int n, int p, float[][] d,
                                               double alpha, double avg) {
        int[] x = sol.getX();
        int[] c1 = sol.getC1();
        int[] c2 = sol.getC2();
        int[] xidx = sol.getXidx();

        int[][] nc1 = new int[p][n];
        int[][] counts = new int[p][p];
        int[] goincounts = new int[p];

        double[] fs = new double[p];
        for (int j=0; j<p; j++) {
            fs[j] = 0;
            for (int i=0; i<n; i++) {
                nc1[j][i] = sol.getC1()[i];
                counts[j][sol.getXidx()[c1[i]]] += 1;
                fs[j] += d[i][c1[i]];
            }
        }

        // set deleted counts and distances to null
        for (int j=0; j<p; j++)
            counts[j][j] = 0;

        // update with new medians
        for (int i=0; i<n; i++) {
            if (d[i][goin] < d[i][c1[i]]) {
                for (int j=0; j<p; j++) {
                    goincounts[j] += 1;
                    fs[j] += d[i][goin];
                    fs[j] -= d[i][c1[i]];
                    nc1[j][i] = goin;
                    if (xidx[c1[i]] != j)
                        counts[j][xidx[c1[i]]] -= 1;
                }
            } else {
                if (d[i][goin] <= d[i][c2[i]])  {
                    fs[xidx[c1[i]]] += d[i][goin];
                    fs[xidx[c1[i]]] -= d[i][c2[i]];
                    nc1[xidx[c1[i]]][i] = goin;
                    goincounts[xidx[c1[i]]] += 1;
                } else {
                    fs[xidx[c1[i]]] += d[i][c2[i]];
                    fs[xidx[c1[i]]] -= d[i][c1[i]];
                    nc1[xidx[c1[i]]][i] = c2[i];
                    counts[xidx[c1[i]]][xidx[c2[i]]] += 1;
                }
            }
        }

        for (int j=0; j<p; j++) {
            for (int k=0; k<p; k++) {
                fs[j] += alpha * Math.abs(counts[j][k] - avg);
            }
            fs[j] += alpha * Math.abs(goincounts[j] - avg);
        }

        int bestJ = 0;
        double bestF = fs[0];
        for (int j=1; j<p; j++) {
            if (fs[j] < bestF) {
                bestF = fs[j];
                bestJ = j;
            }
        }

        int i = bestJ;
        for (int j=0; j<n; j++) {
            int jmed = nc1[i][j];
            int jmedcount = jmed == goin ? goincounts[i] : counts[i][xidx[jmed]];
            if (jmedcount > avg && jmed != j) {
                // gain in the objective function due to removing j from jmed
                double removalGain = d[j][jmed];
                removalGain += alpha * Math.min(jmedcount - avg, 1);
                removalGain -= alpha * Math.max(avg - (jmedcount - 1), 0);
                // check if there's a median with a lower delta. Skip median with count >= avg because they cannot
                // do better than jmed (by construction jmed is closer to j then any other),
                double bestInsertionCost = Double.MAX_VALUE;
                int bestInsertionMedian = -1;
                for (int k=0; k<p; k++) {
                    int kcount = counts[i][k];
                    if (kcount >= avg || k == i) continue;
                    double insertionCost = d[j][x[k]];
                    insertionCost -= alpha * Math.min(Math.max(avg - (kcount + 1), 0), 1);
                    insertionCost += alpha * Math.max(kcount + 1 - avg, 0);
                    if (insertionCost < bestInsertionCost) {
                        bestInsertionCost = insertionCost;
                        bestInsertionMedian = x[k];
                    }
                }
                int kcount = goincounts[i];
                if (kcount < avg) {
                    double insertionCost = d[j][goin];
                    insertionCost -= alpha * Math.min(Math.max(avg - (kcount + 1), 0), 1);
                    insertionCost += alpha * Math.max(kcount + 1 - avg, 0);
                    if (insertionCost < bestInsertionCost) {
                        bestInsertionCost = insertionCost;
                        bestInsertionMedian = goin;
                    }
                }

                if (bestInsertionMedian != -1 && bestInsertionCost < removalGain) {
                    nc1[i][j] = bestInsertionMedian;
                    if (jmed == goin) {
                        goincounts[i] -= 1;
                    } else {
                        counts[i][xidx[jmed]] -= 1;
                    }
                    if (bestInsertionMedian == goin) {
                        goincounts[i] += 1;
                    } else {
                        counts[i][xidx[bestInsertionMedian]] += 1;
                    }
                }
            }
        }

        int goout = i;
        int[] ax = nc1[i];
        double z = 0.;
        for (int j=0; j<n; j++)
            z += d[j][nc1[i][j]];

        for (int j=0; j<p; j++) {
            if (j == i) continue;
            z += alpha * Math.abs(counts[i][j] - avg);
        }
        z += alpha * Math.abs(goincounts[i] - avg);

        return new Triple<>(x[goout], z, ax);
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