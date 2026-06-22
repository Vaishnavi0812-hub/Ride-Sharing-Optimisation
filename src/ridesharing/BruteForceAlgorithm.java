package ridesharing;

import java.util.*;

/**
 * Brute Force – All Assignments
 *
 * Enumerate every permutation of drivers and compute the total cost.
 * The permutation with the lowest total cost is the optimal solution.
 *
 * Time Complexity:  O(n!)   – practical only for n ≤ 8
 * Space Complexity: O(n)
 */
public class BruteForceAlgorithm {

    private static double   bestCost;
    private static int[]    bestPerm;

    public static List<Assignment> assign(List<Driver> drivers, List<Passenger> passengers) {
        int n = Math.min(drivers.size(), passengers.size());

        // Build cost matrix
        double[][] cost = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                cost[i][j] = drivers.get(i).distanceTo(passengers.get(j));

        // Initialise global best
        bestCost = Double.MAX_VALUE;
        bestPerm = new int[n];

        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;

        permute(perm, 0, cost, n);

        // Build result
        List<Assignment> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int j = bestPerm[i];
            result.add(new Assignment(drivers.get(i), passengers.get(j),
                                      cost[i][j], "BruteForce"));
        }
        return result;
    }

    /** Heap's algorithm to generate permutations in-place. */
    private static void permute(int[] arr, int k, double[][] cost, int n) {
        if (k == n) {
            double total = 0;
            for (int i = 0; i < n; i++) total += cost[i][arr[i]];
            if (total < bestCost) {
                bestCost = total;
                bestPerm = Arrays.copyOf(arr, n);
            }
            return;
        }
        for (int i = k; i < n; i++) {
            swap(arr, i, k);
            permute(arr, k + 1, cost, n);
            swap(arr, i, k);
        }
    }

    private static void swap(int[] a, int i, int j) {
        int t = a[i]; a[i] = a[j]; a[j] = t;
    }

    public static double totalCost(List<Assignment> assignments) {
        double sum = 0;
        for (Assignment a : assignments) sum += a.distance;
        return sum;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TIMED VERSION — records start time, end time, and actual step count
    // ════════════════════════════════════════════════════════════════════════

    private static long permCount;   // counts how many full permutations are evaluated

    /**
     * Runs the Brute Force algorithm with full timing instrumentation.
     *
     * HOW TIME COMPLEXITY IS CALCULATED HERE:
     *   - System.nanoTime() is captured before and after the algorithm.
     *   - Every time the recursive permute() method reaches a complete
     *     arrangement (base case k == n), permCount is incremented.
     *   - For n items the number of complete arrangements is exactly n!
     *     (n factorial), so permCount ends up equalling n!.
     *   - We also compute n! mathematically and store it as the theoretical
     *     value so the dashboard can compare actual vs theoretical.
     */
    public static ComplexityResult assignWithTiming(List<Driver> drivers,
                                                    List<Passenger> passengers) {
        int n = Math.min(drivers.size(), passengers.size());

        double[][] cost = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                cost[i][j] = drivers.get(i).distanceTo(passengers.get(j));

        bestCost  = Double.MAX_VALUE;
        bestPerm  = new int[n];
        permCount = 0;                         // reset before the run

        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;

        // ── START TIMER ──────────────────────────────────────────────────────
        long startTime = System.nanoTime();

        permuteTimed(perm, 0, cost, n);        // uses permCount

        // ── END TIMER ────────────────────────────────────────────────────────
        long endTime = System.nanoTime();

        List<Assignment> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int j = bestPerm[i];
            result.add(new Assignment(drivers.get(i), passengers.get(j),
                                      cost[i][j], "BruteForce"));
        }

        long theoretical = factorial(n);       // n!

        return new ComplexityResult(
            "BruteForce", n,
            startTime, endTime,
            permCount, theoretical, "n! (n factorial)",
            result
        );
    }

    /** Identical to permute() but increments permCount at the base case. */
    private static void permuteTimed(int[] arr, int k, double[][] cost, int n) {
        if (k == n) {
            permCount++;                       // << COUNT each complete permutation
            double total = 0;
            for (int i = 0; i < n; i++) total += cost[i][arr[i]];
            if (total < bestCost) {
                bestCost = total;
                bestPerm = Arrays.copyOf(arr, n);
            }
            return;
        }
        for (int i = k; i < n; i++) {
            swap(arr, i, k);
            permuteTimed(arr, k + 1, cost, n);
            swap(arr, i, k);
        }
    }

    private static long factorial(int n) {
        long f = 1;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }
}
