package it.polimi.algorithm.capacitatedpmedian;

import it.polimi.distances.Distance;
import it.polimi.domain.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CapacitatedPMedianProblem {
    private final List<Service> services;

    private final int n;
    private final int p;
    private final int Q;
    private final float[][] c;
    private final int[] q;
    private int kmax;
    private int r;

    public CapacitatedPMedianProblem(List<Service> services, int capacity, int numMedians, Distance distance) {
        this.services = services;
        this.n = services.size();
        this.p = numMedians;
        this.Q = capacity;
        this.c = distance.getDistancesMatrix();
        this.q = services.stream().mapToInt(Service::getDemand).toArray();
        this.kmax = p;
        this.r = (int) Math.ceil((double) p / 5);
    }

    public CapacitatedPMedianProblem(int n, int p, float[][] c, int Q, int[] q) {
        this.services = null;
        this.n = n;
        this.p = p;
        this.c = c;
        this.Q = Q;
        this.q = q;
        this.kmax = p;
        this.r = (int) Math.ceil((double) p / 5);
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

    public float[][] getC() {
        return c;
    }

    public int getQ() {
        return Q;
    }

    public int[] getQs() {
        return q;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getKmax() {
        return kmax;
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
}
