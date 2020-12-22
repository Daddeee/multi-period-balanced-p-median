package it.polimi.algorithm;

import com.ampl.*;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

import java.util.*;

public class Exact implements Solver {

    public Exact() {
    }

    @Override
    public Solution run(Problem problem) {
        AMPL ampl = new AMPL();
        try {
            ampl.read("models/multi-period-balanced-p-median-old.mod");
            ampl.readData("");
            ampl.setOption("solver", "cplex");
            ampl.setOption("cplex_options", "threads=1");
            ampl.setOutputHandler((kind, s) -> {});

            Parameter n = ampl.getParameter("n");
            n.setValues(problem.getN());

            Parameter p = ampl.getParameter("p");
            p.setValues(problem.getP());

            Parameter m = ampl.getParameter("m");
            m.setValues(problem.getM());

            Parameter c = ampl.getParameter("c");
            setMatrix(c, problem.getC());

            Parameter r = ampl.getParameter("r");
            r.setValues(Arrays.stream(problem.getR()).boxed().mapToDouble(Integer::doubleValue).toArray());

            Parameter d = ampl.getParameter("d");
            d.setValues(Arrays.stream(problem.getD()).boxed().mapToDouble(Integer::doubleValue).toArray());

            Parameter alpha = ampl.getParameter("alpha");
            alpha.setValues(problem.getAlpha());

            long start = System.nanoTime();
            ampl.solve();
            long end = System.nanoTime();
            double elapsedTime = (end - start) / 1e6;
            double objective = ampl.getObjective(ampl.getCurrentObjectiveName()).value();

            return new Solution(getPeriods(ampl, problem.getN()), getMedians(ampl, problem.getN()),
                    getSupermedians(ampl, problem.getN()), objective, elapsedTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
        return null;
    }

    private void setMatrix(Parameter param, float[][] matrix) {
        int r = matrix.length, c = -1;
        for (int i=0; i<r; i++)
            c = Math.max(c, matrix[i].length);
        Tuple[] tuples = new Tuple[r*c];
        double[] values = new double[r*c];
        int k = 0;
        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {
                tuples[k] = new Tuple(i, j);
                values[k] = matrix[i][j];
                k++;
            }
        }
        param.setValues(tuples, values);
    }

    private int[] getPeriods(final AMPL ampl, final int n) {
        Variable x = ampl.getVariable("x");
        DataFrame df = x.getValues();
        int[] periods = new int[n];
        df.iterator().forEachRemaining(os -> {
            if ((double) os[3] == 1.)
                periods[(int) Math.round((double) os[0])] = (int) Math.round((double) os[2]);
        });
        return periods;
    }

    private int[] getMedians(final AMPL ampl, final int n) {
        Variable x = ampl.getVariable("x");
        DataFrame df = x.getValues();
        int[] labels = new int[n];
        df.iterator().forEachRemaining(os -> {
            if ((double) os[3] == 1.)
                labels[(int) Math.round((double) os[0])] = (int) Math.round((double) os[1]);
        });
        return labels;
    }

    private int[] getSupermedians(final AMPL ampl, final int n) {
        Variable x = ampl.getVariable("y");
        DataFrame df = x.getValues();
        int[] labels = new int[n];
        Arrays.fill(labels, -1);
        df.iterator().forEachRemaining(os -> {
            if ((double) os[2] == 1.)
                labels[(int) Math.round((double) os[0])] = (int) Math.round((double) os[1]);
        });
        return labels;
    }
}
