package it.polimi;

import it.polimi.algorithm.capacitatedpmedian.GAPSolver;
import it.polimi.algorithm.capacitatedpmedian.KPSolver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class KPSolverTest {

    @Test
    public void wikipedia() {
        int n = 10;
        int W = 67;
        int[] w = new int[] { 23, 26, 20, 18, 32, 27, 29, 26, 30, 27 };
        double[] v = new double[] { 505, 352, 458, 220, 354, 414, 498, 545, 473, 543 };

        int[] ex = KPSolver.exact(n, v, w, W);
        assert ex != null;
        double exCost = Arrays.stream(ex).mapToDouble(i -> v[i]).sum();

        int[] he = KPSolver.fptas(n, v, w, W, 0.01);
        double heCost = Arrays.stream(he).mapToDouble(i -> v[i]).sum();

        System.out.println("Wikipedia exact: " + exCost);
        System.out.println("Wikipedia heuristic: " + heCost);
    }

    @Test
    public void random() {
        Random random = new Random();
        int n = 100;
        int W = 300;
        int P = 100;

        int[] w = new int[n];
        double[] v = new double[n];
        for (int i=0; i<n; i++) {
            w[i] = random.nextInt(W/10);
            v[i] = random.nextDouble() * P;
        }

        int[] ex = KPSolver.exact(n, v, w, W);
        assert ex != null;
        double exCost = Arrays.stream(ex).mapToDouble(i -> v[i]).sum();

        int[] he = KPSolver.fptas(n, v, w, W, 0.01);
        double heCost = Arrays.stream(he).mapToDouble(i -> v[i]).sum();

        System.out.println("Random exact: " + exCost);
        System.out.println("Random heuristic: " + heCost);
    }
}
