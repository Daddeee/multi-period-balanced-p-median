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
}
