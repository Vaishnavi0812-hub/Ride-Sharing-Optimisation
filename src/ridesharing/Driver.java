package ridesharing;

public class Driver {
    public int id;
    public String name;
    public double x, y;
    public boolean assigned;

    public Driver(int id, String name, double x, double y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.assigned = false;
    }

    public double distanceTo(Passenger p) {
        return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
    }

    @Override
    public String toString() {
        return "Driver " + id + " (" + name + ")";
    }
}
