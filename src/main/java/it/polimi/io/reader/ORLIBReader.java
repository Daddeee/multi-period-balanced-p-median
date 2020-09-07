package it.polimi.io.reader;

import it.polimi.domain.Problem;
import it.polimi.util.FloydWarshall;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ORLIBReader {

    public static Problem read(String filepath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));

            String line = reader.readLine();
            line = line.trim();
            String[] splitted = line.split(" ");
            int n = Integer.parseInt(splitted[0]);
            int p = Integer.parseInt(splitted[2]);

            float[][] adj = new float[n][n];
            for (int i=0; i<n; i++) {
                for (int j=0; j<n; j++) {
                    adj[i][j] = (i == j) ? 0 : Float.MAX_VALUE;
                }
            }

            line = reader.readLine();
            while (line != null && line.length() > 0) {
                line = line.trim();
                splitted = line.split(" ");
                int from = Integer.parseInt(splitted[0]) - 1;
                int to = Integer.parseInt(splitted[1]) - 1;
                float w = Float.parseFloat(splitted[2]);
                adj[from][to] = w;
                adj[to][from] = w;
                line = reader.readLine();
            }
            reader.close();

            float[][] d = FloydWarshall.compute(adj);

            return new Problem(n, p, d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
