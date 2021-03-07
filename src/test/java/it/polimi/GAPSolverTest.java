package it.polimi;

import it.polimi.algorithm.capacitatedpmedian.GAPSolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class GAPSolverTest {

    @Test
    public void random() {
        Random random = new Random();
        int n = 200;
        int m = 10;
        int Q = 400;

        int[] c = new int[m];
        Arrays.fill(c, Q);

        int[][] s = new int[n][m];
        for (int i=0; i<n; i++)
            Arrays.fill(s[i], random.nextInt(Q / 10));

        double maxP = 200;
        double[][] p = new double[n][m];
        for (int i=0; i<n; i++) {
            Arrays.fill(p[i], random.nextInt((int) maxP));
        }

        int[][] ex = GAPSolver.exact(n, m, c, s, p);
        double exCost = getCost(ex, p);

        int[][] he = GAPSolver.heuristic(n, m, c, s, p);
        double heCost = getCost(he, p);

        System.out.println("Exact: " + exCost);
        System.out.println("Heuristic: " + heCost);
    }

    private double getCost(int[][] x, double[][] p) {
        double f = 0.;
        for (int i=0; i<x.length; i++) {
            for (int j=0; j<x[i].length; j++) {
                f += x[i][j] * p[i][j];
            }
        }
        return f;
    }
}
