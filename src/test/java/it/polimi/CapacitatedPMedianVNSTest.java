package it.polimi;

import it.polimi.algorithm.capacitatedpmedian.CapacitatedPMedianExact;
import it.polimi.algorithm.capacitatedpmedian.CapacitatedPMedianProblem;
import it.polimi.algorithm.capacitatedpmedian.CapacitatedPMedianSolution;
import it.polimi.algorithm.capacitatedpmedian.CapacitatedPMedianVNS;
import it.polimi.domain.Solution;
import it.polimi.io.reader.LorenaReader;
import it.polimi.io.writer.TestCSVWriter;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CapacitatedPMedianVNSTest {

    @Test
    public void lorena() {
        String basePath = "instances/lorena/";
        String[] files = getFiles(basePath);
        files = new String[] { files[0] };
        Arrays.sort(files);

        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] opttimes = new double[numInstances];

        for (int i=0; i<files.length; i++) {
            String filePath = basePath + files[i];
            CapacitatedPMedianProblem problem = LorenaReader.read(filePath);

            System.out.println("INSTANCE: " + filePath);

            CapacitatedPMedianVNS vns = new CapacitatedPMedianVNS(1337);
            CapacitatedPMedianSolution vnsSol = vns.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            opt[i] = vnsSol.getF();
            opttimes[i] = vnsSol.getElapsedTime();
        }

        TestCSVWriter.write("results/capacitatedpmedian/lorena-vns.csv", opt, opt, opttimes, opttimes, n, p);
    }

    private String[] getFiles(String baseDir) {
        File directoryPath = new File(baseDir);
        return directoryPath.list();
    }
}
