package it.polimi.algorithm.balancedpmedian;

import it.polimi.util.Rand;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BalancedPMedianSolution {
    private int[] x;
    private int[] xidx;
    private int[] ax;
    private int[] c1;
    private int[] c2;

    private double f;
    private double lb1;

    public BalancedPMedianSolution(int[] x, int[] xidx, int[] ax, int[] c1, int[] c2, double f, double lb1) {
        this.x = x;
        this.xidx = xidx;
        this.ax = ax;
        this.c1 = c1;
        this.c2 = c2;
        this.f = f;
        this.lb1 = lb1;
    }

    public BalancedPMedianSolution(int n, int p, float[][] d, double alpha, double avg, Random random){
        this.x = Rand.permutateRange(0, n, random);
        this.xidx = getIndices(x);
        int[][] c = getClosestMedians(x, n, p, d);
        this.c1 = c[0];
        this.c2 = c[1];
        this.f = objectiveFunction(n, d, alpha, avg);

        this.ax = c1.clone();
    }

    public BalancedPMedianSolution clone() {
        return new BalancedPMedianSolution(x.clone(), xidx.clone(), ax.clone(), c1.clone(), c2.clone(), f, lb1);
    }

    public void swap(int goin, int goout, int n, int p, float[][] d, double alpha, double avg) {
        int inidx = xidx[goin];
        int outidx = xidx[goout];
        x[outidx] = goin;
        x[inidx] = goout;
        xidx[goin] = outidx;
        xidx[goout] = inidx;
        update(n, p, d, x, c1, c2, goin, goout);
    }

    public int[] getX() {
        return x;
    }

    public int[] getXidx() {
        return xidx;
    }

    public int[] getAx() {
        return ax;
    }

    public int[] getC1() {
        return c1;
    }

    public int[] getC2() {
        return c2;
    }

    public double getF() {
        return f;
    }

    public double getLb1() {
        return lb1;
    }

    public void setAx(int[] ax) {
        this.ax = ax;
    }

    public void setF(double f) {
        this.f = f;
    }

    public void setLb1(double lb1) {
        this.lb1 = lb1;
    }

    // arr contains numbers 0 to n
    private int[] getIndices(int[] arr) {
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

    public double objectiveFunction(int n, float[][] d, double alpha, double avg) {
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

    public void update(int n, int p, float[][] d, int[]x, int[] c1, int[] c2, int goin, int goout) {
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
                    c2[i] = searchSecondMedian(i, x, c1, p, d);
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
}
