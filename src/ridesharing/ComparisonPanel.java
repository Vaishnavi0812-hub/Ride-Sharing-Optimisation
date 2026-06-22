package ridesharing;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Bar-chart comparing total cost and per-assignment costs
 * for all three algorithms side by side.
 */
public class ComparisonPanel extends JPanel {

    private double greedyCost   = 0;
    private double hungarianCost = 0;
    private double bruteForceCost = 0;

    private List<Assignment> greedyAssignments;
    private List<Assignment> hungarianAssignments;
    private List<Assignment> bruteForceAssignments;

    private static final Color COL_GREEDY    = new Color(0xFF6B6B);
    private static final Color COL_HUNGARIAN = new Color(0x4ECDC4);
    private static final Color COL_BRUTE     = new Color(0xFFE66D);
    private static final Color COL_BG        = new Color(0x16213E);

    public ComparisonPanel() {
        setBackground(COL_BG);
        setPreferredSize(new Dimension(600, 500));
    }

    public void update(List<Assignment> greedy, List<Assignment> hungarian,
                       List<Assignment> brute) {
        this.greedyAssignments    = greedy;
        this.hungarianAssignments = hungarian;
        this.bruteForceAssignments = brute;
        greedyCost    = sum(greedy);
        hungarianCost = sum(hungarian);
        bruteForceCost = sum(brute);
        repaint();
    }

