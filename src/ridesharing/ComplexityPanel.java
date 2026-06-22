package ridesharing;

import javax.swing.*;
import java.awt.*;

/**
 * Shows for every algorithm:
 *   1. The exact start/end/elapsed timer values
 *   2. How the time-complexity formula is derived (n substituted in)
 *   3. Actual step count vs theoretical
 *   4. A side-by-side comparison chart (elapsed time + step count)
 */
public class ComplexityPanel extends JPanel {

    private ComplexityResult greedy;
    private ComplexityResult hungarian;
    private ComplexityResult bruteForce;

    private static final Color COL_G  = new Color(0xFF6B6B);   // Greedy    – red
    private static final Color COL_H  = new Color(0x4ECDC4);   // Hungarian – teal
    private static final Color COL_B  = new Color(0xFFE66D);   // BruteForce– yellow
    private static final Color COL_BG = new Color(0x0D0D1A);
    private static final Color COL_CARD = new Color(0x0F1A3A);

    public ComplexityPanel() {
        setBackground(COL_BG);
        setPreferredSize(new Dimension(800, 700));
    }

    /** Called by Dashboard after all three timed runs finish. */
    public void update(ComplexityResult g, ComplexityResult h, ComplexityResult b) {
        this.greedy    = g;
        this.hungarian = h;
        this.bruteForce = b;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (greedy == null) {
            drawPlaceholder(g2);
            return;
        }

        int W = getWidth();
        int H = getHeight();

        // ── Title ─────────────────────────────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        g2.setColor(new Color(0xFFD700));
        String title = "Time Complexity Analysis  —  n = " + greedy.n + " drivers / passengers";
        g2.drawString(title, (W - g2.getFontMetrics().stringWidth(title)) / 2, 28);

        // ── Three algorithm info cards ────────────────────────────────────
        int cardW = (W - 60) / 3;
        int cardH = (int)(H * 0.52);
        int cardY = 42;

        drawAlgoCard(g2, 15,              cardY, cardW, cardH, greedy,    COL_G);
        drawAlgoCard(g2, 20 + cardW,      cardY, cardW, cardH, hungarian, COL_H);
        drawAlgoCard(g2, 25 + cardW * 2,  cardY, cardW, cardH, bruteForce,COL_B);

        // ── Comparison charts ────────────────────────────────────────────
        int chartY = cardY + cardH + 18;
        int chartH = H - chartY - 20;
        int halfW  = W / 2 - 20;

        drawTimeChart(g2,  10, chartY, halfW, chartH);
        drawStepsChart(g2, W / 2 + 10, chartY, halfW, chartH);
    }

    // ── Algorithm info card ──────────────────────────────────────────────────

