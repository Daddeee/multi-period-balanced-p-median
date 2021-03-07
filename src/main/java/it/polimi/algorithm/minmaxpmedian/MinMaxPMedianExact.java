package it.polimi.algorithm.minmaxpmedian;

import com.ampl.*;
import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

import java.util.Arrays;

public class MinMaxPMedianExact implements Solver {

    private double lambda;

    public MinMaxPMedianExact() {}

    @Override
    public Solution run(Problem problem) {
        AMPL ampl = new AMPL();
        try {
            ampl.read("models/min-max-p-median.mod");
            ampl.readData("");
            ampl.setOption("solver", "cplex");
            //ampl.setOption("cplex_options", "threads=1");
            //ampl.setOutputHandler((kind, s) -> {});

            Parameter n = ampl.getParameter("n");
            n.setValues(problem.getN());

            Parameter pp = ampl.getParameter("p");
            pp.setValues(problem.getP());

            float[][] distMatrix = problem.getC();

            Parameter M = ampl.getParameter("M");
            double[] valuesM = new double[problem.getN()];
            for (int i=0; i<distMatrix.length; i++) {
                valuesM[i] = Double.MIN_VALUE;
                for (int j = 0; j < distMatrix[i].length; j++)
                    if (distMatrix[i][j] > valuesM[i])
                        valuesM[i] = distMatrix[i][j];
            }
            M.setValues(valuesM);

            Tuple[] tuples = new Tuple[distMatrix.length*distMatrix.length];
            double[] distances = new double[distMatrix.length*distMatrix.length];
            int count = 0;
            for (int i=0; i<distMatrix.length; i++) {
                for (int j=0; j<distMatrix[i].length; j++) {
                    tuples[count] = new Tuple(i, j);
                    distances[count] = distMatrix[i][j];
                    count++;
                }
            }

            Parameter d = ampl.getParameter("c");
            d.setValues(tuples, distances);

            long start = System.nanoTime();
            ampl.solve();
            long end = System.nanoTime();
            double elapsedTime = (end - start) / 1e6;

            // Get the values of the variable Buy in a dataframe object
            Variable x = ampl.getVariable("x");
            DataFrame df = x.getValues();
            int[] medians = new int[problem.getN()];
            df.iterator().forEachRemaining(os -> {
                if ((double) os[2] == 1.)
                    medians[(int) Math.round((double) os[1])] = (int) Math.round((double) os[0]);
            });

            double obj = ampl.getObjective("max_min").value();

            ampl.eval("param status symbolic; let status := solve_result;");
            String status = (String) ampl.getParameter("status").get();

            if (!status.equals("solved") && !status.equals("solved?"))
                return null;

            return new Solution(new int[medians.length], medians, new int[medians.length], obj, elapsedTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
        return null;
    }
}
