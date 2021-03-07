package it.polimi.algorithm.capacitatedpmedian;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class CapacitatedPMedianSolution {

    private Set<Integer> medians;
    private int[] a;
    private int[] x;
    private int[] xidx;
    private int[] c1;
    private int[] c2;
    private double lb1;
    private double lb2;
    private double lb3;
    private double f;
    private double elapsedTime;
    double[][] warmStartX;

    public CapacitatedPMedianSolution(Set<Integer> medians, int[] x, CapacitatedPMedianProblem problem) {
        this.medians = medians;
        this.x = x;
        this.xidx = getIndexes(x);
        int[][] c = getClosestMedians(x, problem.getN(), problem.getP(), problem.getC());
        this.c1 = c[0];
        this.c2 = c[1];
        this.a = c[0].clone();
        this.computeLB1(problem);
        this.warmStartX = null;
    }

    public CapacitatedPMedianSolution(Set<Integer> medians, int[] a, int[] x, CapacitatedPMedianProblem problem) {
        this.medians = medians;
        this.a = a;
        this.x = x;
        this.xidx = getIndexes(x);
        int[][] c = getClosestMedians(x, problem.getN(), problem.getP(), problem.getC());
        this.c1 = c[0];
        this.c2 = c[1];
        this.computeLB1(problem);
        this.warmStartX = null;
    }

    public CapacitatedPMedianSolution(Set<Integer> medians, int[] a, int[] x, int[] c1, int[] c2, double lb1, double lb2,
                                      double lb3, double f, double[][] warmStartX) {
        this.medians = medians;
        this.a = a;
        this.x = x;
        this.xidx = getIndexes(x);
        this.c1 = c1;
        this.c2 = c2;
        this.lb1 = lb1;
        this.lb2 = lb2;
        this.lb3 = lb3;
        this.f = f;
        this.warmStartX = warmStartX;
    }

    public void computeF(CapacitatedPMedianProblem prob) {
        double f = 0.;
        for (int i=0; i<a.length; i++)
            f += prob.getC()[i][a[i]];
        this.f = f;
    }

    public void computeLB1(CapacitatedPMedianProblem prob) {
        double lb1 = 0.;
        for (int i=0; i<a.length; i++)
            lb1 += prob.getC()[i][c1[i]];
        this.lb1 = lb1;
    }

    public void computeLB2(CapacitatedPMedianProblem prob) {
        int[] c = new int[prob.getP()];
        Arrays.fill(c, prob.getQ());
        int[][] s = new int[prob.getN()][prob.getP()];
        for (int i=0; i< prob.getN(); i++)
            Arrays.fill(s[i], prob.getQs()[i]);
        double[][] p = new double[prob.getN()][prob.getP()];
        for (int i=0; i<prob.getN(); i++) {
            for (int j=0; j< prob.getP(); j++) {
                p[i][j] = prob.getC()[i][j];
            }
        }
        int[][] he = GAPSolver.heuristic(prob.getN(), prob.getP(), c, s, p);
        double lb2 = 0.;
        for (int i=0; i<he.length; i++) {
            for (int j=0; j<he[i].length; j++) {
                lb2 += he[i][j] * p[i][j];
            }
        }
        this.lb2 = lb2;
    }

    public boolean isBetterLB(CapacitatedPMedianSolution a) {
        if (this.lb1 > a.getLb1()) return false;
        if (this.lb2 > a.getLb2()) return  false;
        return true;
    }

    public CapacitatedPMedianSolution clone() {
        return new CapacitatedPMedianSolution(new HashSet<>(medians), a.clone(), x.clone(), c1.clone(), c2.clone(), lb1,
                lb2, lb3, f, warmStartX);
    }

    public void update(int goout, int goin, CapacitatedPMedianProblem prob) {
        int n = prob.getN();
        int p = prob.getP();
        float[][] d = prob.getC();

        medians.remove(goout);
        medians.add(goin);

        int outidx = xidx[goout], inidx = xidx[goin];
        x[outidx] = goin;
        x[inidx] = goout;
        xidx[goin] = outidx;
        xidx[goout] = inidx;

        for (int i=0; i<n; i++) {
            if (c1[i] == goout) {
                lb1 -= d[i][goout];
                if (d[i][goin] <= d[i][c2[i]]) {
                    c1[i] = goin;
                } else {
                    c1[i] = c2[i];
                    c2[i] = searchSecondMedian(i, x, c1, p, d);
                }
                lb1 += d[i][c1[i]];
            } else {
                if (d[i][goin] < d[i][c1[i]]) {
                    lb1 -= d[i][c1[i]];
                    c2[i] = c1[i];
                    c1[i] = goin;
                    lb1 += d[i][goin];
                } else if (d[i][goin] < d[i][c2[i]]) {
                    c2[i] = goin;
                } else if (c2[i] == goout) {
                    c2[i] = searchSecondMedian(i, x, c1, p, d);
                }
            }
        }
    }

    private int searchSecondMedian(int i, int[] x, int[] c1, int p, float[][] d) {
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

    public Set<Integer> getMedians() {
        return medians;
    }

    public int[] getA() {
        return a;
    }

    public int[] getX() {
        return x;
    }

    public int[] getXidx() {
        return xidx;
    }

    public int[] getC1() {
        return c1;
    }

    public int[] getC2() {
        return c2;
    }

    public double getLb1() {
        return lb1;
    }

    public double getLb2() {
        return lb2;
    }

    public double getLb3() {
        return lb3;
    }

    public double getF() {
        return f;
    }

    public void setLb1(double lb1) {
        this.lb1 = lb1;
    }

    public void setLb2(double lb2) {
        this.lb2 = lb2;
    }

    public void setLb3(double lb3) {
        this.lb3 = lb3;
    }

    public void setF(double f) {
        this.f = f;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapacitatedPMedianSolution that = (CapacitatedPMedianSolution) o;
        return Objects.equals(medians, that.medians);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medians);
    }

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
}
