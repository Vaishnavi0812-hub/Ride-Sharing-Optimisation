package ridesharing;

public class Passenger {
    public int id;
    public String name;
    public double x, y;
    public boolean assigned;

    public Passenger(int id, String name, double x, double y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.assigned = false;
    }

    @Override
    public String toString() {
        return "Passenger " + id + " (" + name + ")";
    }
}
