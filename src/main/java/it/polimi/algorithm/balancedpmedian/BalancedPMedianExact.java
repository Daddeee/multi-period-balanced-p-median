package it.polimi.algorithm.balancedpmedian;

import com.ampl.*;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

import java.util.Arrays;

public class BalancedPMedianExact implements Solver {

    private double lambda;

    private double cost;
    private double fairness;

    public BalancedPMedianExact(double lambda) {
        this.lambda = lambda;
    }

    public double getCost() {
        return cost;
    }

    public double getFairness() {
        return fairness;
    }

    @Override
    public Solution run(Problem problem) {
        try {
            IloCplex cplex = new IloCplex();

            IloIntVar[][] x = new IloIntVar[problem.getN()][problem.getN()];
            for (int i=0; i< problem.getN(); i++)
                x[i] = cplex.intVarArray(problem.getN(),0, 1);

            IloNumVar[] w = cplex.numVarArray(problem.getN(), 0, Double.MAX_VALUE);

            // exact p medians
            IloLinearIntExpr pmeds = cplex.linearIntExpr();
            for (int j=0; j<problem.getN(); j++)
                pmeds.addTerm(x[j][j], 1);
            cplex.addEq(pmeds, problem.getP());

            // assign to medians only
            IloLinearIntExpr medass = cplex.linearIntExpr();
            for (int i=0; i<problem.getN(); i++) {
                for (int j=0; j<problem.getN(); j++) {
                    IloLinearIntExpr mexpr = cplex.linearIntExpr();
                    mexpr.addTerm(x[i][j], 1);
                    mexpr.addTerm(x[j][j], -1);
                    cplex.addLe(mexpr, 0);
                }
            }

            // assign to one median only
            for (int i=0; i< problem.getN(); i++) {
                IloLinearIntExpr sexpr = cplex.linearIntExpr();
                for (int j=0; j<problem.getN(); j++) {
                    sexpr.addTerm(x[i][j], 1);
                }
                cplex.addEq(sexpr, 1);
            }

            // define w
            for (int j=0; j<problem.getN(); j++) {
                IloLinearNumExpr w1expr = cplex.linearNumExpr();
                for (int i=0; i<problem.getN(); i++)
                    w1expr.addTerm(x[i][j], 1);
                w1expr.addTerm(x[j][j], -problem.getAvg());
                w1expr.addTerm(w[j], -1);
                cplex.addLe(w1expr, 0);

                IloLinearNumExpr w2expr = cplex.linearNumExpr();
                for (int i=0; i<problem.getN(); i++)
                    w2expr.addTerm(x[i][j], -1);
                w2expr.addTerm(x[j][j], problem.getAvg());
                w2expr.addTerm(w[j], -1);
                cplex.addLe(w2expr, 0);
            }

            IloLinearNumExpr obj = cplex.linearNumExpr();
            for (int i=0; i< problem.getN(); i++) {
                obj.addTerm(w[i], problem.getAlpha());
                for (int j = 0; j < problem.getN(); j++) {
                    obj.addTerm(x[i][j], problem.getC()[i][j]);
                }
            }
            cplex.addMinimize(obj);

            long start = System.nanoTime();
            boolean status = cplex.solve();
            long end = System.nanoTime();
            double elapsedTime = (end - start) / 1e6;

            if (!status) return null;

            int[] periods = new int[problem.getN()];
            int[] supermedians = new int[problem.getN()];
            Arrays.fill(supermedians, Solution.NO_SUPERMEDIAN);

            int[] medians = new int[problem.getN()];
            for (int i=0; i<problem.getN(); i++)
                for (int j=0; j<problem.getN(); j++)
                    if (cplex.getValue(x[i][j]) == 1.)
                        medians[i] = j;

            double objValue = cplex.getObjValue();

            cplex.close();

            return new Solution(periods, medians, supermedians, objValue, elapsedTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
