import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.QuadCurve2D;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

class Panel extends JPanel{
    protected Graph graph;
    protected JTextArea graphStateTextArea;  // Text area to display the graph state
    protected JScrollPane scrollPane;  // To make the text area scrollable
    public Panel(Graph graph) {

        setLayout(new BorderLayout());
        this.graph = graph;
        graphStateTextArea = new JTextArea(20, 20);
        graphStateTextArea.setEditable(false);
        graphStateTextArea.setText(graph.getGraphState());  // Initialize with current state
        scrollPane = new JScrollPane(graphStateTextArea);
        add(scrollPane, BorderLayout.EAST);  // Add to the right side of the panel

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        for (Edge edge : graph.edges) {
            int startX = edge.startNode.x;
            int startY = edge.startNode.y;
            int endX = edge.endNode.x;
            int endY = edge.endNode.y;
            int labelX = 0;
            int labelY = 0;
            if (edge.curved) {
                int controlX = (startX + endX) / 2 - 50; // Example control point
                int controlY = (startY + endY) / 2 - 50; // Adjust control point for curvature
                double t2 = 0.5;  // Stelle auf der Kurve, an der der Pfeil gezeichnet wird (nahe am Ende)
                labelX = (int) ((1 - t2) * (1 - t2) * startX + 2 * (1 - t2) * t2 * controlX + t2 * t2 * endX);
                labelY = (int) ((1 - t2) * (1 - t2) * startY + 2 * (1 - t2) * t2 * controlY + t2 * t2 * endY);

            } else {
                labelX = (startX + endX) / 2;
                labelY = (startY + endY) / 2;
            }
            drawArrowLine(g,startX, startY, endX, endY, edge.curved);
            drawArrowHead(g,startX, startY, endX, endY, edge.curved);
            //draw label
            String label = edge.getLabel();
            g.drawString(label, labelX, labelY);
        }
        for (Node node : graph.getNodes()) {
            node.draw(g);
        }
    }

    protected void updateGraphState() {
        graphStateTextArea.setText(graph.getGraphState());
    }

    protected void drawArrowLine(Graphics g, int startX, int startY, int endX, int endY, boolean curved) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = endX - startX, dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double nodeRadius = 15; // Radius of the node

        // Adjust end point to stop at the edge of the node
        double adjustedX2 = endX - (dx / distance) * nodeRadius;
        double adjustedY2 = endY - (dy / distance) * nodeRadius;

        // Draw the line
        if (curved) {
            int controlX = (startX + endX) / 2 - 50; // Example control point
            int controlY = (startY + endY) / 2 - 50; // Adjust control point for curvature
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.draw(new QuadCurve2D.Float(startX, startY, controlX, controlY, endX, endY));

        }
        else {
            g2.drawLine(startX, startY, (int) adjustedX2, (int) adjustedY2);
        }

    }

    protected void drawArrowHead(Graphics g, int startX, int startY, int endX, int endY, boolean curved) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = endX - startX, dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double nodeRadius = 15; // Radius of the node

        // Adjust end point to stop at the edge of the node
        double adjustedX2 = endX - (dx / distance) * nodeRadius;
        double adjustedY2 = endY - (dy / distance) * nodeRadius;

        // Draw the line
        if (curved) { //todo also das is ja wohl katastrophal
            // Bézier-Kurvenparameter t für die Position nahe dem Ende der Kurve
            double t = 0.85; // Position des Pfeilkopfes nahe dem Endpunkt der Kurve

            // Kontrollpunkt für die Bézier-Kurve
            int controlX = (startX + endX) / 2;
            int controlY = (startY + endY) / 2 - 50;

            // Berechnung der Tangente für den Winkel an der Position t
            double arrowX = 2 * (1 - t) * (controlX - startX) + 2 * t * (endX - controlX);
            double arrowY = 2 * (1 - t) * (controlY - startY) + 2 * t * (endY - controlY);
            double angle = Math.atan2(arrowY, arrowX);

            // Berechnung des Pfeilkopfes
            int arrowLength = 10;
            int arrowWidth = 6;

            // Position des Pfeilkopfes (x und y an der Kurve für t)
            double curveX = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * controlX + t * t * endX;
            double curveY = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * controlY + t * t * endY;

            int arrowX1 = (int) (curveX - arrowLength * Math.cos(angle - Math.PI / 6));
            int arrowY1 = (int) (curveY - arrowLength * Math.sin(angle - Math.PI / 6));
            int arrowX2 = (int) (curveX - arrowLength * Math.cos(angle + Math.PI / 6));
            int arrowY2 = (int) (curveY - arrowLength * Math.sin(angle + Math.PI / 6));

            g2.drawLine((int) curveX, (int) curveY, arrowX1, arrowY1);
            g2.drawLine((int) curveX, (int) curveY, arrowX2, arrowY2);
        }
        else {
            // Draw the arrowhead
            double angle = Math.atan2(dy, dx);
            int arrowLength = 10;
            int arrowWidth = 6;

            int arrowX1 = (int) (adjustedX2 - arrowLength * Math.cos(angle - Math.PI / 6));
            int arrowY1 = (int) (adjustedY2 - arrowLength * Math.sin(angle - Math.PI / 6));
            int arrowX2 = (int) (adjustedX2 - arrowLength * Math.cos(angle + Math.PI / 6));
            int arrowY2 = (int) (adjustedY2 - arrowLength * Math.sin(angle + Math.PI / 6));

            g2.drawLine((int) adjustedX2, (int) adjustedY2, arrowX1, arrowY1);
            g2.drawLine((int) adjustedX2, (int) adjustedY2, arrowX2, arrowY2);
        }


    }
}
