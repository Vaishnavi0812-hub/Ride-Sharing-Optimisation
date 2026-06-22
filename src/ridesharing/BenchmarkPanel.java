package ridesharing;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Runs all 3 algorithms at multiple input sizes and plots
 * real measured timing side-by-side — demonstrating O(n²) vs O(n³) vs O(n!)
 * visually with actual wall-clock measurements.
 *
 * Greedy    → tested at large n (100-2000) to make O(n²) visible
 * Hungarian → tested at medium n (20-250)  to make O(n³) visible
 * BruteForce→ tested at small n (5-11)     to make O(n!) visible
 */
public class BenchmarkPanel extends JPanel {

    // Input sizes chosen so each algorithm's growth curve is clearly visible
    private static final int[] GREEDY_SIZES    = {100, 300, 500, 800, 1200, 2000};
    private static final int[] HUNGARIAN_SIZES = {20,  50, 100, 150,  200,  250};
    private static final int[] BRUTE_SIZES     = {5,    6,   7,   8,    9,   10,  11};

    private long[] greedyTimes;
    private long[] hungarianTimes;
    private long[] bruteTimes;

    private boolean benchmarkDone = false;

    private JButton  runBtn;
    private JLabel   statusLabel;
    private JPanel   chartArea;

    private static final Color COL_GREEDY    = new Color(0xFF6B6B);
    private static final Color COL_HUNGARIAN = new Color(0x4ECDC4);
    private static final Color COL_BRUTE     = new Color(0xFFE66D);
    private static final Color COL_BG        = new Color(0x16213E);
    private static final Color COL_CHART_BG  = new Color(0x0F3460);

