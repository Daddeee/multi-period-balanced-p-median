package it.polimi.io.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestCSVWriter {

    private final int k;
    private final int[] n;
    private final int[] p;
    private final double[] optima;
    private final double[] results;
    private final double[] times;
    private final double[] errors;

    public TestCSVWriter(final int n[], final int p[], final double[] optima, final double[] results,
                         final double[] times) {
        assert n.length == p.length && p.length == optima.length && optima.length == results.length
                && results.length == times.length;

        this.k = optima.length;
        this.n = n;
        this.p = p;
        this.optima = optima;
        this.results = results;
        this.times = times;
        this.errors = new double[k];
        for (int i=0; i<k; i++) {
            double diff = results[i] - optima[i];
            errors[i] = 100*diff/optima[i];
        }
    }

    public void write(String filepath) {
        try {
            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%-10s %-10s %-10s %-10s %-10s %-10s%n";

            writer.write(String.format(formatRow, "N", "P", "Opt", "Res", "Err", "Time"));

            for (int i=0; i<k; i++) {
                writer.write(String.format(formatRow, Integer.toString(n[i]), Integer.toString(p[i]),
                        String.format("%.2f", optima[i]), String.format("%.2f", results[i]),
                        String.format("%.2f%%", errors[i]), String.format("%.2fms", times[i])));
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(String filepath, double[] opt, double[] res, double[] opttime, double[] restime, int[] n,
                             int[] m, int[] p) {
        try {
            int len = Math.min(res.length, opt.length);
            double[] err = new double[len];
            for (int i=0; i<len; i++)
                err[i] = 100*(res[i] - opt[i])/opt[i];

            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s%n";

            writer.write(String.format(formatRow, "N", "M", "P", "Opt", "Res", "Err", "Opt. Time", "Res. Time"));

            for (int i=0; i<len; i++) {
                writer.write(String.format(formatRow, n[i], m[i], p[i], String.format("%.2f", opt[i]),
                        String.format("%.2f", res[i]), String.format("%.2f%%", err[i]),
                        String.format("%.2fms", opttime[i]), String.format("%.2fms", restime[i])));
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(String filepath, double[] opt, double[] opttime, double[] costs, double[] avgs,
            double[] bmeans, int[] n, int[] p) {
        try {
            int len =opt.length;

            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%-10s %-10s %-10s %-10s %-10s %-10s %-10s%n";

            writer.write(String.format(formatRow, "N", "P", "Opt", "Opt. Time", "Cost", "Avg", "B-Mean"));

            for (int i=0; i<len; i++) {
                writer.write(String.format(formatRow, n[i], p[i], String.format("%.4f", opt[i]),
                        String.format("%.2f", opttime[i]), String.format("%.2f", costs[i]),
                        String.format("%.2f", avgs[i]), String.format("%.2f", bmeans[i])));
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(String filepath, double[] opt, double[] res, double[] opttime, double[] restime, int[] n,
                             int[] p) {
        try {
            int len = Math.min(res.length, opt.length);
            double[] err = new double[len];
            for (int i=0; i<len; i++)
                err[i] = 100*(res[i] - opt[i])/opt[i];

            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            String formatRow = "%-10s %-10s %-10s %-10s %-10s %-10s %-10s%n";

            writer.write(String.format(formatRow, "N", "P", "Opt", "Res", "Err", "Opt. Time", "Res. Time"));

            for (int i=0; i<len; i++) {
                writer.write(String.format(formatRow, n[i], p[i], String.format("%.2f", opt[i]),
                        String.format("%.2f", res[i]), String.format("%.2f%%", err[i]),
                        String.format("%.2fms", opttime[i]), String.format("%.2fms", restime[i])));
            }

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
