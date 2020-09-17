package it.polimi.algorithm.balancedpmedian.alns;

import it.polimi.algorithm.alns.Problem;
import it.polimi.distances.Distance;
import it.polimi.domain.Service;

import java.util.List;

public class BPMProblem implements Problem {
    private final int n;
    private final int p;
    private final float[][] c;
    private double avg;
    private double alpha;

    public BPMProblem(List<Service> services, int numMedians, Distance distance) {
        this.n = services.size();
        this.p = numMedians;
        this.c = distance.getDistancesMatrix();
        this.avg = (double) this.n / this.p;
        this.alpha = computeAlpha();
    }

    public BPMProblem(int n, int p, float[][] c) {
        this.n = n;
        this.p = p;
        this.c = c;
        this.avg = (double) n/p;
        this.alpha = computeAlpha();
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

    public double getAvg() {
        return avg;
    }

    public double getAlpha() {
        return alpha;
    }

    private double computeAlpha() {
        double distSum = 0f;
        int count = 0;
        for (int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    distSum += c[i][j];
                    count += 1;
                }
            }
        }
        return 0.2d * distSum / count;
    }
}
