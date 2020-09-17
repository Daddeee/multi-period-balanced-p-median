package it.polimi;

import it.polimi.algorithm.balancedpmedian.BalancedPMedianExact;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianVNS;
import it.polimi.algorithm.balancedpmedian.alns.BalancedPMedianALNS;
import it.polimi.algorithm.pmedian.PMedianVNS;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.io.reader.ORLIBReader;
import it.polimi.io.writer.TestCSVWriter;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BalancedPMedianTest {

    @Test
    public void orlib() {
        String basePath = "instances/orlib/pmed%d.txt";
        double[] opt = readopt();
        int numInstances = opt.length;
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] res = new double[numInstances];
        double[] times = new double[numInstances];

        int tries = 10;

        int from = 1, to = 1;
        for (int i=from; i<=to; i++) {
            String path = String.format(basePath, i);
            Problem problem = ORLIBReader.read(path);

            //BalancedPMedianExact exact = new BalancedPMedianExact();
            //Solution exactSolution = exact.run(problem);

            double avgRes = 0., avgTime = 0.;
            for (int j=0; j<tries; j++) {
                BalancedPMedianVNS solver = new BalancedPMedianVNS();
                //BalancedPMedianALNS solver = new BalancedPMedianALNS();
                Solution vnsSolution = solver.run(problem);
                avgRes += vnsSolution.getObjective();
                avgTime += vnsSolution.getElapsedTime();
            }
            avgRes = avgRes / tries;
            avgTime = avgTime / tries;

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", avgRes, avgTime));

            n[i-1] = problem.getN();
            p[i-1] = problem.getP();
            //opt[i-1] = exactSolution.getObjective();
            res[i-1] = avgRes;
            times[i-1] = avgTime;
        }

        TestCSVWriter writer = new TestCSVWriter(n, p, opt, res, times);
        writer.write("results/balancedpmedian/orlib.csv");
    }

    private static double[] readopt() {
        try {
            double[] opt = new double[10];
            BufferedReader reader = new BufferedReader(new FileReader("instances/orlib/pmedbalopt.txt"));
            reader.readLine();
            String line = reader.readLine();
            int count = 0;
            while (line != null && line.length() > 0) {
                line = line.trim();
                String[] splitted = line.split("\\s+");
                opt[count] = Double.parseDouble(splitted[1]);
                count++;
                line = reader.readLine();
            }
            reader.close();
            return opt;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
