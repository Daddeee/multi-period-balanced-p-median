package it.polimi.algorithm.capacitatedpmedian;

import com.ampl.*;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

import java.util.Arrays;
import java.util.HashSet;

public class CapacitatedPMedianExact {

    public CapacitatedPMedianSolution run(CapacitatedPMedianProblem problem) {
        try {
            IloCplex cplex = new IloCplex();

            // boolean X variables
            IloIntVar[][] x = new IloIntVar[problem.getN()][problem.getN()];
            for (int i=0; i<problem.getN(); i++)
                x[i] = cplex.boolVarArray(problem.getN());

            // single assignment constraint
            for (int i=0; i<problem.getN(); i++) {
                IloLinearIntExpr sass = cplex.linearIntExpr();
                for (int j=0; j<problem.getN(); j++)
                    sass.addTerm(x[i][j], 1);
                cplex.addEq(sass, 1);
            }

            // exactly p-medians constraints
            IloLinearIntExpr pmed = cplex.linearIntExpr();
            for (int j=0; j<problem.getN(); j++)
                pmed.addTerm(x[j][j], 1);
            cplex.addEq(pmed, problem.getP());

            // capacity constraints
            for (int j=0; j<problem.getN(); j++) {
                IloLinearIntExpr cap = cplex.linearIntExpr();
                for (int i=0; i<problem.getN(); i++)
                    cap.addTerm(x[i][j], problem.getQs()[i]);
                IloLinearIntExpr maxcap = cplex.linearIntExpr();
                maxcap.addTerm(x[j][j], problem.getQ());
                cplex.addLe(cap, maxcap);
            }

            // objective minimize total dispersion
            IloLinearNumExpr obj = cplex.linearNumExpr();
            for (int i=0; i<problem.getN(); i++)
                for (int j=0; j<problem.getN(); j++)
                    obj.addTerm(x[i][j], problem.getC()[i][j]);
            cplex.addMinimize(obj);

            long start = System.nanoTime();
            boolean status = cplex.solve();
            long end = System.nanoTime();
            double elapsedTime = (end - start) / 1e6;

            // extract variables

            HashSet<Integer> medians = new HashSet<>();
            for (int j=0; j<problem.getN(); j++) {
                double xjj = cplex.getValue(x[j][j]);
                if (xjj == 1.) medians.add(j);
            }

            int[] xval = new int[problem.getN()];
            for (int i=0; i<problem.getN(); i++) xval[i] = i;
            int j=0;
            for (int i=0; i<problem.getN(); i++) {
                if (medians.contains(xval[i])) {
                    int temp = xval[i];
                    xval[i] = xval[j];
                    xval[j] = temp;
                    j++;
                }
            }

            int[] aval = new int[problem.getN()];
            for (int i=0; i<problem.getN(); i++) {
                for (int k=0; k<problem.getN(); k++) {
                    if (cplex.getValue(x[i][k]) == 1.) {
                        aval[i] = k;
                        break;
                    }
                }
            }

            if (status) {
                CapacitatedPMedianSolution sol = new CapacitatedPMedianSolution(medians, aval, xval, problem);
                sol.setElapsedTime(elapsedTime);
                sol.setF(cplex.getObjValue());
                return sol;
            } else {
                return null;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;

    }

}
