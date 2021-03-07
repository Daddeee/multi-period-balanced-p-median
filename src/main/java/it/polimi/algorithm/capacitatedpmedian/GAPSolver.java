package it.polimi.algorithm.capacitatedpmedian;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class GAPSolver {

    public static int[][] exact(int n, int m, int[] c, int[][] s, double[][] p) {
        try {
            IloCplex cplex = new IloCplex();
            //cplex.setParam(IloCplex.IntParam.RootAlgorithm, IloCplex.Algorithm.Network);

            cplex.setOut(new OutputStream() {
                @Override
                public void write(int i) throws IOException { }
            });

            // var x
            IloNumVar[][] x = new IloNumVar[n][m];
            for (int i=0; i<n; i++) {
                for (int j=0; j<m; j++) {
                    x[i][j] = cplex.numVar(0, Double.MAX_VALUE);
                }
            }

            // capacity constraint
            for (int j=0; j<m; j++) {
                IloLinearNumExpr cap = cplex.linearNumExpr();
                for (int i=0; i<n; i++) {
                    cap.addTerm(x[i][j], s[i][j]);
                }
                cplex.addLe(cap, c[j]);
            }

            // single assignment constraint
            for (int i=0; i<n; i++) {
                IloLinearNumExpr sass = cplex.linearNumExpr();
                for (int j=0; j<m; j++) {
                    sass.addTerm(x[i][j], 1);
                }
                cplex.addEq(sass, 1);
            }

            IloLinearNumExpr profit = cplex.linearNumExpr();
            for (int i=0; i<n; i++) {
                for (int j=0; j<m; j++) {
                    profit.addTerm(x[i][j], p[i][j]);
                }
            }
            cplex.addMaximize(profit);

            boolean status = cplex.solve();

            if(status) {
                int[][] sol = new int[n][m];
                for (int i=0; i<n; i++) {
                    for (int j=0; j<m; j++) {
                        sol[i][j] = (int) cplex.getValue(x[i][j]);
                    }
                }
                cplex.end();
                return sol;
            } else {
                cplex.end();
                return null;
            }
        } catch (IloException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[][] heuristic(int n, int m, int[] c, int[][] s, double[][] p) {
        int[] t = new int[n];
        Arrays.fill(t, -1);
        for (int j=0; j<m; j++) {
            double[] pj = new double[n];
            int[] sj = new int[n];
            for(int i=0; i<n; i++) {
                pj[i] = p[i][j];
                if (t[i] != -1) {
                    pj[i] -= p[i][t[i]];
                }
                sj[i] = s[i][j];
            }
            int[] selj = KPSolver.fptas(n, pj, sj, c[j], 0.1);
            for (int sel : selj) {
                t[sel] = j;
            }
        }
        int[][] sol = new int[n][m];
        for (int i=0; i<t.length; i++) {
            if (t[i] == -1) continue;
            sol[i][t[i]] = t[i] == -1 ? 0 : 1;
        }
        return sol;
    }
}
