package it.polimi.algorithm;

import com.google.common.collect.Lists;
import it.polimi.algorithm.balancedpmedian.BalancedPMedianVNS;
import it.polimi.algorithm.highlevelpmedian.HighLevelPMedian;
import it.polimi.domain.Solution;
import it.polimi.domain.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VNDS implements Solver {
    protected final int MAX_RESTART_WITHOUT_IMPROVEMENTS = 10;
    private final Logger LOGGER = LoggerFactory.getLogger(VNDS.class);
    private final Random random;

    public VNDS() {
        this(new Random());
    }

    public VNDS(int seed) {
        this(new Random(seed));
    }

    public VNDS(Random random) {
        this.random = random;
    }

    @Override
    public Solution run(Problem problem) {
        double start = System.nanoTime();

        Solution opt = getInitial(problem);

        int count = 0;
        do {
            Solution acc = opt.clone();
            Solution cur = opt.clone();
            int k = 1;
            List<Integer> shakable = IntStream.range(0, problem.getN())
                    .filter(i -> problem.getD()[i] - problem.getR()[i] > 0).boxed().collect(Collectors.toList());
            double temperature = getInitialTemperature(cur.getObjective());
            double cooling = 0.995;
            while (k <= problem.getKmax()) {
                Collections.shuffle(shakable, random);
                Set<Integer> touchedPeriods = new HashSet<>();
                // shaking
                for (int j = 0; j < k; j++) {
                    if (shakable.size() <= 0) break;
                    int toSwap = shakable.get(j);
                    int oldPeriod = cur.getPeriods()[toSwap];

                    // using best insertion
                    int insertionPeriod = oldPeriod, bestCnt = Integer.MAX_VALUE;
                    for (int i=problem.getR()[toSwap]; i<= problem.getD()[toSwap]; i++) {
                        if (i == oldPeriod) continue;
                        int cnt = cur.getPointsInPeriod(i).size();
                        if (cnt < bestCnt) {
                            bestCnt = cnt;
                            insertionPeriod = i;
                        }
                    }

                    touchedPeriods.add(oldPeriod);
                    touchedPeriods.add(insertionPeriod);
                    // do not care about updating medians or objectives there
                    cur.setPeriod(toSwap, insertionPeriod);
                }

                // local search
                for (int period : touchedPeriods) {
                    solveSinglePeriod(cur, problem, period);
                }

                // Move or not
                if (accept(acc.getObjective(), cur.getObjective(), temperature)) {
                    acc = cur.clone();
                    k = 1;
                    temperature = temperature*cooling;
                    if (acc.getObjective() < opt.getObjective()) {
                        LOGGER.info("New optimum! cost=" + cur.getObjective() + " k=" + k);
                        opt = acc.clone();
                    }
                } else {
                    cur = acc.clone();
                    k = k + 1;
                }
            }
            count++;
            LOGGER.info("No improvement founds. best=" + opt.getObjective() + " count=" + count);
        } while (count < MAX_RESTART_WITHOUT_IMPROVEMENTS);
        double end = System.nanoTime();
        double elapsedTime = (end - start) / 1e6;
        opt.setElapsedTime(elapsedTime);

        LOGGER.info("Solved vnds. elapsed_time=" + elapsedTime + " cost=" + opt.getObjective());

        solveSuperMedians(opt, problem);

        LOGGER.info("Completed! Solution cost=" + opt.getObjective() + "\n");

        return opt;
    }

    public Solution getInitial(Problem problem) {
        int[] periods = new int[problem.getN()];
        int[] medians = new int[problem.getN()];
        int periodSize = (int) Math.ceil((double) problem.getN() / problem.getM());
        List<Integer> points = IntStream.range(0, problem.getN()).boxed().collect(Collectors.toList());
        List<Integer> medianPoints = new ArrayList<>();
        List<Integer> medianPeriods = new ArrayList<>();
        Collections.shuffle(points, random);

        Map<Integer, List<Integer>> periodPointsMap = new HashMap<>();
        for (int i=0; i<problem.getN(); i++) {
            int insertionPeriod = problem.getR()[i] + random.nextInt(problem.getD()[i] - problem.getR()[i] + 1);
            List<Integer> periodPoints = periodPointsMap.getOrDefault(insertionPeriod, new ArrayList<>());
            periodPoints.add(i);
            periodPointsMap.put(insertionPeriod, periodPoints);
        }

        for (int period = 0; period < problem.getM(); period++) {
            List<Integer> periodPoints = periodPointsMap.get(period);
            for (int i=0; i<periodPoints.size(); i++) {
                int pi = periodPoints.get(i);
                periods[pi] = period;
            }
            float[][] dist = getDist(problem, periodPoints);
            Problem periodProblem = new Problem(periodPoints.size(), problem.getP(), dist);
            BalancedPMedianVNS vns = new BalancedPMedianVNS(random);
            Solution periodSolution = vns.run(periodProblem);
            int[] labels = periodSolution.getMedians();
            for (int i=0; i<labels.length; i++) {
                medians[periodPoints.get(i)] = periodPoints.get(labels[i]);
                medianPoints.add(periodPoints.get(labels[i]));
                medianPeriods.add(period);
            }
        }

        int[] supermedians = new int[problem.getN()];
        Arrays.fill(supermedians, Solution.NO_SUPERMEDIAN);

        Solution solution = new Solution(periods, medians, supermedians, 0., 0.);
        double cost = computeObjectiveFunction(solution, problem);
        solution.setObjective(cost);
        return solution;
    }

    private void solveSuperMedians(Solution solution, Problem problem) {
        List<Integer> medianPoints = Arrays.stream(solution.getMedians()).boxed().distinct().collect(Collectors.toList());
        List<Integer> medianPeriods = medianPoints.stream().map(solution::getPeriod).collect(Collectors.toList());
        int[] supermeds = getSuperMedians(medianPoints, medianPeriods, problem);
        for (int i=0; i<supermeds.length; i++)
            solution.setSupermedian(i, supermeds[i]);
        double cost = computeObjectiveFunction(solution, problem);
        solution.setObjective(cost);
    }

    private int[] getSuperMedians(List<Integer> medianPoints, List<Integer> medianPeriods, Problem problem) {
        int[] supermedians = new int[problem.getN()];
        Arrays.fill(supermedians, Solution.NO_SUPERMEDIAN);
        float[][] dist = getDist(problem, medianPoints);
        int[][] prds = oneHotEncode(medianPeriods, problem.getM());
        HighLevelPMedian hlpm = new HighLevelPMedian(medianPoints.size(), problem.getP(), problem.getM(), dist, prds);
        int[] res = hlpm.run();
        for (int i=0; i<res.length; i++) {
            int median = medianPoints.get(i);
            int supermedian = medianPoints.get(res[i]);
            supermedians[median] = supermedian;
        }
        return supermedians;
    }

    private float[][] getDist(Problem problem, List<Integer> periodPoints) {
        float[][] dist = new float[periodPoints.size()][periodPoints.size()];
        for (int i = 0; i < periodPoints.size(); i++) {
            int pi = periodPoints.get(i);
            for (int j = 0; j < periodPoints.size(); j++) {
                int pj = periodPoints.get(j);
                dist[i][j] = problem.getC()[pi][pj];
            }
        }
        return dist;
    }

    public void solveSinglePeriod(Solution solution, Problem problem, int period) {
        List<Integer> periodPoints = solution.getPointsInPeriod(period);

        // computing distance submatrix for points in this period
        float[][] dist = getDist(problem, periodPoints);

        // solving the period
        Problem periodProblem = new Problem(periodPoints.size(), problem.getP(), dist);
        BalancedPMedianVNS vns = new BalancedPMedianVNS(random);
        Solution periodSolution = vns.run(periodProblem);
        int[] labels = periodSolution.getMedians();

        for (int i=0; i<periodPoints.size(); i++) {
            int point = periodPoints.get(i);
            int newMedian = periodPoints.get(labels[i]);
            solution.setMedian(point, newMedian);
        }

        solution.setObjective(computeObjectiveFunction(solution, problem));
    }

    public double computeObjectiveFunction(Solution solution, Problem problem) {
        double distCost = 0, balanceCost = 0;
        double avg = problem.getAvg();
        double alpha = problem.getAlpha();
        float[][] c = problem.getC();
        int[] medians = solution.getMedians();
        int[] supermedians = solution.getSupermedians();
        Map<Integer, Integer> counts = new HashMap<>();

        for (int i=0; i<problem.getN(); i++) {
            distCost += c[i][medians[i]];
            counts.put(medians[i], counts.getOrDefault(medians[i], 0) + 1);
            if (supermedians[i] != Solution.NO_SUPERMEDIAN)
                distCost += c[i][supermedians[i]];
        }

        for (int count : counts.values())
            balanceCost += alpha * Math.abs(count - avg);

        double cost = distCost + balanceCost;
        return cost;
    }

    private int[][] oneHotEncode(List<Integer> arr, int max) {
        int[][] encoded = new int[arr.size()][max];
        for (int i=0; i<arr.size(); i++) {
            encoded[i][arr.get(i)] = 1;
        }
        return encoded;
    }

    private double getInitialTemperature(double obj) {
        double w = 0.05;
        return w*obj / Math.log(2);
    }

    private boolean accept(double opt, double cur, double temperature) {
        double prob = Math.exp(-(cur - opt)/temperature);
        return random.nextDouble() < prob;
    }
}
