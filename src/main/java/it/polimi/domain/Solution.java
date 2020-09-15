package it.polimi.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Solution {
    public static final int NO_SUPERMEDIAN = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger(Solution.class);
    private final int[] periods;
    private final int[] medians;
    private final int[] supermedians;
    private double objective;
    private double elapsedTime;

    public Solution(int[] periods, int[] medians, int[] supermedians, double objective, double elapsedTime) {
        if (periods.length != medians.length)
            throw new IllegalArgumentException("Wrong sizes for periods and medians: (" + periods.length + "," + medians.length + ")");
        this.periods = periods;
        this.medians = medians;
        this.supermedians = supermedians;
        this.objective = objective;
        this.elapsedTime = elapsedTime;
    }

    public int[] getPeriods() {
        return periods;
    }

    public int[] getMedians() {
        return medians;
    }

    public int[] getSupermedians() {
        return supermedians;
    }

    public double getObjective() {
        return objective;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setObjective(double objective) {
        this.objective = objective;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Solution clone() {
        return new Solution(periods.clone(), medians.clone(), supermedians.clone(), objective, elapsedTime);
    }

    public int getPeriod(int point) {
        return periods[point];
    }

    public void setPeriod(int point, int newPeriod) {
        periods[point] = newPeriod;
    }

    public int getMedian(int point) {
        return medians[point];
    }

    public void setMedian(int point, int newMedian) {
        medians[point] = newMedian;
    }

    public int getSuperMedian(int point) {
        return supermedians[point];
    }

    public void setSupermedian(int point, int newSuperMedian) {
        supermedians[point] = newSuperMedian;
    }

    public List<Integer> getPointsInPeriod(int period) {
        return IntStream.range(0, periods.length).filter(i -> periods[i] == period).boxed().collect(Collectors.toList());
    }

    public Map<Integer, Integer> getMedianCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i<medians.length; i++) {
            int c = counts.getOrDefault(medians[i], 0);
            counts.put(medians[i], c+1);
        }
        return counts;
    }
}
