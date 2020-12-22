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

public class AugeratReader {

    public static Problem read(String filepath) {
        try {
            int n, p;

            BufferedReader reader = new BufferedReader(new FileReader(filepath));

            String line = reader.readLine(); // NAME
            String instancename = line.split(" : ")[1];
            p = Integer.parseInt(instancename.split("-")[2].substring(1));

            reader.readLine(); // COMMENT
            reader.readLine(); // TYPE
            reader.readLine(); // DIMENSION
            reader.readLine(); // EDGE_WEIGHT_TYPE
            reader.readLine(); // CAPACITY
            reader.readLine(); // NODE_COORD_SECTION
            line = reader.readLine(); // skip first location because it's the depot

            List<Location> locations = new ArrayList<>();
            line = reader.readLine();
            line = line.trim();
            while (line.length() > 0 && !line.equals("DEMAND_SECTION")) {
                String[] splitted = line.split(" ");
                double lat = Double.parseDouble(splitted[1]);
                double lng = Double.parseDouble(splitted[2]);
                locations.add(new Location(splitted[0], lat, lng));
                line = reader.readLine();
                line = line.trim();
            }
            n = locations.size();

            Distance distance = new Euclidean(locations);

            reader.close();

            return new Problem(locations.stream().map(l -> new Service(l.getId(), l, 0, 0)).collect(Collectors.toList()),
                    n, p, distance.getDistancesMatrix());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
