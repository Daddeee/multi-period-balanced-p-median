package it.polimi.io.reader;

import it.polimi.distances.Distance;
import it.polimi.distances.Haversine;
import it.polimi.domain.Location;
import it.polimi.domain.Problem;
import it.polimi.domain.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpeedyReader {

    public static Problem read(String filepath) {
        try {
            Random random = new Random(1337);
            List<Location> locations = new ArrayList<>();
            List<Calendar> deliveryDates = new ArrayList<>();
            String[] splt = filepath.split("/");
            String filename = splt[splt.length-1];
            int p = Integer.parseInt(filename.split("-")[1].split("d")[1].replaceAll("\\D+",""));
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = reader.readLine();
            int count = 0;
            while (line != null) {
                String[] splitted = line.split(",");
                double lat = Double.parseDouble(splitted[0]);
                double lng = Double.parseDouble(splitted[1]);
                if (lat != 0 && lng != 0) {
                    Location loc = new Location(Integer.toString(count), lat, lng);
                    locations.add(loc);

                    TemporalAccessor tacc = DateTimeFormatter.ISO_INSTANT.parse(splitted[2]);
                    Instant instant = Instant.from(tacc);
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                    Calendar from = GregorianCalendar.from(zdt);
                    deliveryDates.add(from);
                    count++;
                }
                line = reader.readLine();
            }
            reader.close();
            List<Integer> deliveryDays = parseReleaseDates(deliveryDates);
            int m = deliveryDays.stream().max(Integer::compareTo).orElse(0) + 1;
            List<Service> services = IntStream.range(0, locations.size()).mapToObj(i -> {
                int releaseDate = random.nextInt(deliveryDays.get(i) + 1);
                int dueDate = releaseDate + random.nextInt(m - releaseDate);
                Location loc = locations.get(i);
                return new Service(loc.getId(), loc, releaseDate, dueDate);
            }).collect(Collectors.toList());
            Distance distance = new Haversine(locations);
            return new Problem(services, m, p, distance);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Service> read(String branch, Calendar startDate, int t) {
        String dirPath = "instances/speedy/" + t + "/";
        Calendar endDate = (Calendar) startDate.clone();
        endDate.add(Calendar.DATE, t - 1);
        String filePath = dirPath + getFileName(branch, startDate, endDate);
        File file = new File(filePath);
        if (!file.exists() || !file.isFile())
            throw new IllegalArgumentException("Cannot find dataset " + filePath);
        return readCSV(file);
    }

    public List<Service> readCSV(File csv) {
        List<Location> locations = new ArrayList<>();
        List<Calendar> releaseDates = new ArrayList<>();
        List<Integer> days = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(csv));
            String line = reader.readLine();
            int count = 0;
            while (line != null) {
                String[] splitted = line.split(",");

                double lat = Double.parseDouble(splitted[0]);
                double lng = Double.parseDouble(splitted[1]);
                Location loc = new Location(Integer.toString(count), lat, lng);
                locations.add(loc);

                TemporalAccessor tacc = DateTimeFormatter.ISO_INSTANT.parse(splitted[2]);
                Instant instant = Instant.from(tacc);
                ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
                Calendar from = GregorianCalendar.from(zdt);
                releaseDates.add(from);

                days.add(1); // ndays, TODO: do better
                scores.add(1); // scores, TODO: do better

                line = reader.readLine();
                count++;
            }
            reader.close();

            List<Integer> releaseDays = parseReleaseDates(releaseDates);

            return IntStream.range(0, locations.size())
                    .mapToObj(i -> new Service(locations.get(i).getId(), locations.get(i), releaseDays.get(i),
                            releaseDays.get(i) + days.get(i) - 1))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(String branch, Calendar from, Calendar to) {
        return branch + "-" + formatCalendar(from) + "-" + formatCalendar(to) + ".csv";
    }

    private String formatCalendar(Calendar c) {
        int date = c.get(Calendar.DATE);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        return date + "-" + (month + 1) + "-" + year;
    }

    public static List<Integer> parseReleaseDates(final List<Calendar> releaseDates) {
        List<Long> releaseDays = releaseDates.stream()
                .map(Calendar::getTimeInMillis)
                .map(TimeUnit.MILLISECONDS::toDays)
                .collect(Collectors.toList());

        Long min = releaseDays.stream().min(Long::compareTo).orElse(0L);

        return releaseDays.stream().map(rd -> rd - min).mapToInt(Long::intValue).boxed().collect(Collectors.toList());
    }

    public static List<Integer> parseDueDates(final List<Integer> releaseDays, final List<Integer> days) {
        return IntStream.range(0, releaseDays.size())
                .map(i -> releaseDays.get(i) + days.get(i) - 1)
                .boxed()
                .collect(Collectors.toList());
    }
}
