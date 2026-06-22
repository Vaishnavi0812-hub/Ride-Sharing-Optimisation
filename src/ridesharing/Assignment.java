package ridesharing;

public class Assignment {
    public Driver driver;
    public Passenger passenger;
    public double distance;
    public String algorithm;

    public Assignment(Driver driver, Passenger passenger, double distance, String algorithm) {
        this.driver = driver;
        this.passenger = passenger;
        this.distance = distance;
        this.algorithm = algorithm;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s (%.2f units)", driver.name, passenger.name, distance);
    }
}
