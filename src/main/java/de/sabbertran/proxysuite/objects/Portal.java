package de.sabbertran.proxysuite.objects;

public class Portal {

    private int id;
    private String name, type;
    private Location loc1, loc2;
    private String destination;

    public Portal(String name, String type, Location loc1, Location loc2, String destination) {
        this(-1, name, type, loc1, loc2, destination);
    }

    public Portal(int id, String name, String type, Location loc1, Location loc2, String destination) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.destination = destination;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Location getLoc1() {
        return loc1;
    }

    public Location getLoc2() {
        return loc2;
    }

    public String getDestination() {
        return destination;
    }

    public void setId(int id) {
        this.id = id;
    }
}
