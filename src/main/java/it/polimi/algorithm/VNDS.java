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
    protected final int MAX_SOLUTION_CHANGES = 100;
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

        Solution cur = opt.clone();
        int k = 1;
        int changes = 0;
        List<Integer> shakable = IntStream.range(0, problem.getN())
                .filter(i -> problem.getD()[i] - problem.getR()[i] > 0).boxed().collect(Collectors.toList());
        while (k <= problem.getKmax()) {
            Collections.shuffle(shakable, random);
            Set<Integer> touchedPeriods = new HashSet<>();
            // shaking
            for (int j = 0; j < k; j++) {
                if (shakable.size() <= 0) break;
                int toSwap = shakable.get(j);
                int oldPeriod = cur.getPeriods()[toSwap];
                // using random insertion
                int insertionPeriod = problem.getR()[toSwap] + random.nextInt(problem.getD()[toSwap] - problem.getR()[toSwap] + 1);
                while (insertionPeriod == cur.getPeriods()[toSwap])
                    insertionPeriod = problem.getR()[toSwap] + random.nextInt(problem.getD()[toSwap] - problem.getR()[toSwap] + 1);
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
            if (cur.getObjective() < opt.getObjective()) {
                opt = cur.clone();
                k = 1;
                changes++;
                if (changes >= MAX_SOLUTION_CHANGES) {
                    LOGGER.info("Max solution changes limit hit.");
                    break;
                }
            } else {
                cur = opt.clone();
                k = k + 1;
            }
        }
        double end = System.nanoTime();
        double elapsedTime = (end - start) / 1e6;
        opt.setElapsedTime(elapsedTime);

        solveSuperMedians(opt, problem);

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
        int period = 0;
        for (List<Integer> periodPoints : Lists.partition(points, periodSize)) {
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
            period += 1;
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
}
