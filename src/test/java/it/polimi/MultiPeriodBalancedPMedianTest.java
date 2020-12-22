package it.polimi;

import it.polimi.algorithm.Exact;
import it.polimi.algorithm.VNDS;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.RandReader;
import it.polimi.io.reader.SpeedyReader;
import it.polimi.io.writer.TestCSVWriter;
import it.polimi.io.writer.ZonesCSVWriter;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MultiPeriodBalancedPMedianTest {

    @Test
    public void speedy() {
        String basePath = "instances/speedy/weekly/";
        String resultPath = "results/multiperiodbalancedpmedian/speedy/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        int numInstances = files.length;
        double[] opt = readopt();
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        int[] m = new int[numInstances];
        double[] heu = new double[numInstances];
        double[] opttime = new double[numInstances];
        double[] heutime = new double[numInstances];

        for (int i=0; i<files.length; i++) {
            System.out.println("Solving " + files[i]);

            String filePath = basePath + files[i];
            Problem problem = SpeedyReader.read(filePath);

            VNDS vnds = new VNDS();
            Solution vndsSolution = vnds.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            m[i] = problem.getM();
            opt[i] = -1;
            heu[i] = vndsSolution.getObjective();
            opttime[i] = -1;
            heutime[i] = vndsSolution.getElapsedTime();

            System.out.println("Done. time=" + vndsSolution.getElapsedTime() + ", cost=" + vndsSolution.getObjective());
            writeSolution(vndsSolution, resultPath + files[i] + "-summary.txt");
            for (int k=0; k<problem.getM(); k++) {
                List<Location> locations = problem.getServices().stream().map(Service::getLocation).collect(Collectors.toList());
                Location[] periodLocations = vndsSolution.getPointsInPeriod(k).stream().map(locations::get).toArray(Location[]::new);
                int[] meds = vndsSolution.getPointsInPeriod(k).stream().mapToInt(vndsSolution::getMedian).toArray();
                ZonesCSVWriter.write(resultPath + files[i] + "-day" + k + ".csv", periodLocations, meds);
            }
        }
        TestCSVWriter.write("results/multiperiodbalancedpmedian/speedy.csv", opt, heu, opttime, heutime, n, m, p);
    }

    @Test
    public void rand() {
        String basePath = "instances/rand/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        //String basePath = "instances/rand/";
        //String[] files = { "n100p10t5.csv" };

        int numInstances = files.length;
        double[] opt = readopt();
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        int[] m = new int[numInstances];
        double[] heu = new double[numInstances];
        double[] opttime = new double[numInstances];
        double[] heutime = new double[numInstances];

        for (int i=0; i<files.length; i++) {
            System.out.println("Solving " + files[i]);

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

            System.out.println("Done. time=" + vndsSolution.getElapsedTime() + ", cost=" + vndsSolution.getObjective());
        }

        TestCSVWriter.write("results/multiperiodbalancedpmedian/rand-new.csv", opt, heu, opttime, heutime, n, m, p);
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

    private static void writeSolution(Solution solution, String filepath) {
        try {
            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));
            writer.write("obj=" + solution.getObjective() + ", time=" + solution.getElapsedTime() + "\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
