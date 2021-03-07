package it.polimi.domain;

import java.util.Objects;

public class Service {
    private final String id;
    private final Location location;
    private final int releaseDate;
    private final int dueDate;
    private final int demand;

    public Service(String id, Location location, int releaseDate, int dueDate) {
        this.id = id;
        this.location = location;
        this.releaseDate = releaseDate;
        this.dueDate = dueDate;
        this.demand = 0;
    }

    public Service(String id, Location location, int releaseDate, int dueDate, int demand) {
        this.id = id;
        this.location = location;
        this.releaseDate = releaseDate;
        this.dueDate = dueDate;
        this.demand = demand;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public int getDueDate() {
        return dueDate;
    }

    public int getDemand() {
        return demand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(id, service.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
