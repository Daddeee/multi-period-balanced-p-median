package it.polimi;

import it.polimi.algorithm.fairpmedian.FairPMedianExact;
import it.polimi.algorithm.pmedian.PMedianExact;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.AugeratReader;
import it.polimi.io.writer.TestCSVWriter;
import it.polimi.io.writer.ZonesCSVWriter;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PMedianExactTest {
    @Test
    public void augerat() {
        String basePath = "instances/augerat/A-VRP/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] costs = new double[numInstances];
        double[] avgs = new double[numInstances];
        double[] bmeans = new double[numInstances];
        double[] opttimes = new double[numInstances];

        int tries = 10;

        for (int i=0; i<files.length; i++) {

            String filePath = basePath + files[i];
            Problem problem = AugeratReader.read(filePath);

            System.out.println("INSTANCE: " + filePath);

            PMedianExact exact = new PMedianExact();
            Solution exactSolution = exact.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            opt[i] = exactSolution.getObjective();
            opttimes[i] = exactSolution.getElapsedTime();
            costs[i] = exactSolution.getObjective();
            avgs[i] = getAvg(problem.getP(), problem.getN(), exactSolution);
            bmeans[i] = getBMean(0.4, problem.getP(), problem.getN(), exactSolution);


            ZonesCSVWriter.write("results/pmedian/pmp-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), exactSolution.getMedians());
        }

        TestCSVWriter.write("results/pmedian/augerat.csv", opt, opttimes, costs, avgs, bmeans, n, p);
    }


    private double getBMean(double beta, int p, int n, Solution solution) {
        int db = (int) Math.ceil(p * beta);

        Map<Integer, Integer> counts = new HashMap<>();
        for (Integer median : solution.getMedians()) {
            int c = counts.getOrDefault(median, 0);
            counts.put(median, c+1);
        }

        return counts.values().stream()
                .sorted(Comparator.comparingInt(Integer::intValue).reversed())
                .limit(db)
                .mapToDouble(i -> (double) i)
                .average().orElse(0);
    }

    private double getAvg(int p, int n, Solution solution) {
        double xavg = (double) n / p;

        Map<Integer, Integer> counts = new HashMap<>();
        for (Integer median : solution.getMedians()) {
            int c = counts.getOrDefault(median, 0);
            counts.put(median, c+1);
        }

        return counts.values().stream().mapToDouble(i -> Math.abs(i - xavg)).sum();
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
