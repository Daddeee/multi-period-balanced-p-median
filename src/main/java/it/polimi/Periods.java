package it.polimi;

import it.polimi.algorithm.VNDS;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianRVNS;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.RandReader;
import it.polimi.io.writer.ZonesCSVWriter;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Periods {

    public static void main(String[] args) {
        final int seed = 1337;
        Random random = new Random(seed);
        String path = "instances/n500p5t5.csv";
        Problem problem = RandReader.read(path);

        System.out.println("Solving BPMP");

        BalancedPMedianRVNS.MAX_RESTART_WITHOUT_IMPROVEMENTS = 10;

        BalancedPMedianRVNS bpm = new BalancedPMedianRVNS(seed);
        Solution bmpsol = bpm.run(problem);
        for (int i=0; i<problem.getN(); i++) {
            int span = problem.getD()[i] - problem.getR()[i];
            int period = problem.getR()[i] + random.nextInt(span + 1);
            bmpsol.setPeriod(i, period);
            bmpsol.setSupermedian(i, bmpsol.getMedian(i));
        }
        printPeriods(bmpsol, problem, "results/presentation/bpmp/");

        System.out.println("Done. Now solving MPBPMP.");

        BalancedPMedianRVNS.MAX_RESTART_WITHOUT_IMPROVEMENTS = 3;

        VNDS mpbpmp = new VNDS(seed);
        Solution mpbpmsol = mpbpmp.run(problem);
        printPeriods(mpbpmsol, problem, "results/presentation/mpbpmp/");

        System.out.println("Done.");
    }

    private static void printPeriods(Solution solution, Problem problem, String basePath) {
        for (int t=0; t<problem.getM(); t++) {
            List<Integer> periodPoints = solution.getPointsInPeriod(t);
            Location[] locations = periodPoints.stream()
                    .map(i -> problem.getServices().get(i))
                    .map(Service::getLocation)
                    .toArray(Location[]::new);
            int[] labels = periodPoints.stream().mapToInt(solution::getMedian).toArray();
            ZonesCSVWriter.write(basePath + "t" + t + ".csv", locations, labels);
        }
        Location[] locations = problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new);
        int[] periods = solution.getPeriods();
        int[] medians = solution.getMedians();
        int[] supermedians = IntStream.range(0, problem.getN()).map(solution::getMedian).map(solution::getSuperMedian).toArray();
        ZonesCSVWriter.write(basePath + "tot.csv", locations, periods, medians, supermedians);
    }
}
