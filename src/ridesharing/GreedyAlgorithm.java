package ridesharing;

import java.util.*;

/**
 * Greedy Nearest Driver Heuristic
 *
 * Approach:
 *   For each passenger (in order), scan all unassigned drivers and pick
 *   the one with the smallest Euclidean distance.  This is a simple O(n²)
 *   heuristic – fast but NOT guaranteed optimal.
 *
 * Time Complexity:
 *   Best / Average / Worst: O(n²)  where n = number of passengers (= drivers)
 *
 * Space Complexity: O(n)
 */
public class GreedyAlgorithm {

    public static List<Assignment> assign(List<Driver> drivers, List<Passenger> passengers) {
        // Work on copies so originals stay clean
        List<Driver>    dCopy = deepCopyDrivers(drivers);
        List<Passenger> pCopy = deepCopyPassengers(passengers);

        List<Assignment> result = new ArrayList<>();

        // Step 1 – iterate over every passenger
        for (Passenger p : pCopy) {
            Driver best  = null;
            double bestD = Double.MAX_VALUE;

            // Step 2 – find the nearest unassigned driver
            for (Driver d : dCopy) {
                if (!d.assigned) {
                    double dist = d.distanceTo(p);
                    if (dist < bestD) {
                        bestD = dist;
                        best  = d;
                    }
                }
            }

            // Step 3 – commit the assignment
            if (best != null) {
                best.assigned = true;
                p.assigned    = true;
                result.add(new Assignment(best, p, bestD, "Greedy"));
            }
        }

        return result;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static List<Driver> deepCopyDrivers(List<Driver> src) {
        List<Driver> out = new ArrayList<>();
        for (Driver d : src) out.add(new Driver(d.id, d.name, d.x, d.y));
        return out;
    }

    private static List<Passenger> deepCopyPassengers(List<Passenger> src) {
        List<Passenger> out = new ArrayList<>();
        for (Passenger p : src) out.add(new Passenger(p.id, p.name, p.x, p.y));
        return out;
    }

    /** Total waiting time (sum of distances) for a set of assignments. */
    public static double totalCost(List<Assignment> assignments) {
        double sum = 0;
        for (Assignment a : assignments) sum += a.distance;
        return sum;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TIMED VERSION — records start time, end time, and actual step count
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Runs the Greedy algorithm with full timing instrumentation.
     *
     * HOW TIME COMPLEXITY IS CALCULATED HERE:
     *   - We call System.nanoTime() immediately before the algorithm starts
     *     and immediately after it ends.
     *   - Inside the nested loops we increment a counter (stepCount) on every
     *     single comparison between a driver and a passenger.
     *   - Because there are n passengers and for each passenger we check all
     *     n drivers, the counter reaches exactly n*n (= n²) in the worst case.
     *   - We also compute the theoretical value n² and store both so the
     *     dashboard can compare actual vs theoretical.
     */
    public static ComplexityResult assignWithTiming(List<Driver> drivers,
                                                    List<Passenger> passengers) {
        int n = Math.min(drivers.size(), passengers.size());

        List<Driver>     dCopy  = deepCopyDrivers(drivers);
        List<Passenger>  pCopy  = deepCopyPassengers(passengers);
        List<Assignment> result = new ArrayList<>();

        long stepCount = 0;

        // ── START TIMER ──────────────────────────────────────────────────────
        long startTime = System.nanoTime();

        for (Passenger p : pCopy) {
            Driver best  = null;
            double bestD = Double.MAX_VALUE;

            for (Driver d : dCopy) {
                stepCount++;                   // << COUNT every driver check
                if (!d.assigned) {
                    double dist = d.distanceTo(p);
                    if (dist < bestD) {
                        bestD = dist;
                        best  = d;
                    }
                }
            }

            if (best != null) {
                best.assigned = true;
                p.assigned    = true;
                result.add(new Assignment(best, p, bestD, "Greedy"));
            }
        }

        // ── END TIMER ────────────────────────────────────────────────────────
        long endTime = System.nanoTime();

        long theoretical = (long) n * n;       // n²

        return new ComplexityResult(
            "Greedy", n,
            startTime, endTime,
            stepCount, theoretical, "n x n = n^2",
            result
        );
    }
}