    private double sum(List<Assignment> list) {
        if (list == null) return 0;
        double s = 0;
        for (Assignment a : list) s += a.distance;
        return s;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (greedyAssignments == null) {
            drawPlaceholder(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawTotalCostChart(g2);
        drawPerAssignmentTable(g2);
        drawComplexityInfo(g2);
    }

    private void drawTotalCostChart(Graphics2D g2) {
        int chartX = 60, chartY = 60;
        int chartW = getWidth() - 120, chartH = 180;

        // Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("Total Waiting Time (Sum of Distances)", chartX, chartY - 10);

        // Background
        g2.setColor(new Color(0x0F3460));
        g2.fillRoundRect(chartX, chartY, chartW, chartH, 12, 12);

        double maxCost = Math.max(greedyCost, Math.max(hungarianCost, bruteForceCost));
        if (maxCost == 0) maxCost = 1;

        int barCount = 3;
        int barW     = (chartW - 40) / (barCount * 2 + 1);
        int gap      = barW;
        int startX   = chartX + gap;

        double[] costs  = {greedyCost, hungarianCost, bruteForceCost};
        Color[]  colors = {COL_GREEDY, COL_HUNGARIAN, COL_BRUTE};
        String[] labels = {"Greedy", "Hungarian", "Brute Force"};

        for (int i = 0; i < barCount; i++) {
            int bx = startX + i * (barW + gap);
            int bh = (int) (costs[i] / maxCost * (chartH - 40));
            int by = chartY + chartH - 20 - bh;

            // Bar shadow
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(bx + 3, by + 3, barW, bh, 6, 6);

            // Bar fill with gradient
            GradientPaint gp = new GradientPaint(bx, by, colors[i].brighter(),
                                                 bx, by + bh, colors[i].darker());
            g2.setPaint(gp);
            g2.fillRoundRect(bx, by, barW, bh, 6, 6);

            // Bar border
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(bx, by, barW, bh, 6, 6);
            g2.setStroke(new BasicStroke(1f));

            // Value on top
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(Color.WHITE);
            String val = String.format("%.1f", costs[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(val, bx + (barW - fm.stringWidth(val)) / 2, by - 5);

            // Label below
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(colors[i]);
            fm = g2.getFontMetrics();
            g2.drawString(labels[i], bx + (barW - fm.stringWidth(labels[i])) / 2,
                          chartY + chartH - 3);
        }

        // Optimal annotation
        double optimal = Math.min(hungarianCost, bruteForceCost);
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g2.setColor(new Color(0x00FF88));
        g2.drawString("Optimal (Hungarian/BruteForce): " + String.format("%.2f", optimal),
                      chartX, chartY + chartH + 18);
    }

    private void drawPerAssignmentTable(Graphics2D g2) {
        int tableY = 285;
        int col1 = 20, col2 = 180, col3 = 340, col4 = 490;
        int rowH  = 22;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("Per-Assignment Distances", col1, tableY);

        tableY += 8;
        // Header
        g2.setColor(new Color(0x0F3460));
        g2.fillRoundRect(col1 - 5, tableY, getWidth() - 30, rowH + 2, 6, 6);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        g2.drawString("Pair", col1, tableY + 15);
        g2.setColor(COL_GREEDY);    g2.drawString("Greedy",     col2, tableY + 15);
        g2.setColor(COL_HUNGARIAN); g2.drawString("Hungarian",  col3, tableY + 15);
        g2.setColor(COL_BRUTE);     g2.drawString("Brute Force",col4, tableY + 15);

        int rows = 0;
        if (greedyAssignments != null) rows = greedyAssignments.size();

        for (int i = 0; i < rows; i++) {
            int ry = tableY + (i + 1) * (rowH + 2) + 2;
            if (ry > getHeight() - 80) break;

            g2.setColor(i % 2 == 0 ? new Color(0x1A2A5E) : new Color(0x142040));
            g2.fillRoundRect(col1 - 5, ry - rowH + 4, getWidth() - 30, rowH, 4, 4);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            Assignment ga = greedyAssignments.get(i);
            g2.setColor(Color.WHITE);
            g2.drawString("D" + ga.driver.id + " → P" + ga.passenger.id, col1, ry);

            g2.setColor(COL_GREEDY);
            g2.drawString(String.format("%.2f", ga.distance), col2, ry);

            if (hungarianAssignments != null && i < hungarianAssignments.size()) {
                g2.setColor(COL_HUNGARIAN);
                g2.drawString(String.format("%.2f", hungarianAssignments.get(i).distance), col3, ry);
            }
            if (bruteForceAssignments != null && i < bruteForceAssignments.size()) {
                g2.setColor(COL_BRUTE);
                g2.drawString(String.format("%.2f", bruteForceAssignments.get(i).distance), col4, ry);
            }
        }
    }

    private void drawComplexityInfo(Graphics2D g2) {
        int n = (greedyAssignments != null) ? greedyAssignments.size() : 5;

        // ── Theoretical operations bar chart (log scale) ─────────────────────
        int secY = getHeight() - 170;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("Theoretical Operations for n = " + n + "  (log scale — each step = 10x)", 20, secY);

        long opG = (long) n * n;                      // n²
        long opH = (long) n * n * n;                  // n³
        long opB = factorial(n);                       // n!  (capped at Long.MAX_VALUE)

        double logG = Math.log10(Math.max(opG, 1));
        double logH = Math.log10(Math.max(opH, 1));
        double logB = Math.log10(Math.max(opB, 1));
        double maxLog = Math.max(logG, Math.max(logH, logB));
        if (maxLog < 1) maxLog = 1;

        int barMaxH = 80;
        int barW    = 60;
        int barGap  = 40;
        int barBaseY = secY + barMaxH + 20;
        int startX  = 60;

        double[]  logVals  = {logG, logH, logB};
        long[]    opVals   = {opG,  opH,  opB};
        Color[]   cols     = {COL_GREEDY, COL_HUNGARIAN, COL_BRUTE};
        String[]  names    = {"Greedy\nn²", "Hungarian\nn³", "BruteForce\nn!"};
        String[]  formulas = {"n²=" + fmt(opG), "n³=" + fmt(opH), "n!=" + fmt(opB)};

        for (int i = 0; i < 3; i++) {
            int bh = (int) (logVals[i] / maxLog * barMaxH);
            int bx = startX + i * (barW + barGap);
            int by = barBaseY - bh;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(bx + 3, by + 3, barW, bh, 6, 6);

            // Gradient bar
            GradientPaint gp = new GradientPaint(bx, by, cols[i].brighter(),
                                                 bx, barBaseY, cols[i].darker());
            g2.setPaint(gp);
            g2.fillRoundRect(bx, by, barW, bh, 6, 6);

            g2.setColor(Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(1.5f));
            g2.drawRoundRect(bx, by, barW, bh, 6, 6);
            g2.setStroke(new java.awt.BasicStroke(1f));

            // Operation count above bar
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            String val = formulas[i];
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(val, bx + (barW - fm.stringWidth(val)) / 2, by - 4);

            // Algorithm name below bar
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(cols[i]);
            String[] parts = names[i].split("\n");
            g2.drawString(parts[0], bx + (barW - fm.stringWidth(parts[0])) / 2, barBaseY + 14);
            g2.setFont(new Font("Consolas", Font.BOLD, 10));
            g2.drawString(parts[1], bx + (barW - fm.stringWidth(parts[1])) / 2, barBaseY + 26);
        }

        // Log scale note + complexity labels
        int noteX = startX + 3 * (barW + barGap) + 20;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(new Color(0x888888));
        g2.drawString("Time Complexity:", noteX, secY + 18);
        g2.setColor(COL_GREEDY);
        g2.drawString("  Greedy     O(n²)", noteX, secY + 36);
        g2.setColor(COL_HUNGARIAN);
        g2.drawString("  Hungarian  O(n³)", noteX, secY + 52);
        g2.setColor(COL_BRUTE);
        g2.drawString("  BruteForce O(n!)", noteX, secY + 68);

        g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g2.setColor(new Color(0x00FF88));
        g2.drawString("Hungarian & BruteForce = OPTIMAL", noteX, secY + 90);
        g2.setColor(new Color(0xFF9944));
        g2.drawString("Greedy = fast but may not be optimal", noteX, secY + 106);

        // Use 'Performance Benchmark' tip
        g2.setColor(new Color(0x6688AA));
        g2.drawString("Use 'Performance Benchmark'", noteX, secY + 128);
        g2.drawString("button to see real timing.", noteX, secY + 142);
    }

    private static long factorial(int n) {
        if (n > 18) return Long.MAX_VALUE; // overflow guard
        long f = 1;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }

    private static String fmt(long v) {
        if (v == Long.MAX_VALUE) return ">10^18";
        if (v >= 1_000_000_000L) return String.format("%.1fB", v / 1_000_000_000.0);
        if (v >= 1_000_000L)     return String.format("%.1fM", v / 1_000_000.0);
        if (v >= 1_000L)         return String.format("%.1fK", v / 1_000.0);
        return String.valueOf(v);
    }

    private void drawPlaceholder(Graphics g) {
        g.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        g.setColor(new Color(0x666666));
        String msg = "Run all algorithms to see the comparison chart";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}
