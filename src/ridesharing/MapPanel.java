package ridesharing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Draws drivers, passengers and assignment lines on a 2-D grid.
 */
public class MapPanel extends JPanel {

    private List<Driver>     drivers;
    private List<Passenger>  passengers;
    private List<Assignment> assignments;
    private String           algorithmName = "";

    // Map extents (world coordinates 0-100)
    private static final int W_MIN = 0, W_MAX = 100;
    private static final int MARGIN = 50;

    private static final Color COL_DRIVER    = new Color(0x2196F3);
    private static final Color COL_PASSENGER = new Color(0xE91E63);
    private static final Color COL_LINE      = new Color(0x4CAF50);
    private static final Color COL_BG        = new Color(0x1A1A2E);
    private static final Color COL_GRID      = new Color(0x2A2A4E);

    public MapPanel() {
        setBackground(COL_BG);
        setPreferredSize(new Dimension(600, 500));
    }

    public void update(List<Driver> drivers, List<Passenger> passengers,
                       List<Assignment> assignments, String algorithmName) {
        this.drivers       = drivers;
        this.passengers    = passengers;
        this.assignments   = assignments;
        this.algorithmName = algorithmName;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);

        if (assignments == null || drivers == null) {
            drawPlaceholder(g2);
            return;
        }

        // Draw assignment lines first (behind nodes)
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                     1f, new float[]{8, 4}, 0));
        for (Assignment a : assignments) {
            // find matching driver/passenger by id
            Driver    d = findDriver(a.driver.id);
            Passenger p = findPassenger(a.passenger.id);
            if (d == null || p == null) continue;
            Point pd = toScreen(d.x, d.y);
            Point pp = toScreen(p.x, p.y);
            g2.setColor(COL_LINE);
            g2.drawLine(pd.x, pd.y, pp.x, pp.y);
            // distance label
            Point mid = new Point((pd.x + pp.x) / 2, (pd.y + pp.y) / 2);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("%.1f", a.distance), mid.x + 4, mid.y - 4);
        }
        g2.setStroke(new BasicStroke(1f));

        // Draw drivers
        for (Driver d : drivers) {
            Point pt = toScreen(d.x, d.y);
            drawNode(g2, pt, COL_DRIVER, "D" + d.id, d.name, true);
        }

        // Draw passengers
        for (Passenger p : passengers) {
            Point pt = toScreen(p.x, p.y);
            drawNode(g2, pt, COL_PASSENGER, "P" + p.id, p.name, false);
        }

        // Algorithm label top-left
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(new Color(0xFFD700));
        g2.drawString(algorithmName + " – Assignment Map", MARGIN, 28);

        drawLegend(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        int steps = 10;
        for (int i = 0; i <= steps; i++) {
            int x = MARGIN + i * (getWidth()  - 2 * MARGIN) / steps;
            int y = MARGIN + i * (getHeight() - 2 * MARGIN) / steps;
            g2.drawLine(x, MARGIN, x, getHeight() - MARGIN);
            g2.drawLine(MARGIN, y, getWidth() - MARGIN, y);
        }
        // Axes
        g2.setColor(new Color(0x3A3A6E));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(MARGIN, MARGIN, getWidth() - 2 * MARGIN, getHeight() - 2 * MARGIN);
    }

    private void drawPlaceholder(Graphics2D g2) {
        g2.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        g2.setColor(new Color(0x888888));
        String msg = "Select an algorithm to visualise assignments";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(msg)) / 2;
        int y =  getHeight() / 2;
        g2.drawString(msg, x, y);
    }

    private void drawNode(Graphics2D g2, Point pt, Color col,
                          String label, String name, boolean isDriver) {
        int r = 18;
        // Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillOval(pt.x - r + 2, pt.y - r + 2, r * 2, r * 2);
        // Fill
        g2.setColor(col);
        g2.fillOval(pt.x - r, pt.y - r, r * 2, r * 2);
        // Border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(pt.x - r, pt.y - r, r * 2, r * 2);
        g2.setStroke(new BasicStroke(1f));
        // Icon letter
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(label, pt.x - fm.stringWidth(label) / 2, pt.y + fm.getAscent() / 2 - 1);
        // Name below
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        g2.setColor(isDriver ? new Color(0xADD8E6) : new Color(0xFFB6C1));
        g2.drawString(name, pt.x - fm.stringWidth(name) / 2, pt.y + r + 13);
    }

    private void drawLegend(Graphics2D g2) {
        int lx = getWidth() - 160, ly = 40;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(0xFFFFFF, true));

        g2.setColor(COL_DRIVER);
        g2.fillOval(lx, ly,      12, 12);
        g2.setColor(Color.WHITE);
        g2.drawString("Driver",    lx + 18, ly + 11);

        g2.setColor(COL_PASSENGER);
        g2.fillOval(lx, ly + 22, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawString("Passenger", lx + 18, ly + 33);

        g2.setColor(COL_LINE);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                     1f, new float[]{6, 3}, 0));
        g2.drawLine(lx, ly + 50, lx + 12, ly + 50);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Color.WHITE);
        g2.drawString("Assigned",  lx + 18, ly + 55);
    }

    private Point toScreen(double wx, double wy) {
        int W = getWidth()  - 2 * MARGIN;
        int H = getHeight() - 2 * MARGIN;
        int sx = MARGIN + (int) ((wx - W_MIN) / (W_MAX - W_MIN) * W);
        int sy = MARGIN + (int) ((1.0 - (wy - W_MIN) / (W_MAX - W_MIN)) * H);
        return new Point(sx, sy);
    }

    private Driver findDriver(int id) {
        if (drivers == null) return null;
        for (Driver d : drivers) if (d.id == id) return d;
        return null;
    }

    private Passenger findPassenger(int id) {
        if (passengers == null) return null;
        for (Passenger p : passengers) if (p.id == id) return p;
        return null;
    }
}
