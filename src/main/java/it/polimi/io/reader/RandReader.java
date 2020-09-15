package it.polimi.io.reader;

import it.polimi.distances.Distance;
import it.polimi.distances.Euclidean;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RandReader {

    public static Problem read(String filepath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = reader.readLine();
            String[] splitted = line.split(",");
            int n = Integer.parseInt(splitted[0]);
            int p = Integer.parseInt(splitted[1]);
            int m = Integer.parseInt(splitted[2]);

            // skip depot
            line = reader.readLine();

            List<Service> services = new ArrayList<>();
            int count = 0;
            line = reader.readLine();
            while (line != null) {
                splitted = line.split(",");
                if (splitted.length < 5) break;
                double x = Double.parseDouble(splitted[0]);
                double y = Double.parseDouble(splitted[1]);
                Location loc = new Location(Integer.toString(count), x, y);
                int r = Integer.parseInt(splitted[2]);
                int d = Integer.parseInt(splitted[3]);
                services.add(new Service(loc.getId(), loc, r, d));
                line = reader.readLine();
                count++;
            }
            reader.close();

            Distance dist = new Euclidean(services.stream().map(Service::getLocation).collect(Collectors.toList()));

            return new Problem(services, m, p, dist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