    // ── Constructor ─────────────────────────────────────────────────────────
    public BenchmarkPanel() {
        setBackground(COL_BG);
        setLayout(new BorderLayout(8, 8));
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildChartArea(), BorderLayout.CENTER);
        add(buildInfoBar(),  BorderLayout.SOUTH);
    }

    // ── UI build helpers ─────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bar.setBackground(new Color(0x0D0D1A));

        JLabel title = new JLabel("Real-Time Complexity Benchmark");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(new Color(0xFFD700));
        bar.add(title);

        runBtn = new JButton("Run Benchmark");
        styleButton(runBtn, new Color(0x4CAF50));
        runBtn.addActionListener(e -> runBenchmark());
        bar.add(runBtn);

        statusLabel = new JLabel("Click 'Run Benchmark' to see real timing differences.");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(0x888888));
        bar.add(statusLabel);

        return bar;
    }

    private JPanel buildChartArea() {
        chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (benchmarkDone) drawAllCharts(g2);
                else               drawPlaceholder(g2);
            }
        };
        chartArea.setBackground(COL_BG);
        return chartArea;
    }

    private JPanel buildInfoBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 6));
        bar.setBackground(new Color(0x0D0D1A));

        bar.add(makeInfoLabel("Greedy tested at n=100..2000",    COL_GREEDY));
        bar.add(makeInfoLabel("Hungarian tested at n=20..250",   COL_HUNGARIAN));
        bar.add(makeInfoLabel("BruteForce tested at n=5..11",    COL_BRUTE));

        return bar;
    }

    // ── Benchmark runner (SwingWorker keeps UI responsive) ───────────────────

    private void runBenchmark() {
        runBtn.setEnabled(false);
        benchmarkDone = false;
        chartArea.repaint();

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {

            @Override
            protected Void doInBackground() {
                // ── Greedy ────────────────────────────────────────────────────
                greedyTimes = new long[GREEDY_SIZES.length];
                for (int i = 0; i < GREEDY_SIZES.length; i++) {
                    int n = GREEDY_SIZES[i];
                    publish("Greedy n=" + n + "…");
                    List<Driver>    d = makeDrivers(n);
                    List<Passenger> p = makePassengers(n);
                    // Warm-up JIT
                    if (i == 0) GreedyAlgorithm.assign(d, p);
                    long t = System.currentTimeMillis();
                    GreedyAlgorithm.assign(d, p);
                    greedyTimes[i] = System.currentTimeMillis() - t;
                }

                // ── Hungarian ────────────────────────────────────────────────
                hungarianTimes = new long[HUNGARIAN_SIZES.length];
                for (int i = 0; i < HUNGARIAN_SIZES.length; i++) {
                    int n = HUNGARIAN_SIZES[i];
                    publish("Hungarian n=" + n + "…");
                    List<Driver>    d = makeDrivers(n);
                    List<Passenger> p = makePassengers(n);
                    if (i == 0) HungarianAlgorithm.assign(d, p);
                    long t = System.currentTimeMillis();
                    HungarianAlgorithm.assign(d, p);
                    hungarianTimes[i] = System.currentTimeMillis() - t;
                }

                // ── BruteForce ───────────────────────────────────────────────
                bruteTimes = new long[BRUTE_SIZES.length];
                for (int i = 0; i < BRUTE_SIZES.length; i++) {
                    int n = BRUTE_SIZES[i];
                    publish("BruteForce n=" + n + "…");
                    List<Driver>    d = makeDrivers(n);
                    List<Passenger> p = makePassengers(n);
                    long t = System.currentTimeMillis();
                    BruteForceAlgorithm.assign(d, p);
                    bruteTimes[i] = System.currentTimeMillis() - t;
                }

                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try { get(); } catch (Exception ignored) {}
                benchmarkDone = true;
                runBtn.setEnabled(true);
                statusLabel.setText("Done!  Each curve shows real growth — steeper = worse scaling.");
                chartArea.repaint();
            }
        };
        worker.execute();
    }

    // ── Painting ─────────────────────────────────────────────────────────────

    private void drawAllCharts(Graphics2D g2) {
        int w  = chartArea.getWidth();
        int h  = chartArea.getHeight();
        int cw = (w - 80) / 3;   // chart width per algorithm
        int ch = h - 60;
        int cy = 20;
        int gap = 20;

        drawLineChart(g2, gap,              cy, cw, ch,
                      GREEDY_SIZES,    greedyTimes,    COL_GREEDY,
                      "Greedy  O(n²)", "n (drivers)");

        drawLineChart(g2, gap + cw + gap,   cy, cw, ch,
                      HUNGARIAN_SIZES, hungarianTimes, COL_HUNGARIAN,
                      "Hungarian  O(n³)", "n (drivers)");

        drawLineChart(g2, gap + (cw+gap)*2, cy, cw, ch,
                      BRUTE_SIZES,     bruteTimes,     COL_BRUTE,
                      "Brute Force  O(n!)", "n (drivers)");
    }

    /**
     * Draws a single line chart inside the rectangle (x,y,w,h).
     */
    private void drawLineChart(Graphics2D g2, int x, int y, int w, int h,
                               int[] xVals, long[] yVals, Color lineCol,
                               String title, String xLabel) {

        final int padL = 52, padR = 10, padT = 35, padB = 45;
        int plotX = x + padL;
        int plotY = y + padT;
        int plotW = w - padL - padR;
        int plotH = h - padT - padB;

        // Panel background
        g2.setColor(COL_CHART_BG);
        g2.fillRoundRect(x, y, w, h, 14, 14);

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(lineCol);
        g2.drawString(title, x + (w - fm.stringWidth(title)) / 2, y + 22);

        // Axes
        g2.setColor(new Color(0x4A4A7A));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(plotX, plotY,         plotX, plotY + plotH);   // Y
        g2.drawLine(plotX, plotY + plotH, plotX + plotW, plotY + plotH); // X

        // Max Y value (ensure at least 1ms to avoid division by zero)
        long maxY = 1;
        for (long v : yVals) if (v > maxY) maxY = v;

        // Y-axis tick labels  (0, mid, max)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(new Color(0xAAAAAA));
        String[] yLabels  = { "0 ms", (maxY/2) + " ms", maxY + " ms" };
        int[]    yTickPts = { plotY + plotH, plotY + plotH/2, plotY };
        for (int i = 0; i < 3; i++) {
            g2.drawLine(plotX - 3, yTickPts[i], plotX + 3, yTickPts[i]);
            String lbl = yLabels[i];
            g2.drawString(lbl, x + 2, yTickPts[i] + 4);
            // Horizontal grid line (dotted)
            if (i > 0) {
                g2.setColor(new Color(0x2A2A5A));
                float[] dash = {4f, 4f};
                g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
                                             BasicStroke.JOIN_MITER, 1f, dash, 0f));
                g2.drawLine(plotX, yTickPts[i], plotX + plotW, yTickPts[i]);
                g2.setColor(new Color(0xAAAAAA));
                g2.setStroke(new BasicStroke(1.5f));
            }
        }

        // Compute point positions
        int n = xVals.length;
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            px[i] = plotX + i * plotW / (n - 1);
            py[i] = plotY + plotH - (int)(yVals[i] * plotH / maxY);
        }

        // Shaded area under the curve
        int[] polyX = new int[n + 2];
        int[] polyY = new int[n + 2];
        for (int i = 0; i < n; i++) { polyX[i] = px[i]; polyY[i] = py[i]; }
        polyX[n]   = px[n-1]; polyY[n]   = plotY + plotH;
        polyX[n+1] = px[0];   polyY[n+1] = plotY + plotH;
        g2.setColor(new Color(lineCol.getRed(), lineCol.getGreen(), lineCol.getBlue(), 40));
        g2.setStroke(new BasicStroke(1f));
        g2.fillPolygon(polyX, polyY, n + 2);

        // Line
        g2.setColor(lineCol);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) g2.drawLine(px[i], py[i], px[i+1], py[i+1]);

        // Dots + X labels + value balloons
        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        for (int i = 0; i < n; i++) {
            // Dot
            g2.setColor(COL_CHART_BG);
            g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
            g2.setColor(lineCol);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(px[i] - 5, py[i] - 5, 10, 10);
            g2.setStroke(new BasicStroke(1f));

            // X-axis tick label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(new Color(0xCCCCCC));
            String xl = String.valueOf(xVals[i]);
            int xlw = g2.getFontMetrics().stringWidth(xl);
            g2.drawLine(px[i], plotY + plotH, px[i], plotY + plotH + 4);
            g2.drawString(xl, px[i] - xlw/2, plotY + plotH + 14);

            // Value balloon above dot
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            String val = yVals[i] + "ms";
            int vw = g2.getFontMetrics().stringWidth(val) + 6;
            int bx = px[i] - vw/2;
            int by = py[i] - 22;
            g2.setColor(lineCol.darker());
            g2.fillRoundRect(bx, by, vw, 14, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(val, bx + 3, by + 11);
        }

        // X-axis label
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(0xAAAAAA));
        fm = g2.getFontMetrics();
        g2.drawString(xLabel, plotX + (plotW - fm.stringWidth(xLabel))/2, y + h - 5);
    }

    private void drawPlaceholder(Graphics2D g2) {
        int w = chartArea.getWidth();
        int h = chartArea.getHeight();

        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(new Color(0x555577));
        String line1 = "Click 'Run Benchmark' to run algorithms at large inputs";
        String line2 = "and visualise how O(n²), O(n³), and O(n!) grow at different rates.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(line1, (w - fm.stringWidth(line1))/2, h/2 - 30);
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        g2.setColor(new Color(0x444466));
        fm = g2.getFontMetrics();
        g2.drawString(line2, (w - fm.stringWidth(line2))/2, h/2 - 8);

        // Theoretical operations preview table
        drawTheoreticalTable(g2, w, h);
    }

    private void drawTheoreticalTable(Graphics2D g2, int w, int h) {
        int tx = w/2 - 280;
        int ty = h/2 + 30;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("Theoretical operation counts (why sizes are chosen differently):", tx, ty);

        String[][] rows = {
            {"n",  "Greedy n²",  "Hungarian n³", "BruteForce n!"},
            {"5",  "25",         "125",           "120"},
            {"7",  "49",         "343",           "5,040"},
            {"10", "100",        "1,000",         "3,628,800"},
            {"11", "121",        "1,331",         "39,916,800"},
            {"100","10,000",     "1,000,000",     "~10^157 (impossible!)"},
        };
        Color[] colColors = {Color.WHITE, COL_GREEDY, COL_HUNGARIAN, COL_BRUTE};
        int[] colX = {tx, tx+60, tx+170, tx+310};

        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        for (int r = 0; r < rows.length; r++) {
            int ry = ty + 22 + r * 20;
            if (r == 0) {
                g2.setColor(new Color(0x444466));
                g2.fillRoundRect(tx - 5, ry - 14, 545, 18, 4, 4);
            } else if (r % 2 == 0) {
                g2.setColor(new Color(0x1A1A3A));
                g2.fillRoundRect(tx - 5, ry - 14, 545, 18, 4, 4);
            }
            for (int c = 0; c < 4; c++) {
                g2.setColor(r == 0 ? Color.WHITE : colColors[c]);
                g2.drawString(rows[r][c], colX[c], ry);
            }
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static List<Driver> makeDrivers(int n) {
        List<Driver> list = new ArrayList<>();
        Random rng = new Random(7);
        for (int i = 0; i < n; i++)
            list.add(new Driver(i+1, "D"+i, rng.nextDouble()*100, rng.nextDouble()*100));
        return list;
    }

    private static List<Passenger> makePassengers(int n) {
        List<Passenger> list = new ArrayList<>();
        Random rng = new Random(13);
        for (int i = 0; i < n; i++)
            list.add(new Passenger(i+1, "P"+i, rng.nextDouble()*100, rng.nextDouble()*100));
        return list;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));
    }

    private JLabel makeInfoLabel(String text, Color col) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(col);
        return lbl;
    }
}
