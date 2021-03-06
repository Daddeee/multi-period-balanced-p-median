package it.polimi.algorithm.balancedpmedian;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import it.polimi.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class BalancedAssignmentSolver {
    public static final Logger LOGGER = LoggerFactory.getLogger(BalancedAssignmentSolver.class);

    private HashMap<int[], Pair<Double, int[]>> results = new HashMap<>();
    private int count = 0;

    public Pair<Double, int[]> solve(int n, int p, float[][] c, double alpha, double avg, int[] x) {
        return solve(n, p, c, alpha, avg, x, -1);
    }

    public Pair<Double, int[]> solve(int n, int p, float[][] c, double alpha, double avg, int[] x, double ub) {
        int[] medians = Arrays.copyOfRange(x, 0, p);
        Arrays.sort(medians);

        if (results.containsKey(medians)) {
            LOGGER.info("Balanced assignment: CACHE HIT!.");
            return results.get(medians);
        }

        count++;
        //LOGGER.info("Solving balanced assignment for the " + count +" time.");

        IloCplex cplex = null;
        try {
            cplex = getCplex();

            IloIntVar[][] x1 = new IloIntVar[n][p];
            for (int i=0; i<n; i++)
                x1[i] = cplex.intVarArray(p, 0, 1);

            IloNumVar[] w = cplex.numVarArray(p, 0, Double.MAX_VALUE);

            // sum {j in 1..p} x[i][j] = 1, for i in 1..n
            for (int i=0; i<n; i++) {
                IloLinearIntExpr expr = cplex.linearIntExpr();
                for (int j=0; j<p; j++)
                    expr.addTerm(x1[i][j], 1);
                cplex.addEq(expr, 1);
            }

            // sum {i in 1..n} x[i][j] - xavg <= w[j], for j in 1..p
            for (int j=0; j<p; j++) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int i=0; i<n; i++)
                    expr.addTerm(x1[i][j], 1);
                expr.addTerm(w[j], -1);
                cplex.addLe(expr, avg);
            }

            // xavg - sum {i in 1..n} x[i][j] <= w[j], for j in 1..p
            for (int j=0; j<p; j++) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int i=0; i<n; i++)
                    expr.addTerm(x1[i][j], 1);
                expr.addTerm(w[j], 1);
                cplex.addGe(expr, avg);
            }

            IloLinearNumExpr ubexpr = cplex.linearNumExpr();
            IloLinearNumExpr objexpr = cplex.linearNumExpr();
            for (int j=0; j<p; j++) {
                for (int i=0; i<n; i++)
                    objexpr.addTerm(x1[i][j], c[i][x[j]]);
                objexpr.addTerm(w[j], alpha);
            }

            if (ub > 0)
                cplex.addLe(objexpr, ub);

            cplex.addMinimize(objexpr);

            long start = System.nanoTime();
            boolean status = cplex.solve();
            long end = System.nanoTime();
            double elapsedTime = (end - start) / 1e6;
            double obj = cplex.getBestObjValue();

            //LOGGER.info("Solved assignment GAP. obj=" + obj + " time=" + elapsedTime + "ms");

            if (!status) return null;

            int[] ax = new int[n];
            for (int i=0; i<n; i++) {
                for (int j=0; j<p; j++) {
                    if (cplex.getValue(x1[i][j]) == 1) {
                        ax[i] = x[j];
                    }
                }
            }

            Pair<Double, int[]> result = new Pair<>(obj, ax);
            results.put(medians, result);

            return result;
        } catch (IloException e) {
            e.printStackTrace();
        } finally {
            if (cplex != null)
                cplex.close();
        }
        return null;
    }

    private static IloCplex getCplex() throws IloException {
        IloCplex cplex = new IloCplex();

        cplex.setOut(new OutputStream() {
            @Override
            public void write(int i) { }
        });

        return cplex;
    }

}
