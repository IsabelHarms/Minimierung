import javax.swing.*;
import java.awt.*;
import java.awt.geom.QuadCurve2D;

class Panel extends JPanel{
    static final int NODE_RADIUS = 30;
    protected Graph graph;
    protected JTextPane  graphStateTextArea;
    protected JScrollPane scrollPane;  // To make the text area scrollable
    public Panel(Graph graph) {

        setLayout(new BorderLayout());
        this.graph = graph;
        graphStateTextArea = new JTextPane();
        graphStateTextArea.setPreferredSize(new Dimension(500, 1100));
        graphStateTextArea.setEditable(false);
        graphStateTextArea.setFont(new Font("Arial", Font.PLAIN, 20));

        graphStateTextArea.setText(graph.getGraphState());  // Initialize with current state
        scrollPane = new JScrollPane(graphStateTextArea);
        add(scrollPane, BorderLayout.EAST);  // Add to the right side of the panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        for (Edge edge : graph.getEdges()) {
            if(edge.endState.isDefault) {
                g.setColor(Color.GRAY);
            }
            int startX = edge.startState.x;
            int startY = edge.startState.y;
            int endX = edge.endState.x;
            int endY = edge.endState.y;
            drawArrowLine(g,startX, startY, endX, endY, edge.arrowType);
            drawArrowHead(g,startX, startY, endX, endY, edge.arrowType);
            drawLabel(g,startX, startY, endX, endY, edge.getLabel(), edge.arrowType);
            g.setColor(Color.BLACK);
        }
        for (State state : graph.getStates()) {
            state.draw(g);
        }
    }

    protected void updateGraphState() {
        graphStateTextArea.setText(graph.getGraphState());
    }

    protected void drawLabel(Graphics g, int startX, int startY, int endX, int endY, String label, ArrowType arrowType) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int controlX = (startX + endX) / 2; // Example control point
        int controlY = (startY + endY) / 2; // Adjust control point for curvature
        int labelX = 0;
        int labelY = 0;
        switch (arrowType) {
            case STRAIGHT:
                labelX = (startX + endX) / 2;
                labelY = (startY + endY) / 2;
                break;
            case CURVE_LEFT:
                labelX = (int) (0.25 * startX + 2 * 0.25 * (controlX - 50) + 0.25 * endX);
                labelY = (int) (0.25 * startY + 2 * 0.25 * (controlY - 50) + 0.25 * endY);
                break;
            case CURVE_RIGHT:
                labelX = (int) (0.25 * startX + 2 * 0.25 * (controlX + 50) + 0.25 * endX);
                labelY = (int) (0.25 * startY + 2 * 0.25 * (controlY + 50) + 0.25 * endY);
                break;
            case SELF:
                int loopRadius = (2 * NODE_RADIUS) / 3; // Schleifen-Radius: 2/3 des Node-Radius
                labelX = startX; // X-Koordinate bleibt zentriert auf dem Knoten
                labelY = startY - loopRadius - (int) (1.7 * NODE_RADIUS); // Weiter oberhalb platzieren
                break;
            default:
                throw new IllegalStateException("Unbekannter ArrowType: " + arrowType);
        }
        Font font = new Font("Arial", Font.BOLD, 16); // Arial, Fett, Größe 16
        g.setFont(font);
        g.drawString(label, labelX, labelY);
    }
    protected void drawArrowLine(Graphics g, int startX, int startY, int endX, int endY, ArrowType arrowType) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = endX - startX, dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Adjust end point to stop at the edge of the node
        double adjustedX2 = endX - (dx / distance) * NODE_RADIUS;
        double adjustedY2 = endY - (dy / distance) * NODE_RADIUS;

        int midX = (startX + endX) / 2; // Example control point
        int midY = (startY + endY) / 2; // Adjust control point for curvature

        Graphics2D g2d = (Graphics2D) g;

        switch (arrowType) {
            case STRAIGHT:
                g2.drawLine(startX, startY, (int) adjustedX2, (int) adjustedY2);
                break;
            case CURVE_LEFT:
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.draw(new QuadCurve2D.Float(startX, startY, midX  - 50, midY  - 50, endX, endY));
                break;
            case CURVE_RIGHT:
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.draw(new QuadCurve2D.Float(startX, startY, midX + 50, midY + 50, endX, endY));
                break;
            case SELF:
                int loopRadius = (2 * NODE_RADIUS) / 3; // Schleifen-Radius: 2/3 des Node-Radius
                int offsetX = startX - loopRadius; // Startposition für den Kreis
                int offsetY = startY - loopRadius - (int) (1.3 * NODE_RADIUS);; // Etwas oberhalb des Knotens platzieren

                // Zeichne einen vollständigen Kreis (Self-Loop)
                g2.drawOval(offsetX, offsetY, 2 * loopRadius, 2 * loopRadius);
                break;
            default:
                throw new IllegalStateException("Unbekannter ArrowType: " + arrowType);
        }
    }

    protected void drawArrowHead(Graphics g, int startX, int startY, int endX, int endY, ArrowType arrowType) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double dx = endX - startX, dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        int controlX = (startX + endX) / 2;
        int controlY = (startY + endY) / 2;
        int offset = 50;

        double t = 0.85;
        double curveX = 0, curveY = 0;
        double tangentX = 0, tangentY = 0;

        switch (arrowType) {
            case STRAIGHT:
                curveX = endX - (dx / distance) * NODE_RADIUS;
                curveY = endY - (dy / distance) * NODE_RADIUS;
                tangentX = dx;
                tangentY = dy;
                break;

            case CURVE_LEFT:
            case CURVE_RIGHT:
                int curveOffset = (arrowType == ArrowType.CURVE_LEFT) ? -offset : offset;

                curveX = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * (controlX + curveOffset) + t * t * endX;
                curveY = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * (controlY + curveOffset) + t * t * endY;

                tangentX = 2 * (1 - t) * ((controlX + curveOffset) - startX) + 2 * t * (endX - (controlX + curveOffset));
                tangentY = 2 * (1 - t) * (controlY - startY) + 2 * t * (endY - controlY);

                double tangentLength = Math.sqrt(tangentX * tangentX + tangentY * tangentY);
                curveX = curveX - (tangentX / tangentLength) * NODE_RADIUS / 2;
                curveY = curveY - (tangentY / tangentLength) * NODE_RADIUS / 2;
                break;

            case SELF:
                double loopRadius = (2 * NODE_RADIUS) / 2;

                curveX = startX + loopRadius * Math.cos(Math.toRadians(45)) - 5;
                curveY = startY - loopRadius * Math.sin(Math.toRadians(45)) - 5;

                tangentX = -0.5;
                tangentY = 1;
                break;
            default:
                throw new IllegalStateException("Unbekannter ArrowType: " + arrowType);
        }

        // Berechnung des Pfeilkopfes
        double arrowAngle = Math.atan2(tangentY, tangentX);
        int arrowLength = 10;
        int arrowWidth = 6;

        int arrowX1 = (int) (curveX - arrowLength * Math.cos(arrowAngle - Math.PI / 6));
        int arrowY1 = (int) (curveY - arrowLength * Math.sin(arrowAngle - Math.PI / 6));
        int arrowX2 = (int) (curveX - arrowLength * Math.cos(arrowAngle + Math.PI / 6));
        int arrowY2 = (int) (curveY - arrowLength * Math.sin(arrowAngle + Math.PI / 6));

        // Pfeilkopf zeichnen
        g2.drawLine((int) curveX, (int) curveY, arrowX1, arrowY1);
        g2.drawLine((int) curveX, (int) curveY, arrowX2, arrowY2);
    }


}
