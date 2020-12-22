package it.polimi.algorithm.fairpmedian;

import it.polimi.algorithm.Solver;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.domain.Solution;
import it.polimi.io.reader.AugeratReader;
import it.polimi.io.writer.TestCSVWriter;
import it.polimi.io.writer.ZonesCSVWriter;
import it.polimi.util.Pair;

import java.io.File;
import java.util.*;

public class IteratedWeightedAverage implements Solver {

    private class Point {
        public Solution solution;
        public double x;
        public double y;

        public Point(Solution solution, double x, double y) {
            this.solution = solution;
            this.x = x;
            this.y = y;
        }
    }

    private class Area {
        public Point a;
        public Point b;
        public double epsilon;
        public double lambda;
        public double l;

        public Area(Point a, Point b) {
            if (a.x > b.x || a.y < b.y)
                throw new IllegalArgumentException("Inverted points in area");
            this.a = a;
            this.b = b;
            this.epsilon = 0.01;
            this.lambda = (a.y - b.y) / (a.y - b.y + b.x - a.x);
            this.l = lambda*a.x + (1 - lambda)*a.y - epsilon;
        }
    }

    public Point solveFPM(Problem problem, double lambda) {
        double beta = 0.5;
        FairPMedianExact fpm = new FairPMedianExact(beta, lambda);
        Solution fpmSolution = fpm.run(problem);
        return new Point(fpmSolution, fpm.getCost(), fpm.getFairness());
    }

    public Pair<Point,Point> getExtremes(Problem problem) {
        double beta = 0.5;

        // solve minimizing only cost
        FairPMedianExact fpmI1 = new FairPMedianExact(beta, 1);
        Solution fpmI1Solution = fpmI1.run(problem);
        double yI1 = fpmI1Solution.getObjective();

        // solve minimizing unfairness
        FairPMedianExact fpmI2 = new FairPMedianExact(beta, 0);
        Solution fpmI2Solution = fpmI2.run(problem);
        double yI2 = fpmI2Solution.getObjective();

        FairPMedianNadirExact fpmN1 = new FairPMedianNadirExact(beta, 1, Double.MAX_VALUE, yI2);
        Solution fpmN1Solution = fpmN1.run(problem);
        double yN1 = fpmN1Solution.getObjective();

            FairPMedianNadirExact fpmN2 = new FairPMedianNadirExact(beta, 0, yI1, Double.MAX_VALUE);
        Solution fpmN2Solution = fpmN2.run(problem);
        double yN2 = fpmN2Solution.getObjective();

        return new Pair<>(new Point(null, yI1, yI2), new Point(null, yN1, yN2));
    }

    public Point solveFPM(Problem problem, double lambda, double l) {
        double beta = 0.5;
        FairPMedianWAExact fpm = new FairPMedianWAExact(beta, lambda, l);
        Solution fpmSolution = fpm.run(problem);
        return new Point(fpmSolution, fpm.getCost(), fpm.getFairness());
    }

    private List<Point> frontier;
    private Queue<Area> searchAreas;

    @Override
    public Solution run(Problem problem) {
        solve(problem, 5);
        return this.frontier.get(this.frontier.size() - 1).solution;
    }

    public void solve(Problem problem, int iterations) {
        Pair<Point, Point> extrs = getExtremes(problem);
        Point ideal = extrs.getFirst();
        Point nadir = extrs.getSecond();

        Point boundIdeal = new Point(null, ideal.x, nadir.y);
        Point boundNadir = new Point(null, nadir.x, ideal.y);

        this.frontier = Arrays.asList(boundNadir, boundIdeal);
        this.searchAreas = new LinkedList<>();
        this.searchAreas.add(new Area(boundNadir, boundIdeal));

        int k = 2;

        while (k < iterations && !this.searchAreas.isEmpty()) {
            Area toSearch = this.searchAreas.poll();
            Point p = solveFPM(problem, toSearch.lambda, toSearch.l);
            if (p.solution != null) {
                this.frontier.add(p);
                this.searchAreas.add(new Area(toSearch.a, p));
                this.searchAreas.add(new Area(p, toSearch.b));
                k = k + 1;
            }
        }
    }

    public List<Point> getFrontier() {
        return frontier;
    }

    public List<Area> getSearchAreas() {
        return new ArrayList<>(searchAreas);
    }

    private  static String[] getFiles(String baseDir) {
        File directoryPath = new File(baseDir);
        return directoryPath.list();
    }

    public static void main(String[] args) {
        String basePath = "instances/augerat/A-VRP/";
        String[] files = getFiles(basePath);
        Arrays.sort(files);

        files = new String[]{ files[0] };

        int numInstances = files.length;
        double[] opt = new double[numInstances];
        int[] n = new int[numInstances];
        int[] p = new int[numInstances];
        double[] res = new double[numInstances];
        double[] restimes = new double[numInstances];
        double[] opttimes = new double[numInstances];

        for (int i=0; i<files.length; i++) {

            String filePath = basePath + files[i];
            Problem problem = AugeratReader.read(filePath);

            System.out.println("INSTANCE: " + filePath);

            IteratedWeightedAverage exact = new IteratedWeightedAverage();
            Solution exactSolution = exact.run(problem);

            n[i] = problem.getN();
            p[i] = problem.getP();
            opt[i] = exactSolution.getObjective();
            res[i] = 0;//resSum / tries;
            opttimes[i] = exactSolution.getElapsedTime();
            restimes[i] = 0;//timeSum / tries;

            ZonesCSVWriter.write("results/iwa-" + files[i],
                    problem.getServices().stream().map(Service::getLocation).toArray(Location[]::new), exactSolution.getMedians());
        }

        TestCSVWriter.write("results/fairpmedian/augerat.csv", opt, res, opttimes, restimes, n, p);
    }
}
