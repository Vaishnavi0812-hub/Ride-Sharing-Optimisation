package ridesharing;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main interactive dashboard for Ride-Sharing Driver Assignment Optimisation.
 * Three algorithm buttons + a "Compare All" button.
 */
public class Dashboard extends JFrame {

    // ── Data ────────────────────────────────────────────────────────────────
    private List<Driver>    drivers;
    private List<Passenger> passengers;

    private List<Assignment> greedyResult;
    private List<Assignment> hungarianResult;
    private List<Assignment> bruteForceResult;

    private ComplexityResult greedyComplexity;
    private ComplexityResult hungarianComplexity;
    private ComplexityResult bruteForceComplexity;

    // ── UI components ───────────────────────────────────────────────────────
    private MapPanel         mapPanel;
    private ComparisonPanel  comparisonPanel;
    private BenchmarkPanel   benchmarkPanel;
    private ComplexityPanel  complexityPanel;
    private JTextArea       logArea;
    private JLabel          statusLabel;
    private JLabel          costLabel;
    private JSpinner        sizeSpinner;
    private JPanel          cardPanel;
    private CardLayout      cardLayout;

    private static final Color BG_DARK   = new Color(0x0D0D1A);
    private static final Color BG_PANEL  = new Color(0x1A1A2E);
    private static final Color ACC_BLUE  = new Color(0x2196F3);
    private static final Color ACC_GREEN = new Color(0x4CAF50);
    private static final Color ACC_GOLD  = new Color(0xFFD700);
    private static final Color TEXT_MAIN = new Color(0xE0E0E0);

    // ── Entry point ─────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }

