package ridesharing;

import java.util.*;

/**
 * Hungarian Algorithm – Bipartite Matching for Optimal Assignment
 *
 * Solves the assignment problem: given an n×n cost matrix (distances),
 * find a perfect matching that minimises the total cost.
 *
 * Implementation: Kuhn–Munkres (classic Hungarian method).
 *
 * Time Complexity:  O(n³)
 * Space Complexity: O(n²)
 */
public class HungarianAlgorithm {

    public static List<Assignment> assign(List<Driver> drivers, List<Passenger> passengers) {
        int n = Math.min(drivers.size(), passengers.size());

        // Build n×n cost matrix (Euclidean distances)
        double[][] cost = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                cost[i][j] = drivers.get(i).distanceTo(passengers.get(j));

        // Run the Hungarian algorithm on the cost matrix
        int[] assignment = hungarian(cost, n);

        // Build result list
        List<Assignment> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int j = assignment[i];
            if (j >= 0) {
                double dist = drivers.get(i).distanceTo(passengers.get(j));
                result.add(new Assignment(drivers.get(i), passengers.get(j), dist, "Hungarian"));
            }
        }
        return result;
    }

    /**
     * Core Hungarian algorithm.
     * Returns int[n] where result[i] = column index assigned to row i.
     */
    private static int[] hungarian(double[][] cost, int n) {
        double[] u = new double[n + 1];   // row potentials
        double[] v = new double[n + 1];   // col potentials
        int[]    p = new int[n + 1];      // p[j] = row matched to column j
        int[]    way = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            double[] minVal = new double[n + 1];
            boolean[] used  = new boolean[n + 1];
            Arrays.fill(minVal, Double.MAX_VALUE);

            do {
                used[j0] = true;
                int    i0 = p[j0];
                double delta = Double.MAX_VALUE;
                int    j1 = -1;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        double cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                        if (cur < minVal[j]) {
                            minVal[j] = cur;
                            way[j] = j0;
                        }
                        if (minVal[j] < delta) {
                            delta = minVal[j];
                            j1 = j;
                        }
                    }
                }

                for (int j = 0; j <= n; j++) {
                    if (used[j]) { u[p[j]] += delta; v[j] -= delta; }
                    else          { minVal[j] -= delta; }
                }
                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0] = p[j1];
                j0 = j1;
            } while (j0 != 0);
        }

        // p[j] = row (1-based) assigned to column j
        int[] result = new int[n];
        Arrays.fill(result, -1);
        for (int j = 1; j <= n; j++)
            if (p[j] > 0)
                result[p[j] - 1] = j - 1;

        return result;
    }

    public static double totalCost(List<Assignment> assignments) {
        double sum = 0;
        for (Assignment a : assignments) sum += a.distance;
        return sum;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TIMED VERSION — records start time, end time, and actual step count
    // ════════════════════════════════════════════════════════════════════════

    // Class-level counter so the private recursive helper can update it.
    private static long stepCounter;

    /**
     * Runs the Hungarian algorithm with full timing instrumentation.
     *
     * HOW TIME COMPLEXITY IS CALCULATED HERE:
     *   - System.nanoTime() is captured before and after the algorithm.
     *   - The innermost computation (calculating reduced cost for each column j)
     *     is the heart of the O(n³) work. We increment stepCounter each time
     *     that line executes.
     *   - The outer loop runs n times (one per driver), the do-while runs up to
     *     n times, the inner for-loop runs n times → n * n * n = n³ total.
     *   - We also compute the theoretical value n³ for comparison.
     */
    public static ComplexityResult assignWithTiming(List<Driver> drivers,
                                                    List<Passenger> passengers) {
        int n = Math.min(drivers.size(), passengers.size());

        double[][] cost = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                cost[i][j] = drivers.get(i).distanceTo(passengers.get(j));

        stepCounter = 0;                       // reset before the run

        // ── START TIMER ──────────────────────────────────────────────────────
        long startTime = System.nanoTime();

        int[] assignment = hungarianTimed(cost, n);   // uses stepCounter

        // ── END TIMER ────────────────────────────────────────────────────────
        long endTime = System.nanoTime();

        List<Assignment> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int j = assignment[i];
            if (j >= 0) {
                double dist = drivers.get(i).distanceTo(passengers.get(j));
                result.add(new Assignment(drivers.get(i), passengers.get(j),
                                          dist, "Hungarian"));
            }
        }

        long theoretical = (long) n * n * n;   // n³

        return new ComplexityResult(
            "Hungarian", n,
            startTime, endTime,
            stepCounter, theoretical, "n x n x n = n^3 (reduced-cost + potential-update loops)",
            result
        );
    }

    /** Identical to hungarian() but increments stepCounter in the inner loop. */
    private static int[] hungarianTimed(double[][] cost, int n) {
        double[]  u    = new double[n + 1];
        double[]  v    = new double[n + 1];
        int[]     p    = new int[n + 1];
        int[]     way  = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            p[0] = i;
            int j0 = 0;
            double[]  minVal = new double[n + 1];
            boolean[] used   = new boolean[n + 1];
            Arrays.fill(minVal, Double.MAX_VALUE);

            do {
                used[j0] = true;
                int    i0    = p[j0];
                double delta = Double.MAX_VALUE;
                int    j1    = -1;

                for (int j = 1; j <= n; j++) {
                    if (!used[j]) {
                        stepCounter++;         // << COUNT each reduced-cost computation
                        double cur = cost[i0 - 1][j - 1] - u[i0] - v[j];
                        if (cur < minVal[j]) {
                            minVal[j] = cur;
                            way[j]    = j0;
                        }
                        if (minVal[j] < delta) {
                            delta = minVal[j];
                            j1    = j;
                        }
                    }
                }

                // Potential update: O(n) per do-while step — second half of O(n²) per augmentation.
                // Counting this loop too so total steps reflect the full n³ work.
                for (int j = 0; j <= n; j++) {
                    stepCounter++;             // << COUNT potential update iterations
                    if (used[j]) { u[p[j]] += delta; v[j] -= delta; }
                    else          { minVal[j] -= delta; }
                }
                j0 = j1;
            } while (p[j0] != 0);

            do {
                int j1 = way[j0];
                p[j0]  = p[j1];
                j0     = j1;
            } while (j0 != 0);
        }

        int[] result = new int[n];
        Arrays.fill(result, -1);
        for (int j = 1; j <= n; j++)
            if (p[j] > 0) result[p[j] - 1] = j - 1;

        return result;
    }
}
