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
    public Graph graph;
    public JTextArea graphStateTextArea;  // Text area to display the graph state
    public JScrollPane scrollPane;  // To make the text area scrollable
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
            if (edge.curved) {
                int controlX = (startX + endX) / 2; // Example control point
                int controlY = (startY + endY) / 2 - 50; // Adjust control point for curvature
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.draw(new QuadCurve2D.Float(startX, startY, controlX, controlY, endX, endY));
            } else {
                g.drawLine(startX, startY, endX, endY);
            }
            drawArrow(g,startX, startY, endX, endY);
            //draw label
            String label = edge.getLabel();
            int labelX = (startX + endX) / 2;
            int labelY = (startY + endY) / 2;
            g.drawString(label, labelX, labelY);
        }
        for (Node node : graph.nodes) {
            node.draw(g);
        }
    }


    // Method to get the current state of the graph

    // You can call this method to update the state in the text area whenever the graph changes
    private void updateGraphState() {
        graphStateTextArea.setText(graph.getGraphState());
    }

    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = x2 - x1, dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double nodeRadius = 15; // Radius of the node

        // Adjust end point to stop at the edge of the node
        double adjustedX2 = x2 - (dx / distance) * nodeRadius;
        double adjustedY2 = y2 - (dy / distance) * nodeRadius;

        // Draw the line
        g2.drawLine(x1, y1, (int) adjustedX2, (int) adjustedY2);

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