    // ── Constructor ─────────────────────────────────────────────────────────
    public Dashboard() {
        super("Ride-Sharing Driver Assignment Optimisation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(8, 8));

        generateData(5);   // default: 5 drivers / 5 passengers
        buildUI();
    }

    // ── Data generation ──────────────────────────────────────────────────────
    private void generateData(int n) {
        drivers    = new ArrayList<>();
        passengers = new ArrayList<>();
        greedyResult    = null;
        hungarianResult = null;
        bruteForceResult = null;

        String[] dNames = {"Alice","Bob","Carlos","Diana","Ethan",
                           "Fiona","George","Hannah","Ivan","Julia"};
        String[] pNames = {"P-Ann","P-Ben","P-Cora","P-Dan","P-Eve",
                           "P-Frank","P-Grace","P-Hank","P-Iris","P-Jack"};

        Random rng = new Random(42);
        for (int i = 0; i < n; i++) {
            double dx = 10 + rng.nextDouble() * 80;
            double dy = 10 + rng.nextDouble() * 80;
            drivers.add(new Driver(i + 1, dNames[i % dNames.length], dx, dy));

            double px = 10 + rng.nextDouble() * 80;
            double py = 10 + rng.nextDouble() * 80;
            passengers.add(new Passenger(i + 1, pNames[i % pNames.length], px, py));
        }
    }

    // ── UI construction ──────────────────────────────────────────────────────
    private void buildUI() {
        add(buildHeader(),     BorderLayout.NORTH);
        add(buildCenter(),     BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(0x0F0F2A));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Title
        JLabel title = new JLabel("Ride-Sharing Driver Assignment Optimisation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACC_GOLD);
        header.add(title, BorderLayout.WEST);

        // Right-side controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setBackground(new Color(0x0F0F2A));

        JLabel sizeLabel = new JLabel("Drivers/Passengers:");
        sizeLabel.setForeground(TEXT_MAIN);
        sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        controls.add(sizeLabel);

        sizeSpinner = new JSpinner(new SpinnerNumberModel(5, 2, 8, 1));
        sizeSpinner.setPreferredSize(new Dimension(60, 28));
        controls.add(sizeSpinner);

        JButton generateBtn = makeButton("Regenerate Data", new Color(0x607D8B));
        generateBtn.addActionListener(e -> {
            generateData((Integer) sizeSpinner.getValue());
            resetDisplay();
            log("New random data generated for " + sizeSpinner.getValue() + " drivers/passengers.");
        });
        controls.add(generateBtn);

        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(BG_DARK);
        center.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        center.add(buildAlgorithmButtons(), BorderLayout.WEST);
        center.add(buildVisualisationArea(), BorderLayout.CENTER);
        center.add(buildLogPanel(), BorderLayout.EAST);

        return center;
    }

    private JPanel buildAlgorithmButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x3A3A6E), 1, true),
            BorderFactory.createEmptyBorder(20, 14, 20, 14)));
        panel.setPreferredSize(new Dimension(195, 0));

        JLabel heading = new JLabel("<html><center>Algorithms</center></html>");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(ACC_GOLD);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createVerticalStrut(20));

        // ── Algorithm buttons ────────────────────────────────────────────────
        JButton greedyBtn = makeAlgoButton("Greedy\n(Nearest Driver)", new Color(0xFF6B6B));
        greedyBtn.setToolTipText("O(n²) – Fast heuristic. May not be optimal.");
        greedyBtn.addActionListener(e -> runGreedy());

        JButton hungarianBtn = makeAlgoButton("Hungarian\n(Bipartite Match)", new Color(0x4ECDC4));
        hungarianBtn.setToolTipText("O(n³) – Guaranteed optimal assignment.");
        hungarianBtn.addActionListener(e -> runHungarian());

        JButton bruteBtn = makeAlgoButton("Brute Force\n(All Assignments)", new Color(0xFFE66D));
        bruteBtn.setToolTipText("O(n!) – Exhaustive search. Optimal but slow.");
        bruteBtn.addActionListener(e -> runBruteForce());

        panel.add(greedyBtn);
        panel.add(Box.createVerticalStrut(12));
        panel.add(hungarianBtn);
        panel.add(Box.createVerticalStrut(12));
        panel.add(bruteBtn);
        panel.add(Box.createVerticalStrut(20));

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(new Color(0x3A3A6E));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(20));

        JButton compareBtn = makeAlgoButton("Compare All\nAlgorithms", new Color(0x9C27B0));
        compareBtn.setToolTipText("Run all three and compare results.");
        compareBtn.addActionListener(e -> runAll());
        panel.add(compareBtn);

        panel.add(Box.createVerticalStrut(10));

        JButton benchBtn = makeAlgoButton("Performance\nBenchmark", new Color(0xFF9800));
        benchBtn.setToolTipText("Measure real timing at large n to show O(n²) vs O(n³) vs O(n!).");
        benchBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "BENCHMARK");
            setStatus("Performance Benchmark – click 'Run Benchmark' in the panel.", -1);
        });
        panel.add(benchBtn);

        panel.add(Box.createVerticalStrut(10));

        JButton complexBtn = makeAlgoButton("Complexity\nAnalysis", new Color(0x00BCD4));
        complexBtn.setToolTipText("Run all 3 algorithms with live timers and step counters.");
        complexBtn.addActionListener(e -> runComplexityAnalysis());
        panel.add(complexBtn);

        panel.add(Box.createVerticalStrut(20));

        // Complexity legend
        panel.add(makeComplexityLabel("Greedy:    O(n²)",    new Color(0xFF6B6B)));
        panel.add(Box.createVerticalStrut(4));
        panel.add(makeComplexityLabel("Hungarian: O(n³)",    new Color(0x4ECDC4)));
        panel.add(Box.createVerticalStrut(4));
        panel.add(makeComplexityLabel("BruteForce: O(n!)",   new Color(0xFFE66D)));

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildVisualisationArea() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DARK);

        mapPanel        = new MapPanel();
        comparisonPanel = new ComparisonPanel();
        benchmarkPanel  = new BenchmarkPanel();
        complexityPanel = new ComplexityPanel();

        cardPanel.add(mapPanel,        "MAP");
        cardPanel.add(comparisonPanel, "COMPARE");
        cardPanel.add(benchmarkPanel,  "BENCHMARK");
        cardPanel.add(complexityPanel, "COMPLEXITY");

        cardLayout.show(cardPanel, "MAP");
        return cardPanel;
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PANEL);
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(0x3A3A6E), 1, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.setPreferredSize(new Dimension(280, 0));

        JLabel heading = new JLabel("Assignment Log");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(ACC_GOLD);
        panel.add(heading, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(0x0D0D1A));
        logArea.setForeground(new Color(0xB0FFB0));
        logArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0x2A2A5E)));
        panel.add(scroll, BorderLayout.CENTER);

        JButton clearBtn = makeButton("Clear Log", new Color(0x455A64));
        clearBtn.addActionListener(e -> logArea.setText(""));
        panel.add(clearBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x0F0F2A));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));

        statusLabel = new JLabel("Ready – select an algorithm or regenerate data.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MAIN);
        bar.add(statusLabel, BorderLayout.WEST);

        costLabel = new JLabel("");
        costLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        costLabel.setForeground(ACC_GREEN);
        bar.add(costLabel, BorderLayout.EAST);

        return bar;
    }

    // ── Algorithm runners ────────────────────────────────────────────────────

    private void runGreedy() {
        long t0 = System.nanoTime();
        greedyResult = GreedyAlgorithm.assign(drivers, passengers);
        long ms = (System.nanoTime() - t0) / 1_000_000;

        double cost = GreedyAlgorithm.totalCost(greedyResult);
        mapPanel.update(drivers, passengers, greedyResult, "Greedy (Nearest Driver Heuristic)");
        cardLayout.show(cardPanel, "MAP");

        setStatus("Greedy completed in " + ms + " ms", cost);
        logAssignments("GREEDY", greedyResult, cost, ms);
    }

    private void runHungarian() {
        long t0 = System.nanoTime();
        hungarianResult = HungarianAlgorithm.assign(drivers, passengers);
        long ms = (System.nanoTime() - t0) / 1_000_000;

        double cost = HungarianAlgorithm.totalCost(hungarianResult);
        mapPanel.update(drivers, passengers, hungarianResult, "Hungarian Algorithm (Bipartite Matching)");
        cardLayout.show(cardPanel, "MAP");

        setStatus("Hungarian completed in " + ms + " ms", cost);
        logAssignments("HUNGARIAN", hungarianResult, cost, ms);
    }

    private void runBruteForce() {
        if (drivers.size() > 8) {
            JOptionPane.showMessageDialog(this,
                "Brute Force is limited to ≤ 8 drivers to avoid long runtimes.\n"
                + "Please reduce the count via the spinner.",
                "Brute Force Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long t0 = System.nanoTime();
        bruteForceResult = BruteForceAlgorithm.assign(drivers, passengers);
        long ms = (System.nanoTime() - t0) / 1_000_000;

        double cost = BruteForceAlgorithm.totalCost(bruteForceResult);
        mapPanel.update(drivers, passengers, bruteForceResult, "Brute Force (All Assignments)");
        cardLayout.show(cardPanel, "MAP");

        setStatus("Brute Force completed in " + ms + " ms", cost);
        logAssignments("BRUTE FORCE", bruteForceResult, cost, ms);
    }

    private void runAll() {
        runGreedy();
        runHungarian();
        if (drivers.size() <= 8) runBruteForce();
        else {
            // provide a dummy result for comparison when n > 8
            bruteForceResult = null;
            log("Brute Force skipped (n > 8).");
        }
        comparisonPanel.update(greedyResult, hungarianResult, bruteForceResult);
        cardLayout.show(cardPanel, "COMPARE");
        setStatus("All algorithms compared.", -1);
        log("─── Comparison chart displayed ───");
    }

    private void runComplexityAnalysis() {
        int n = drivers.size();

        // Run all three algorithms with built-in timers and step counters
        greedyComplexity   = GreedyAlgorithm.assignWithTiming(drivers, passengers);
        hungarianComplexity = HungarianAlgorithm.assignWithTiming(drivers, passengers);

        if (n > 8) {
            JOptionPane.showMessageDialog(this,
                "Brute Force is capped at n=8 for complexity analysis.\nReduce the count via the spinner.",
                "Brute Force Skipped", JOptionPane.WARNING_MESSAGE);
            bruteForceComplexity = null;
        } else {
            bruteForceComplexity = BruteForceAlgorithm.assignWithTiming(drivers, passengers);
        }

        // Save assignments so map view still works
        greedyResult    = greedyComplexity.assignments;
        hungarianResult = hungarianComplexity.assignments;
        bruteForceResult = bruteForceComplexity != null ? bruteForceComplexity.assignments : null;

        // If brute force was skipped, create a dummy result so the panel still renders
        ComplexityResult bfForPanel = bruteForceComplexity;
        if (bfForPanel == null) {
            // Synthetic zero-time result for display purposes
            bfForPanel = new ComplexityResult(
                "BruteForce", n, 0, 0, 0, 0, "n! (skipped – n > 8)", new java.util.ArrayList<>());
        }

        complexityPanel.update(greedyComplexity, hungarianComplexity, bfForPanel);
        cardLayout.show(cardPanel, "COMPLEXITY");

        log("\n=== COMPLEXITY ANALYSIS ===");
        logComplexity(greedyComplexity);
        logComplexity(hungarianComplexity);
        if (bruteForceComplexity != null) logComplexity(bruteForceComplexity);

        setStatus("Complexity Analysis complete for n=" + n, -1);
    }

    private void logComplexity(ComplexityResult r) {
        log(String.format("  [%s] elapsed=%d ns (%.3f us) | steps=%d | theoretical=%d",
            r.algorithmName, r.elapsedNanos,
            r.elapsedMicros(), r.actualSteps, r.theoreticalSteps));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void resetDisplay() {
        greedyResult    = null;
        hungarianResult = null;
        bruteForceResult = null;
        mapPanel.update(null, null, null, "");
        comparisonPanel.update(null, null, null);
        cardLayout.show(cardPanel, "MAP");
        costLabel.setText("");
        statusLabel.setText("Data regenerated – select an algorithm.");
    }

    private void setStatus(String msg, double cost) {
        statusLabel.setText(msg);
        if (cost >= 0)
            costLabel.setText("Total cost: " + String.format("%.2f", cost) + " units");
        else
            costLabel.setText("");
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void logAssignments(String algoName, List<Assignment> list, double cost, long ms) {
        log("\n=== " + algoName + " ===");
        log(String.format("  Time: %d ms | Total cost: %.2f", ms, cost));
        for (int i = 0; i < list.size(); i++) {
            Assignment a = list.get(i);
            log(String.format("  [%d] %s -> %s  dist=%.2f",
                i + 1, a.driver.name, a.passenger.name, a.distance));
        }
    }

    // ── Widget factories ─────────────────────────────────────────────────────

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private JButton makeAlgoButton(String text, Color accent) {
        // Multi-line via HTML
        String html = "<html><center>" + text.replace("\n", "<br>") + "</center></html>";
        JButton btn = new JButton(html);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(0x0F0F2A));
        btn.setForeground(accent);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(accent, 2, true),
            BorderFactory.createEmptyBorder(10, 8, 10, 8)));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(165, 62));
        btn.setPreferredSize(new Dimension(165, 62));

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(accent.darker());
                btn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0x0F0F2A));
                btn.setForeground(accent);
            }
        });
        return btn;
    }

    private JLabel makeComplexityLabel(String text, Color col) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Consolas", Font.PLAIN, 11));
        lbl.setForeground(col);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }
}
