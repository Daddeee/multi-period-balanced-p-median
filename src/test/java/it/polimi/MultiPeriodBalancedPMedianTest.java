package it.polimi;

import it.polimi.algorithm.Exact;
import it.polimi.algorithm.VNDS;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.io.reader.RandReader;
import it.polimi.io.writer.TestCSVWriter;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class MultiPeriodBalancedPMedianTest {
    @Test
    public void rand() {
        String basePath = "instances/rand/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        //String basePath = "instances/";
        //String[] files = { "n2000p5t5.csv" };

        int numInstances = files.length;
        double[] opt = readopt();
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        int[] m = new int[numInstances];
        double[] heu = new double[numInstances];
        double[] opttime = new double[numInstances];
        double[] heutime = new double[numInstances];

        for (int i=0; i<files.length; i++) {
            String filePath = basePath + files[i];
            Problem problem = RandReader.read(filePath);

            //Exact exact = new Exact();
            //Solution exactSolution = exact.run(problem);

            VNDS vnds = new VNDS(1337);
            Solution vndsSolution = vnds.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            m[i] = problem.getM();
            //opt[i]
            heu[i] = vndsSolution.getObjective();
            //opttime[i] = exactSolution.getElapsedTime();
            heutime[i] = vndsSolution.getElapsedTime();
        }

        TestCSVWriter.write("results/multiperiodbalancedpmedian/rand-tmp.csv", opt, heu, opttime, heutime, n, m, p);
    }

    private String[] getFiles(String baseDir) {
        File directoryPath = new File(baseDir);
        return directoryPath.list();
    }

    private static double[] readopt() {
        try {
            double[] opt = new double[10];
            BufferedReader reader = new BufferedReader(new FileReader("results/multiperiodbalancedpmedian/opt.csv"));
            reader.readLine();
            String line = reader.readLine();
            int count = 0;
            while (line != null && line.length() > 0) {
                line = line.trim();
                String[] splitted = line.split("\\s+");
                opt[count] = Double.parseDouble(splitted[3]);
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
