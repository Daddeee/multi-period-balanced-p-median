package it.polimi.algorithm.balancedpmedian;

import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.util.Pair;
import it.polimi.util.Rand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BalancedPMedianVNS implements Solver {
    protected final int MAX_SOLUTION_CHANGES = 20;
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

    public Solution run(Problem prob) {
        double start = System.nanoTime();
        int n = prob.getN();
        int p = prob.getP();
        int kmax = prob.getKmax();
        float[][] d = prob.getC();
        double avg = prob.getAvg();
        double alpha = prob.getAlpha();

        // optimal values
        int[] xopt = Rand.permutateRange(0, n);
        int[] xidxopt = getIndexes(xopt);
        int[][] c = getClosestMedians(xopt, n, p, d);
        int[] c1 = c[0];
        int[] c2 = c[1];
        int[] a = c1.clone();
        float fopt = computeObjectiveFunction(c1, n, d);

        // current values
        int[] xcur = xopt.clone();
        int[] xidx = xidxopt.clone();
        int[] c1cur = c1.clone();
        int[] c2cur = c2.clone();
        int[] acur = c1cur.clone();
        float fcur = fopt;

        int k = 1;
        int changes = 0;
        while (k < kmax) {
            // shaking
            for (int j = 1; j <= k; j++) {
                // sample random median to be inserted
                int goin = random.nextInt(n - p) + p;

                // find best median to remove
                Pair<Integer, Float> pair = move(xcur, xidx, c1cur, c2cur, goin, n, p, d);
                int goout = pair.getFirst();
                float w = pair.getSecond();

                // update obj function
                fcur = fcur + w;

                // update xcur and xidx
                int outidx = xidx[goout], inidx = xidx[goin];
                xcur[outidx] = goin;
                xcur[inidx] = goout;
                xidx[goin] = outidx;
                xidx[goout] = inidx;

                // update c1 and c2
                update(xcur, c1cur, c2cur, goin, goout, n, p, d);
            }

            acur = c1cur.clone();

            fcur = localSearch(xcur, xidx, acur, c1cur, c2cur, fcur, n, p, d, alpha, avg);

            // Move or not
            if (fcur < fopt) {
                fopt = fcur;
                xopt = xcur.clone();
                xidxopt = xidx.clone();
                c1 = c1cur.clone();
                c2 = c2cur.clone();
                k = 1;

                //changes++;
                //if (changes >= MAX_SOLUTION_CHANGES) {
                //    LOGGER.info("Max solution changes limit hit.");
                //    break;
                //}
            } else {
                fcur = fopt;
                xcur = xopt.clone();
                xidx = xidxopt.clone();
                c1cur = c1.clone();
                c2cur = c2.clone();
                k = k + 1;
            }
        }
        double end = System.nanoTime();
        double time = (end - start) / 1e6;
        int[] periods = new int[n];
        int[] supermedians = new int[n];
        for (int i=0; i<n; i++)
            supermedians[i] = (c1[i] == i) ? i : Solution.NO_SUPERMEDIAN;
        return new Solution(periods, c1, supermedians, fopt, time);
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

    protected float computeObjectiveFunction(int[] c1, int n, float[][] d) {
        float w = 0;
        for (int i=0; i<n; i++) {
            w += d[i][c1[i]];
        }
        return w;
    }

    protected Pair<Integer, Float> move(int[] x, int[] xidx, int[] c1, int[] c2, int goin, int n, int p, float[][] d) {
        // w is th change in the obj function obtained with the best interchange
        float w = 0;
        // v[j] is the change in the objective function obtained by deleting a facility currently in the solution,
        float[] v = new float[p];

        for (int i=0; i<n; i++) {
            // if the location is closer to the new median instead of its c1[i]
            if (d[i][goin] < d[i][c1[i]]) {
                // goin become the new median of i replacing of c1[i]. Update the change in obj
                w = w + d[i][goin] - d[i][c1[i]];
            } else {
                // calculate the cost of deleting c1[i] from solution
                v[xidx[c1[i]]] = v[xidx[c1[i]]] + Math.min(d[i][goin], d[i][c2[i]]) - d[i][c1[i]];
            }
        }

        float g = Float.MAX_VALUE;
        int goout = -1;
        for (int i=0; i<p; i++) {
            if (v[i] < g) {
                g = v[i];
                goout = x[i];
            }
        }

        w = w + g;

        return new Pair<>(goout, w);
    }

    private void update(int[]x,  int[] c1, int[] c2, int goin, int goout, int n, int p, float[][] d) {
        // updates c1 and c2 for each location by replacing goout with goin
        for (int i=0; i<n; i++) {
            // if goout is current median
            if (c1[i] == goout) {
                // if goin is closer to i than the second median c2[i]
                if (d[i][goin] <= d[i][c2[i]]) {
                    // it becomes the new median
                    c1[i] = goin;
                } else {
                    // otherwise c2[i] becomes the new median
                    c1[i] = c2[i];

                    // and another c2[i] is searched.
                    c2[i] = searchSecondMedian(i, x, c1, p, d);
                }
            } else {
                if (d[i][goin] < d[i][c1[i]]) {
                    c2[i] = c1[i];
                    c1[i] = goin;
                } else if (d[i][goin] < d[i][c2[i]]) {
                    c2[i] = goin;
                } else if (c2[i] == goout) {
                    // and another c2[i] is searched.
                    c2[i] = searchSecondMedian(i,x, c1, p, d);
                }
            }
        }
    }

    private int searchSecondMedian(int i, int[] x, int[] c1, int p, float[][] d) {
        //  TODO: maybe use max heap
        float newMin = Float.MAX_VALUE;
        int secondMedian = -1;
        for (int j=0; j<p; j++) {
            if (x[j] != c1[i] && d[i][x[j]] < newMin) {
                newMin = d[i][x[j]];
                secondMedian = x[j];
            }
        }
        return secondMedian;
    }

    private float localSearch(int[] xopt, int[] xidx, int[] a, int[] c1, int[] c2, float fopt, int n, int p, float[][] d,
                              double alpha, double avg) {

        float f = fopt;
        // pair <node,candidate_repl>
        // O(NlogN)
        List<Pair<Integer, Integer>> pairs = Arrays.stream(xopt, p, n).boxed()
                .map(i -> new Pair<>(i, a[i] == c1[i] ? c2[i] : c1[i]))
                .sorted(Comparator.comparingDouble(pr -> d[pr.getFirst()][pr.getSecond()] - d[pr.getFirst()][a[pr.getFirst()]]))
                .collect(Collectors.toList());

        // O(N)
        Map<Integer, Long> counts = Arrays.stream(a).boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (Pair<Integer, Integer> pair : pairs) {
            int node = pair.getFirst();
            int cand = pair.getSecond();

            if (counts.get(cand) > avg) continue;
            if (counts.get(a[node]) < avg) continue;

            long na = counts.get(a[node]);
            long nc = counts.get(cand);

            double delta = d[node][cand] + alpha * Math.abs(na - 1 - avg) + alpha * Math.abs(nc + 1 - avg);
            delta -= d[node][a[node]] + alpha * Math.abs(na - avg) + alpha * Math.abs(nc - avg);

            if (delta < 0) {
                a[node] = cand;
                f += delta;
                counts.put(a[node], na - 1);
                counts.put(cand, nc + 1);
            } else {
                break;
            }
        }

        return f;
    }
}
