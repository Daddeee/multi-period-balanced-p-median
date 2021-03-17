package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.alns.Problem;

public class BalancedPMedianProblem implements Problem {
    private int n;
    private int p;
    private float[][] c;
    private double alpha;
    private double avg;

    public BalancedPMedianProblem(int n, int p, float[][] c) {
        this.n = n;
        this.p = p;
        this.c = c;
        this.alpha = computeAlpha();
        this.avg = (double) n/p;
    }

    public int getN() {
        return n;
    }

    public int getP() {
        return p;
    }

    public float[][] getC() {
        return c;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getAvg() {
        return avg;
    }

    private float computeAlpha() {
        float distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += c[i][j];
                    count += 1;
                }
            }
        }
        return 0.2f * distSum / count;
    }
}
