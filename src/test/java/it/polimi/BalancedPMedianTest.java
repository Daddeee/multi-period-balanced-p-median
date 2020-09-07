package it.polimi;

import it.polimi.algorithm.balancedpmedian.BalancedPMedianExact;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianVNS;
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

        int from = 1, to = 10;
        for (int i=from; i<=to; i++) {
            String path = String.format(basePath, i);
            Problem problem = ORLIBReader.read(path);
            problem.setKmax(problem.getN() / 3);

            //BalancedPMedianExact exact = new BalancedPMedianExact();
            //Solution exactSolution = exact.run(problem);

            BalancedPMedianVNS vns = new BalancedPMedianVNS();
            Solution vnsSolution = vns.run(problem);

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", vnsSolution.getObjective(), vnsSolution.getElapsedTime()));

            n[i-1] = problem.getN();
            p[i-1] = problem.getP();
            //opt[i-1] = exactSolution.getObjective();
            res[i-1] = vnsSolution.getObjective();
            times[i-1] = vnsSolution.getElapsedTime();
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
