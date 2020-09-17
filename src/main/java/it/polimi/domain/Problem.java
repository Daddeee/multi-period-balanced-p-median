package it.polimi.domain;

import it.polimi.distances.Distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Problem {
    private final List<Service> services;

    private final int n;
    private final int p;
    private final int m;
    private final float[][] c;
    private final int[] r;
    private final int[] d;
    private double avg;
    private double alpha;
    private int kmax;

    public Problem(List<Service> services, int numPeriods, int numMedians, Distance distance) {
        this.services = services;
        this.n = services.size();
        this.p = numMedians;
        this.m = numPeriods;
        this.c = distance.getDistancesMatrix();
        this.r = services.stream().mapToInt(Service::getReleaseDate).toArray();
        this.d = services.stream().mapToInt(Service::getDueDate).toArray();
        this.avg = (double) this.n / (this.p * this.m);
        this.alpha = computeAlpha();
        this.kmax = getKMax();
    }

    public Problem(int n, int p, float[][] c) {
        this.services = null;
        this.n = n;
        this.p = p;
        this.c = c;
        this.m = 1;
        this.r = new int[n];
        this.d = new int[n];
        Arrays.fill(r, 0);
        Arrays.fill(d, 0);
        this.avg = (double) n/p;
        this.alpha = computeAlpha();
        this.kmax = p;
    }

    public List<Service> getServices() {
        return services;
    }

    public int getN() {
        return n;
    }

    public int getP() {
        return p;
    }

    public int getM() {
        return m;
    }

    public float[][] getC() {
        return c;
    }

    public int[] getR() {
        return r;
    }

    public int[] getD() {
        return d;
    }

    public double getAvg() {
        return avg;
    }

    public double getAlpha() {
        return alpha;
    }

    public int getKmax() {
        return kmax;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setKmax(int kmax) {
        this.kmax = kmax;
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

    private int getKMax() {
        int count = 0;
        for (int i=0; i<n; i++)
            count += (d[i] - r[i] == 0) ? 0 : 1;
        return Math.max(1, count);
    }
}
