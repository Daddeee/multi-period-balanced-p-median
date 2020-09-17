package it.polimi;

import it.polimi.algorithm.Exact;
import it.polimi.algorithm.VNDS;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianVNS;
import it.polimi.algorithm.pmedian.PMedianVNS;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.ORLIBReader;
import it.polimi.io.reader.RandReader;
import it.polimi.io.writer.TestCSVWriter;
import it.polimi.io.writer.ZonesCSVWriter;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PMedianVNSTest {

    @Test
    public void orlib() {
        String basePath = "instances/orlib/pmed%d.txt";
        double[] opt = readopt();
        int[] n = new int[opt.length];
        int[] p = new int[opt.length];
        double[] res = new double[opt.length];
        double[] times = new double[opt.length];

        for (int i=1; i<=40; i++) {
            String path = String.format(basePath, i);
            Problem problem = ORLIBReader.read(path);
            PMedianVNS vns = new PMedianVNS();
            Solution solution = vns.run(problem);

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", solution.getObjective(), solution.getElapsedTime()));

            n[i-1] = problem.getN();
            p[i-1] = problem.getP();
            res[i-1] = solution.getObjective();
            times[i-1] = solution.getElapsedTime();
        }

        TestCSVWriter writer = new TestCSVWriter(n, p, opt, res, times);
        writer.write("results/pmedian/orlib.csv");
    }

    private static double[] readopt() {
        try {
            double[] opt = new double[40];
            BufferedReader reader = new BufferedReader(new FileReader("instances/orlib/pmedopt.txt"));
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

    @Test
    public void rand() {
        String basePath = "instances/rand/pmed/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        for (int i=0; i<files.length; i++) {
            String path = basePath + files[i];
            Problem problem = RandReader.read(path);

            //PMedianVNS vns = new PMedianVNS();
            BalancedPMedianVNS vns = new BalancedPMedianVNS();
            Solution solution = vns.run(problem);

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", solution.getObjective(), solution.getElapsedTime()));

            ZonesCSVWriter.write("results/balancedpmedian/" + files[i],
                   problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), solution.getMedians());
        }
    }

    private String[] getFiles(String baseDir) {
        File directoryPath = new File(baseDir);
        return directoryPath.list();
    }
}