    private void drawAlgoCard(Graphics2D g2, int x, int y, int w, int h,
                              ComplexityResult r, Color accent) {
        // Card background
        g2.setColor(COL_CARD);
        g2.fillRoundRect(x, y, w, h, 14, 14);
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        int lx = x + 12;
        int ly = y + 22;
        int gap = 18;

        // ── Algorithm name + complexity ───────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(accent);
        g2.drawString(r.algorithmName.toUpperCase(), lx, ly);

        g2.setFont(new Font("Consolas", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        String bigO = r.algorithmName.equals("Greedy")    ? "O(n^2)" :
                      r.algorithmName.equals("Hungarian") ? "O(n^3)" : "O(n!)";
        g2.drawString("Complexity: " + bigO, lx, ly + gap);

        // ── Divider ───────────────────────────────────────────────────────
        ly += gap + 8;
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
        g2.fillRect(lx, ly, w - 24, 1);
        ly += 10;

        // ── Timer section ─────────────────────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("TIMER", lx, ly);
        ly += gap;

        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(new Color(0xCCCCCC));
        g2.drawString("Start :  0 ns  (reference)", lx, ly);
        ly += gap - 2;

        long elapsed = r.elapsedNanos;
        g2.drawString("End   :  " + fmt(elapsed) + " ns", lx, ly);
        ly += gap - 2;

        g2.setColor(accent);
        g2.setFont(new Font("Consolas", Font.BOLD, 11));
        g2.drawString("Elapsed: " + fmt(elapsed) + " ns", lx, ly);
        ly += gap - 2;
        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(new Color(0xAAAAAA));
        g2.drawString("       = " + String.format("%.3f", r.elapsedMicros()) + " us", lx, ly);
        ly += gap - 2;
        g2.drawString("       = " + String.format("%.6f", r.elapsedMillis()) + " ms", lx, ly);
        ly += gap + 4;

        // ── Divider ───────────────────────────────────────────────────────
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
        g2.fillRect(lx, ly, w - 24, 1);
        ly += 10;

        // ── Complexity derivation ─────────────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("HOW COMPLEXITY IS CALCULATED", lx, ly);
        ly += gap;

        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(new Color(0xCCCCCC));

        int n = r.n;
        if (r.algorithmName.equals("Greedy")) {
            g2.drawString("Outer loop  : n passengers = " + n, lx, ly);          ly += gap - 2;
            g2.drawString("Inner loop  : n drivers    = " + n, lx, ly);          ly += gap - 2;
            g2.drawString("Total steps : " + n + " x " + n + " = " + (n*n), lx, ly); ly += gap - 2;
            g2.setColor(accent);
            g2.setFont(new Font("Consolas", Font.BOLD, 11));
            g2.drawString("=> O(n^2) = O(" + n + "^2) = " + (n*n), lx, ly);
            ly += gap;
        } else if (r.algorithmName.equals("Hungarian")) {
            g2.drawString("Outer loop   : n drivers  = " + n, lx, ly);            ly += gap - 2;
            g2.drawString("Do-while     : up to n    = " + n, lx, ly);            ly += gap - 2;
            g2.drawString("Loop 1 (reduce-cost): n  = " + n, lx, ly);            ly += gap - 2;
            g2.drawString("Loop 2 (pot-update) : n+1= " + (n+1), lx, ly);        ly += gap - 2;
            g2.drawString("Steps counted: loop1 + loop2", lx, ly);               ly += gap - 2;
            g2.drawString("Total : " + n+"x"+n+"x("+n+"+"+(n+1)+") ~ n^3 = "+(n*n*n), lx, ly); ly += gap - 2;
            g2.setColor(accent);
            g2.setFont(new Font("Consolas", Font.BOLD, 11));
            g2.drawString("=> O(n^3) = O(" + n + "^3) = " + (n*n*n), lx, ly);
            ly += gap;
        } else {
            long fact = factorial(n);
            g2.drawString("Permutations: n! = " + factStr(n), lx, ly);           ly += gap - 2;
            g2.drawString("             = " + fact + " arrangements", lx, ly);   ly += gap - 2;
            g2.drawString("Each perm    : evaluates total cost", lx, ly);        ly += gap - 2;
            g2.setColor(accent);
            g2.setFont(new Font("Consolas", Font.BOLD, 11));
            g2.drawString("=> O(n!) = O(" + n + "!) = " + fact, lx, ly);
            ly += gap;
        }

        // ── Divider ───────────────────────────────────────────────────────
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
        g2.fillRect(lx, ly, w - 24, 1);
        ly += 10;

        // ── Actual vs Theoretical ─────────────────────────────────────────
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(0xFFD700));
        g2.drawString("ACTUAL vs THEORETICAL", lx, ly);
        ly += gap;

        g2.setFont(new Font("Consolas", Font.PLAIN, 11));
        g2.setColor(new Color(0xCCCCCC));
        g2.drawString("Actual ops  : " + r.actualSteps, lx, ly);
        ly += gap - 2;
        g2.drawString("Theoretical : " + r.theoreticalSteps, lx, ly);
        ly += gap - 2;

        double ratio = r.theoreticalSteps == 0 ? 0 :
                       (double) r.actualSteps / r.theoreticalSteps * 100;
        g2.setColor(new Color(0x00FF88));
        g2.setFont(new Font("Consolas", Font.BOLD, 11));
        g2.drawString(String.format("Match: %.0f%%  => %s confirmed!", ratio, bigO), lx, ly);
    }

    // ── Comparison charts ────────────────────────────────────────────────────

    /** Bar chart — elapsed time in nanoseconds for all 3 algorithms. */
    private void drawTimeChart(Graphics2D g2, int x, int y, int w, int h) {
        long maxT = Math.max(greedy.elapsedNanos,
                   Math.max(hungarian.elapsedNanos, bruteForce.elapsedNanos));
        if (maxT == 0) maxT = 1;

        drawChartFrame(g2, x, y, w, h, "Elapsed Time (nanoseconds)");

        int pad = 50;
        int bw  = (w - pad * 2) / 5;
        int bBaseY = y + h - 35;
        int bMaxH  = h - 70;

        long[] vals    = { greedy.elapsedNanos, hungarian.elapsedNanos, bruteForce.elapsedNanos };
        Color[] colors = { COL_G, COL_H, COL_B };
        String[] names = { "Greedy", "Hungarian", "BruteForce" };

        for (int i = 0; i < 3; i++) {
            int bh = (int)((double) vals[i] / maxT * bMaxH);
            if (bh < 2) bh = 2;   // always show at least a sliver
            int bx = x + pad + i * (bw + pad / 2);
            int by = bBaseY - bh;

            drawBar(g2, bx, by, bw, bh, colors[i]);

            // value label above bar
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            String lbl = fmt(vals[i]) + " ns";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, bx + (bw - fm.stringWidth(lbl)) / 2, by - 4);

            // name below bar
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(colors[i]);
            fm = g2.getFontMetrics();
            g2.drawString(names[i], bx + (bw - fm.stringWidth(names[i])) / 2, bBaseY + 14);
        }

        // Y-axis label
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(new Color(0x888888));
        g2.drawString("0 ns",       x + 8, bBaseY);
        g2.drawString(fmt(maxT/2) + " ns", x + 8, bBaseY - bMaxH / 2);
        g2.drawString(fmt(maxT) + " ns",   x + 8, y + 50);
    }

