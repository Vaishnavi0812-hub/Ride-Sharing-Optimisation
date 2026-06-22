package ridesharing;

import java.util.List;

/**
 * Holds everything produced by one "timed" algorithm run:
 *   - the assignments (output of the algorithm)
 *   - actual wall-clock start / end time in nanoseconds
 *   - actual operation count (steps counted inside the algorithm)
 *   - theoretical operation count  (n², n³, or n!)
 *   - the Big-O formula string
 */
public class ComplexityResult {

    public final String          algorithmName;
    public final int             n;                // input size

    // ── Timing ──────────────────────────────────────────────────────────────
    public final long startTimeNanos;              // System.nanoTime() at start
    public final long endTimeNanos;                // System.nanoTime() at end
    public final long elapsedNanos;                // endTime - startTime

    // ── Step counts ─────────────────────────────────────────────────────────
    public final long actualSteps;                 // real operations counted in code
    public final long theoreticalSteps;            // n², n³, or n!
    public final String formula;                   // "n^2", "n^3", "n!"

    // ── Assignments ─────────────────────────────────────────────────────────
    public final List<Assignment> assignments;

    public ComplexityResult(String algorithmName, int n,
                            long startTimeNanos, long endTimeNanos,
                            long actualSteps, long theoreticalSteps, String formula,
                            List<Assignment> assignments) {
        this.algorithmName     = algorithmName;
        this.n                 = n;
        this.startTimeNanos    = startTimeNanos;
        this.endTimeNanos      = endTimeNanos;
        this.elapsedNanos      = endTimeNanos - startTimeNanos;
        this.actualSteps       = actualSteps;
        this.theoreticalSteps  = theoreticalSteps;
        this.formula           = formula;
        this.assignments       = assignments;
    }

    /** Elapsed time in microseconds (easier to read than nanoseconds for small n). */
    public double elapsedMicros()  { return elapsedNanos / 1_000.0; }

    /** Elapsed time in milliseconds. */
    public double elapsedMillis()  { return elapsedNanos / 1_000_000.0; }

    /** Total assignment cost (sum of distances). */
    public double totalCost() {
        double sum = 0;
        for (Assignment a : assignments) sum += a.distance;
        return sum;
    }
}
