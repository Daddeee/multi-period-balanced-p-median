package it.polimi.algorithm.capacitatedpmedian;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KPSolver {

    public static int[] exact(int n, double[] p, int[] s, int B) {
        try {
            IloCplex cplex = new IloCplex();
            //cplex.setParam(IloCplex.IntParam.RootAlgorithm, IloCplex.Algorithm.Network);

            cplex.setOut(new OutputStream() {
                @Override
                public void write(int i) throws IOException { }
            });

            // var x
            IloIntVar[] x = cplex.boolVarArray(n);

            // capacity constraint
            IloLinearNumExpr cap = cplex.linearNumExpr();
            for (int i=0; i<n; i++) {
                cap.addTerm(x[i], s[i]);
            }
            cplex.addLe(cap, B);

            IloLinearNumExpr profit = cplex.linearNumExpr();
            for (int i=0; i<n; i++) {
                profit.addTerm(x[i], p[i]);
            }
            cplex.addMaximize(profit);

            boolean status = cplex.solve();

            if(status) {
                List<Integer> res = new ArrayList<>();
                for (int i=0; i<n; i++) {
                    if (cplex.getValue(x[i]) == 1) res.add(i);
                }
                cplex.end();
                return res.stream().mapToInt(Integer::intValue).toArray();
            } else {
                cplex.end();
                return null;
            }
        } catch (IloException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[] fptas(int n, double[] p, int[] s, int B, double eps) {
        double P = Arrays.stream(p).max().orElse(0);
        double K = (eps * P) / n;
        int[] p_scaled = new int[n];
        for(int i=0; i<n; i++)
            p_scaled[i] = (int) (p[i] / K);
        return knapsackDP(n, p_scaled, s, B);
    }


    public static int[] knapsackDP(int n, int[] p, int[] s, int W) {
        int i, w;
        int[][] K = new int[n + 1][W + 1];
        for (i = 0; i <= n; i++) {
            for (w = 0; w <= W; w++) {
                if (i == 0 || w == 0)
                    K[i][w] = 0;
                else if (s[i-1] <= w)
                    K[i][w] = Math.max(p[i-1] + K[i-1][w-s[i-1]], K[i-1][w]);
                else
                    K[i][w] = K[i-1][w];
            }
        }

        List<Integer> items = new ArrayList<>();
        int k = n, l = W;
        while (k > 0) {
            if (K[k][l] != K[k-1][l]) {
                items.add(k-1);
                l = l - s[k-1];
            }
            k--;
        }

        return items.stream().mapToInt(Integer::intValue).toArray();
    }

}
