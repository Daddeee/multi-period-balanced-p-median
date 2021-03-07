package it.polimi;

import it.polimi.algorithm.minmaxpmedian.MinMaxPMedianExact;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.AugeratReader;
import it.polimi.io.reader.ORLIBReader;
import it.polimi.io.writer.TestCSVWriter;
import it.polimi.io.writer.ZonesCSVWriter;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MinMaxPMedianExactTest {

    @Test
    public void augerat() {
        String basePath = "instances/augerat/A-VRP/";
        String[] files = getFiles(basePath);
        files = Arrays.stream(files).sorted().toArray(String[]::new);
        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] opttimes = new double[numInstances];

        for (int i=0; i<files.length; i++) {
            String filePath = basePath + files[i];
            Problem problem = AugeratReader.read(filePath, false);

            System.out.println("INSTANCE: " + filePath);

            MinMaxPMedianExact exact = new MinMaxPMedianExact();
            Solution exactSolution = exact.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            opt[i] = exactSolution.getObjective();
            opttimes[i] = exactSolution.getElapsedTime();

            ZonesCSVWriter.write("results/minmaxpmedian/mmpmp-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), exactSolution.getMedians());
        }

        String[] instances = Arrays.stream(files).map(s -> s.split("\\.")[0]).toArray(String[]::new);

        TestCSVWriter.write("results/minmaxpmedian/augerat.csv", instances, opt, opttimes, n, p);
    }

    @Test
    public void orlib() {
        String basePath = "instances/orlib/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);
        files = new String[] { files[0] };

        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] opttimes = new double[numInstances];

        for (int i=0; i< numInstances; i++) {
            String filePath = basePath + files[i];
            Problem problem = ORLIBReader.read(filePath);

            System.out.println("INSTANCE: " + filePath);

            MinMaxPMedianExact exact = new MinMaxPMedianExact();
            Solution exactSolution = exact.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            opt[i] = exactSolution.getObjective();
            opttimes[i] = exactSolution.getElapsedTime();

            ZonesCSVWriter.write("results/minmaxpmedian/mmpmp-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), exactSolution.getMedians());
        }

        TestCSVWriter.write("results/minmaxpmedian/orlib.csv", files, opt, opttimes, n, p);
    }


    private String[] getFiles(String baseDir) {
        File directoryPath = new File(baseDir);
        return directoryPath.list();
    }

    private void writeZones(String filepath, int[] labels) {
        try {
            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%d\n";

            for (int i=0; i<labels.length; i++)
                writer.write(String.format(formatRow, labels[i]));

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
