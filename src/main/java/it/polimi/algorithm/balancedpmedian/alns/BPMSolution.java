package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.alns.Solution;

import java.util.*;
import java.util.stream.Collectors;

public class BPMSolution implements Solution {
    private static final int NO_TABU = -1;
    private static final double UNASSIGNED_POINT_PENALTY = 1e6;
    private static final double UNASSIGNED_MEDIAN_PENALTY = 1e6;

    private Map<Integer, PriorityQueue<Integer>> medianPoints;
    private Map<Integer, Integer> pointsMedian;
    private Map<Integer, Integer> unassignedPointsTabu;

    private double cost;
    private double elapsedTime;

    public BPMSolution(Map<Integer, PriorityQueue<Integer>> medianPoints, Map<Integer, Integer> pointsMedian,
                       Map<Integer, Integer> unassignedPointsTabu) {
        this.medianPoints = medianPoints;
        this.pointsMedian = pointsMedian;
        this.unassignedPointsTabu = unassignedPointsTabu;
    }

    public Map<Integer, PriorityQueue<Integer>> getMedianPoints() {
        return medianPoints;
    }

    public Map<Integer, Integer> getPointsMedian() {
        return pointsMedian;
    }

    public Map<Integer, Integer> getUnassignedPointsTabu() {
        return unassignedPointsTabu;
    }

    @Override
    public double getCost() {
        return cost;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public double computeCost(BPMProblem prob) {
        double obj = 0.;
        for (Map.Entry<Integer, Integer> e : pointsMedian.entrySet())
            obj += prob.getC()[e.getKey()][e.getValue()];
        for (Map.Entry<Integer, PriorityQueue<Integer>> e : medianPoints.entrySet())
            obj += prob.getAlpha() * Math.abs(e.getValue().size() - prob.getAvg());
        obj += unassignedPointsTabu.keySet().size() * UNASSIGNED_POINT_PENALTY;
        obj += Math.max(prob.getP() - medianPoints.keySet().size(), 0) * UNASSIGNED_MEDIAN_PENALTY;
        return obj;
    }

    public void unassign(int point, BPMProblem problem) {
        if (!pointsMedian.containsKey(point))
            return;
        int median = pointsMedian.get(point);
        if (medianPoints.containsKey(median)) {
            medianPoints.get(median).remove(point);
            unassignedPointsTabu.put(point, median);
        }
        pointsMedian.remove(point);
        cost -= problem.getC()[point][median];
        cost -= problem.getAlpha() * Math.abs(medianPoints.get(median).size() + 1 - problem.getAvg());
        cost += problem.getAlpha() * Math.abs(medianPoints.get(median).size() - problem.getAvg());
        cost += UNASSIGNED_POINT_PENALTY;
    }

    public void assign(int point, int median, BPMProblem problem) {
        cost += problem.getC()[point][median];
        cost += problem.getAlpha() * Math.abs(medianPoints.get(median).size() + 1 - problem.getAvg());
        cost -= problem.getAlpha() * Math.abs(medianPoints.get(median).size() - problem.getAvg());
        cost -= UNASSIGNED_POINT_PENALTY;
        unassignedPointsTabu.remove(point);
        pointsMedian.put(point, median);
        medianPoints.get(median).add(point);
    }

    public void remove(int median, BPMProblem problem) {
        List<Integer> vacantPoints = new ArrayList<>(medianPoints.get(median));
        for (int point : vacantPoints) {
            int bestResMedian = -1;
            float bestDist = Float.MAX_VALUE;
            for (int resMedian : medianPoints.keySet()) {
                if (resMedian == median) continue;
                float dist = problem.getC()[point][resMedian];
                if (dist < bestDist) {
                    bestDist = dist;
                    bestResMedian = resMedian;
                }
            }
            unassign(point, problem);
            assign(point, bestResMedian, problem);
        }
        cost -= problem.getAlpha() * Math.abs(medianPoints.get(median).size() - problem.getAvg());
        cost += UNASSIGNED_MEDIAN_PENALTY;
        medianPoints.remove(median);
        cost += UNASSIGNED_POINT_PENALTY;
        unassignedPointsTabu.put(median, NO_TABU);
        int bestResMedian = -1;
        float bestDist = Float.MAX_VALUE;
        for (int resMedian : medianPoints.keySet()) {
            if (resMedian == median) continue;
            float dist = problem.getC()[median][resMedian];
            if (dist < bestDist) {
                bestDist = dist;
                bestResMedian = resMedian;
            }
        }
        assign(median, bestResMedian, problem);
    }

    public void insert(int newMedian, BPMProblem problem) {
        unassign(newMedian, problem);
        unassignedPointsTabu.remove(newMedian);
        PriorityQueue<Integer> pointsQueue = new PriorityQueue<>(new PointComparator(newMedian, problem.getC()));
        medianPoints.put(newMedian, pointsQueue);
        cost -= UNASSIGNED_MEDIAN_PENALTY;
        cost -= UNASSIGNED_POINT_PENALTY; // because i'm using unassign
        cost += problem.getAlpha() * Math.abs(pointsQueue.size() - problem.getAvg());

        Set<Integer> points = new HashSet<>(pointsMedian.keySet());
        for (int p : points) {
            int m = pointsMedian.get(p);
            float oldDist = problem.getC()[p][m], newDist = problem.getC()[p][newMedian];
            if (newDist < oldDist) {
                unassign(p, problem);
                assign(p, newMedian, problem);
            }
        }
    }

    private static class PointComparator implements Comparator<Integer> {
        private int median;
        private float[][] c;

        public PointComparator(int median, float[][] c) {
            this.median = median;
            this.c = c;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            return -Float.compare(c[i1][median], c[i2][median]);
        }
    }

    public static BPMSolution empty(List<Integer> unassigned) {
        return new BPMSolution(new HashMap<>(), new HashMap<>(), unassigned.stream().collect(Collectors.toMap(i->i, i-> NO_TABU)));
    }
}
