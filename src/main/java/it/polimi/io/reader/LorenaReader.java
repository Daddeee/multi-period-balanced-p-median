package it.polimi.io.reader;

import it.polimi.algorithm.capacitatedpmedian.CapacitatedPMedianProblem;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;
import it.polimi.util.FloydWarshall;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LorenaReader {

    public static CapacitatedPMedianProblem read(String filepath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = reader.readLine();
            line = line.trim();
            String[] splitted = line.split("\\s+");
            int n = Integer.parseInt(splitted[0]);
            int p = Integer.parseInt(splitted[1]);
            List<Service> services = new ArrayList<>();
            int Q = 0;
            int count = 0;
            line = reader.readLine();
            while (line != null && line.length() > 0) {
                line = line.trim();
                splitted = line.split("\\s+");
                String id = "" + count++;
                double x = Double.parseDouble(splitted[0]);
                double y = Double.parseDouble(splitted[1]);
                Q = Integer.parseInt(splitted[2]);
                int q = Integer.parseInt(splitted[3]);
                services.add(new Service(id, new Location(id, x, y), 0, 0, q));
                line = reader.readLine();
            }
            reader.close();
            Euclidean dist = new Euclidean(services.stream().map(Service::getLocation).collect(Collectors.toList()));
            return new CapacitatedPMedianProblem(services, Q, p, dist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
