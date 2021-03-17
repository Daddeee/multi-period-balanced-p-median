package it.polimi.algorithm.balancedpmedian;

import it.polimi.util.Pair;
import it.polimi.util.Triple;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BalancedFastInterchange {

    private int n;
    private int p;
    private float[][] d;
    private double alpha;
    private double avg;

    public BalancedFastInterchange(final int n, final int p, final float[][] d, final double alpha, final double avg) {
        this.n = n;
        this.p = p;
        this.d = d;
        this.alpha = alpha;
        this.avg = avg;
    }

    public double fastInterchange(int[] xopt, int[] xidx, int[] ax, int[] c1, int[] c2, double fopt) {
        while(true) {
            // find optimal goin and gout
            double wopt = Float.MAX_VALUE;
            int goinopt = -1, gooutopt = -1;
            int[] axopt = new int[0];
            for (int i=p; i < n; i++) {
                int goin = xopt[i];
                Triple<Integer, Double, int[]> triple = move(xopt, xidx, c1, c2, goin, wopt);
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
            if (wopt - fopt >= 0) {
                System.arraycopy(axopt, 0, ax, 0, axopt.length);
                return fopt;
            }

            // update obj function
            fopt = wopt;

            // swap optimal goin and goout
            int outidx = xidx[gooutopt], inidx = xidx[goinopt];
            xopt[outidx] = goinopt;
            xopt[inidx] = gooutopt;
            xidx[goinopt] = outidx;
            xidx[gooutopt] = inidx;

            update(xopt, c1, c2, goinopt, gooutopt);
        }
    }

    public Triple<Integer, Double, int[]> move(int[] x, int[] xidx, int[] c1, int[] c2, int goin, double fopt) {
        int[][] nc1 = new int[p][n];
        int[][] counts = new int[p][p];
        int[] goincounts = new int[p];

        double[] fs = new double[p];
        for (int j=0; j<p; j++) {
            fs[j] = 0;
            for (int i=0; i<n; i++) {
                nc1[j][i] = c1[i];
                counts[j][xidx[c1[i]]] += 1;
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

        double[] lbs = fs.clone();

        for (int j=0; j<p; j++) {
            for (int k=0; k<p; k++) {
                fs[j] += alpha * Math.abs(counts[j][k] - avg);
            }
            fs[j] += alpha * Math.abs(goincounts[j] - avg);
        }

        for (int i=0; i<p; i++) {
            if (lbs[i] > fopt) continue;
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
        }

        int bestGoout = -1;
        int[] bestAx = null;
        double bestZ = Double.POSITIVE_INFINITY;

        for (int i=0; i<p; i++) {
            if (lbs[i] > fopt) continue;
            int[] ax = nc1[i];
            double z = 0.;

            for (int j=0; j<n; j++)
                z += d[j][nc1[i][j]];

            for (int j=0; j<p; j++) {
                if (j == i) continue;
                z += alpha * Math.abs(counts[i][j] - avg);
            }

            z += alpha * Math.abs(goincounts[i] - avg);

            if (z < bestZ) {
                bestGoout = i;
                bestZ = z;
                bestAx = ax;
            }
        }

        if (bestGoout == -1) return null;

        return new Triple<>(x[bestGoout], bestZ, bestAx);
    }

    public void update(int[]x, int[] c1, int[] c2, int goin, int goout) {
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
                    c2[i] = searchSecondMedian(i, x, c1);
                }
            } else {
                if (d[i][goin] < d[i][c1[i]]) {
                    c2[i] = c1[i];
                    c1[i] = goin;
                } else if (d[i][goin] < d[i][c2[i]]) {
                    c2[i] = goin;
                } else if (c2[i] == goout) {
                    // and another c2[i] is searched.
                    c2[i] = searchSecondMedian(i,x, c1);
                }
            }
        }
    }

    private int searchSecondMedian(int i, int[] x, int[] c1) {
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
}
