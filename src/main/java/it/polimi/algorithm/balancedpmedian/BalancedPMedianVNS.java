package it.polimi.algorithm.balancedpmedian;

import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.util.Rand;
import it.polimi.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BalancedPMedianVNS implements Solver {
    protected final int MAX_RESTART_WITHOUT_IMPROVEMENTS = 3;
    private final Logger LOGGER = LoggerFactory.getLogger(BalancedPMedianVNS.class);
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

        // optimal values
        int[] xopt = Rand.permutateRange(0, n, random);
        int[] xidxopt = getIndexes(xopt);
        int[][] c = getClosestMedians(xopt, n, p, d);
        int[] c1opt = c[0];
        int[] c2opt = c[1];
        double fopt = computeObjectiveFunction(c1opt, n, d, alpha, avg);
        int[] axopt = c1opt.clone();

        int count = 0;
        do {
            // accepted values
            int[] xacc = xopt.clone();
            int[] xidxacc = xidxopt.clone();
            int[] c1acc = c1opt.clone();
            int[] c2acc = c2opt.clone();
            double facc = fopt;
            int[] axacc = axopt.clone();

            // current values
            int[] xcur = xopt.clone();
            int[] xidxcur = xidxopt.clone();
            int[] c1cur = c1opt.clone();
            int[] c2cur = c2opt.clone();
            double fcur = fopt;
            int[] axcur = axopt.clone();

            double temperature = getInitialTemperature(fcur);
            double cooling = 0.995;

            BalancedFastInterchange bfi = new BalancedFastInterchange(n, p, d, alpha, avg);
            int k = 1;
            while (k <= kmax) {
                // shaking
                for (int j = 1; j <= k; j++) {
                    // sample random median to be inserted
                    int goin = xcur[random.nextInt(n - p) + p];

                    // find best median to remove
                    Triple<Integer, Double, int[]> triple = bfi.move(xcur, xidxcur, c1cur, c2cur, goin);
                    int goout = triple.getFirst();
                    fcur = triple.getSecond();
                    axcur = triple.getThird();

                    // update xcur and xidx
                    int outidx = xidxcur[goout], inidx = xidxcur[goin];
                    xcur[outidx] = goin;
                    xcur[inidx] = goout;
                    xidxcur[goin] = outidx;
                    xidxcur[goout] = inidx;

                    // update c1 and c2
                    bfi.update(xcur, c1cur, c2cur, goin, goout);
                }

                // Local search
                //fcur = bfi.fastInterchange(xcur, xidxcur, axcur, c1cur, c2cur, fcur);

                // Move or not
                if (accept(facc, fcur, temperature)) {
                    xacc = xcur.clone();
                    xidxacc = xidxcur.clone();
                    c1acc = c1cur.clone();
                    c2acc = c2cur.clone();
                    facc = fcur;
                    axacc = axcur.clone();
                    k = 1;

                    if (facc < fopt) {
                        fopt = facc;
                        xopt = xacc.clone();
                        xidxopt = xidxacc.clone();
                        c1opt = c1acc.clone();
                        c2opt = c2acc.clone();
                        axopt = axacc.clone();
                    }

                } else {
                    fcur = facc;
                    xcur = xacc.clone();
                    xidxcur = xidxacc.clone();
                    c1cur = c1acc.clone();
                    c2cur = c2acc.clone();
                    axcur = axacc.clone();
                    k = k + 1;
                }

                temperature = temperature*cooling;
            }
            count++;
            LOGGER.info("Restarting. best=" + fopt + " count=" + count);
        } while (count < MAX_RESTART_WITHOUT_IMPROVEMENTS);

        double end = System.nanoTime();
        double time = (end - start) / 1e6;
        int[] periods = new int[n];
        int[] supermedians = new int[n];
        for (int i=0; i<n; i++)
            supermedians[i] = (c1opt[i] == i) ? i : Solution.NO_SUPERMEDIAN;

        LOGGER.info("Completed! Solution cost=" + fopt + "\n");

        return new Solution(periods, axopt, supermedians, fopt, time);
    }

    private boolean accept(double opt, double cur, double temperature) {
        double prob = Math.exp(-(cur - opt)/temperature);
        return opt != cur && random.nextDouble() < prob;
    }

    private double getInitialTemperature(double obj) {
        double w = 0.05;
        return w*obj / Math.log(2);
    }

    // arr contains numbers 0 to n
    private int[] getIndexes(int[] arr) {
        int[] idxs = new int[arr.length];
        for (int i=0; i<arr.length; i++) {
            idxs[arr[i]] = i;
        }
        return idxs;
    }

    private int[][] getClosestMedians(int[] x, int n, int p, float[][] d) {
        int[][] c = new int[2][n];

        // for each location
        for (int i=0; i<n; i++) {
            // initialize the 2 closest distances from medians
            float firstMin = Float.MAX_VALUE, secondMin = Float.MAX_VALUE;
            // for each median in x
            for (int j=0; j<p; j++) {
                // get distance from location
                float dist = d[i][x[j]];

                // if it's less than firstMin update both values and indexes
                if (dist < firstMin) {
                    secondMin = firstMin;
                    firstMin = dist;
                    c[1][i] = c[0][i];
                    c[0][i] = x[j];
                } else if (dist < secondMin) {
                    // otherwise if it's less than secondMin update only second indexes
                    secondMin = dist;
                    c[1][i] = x[j];
                }
            }
        }

        return c;
    }

    private double computeObjectiveFunction(int[] c1, int n, float[][] d, double alpha, double avg) {
        double w = 0;

        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<n; i++) {
            w += d[i][c1[i]];
            int c = counts.getOrDefault(c1[i], 0);
            counts.put(c1[i], c + 1);
        }

        if (counts.values().size() == 0)
            return w;

        for (int c : counts.values())
            w += alpha * Math.abs(c - avg);

        return w;
    }
}