    /** Bar chart — step counts on log scale for all 3 algorithms. */
    private void drawStepsChart(Graphics2D g2, int x, int y, int w, int h) {
        long[] vals    = { greedy.actualSteps, hungarian.actualSteps, bruteForce.actualSteps };
        long[] theor   = { greedy.theoreticalSteps, hungarian.theoreticalSteps,
                            bruteForce.theoreticalSteps };
        Color[] colors = { COL_G, COL_H, COL_B };
        String[] names = { "Greedy\nn^2="+theor[0], "Hungarian\nn^3="+theor[1],
                           "BruteForce\nn!="+theor[2] };

        drawChartFrame(g2, x, y, w, h, "Operation Count  (log scale)");

        double maxLog = 0;
        for (long v : theor) maxLog = Math.max(maxLog, Math.log10(Math.max(v, 1)));
        if (maxLog < 1) maxLog = 1;

        int pad    = 50;
        int bw     = (w - pad * 2) / 5;
        int bBaseY = y + h - 35;
        int bMaxH  = h - 70;

        for (int i = 0; i < 3; i++) {
            // Use theoretical for bar height (log scale)
            double logH = Math.log10(Math.max(theor[i], 1));
            int bh = (int)(logH / maxLog * bMaxH);
            if (bh < 2) bh = 2;
            int bx = x + pad + i * (bw + pad / 2);
            int by = bBaseY - bh;

            drawBar(g2, bx, by, bw, bh, colors[i]);

            // Show actual step count above bar
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(Color.WHITE);
            String lbl = vals[i] + " ops";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, bx + (bw - fm.stringWidth(lbl)) / 2, by - 4);

            // Name + formula below bar (two lines)
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(colors[i]);
            String[] parts = names[i].split("\n");
            fm = g2.getFontMetrics();
            g2.drawString(parts[0], bx + (bw - fm.stringWidth(parts[0])) / 2, bBaseY + 12);
            g2.drawString(parts[1], bx + (bw - fm.stringWidth(parts[1])) / 2, bBaseY + 22);
        }

        // Log-scale labels on Y axis
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g2.setColor(new Color(0x888888));
        g2.drawString("log",          x + 4, bBaseY - 4);
        g2.drawString("scale",        x + 4, bBaseY + 6);
        g2.drawString("10^" + (int)(maxLog / 2), x + 4, bBaseY - bMaxH / 2);
        g2.drawString("10^" + (int) maxLog,      x + 4, y + 50);

        // Note at bottom
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        g2.setColor(new Color(0x6688AA));
        g2.drawString("Bars show THEORETICAL ops; labels show ACTUAL ops counted in code",
                      x + 6, y + h - 5);
    }

    // ── Drawing helpers ──────────────────────────────────────────────────────

    private void drawChartFrame(Graphics2D g2, int x, int y, int w, int h, String title) {
        g2.setColor(new Color(0x0F3460));
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(new Color(0x3A3A6E));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(0xFFD700));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, x + (w - fm.stringWidth(title)) / 2, y + 18);
    }

    private void drawBar(Graphics2D g2, int bx, int by, int bw, int bh, Color col) {
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(bx + 3, by + 3, bw, bh, 6, 6);
        GradientPaint gp = new GradientPaint(bx, by, col.brighter(), bx, by + bh, col.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(bx, by, bw, bh, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 6, 6);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawPlaceholder(Graphics2D g2) {
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        g2.setColor(new Color(0x555577));
        String msg = "Click 'Complexity Analysis' in the left panel to run all 3 algorithms with timing.";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private static String fmt(long v) {
        if (v >= 1_000_000_000L) return String.format("%.2fB", v / 1e9);
        if (v >= 1_000_000L)     return String.format("%.2fM", v / 1e6);
        if (v >= 1_000L)         return String.format("%.1fK", v / 1e3);
        return String.valueOf(v);
    }

    private static long factorial(int n) {
        long f = 1;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }

    private static String factStr(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = n; i >= 1; i--) {
            sb.append(i);
            if (i > 1) sb.append(" x ");
        }
        return sb.toString();
    }
}
