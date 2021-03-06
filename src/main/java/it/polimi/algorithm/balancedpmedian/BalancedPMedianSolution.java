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
    private int[] ccounts;

    private double f;
    private double lb1;

    public BalancedPMedianSolution(int[] x, int[] xidx, int[] ax, int[] c1, int[] c2, int[] ccounts, double f, double lb1) {
        this.x = x;
        this.xidx = xidx;
        this.ax = ax;
        this.c1 = c1;
        this.c2 = c2;
        this.ccounts = ccounts;
        this.f = f;
        this.lb1 = lb1;
    }

    public BalancedPMedianSolution(int n, int p, float[][] d, double alpha, double avg, Random random){
        this.x = Rand.permutateRange(0, n, random);
        this.xidx = getIndices(x);
        int[][] c = getClosestMedians(x, n, p, d);
        this.c1 = c[0];
        this.c2 = c[1];
        this.ccounts = getCounts(p, c1, xidx);
        this.ax = c1.clone();
        this.f = objectiveFunction(n, p, d, alpha, avg);
        this.lb1 = computeLb1(n, p, d, alpha, avg);
    }

    public BalancedPMedianSolution clone() {
        return new BalancedPMedianSolution(x.clone(), xidx.clone(), ax.clone(), c1.clone(), c2.clone(), ccounts.clone(), f, lb1);
    }

    public void swap(int goin, int goout, int n, int p, float[][] d, double alpha, double avg) {
        int inidx = xidx[goin];
        int outidx = xidx[goout];
        x[outidx] = goin;
        x[inidx] = goout;
        xidx[goin] = outidx;
        xidx[goout] = inidx;
        update(n, p, d, alpha, avg, goin, goout);
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

    public double objectiveFunction(int n, int p, float[][] d, double alpha, double avg) {
        double w = 0;
        int[] counts = new int[p];
        for (int i=0; i<n; i++) {
            w += d[i][ax[i]];
            counts[xidx[ax[i]]] += 1;
        }
        for (int c : counts) {
            w += alpha * Math.abs(c - avg);
        }
        return w;
    }

    public double computeLb1(int n, int p, float[][] d, double alpha, double avg) {
        double w = 0;
        for (int i=0; i<n; i++)
            w += d[i][c1[i]];
        for (int i=0; i<p; i++)
            w += alpha * Math.abs(ccounts[i] - avg);
        return w;
    }

    public void update(int n, int p, float[][] d, double alpha, double avg, int goin, int goout) {
        // updates c1, c2 and counts for each location by replacing goout with goin
        for (int i=0; i<n; i++) {
            // if goout is current median
            if (c1[i] == goout) {
                this.lb1 -= d[i][c1[i]];
                // if goin is closer to i than the second median c2[i]
                if (d[i][goin] <= d[i][c2[i]]) {
                    // it becomes the new median
                    c1[i] = goin;
                } else {
                    this.lb1 -= alpha * Math.abs(ccounts[xidx[goin]] - avg);
                    ccounts[xidx[goin]] -= 1;
                    this.lb1 += alpha * Math.abs(ccounts[xidx[goin]] - avg);

                    // otherwise c2[i] becomes the new median
                    c1[i] = c2[i];
                    // and another c2[i] is searched.
                    c2[i] = searchSecondMedian(i, x, c1, p, d);

                    this.lb1 -= alpha * Math.abs(ccounts[xidx[c1[i]]] - avg);
                    ccounts[xidx[c1[i]]] += 1;
                    this.lb1 += alpha * Math.abs(ccounts[xidx[c1[i]]] - avg);
                }
                this.lb1 += d[i][c1[i]];
            } else {
                if (d[i][goin] < d[i][c1[i]]) {
                    this.lb1 -= alpha * Math.abs(ccounts[xidx[c1[i]]] - avg);
                    ccounts[xidx[c1[i]]] -= 1;
                    this.lb1 += alpha * Math.abs(ccounts[xidx[c1[i]]] - avg);

                    this.lb1 -= d[i][c1[i]];
                    c2[i] = c1[i];
                    c1[i] = goin;
                    this.lb1 += d[i][c1[i]];

                    this.lb1 -= alpha * Math.abs(ccounts[xidx[goin]] - avg);
                    ccounts[xidx[goin]] += 1;
                    this.lb1 += alpha * Math.abs(ccounts[xidx[goin]] - avg);
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

    private int[] getCounts(int p, int[] ax, int[] xidx) {
        int[] counts = new int[p];
        for (int a : ax)
            counts[xidx[a]] += 1;
        return counts;
    }
}
