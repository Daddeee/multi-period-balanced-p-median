package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.alns.Solution;

import java.util.*;

public class BalancedPMedianSolution implements Solution {
    private int[] assignment;
    private int[] closest1;
    private int[] closest2;
    private Set<Integer> medians;
    private Map<Integer, Integer> counts;

    private List<Integer> unassigned;
    private double objective;
    private double elapsedTime;

    public BalancedPMedianSolution(BalancedPMedianProblem problem, int[] assignment, Set<Integer> medians,
                                   Map<Integer, Integer> counts, double objective, double elapsedTime) {
        this.assignment = assignment;
        this.medians = medians;
        this.counts = counts;
        computeClosests(problem);
        this.unassigned = new ArrayList<>();
        this.objective = objective;
        this.elapsedTime = elapsedTime;
    }

    public BalancedPMedianSolution(BalancedPMedianProblem problem, int[] assignment, Set<Integer> medians,
                                   Map<Integer, Integer> counts) {
        this.assignment = assignment;
        this.medians = medians;
        this.counts = counts;
        computeClosests(problem);
        this.unassigned = new ArrayList<>();
    }

    @Override
    public double getCost() {
        return objective;
    }

    public void remove(int goout, BalancedPMedianProblem problem) {
        if (!medians.contains(goout)) throw new IllegalStateException("Attempting to remove a non-median.");
        medians.remove(goout);
        int count = counts.remove(goout);
        objective -= problem.getAlpha() * Math.abs(count - problem.getAvg());
        for (int i=0; i<problem.getN(); i++) {
            if (assignment[i] == goout) {
                assignment[i] = -1;
                unassigned.add(i);
                objective -= problem.getC()[i][goout];
            }
            if (closest1[i] == goout) {
                closest1[i] = closest2[i];
                closest2[i] = -1;
            }
            if (closest2[i] == goout) {
                closest2[i] = -1;
            }
        }
    }

    public void insert(int goin, BalancedPMedianProblem problem) {
        if (medians.contains(goin)) throw new IllegalStateException("Attempting to insert a median.");
        medians.add(goin);
        float[][] c = problem.getC();
        for (int i=0; i<problem.getN(); i++) {
            if (assignment[i] == -1) {
                if (c[i][goin] < c[i][closest1[i]]) {
                    assignment[i] = goin;
                    counts.put(goin, counts.getOrDefault(goin, 0) + 1);
                    closest2[i] = closest1[i];
                    closest1[i] = goin;
                } else {
                    assignment[i] = closest1[i];
                    counts.put(closest1[i], counts.getOrDefault(closest1[i], 0) + 1);
                    if (closest2[i] == -1) {
                        setSecondClosestMedian(i, c);
                    } else if (c[i][goin] < c[i][closest2[i]]) {
                        closest2[i] = goin;
                    }
                }
            }
        }
    }

    public int setSecondClosestMedian(int node, float[][] c) {
        for (int med : medians) {
            if (med == closest1[node]) continue;
            if (c[node][med] < c[node][closest2[node]]) closest2[node] = med;
        }
        return -1;
    }

    public int[] getAssignment() {
        return assignment;
    }

    public Set<Integer> getMedians() {
        return medians;
    }

    public List<Integer> getUnassigned() {
        return unassigned;
    }

    public double getObjective() {
        return objective;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setUnassigned(List<Integer> unassigned) {
        this.unassigned = unassigned;
    }

    public void setObjective(double objective) {
        this.objective = objective;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void computeClosests(BalancedPMedianProblem problem) {
        int n = problem.getN();
        int p = problem.getP();
        float[][] c = problem.getC();
        closest1 = new int[n];
        closest2 = new int[n];

        for (int i = 0; i< n; i++) {
            float firstMin = Float.MAX_VALUE, secondMin = Float.MAX_VALUE;
            // for each median in x
            for (int med : medians) {
                // get distance from location
                float dist = c[i][med];

                // if it's less than firstMin update both values and indexes
                if (dist < firstMin) {
                    secondMin = firstMin;
                    firstMin = dist;
                    closest2[i] = closest1[i];
                    closest1[i] = med;
                } else if (dist < secondMin) {
                    // otherwise if it's less than secondMin update only second indexes
                    secondMin = dist;
                    closest2[i] = med;
                }
            }
        }
    }

    public static double computeObjective(BalancedPMedianProblem p, BalancedPMedianSolution s) {
        double w = 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<p.getN(); i++) {
            int med = s.getAssignment()[i];
            w += p.getC()[i][med];
            counts.put(med, counts.getOrDefault(med, 0) + 1);
        }
        for (Integer val : counts.values()) {
            w += p.getAlpha() * Math.abs(val - p.getAvg());
        }
        return w;
    }
}
