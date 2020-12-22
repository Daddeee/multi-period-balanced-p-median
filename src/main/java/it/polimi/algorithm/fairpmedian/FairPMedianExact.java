package it.polimi.algorithm.fairpmedian;

import com.ampl.*;
import it.polimi.algorithm.Solver;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;

import java.util.Arrays;

public class FairPMedianExact implements Solver {

    private double beta;
    private double lambda;

    private double cost;
    private double fairness;

    public FairPMedianExact(double beta, double lambda) {
        this.beta = beta;
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
        AMPL ampl = new AMPL();
        try {
            ampl.read("models/fair-p-median.mod");
            ampl.readData("");
            ampl.setOption("solver", "cplex");
            ampl.setOption("cplex_options", "threads=1");
            //ampl.setOutputHandler((kind, s) -> {});

            Parameter n = ampl.getParameter("n");
            n.setValues(problem.getN());

            Parameter pp = ampl.getParameter("p");
            pp.setValues(problem.getP());

            float[][] distMatrix = problem.getC();
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

            Parameter lambda = ampl.getParameter("lambda");
            lambda.setValues(this.lambda);

            Parameter dbeta = ampl.getParameter("Dbeta");
            dbeta.setValues((int) Math.ceil(problem.getP() / this.beta));

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

            double obj = ampl.getObjective("weighted_average_cost_dual_fairness").value();

            ampl.eval("param status symbolic; let status := solve_result;");
            String status = (String) ampl.getParameter("status").get();

            if (!status.equals("solved") && !status.equals("solved?"))
                return null;

            int[] periods = new int[problem.getN()];
            int[] supermedians = new int[problem.getN()];
            Arrays.fill(supermedians, Solution.NO_SUPERMEDIAN);
            for (int i=0; i<medians.length; i++)
                if (medians[i] == i)
                    supermedians[i] = i;


            ampl.eval("param cost; let cost := sum {i in V, j in V} c[i,j]*x[i,j];");
            this.cost = (double) ampl.getParameter("cost").get();

            ampl.eval("param fairness; let fairness := Dbeta*u + sum {i in V} v[i];");
            this.fairness = (double) ampl.getParameter("fairness").get();

            return new Solution(periods, medians, supermedians, obj, elapsedTime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ampl.close();
        }
        return null;
    }

}
