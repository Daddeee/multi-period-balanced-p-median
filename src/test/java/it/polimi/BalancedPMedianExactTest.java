package it.polimi;

import it.polimi.algorithm.balancedpmedian.BalancedPMedianExact;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianVNS;
import it.polimi.domain.Problem;
import it.polimi.domain.Solution;
import it.polimi.io.reader.ORLIBReader;
import it.polimi.io.writer.TestCSVWriter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BalancedPMedianExactTest {
    @Test
    public void orlib() {
        String basePath = "instances/orlib/pmed%d.txt";
        int numInstances = 1;
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] res = new double[numInstances];
        double[] times = new double[numInstances];

        int tries = 1;

        int from = 1, to = 1;
        for (int i=from; i<=to; i++) {
            String path = String.format(basePath, i);
            Problem problem = ORLIBReader.read(path);

            Map<Integer, Integer> medianCounts = new HashMap<>();

            double avgRes = 0., avgTime = 0.;
            for (int j=0; j<tries; j++) {
                //BalancedPMedianExact solver = new BalancedPMedianExact();
                BalancedPMedianVNS solver = new BalancedPMedianVNS();
                Solution sol = solver.run(problem);
                medianCounts = sol.getMedianCounts();
                avgRes += sol.getObjective();
                avgTime += sol.getElapsedTime();
            }
            avgRes = avgRes / tries;
            avgTime = avgTime / tries;

            System.out.print(path);
            System.out.println(String.format(" res=%.2f time=%.2fms", avgRes, avgTime));

            System.out.print(" medians=(");
            System.out.print(medianCounts.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", ")));
            System.out.println(")");

            n[i-1] = problem.getN();
            p[i-1] = problem.getP();
            //opt[i-1] = exactSolution.getObjective();
            res[i-1] = avgRes;
            times[i-1] = avgTime;
        }
    }
}
