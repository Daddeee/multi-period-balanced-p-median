package it.polimi;

import it.polimi.algorithm.Exact;
import it.polimi.algorithm.VNDS;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.io.reader.RandReader;
import it.polimi.io.writer.TestCSVWriter;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class MultiPeriodBalancedPMedianTest {
    @Test
    public void rand() {
        String basePath = "instances/rand/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        int[] m = new int[numInstances];
        double[] heu = new double[numInstances];
        double[] opttime = new double[numInstances];
        double[] heutime = new double[numInstances];

        for (int i=0; i<files.length; i++) {
            String filePath = basePath + files[i];
            Problem problem = RandReader.read(filePath);

            Exact exact = new Exact();
            Solution exactSolution = exact.run(problem);

            VNDS vnds = new VNDS();
            Solution vndsSolution = vnds.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            m[i] = problem.getM();
            opt[i] = exactSolution.getObjective();
            heu[i] = vndsSolution.getObjective();
            opttime[i] = exactSolution.getElapsedTime();
            heutime[i] = vndsSolution.getElapsedTime();
        }

        TestCSVWriter.write("results/multiperiodbalancedpmedian/rand.csv", opt, heu, opttime, heutime, n, m, p);
    }

    private String[] getFiles(String baseDir) {
        File directoryPath = new File(baseDir);
        return directoryPath.list();
    }
}
