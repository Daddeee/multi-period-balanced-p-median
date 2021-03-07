package it.polimi;

import it.polimi.algorithm.balancedpmedian.BalancedPMedianExactOld;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianRVNS;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianVNS;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.AugeratReader;
import it.polimi.io.reader.ORLIBReader;
import it.polimi.io.reader.RandReader;
import it.polimi.io.reader.SpeedyReader;
import it.polimi.io.writer.TestCSVWriter;
import it.polimi.io.writer.ZonesCSVWriter;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BalancedPMedianTest {



    @Test
    public void speedy() {
        /*
        String basePath = "instances/rand/pmed/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);
         */
        String basePath = "instances/speedy/small/";
        String[] files = { "empoli-13d6.csv" };

        for (int i=0; i<files.length; i++) {
            String path = basePath + files[i];
            Problem problem = SpeedyReader.read(path);

            //PMedianVNS vns = new PMedianVNS();
            //BalancedPMedianRVNS vns = new BalancedPMedianRVNS();
            BalancedPMedianRVNS vns = new BalancedPMedianRVNS();
            Solution solution = vns.run(problem);

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", solution.getObjective(), solution.getElapsedTime()));

            ZonesCSVWriter.write("results/speedy/bpmp-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), solution.getMedians());
        }
    }

    @Test
    public void rand() {
        /*
        String basePath = "instances/rand/pmed/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);
         */
        String basePath = "instances/";
        String[] files = { "n100p5t1.csv" };

        for (int i=0; i<files.length; i++) {
            String path = basePath + files[i];
            Problem problem = RandReader.read(path);

            BalancedPMedianExactOld solver = new BalancedPMedianExactOld();
            //Solution solution = solver.run(problem);
            BalancedPMedianRVNS vns = new BalancedPMedianRVNS();
            Solution solution = vns.run(problem);

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", solution.getObjective(), solution.getElapsedTime()));

            ZonesCSVWriter.write("results/bpmp-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), solution.getMedians());
        }
    }

    @Test
    public void augerat() {
        String basePath = "instances/augerat/A-VRP/";
        String baseZonePath = "instances/augerat/zones/A-VRP/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        files = new String[] { files[0] };

        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] res = new double[numInstances];
        double[] restimes = new double[numInstances];
        double[] opttimes = new double[numInstances];

        int tries = 10;

        for (int i=0; i<files.length; i++) {

            String filePath = basePath + files[i];
            Problem problem = AugeratReader.read(filePath);

            System.out.println("INSTANCE: " + filePath);

            BalancedPMedianExactOld exact = new BalancedPMedianExactOld();
            Solution exactSolution = exact.run(problem);

            double resSum = 0., timeSum = 0.;
            Solution best = null;
            for (int k=0; k<tries; k++) {
                BalancedPMedianRVNS rvns = new BalancedPMedianRVNS(1337);
                Solution rvnsSolution = rvns.run(problem);
                resSum += rvnsSolution.getObjective();
                timeSum += rvnsSolution.getElapsedTime();
                if (best == null || rvnsSolution.getObjective() < best.getObjective())
                    best = rvnsSolution;
            }

            n[i] = problem.getN();
            p[i] = problem.getP();
            opt[i] = exactSolution.getObjective();
            res[i] = resSum / tries;
            opttimes[i] = exactSolution.getElapsedTime();
            restimes[i] = timeSum / tries;

            ZonesCSVWriter.write("results/bpmp-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), exactSolution.getMedians());
            //writeZones(zonePath, exactSolution.getMedians());
        }

        TestCSVWriter.write("results/balancedpmedian/augerat.csv", opt, res, opttimes, restimes, n, p);
    }

    @Test
    public void orlib() {
        String basePath = "instances/orlib/";
        String filename = "pmed%d";
        double[] opt = readopt();
        int numInstances = opt.length;
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] res = new double[numInstances];
        double[] times = new double[numInstances];

        int tries = 1;

        double avgTimeExec = 0.;
        double avgGap = 0.;

        int from = 1, to = numInstances;
        for (int i=from; i<=to; i++) {
            String path = String.format(basePath + filename + ".txt", i);
            Problem problem = ORLIBReader.read(path);

            //PMedianExact exact = new PMedianExact();
            //Solution exactSolution = exact.run(problem);

            double avgRes = 0., avgTime = 0.;
            Solution vnsSolution = null;
            for (int j=0; j<tries; j++) {
                //PMedianVNS solver = new PMedianVNS();
                //BalancedPMedianRVNS solver = new BalancedPMedianRVNS();
                BalancedPMedianVNS solver = new BalancedPMedianVNS();
                //BalancedPMedianALNS solver = new BalancedPMedianALNS();
                vnsSolution = solver.run(problem);
                avgRes += getObjective(problem, vnsSolution);
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

            double diff = res[i-1] - opt[i-1];
            avgGap += 100*diff/opt[i-1];
            avgTimeExec += times[i-1];

            //ZonesCSVWriter.write("results/balancedpmedian/orlib/" + String.format(filename, i) + ".res",
            //        problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), vnsSolution.getMedians());
        }

        TestCSVWriter writer = new TestCSVWriter(n, p, opt, res, times);
        writer.write("results/balancedpmedian/orlib-rvns.csv");
        System.out.println("avg gap = " + avgGap/numInstances + "%");
        System.out.println("avg time= " + avgTimeExec/numInstances + "ms");
    }

    public double getObjective(Problem problem, Solution solution) {
        double w = 0;

        Map<Integer, Integer> counts = new HashMap<>();
        for (int i=0; i< problem.getN(); i++) {
            int c1 = solution.getMedian(i);
            w += problem.getC()[i][c1];
            int c = counts.getOrDefault(c1, 0);
            counts.put(c1, c + 1);
        }

        if (counts.values().size() == 0)
            return w;

        for (int c : counts.values())
            w += problem.getAlpha() * Math.abs(c - problem.getAvg());

        return w;
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
