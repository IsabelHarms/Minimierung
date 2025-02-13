import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class PanelGraph extends Panel implements MouseListener, MouseMotionListener {
    private State draggedNode;
    private State edgeStartNode = null;
    private int tempX, tempY;

    JButton exportButton = new JButton("Export Graph");
    JButton importButton = new JButton("Import Graph");
    JButton startMinimizingButton = new JButton("Start Minimizing");
    public PanelGraph(Graph graph) {
        super(graph);
        JPanel topButtonPanel = new JPanel();
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exportButton.setFont(new Font("Arial", Font.BOLD, 20)); // Größere Schrift
        exportButton.setPreferredSize(new Dimension(200, 50)); // Größerer Button
        importButton.setFont(new Font("Arial", Font.BOLD, 20)); // Größere Schrift
        importButton.setPreferredSize(new Dimension(200, 50)); // Größerer Button
        startMinimizingButton.setFont(new Font("Arial", Font.BOLD, 20)); // Größere Schrift
        startMinimizingButton.setPreferredSize(new Dimension(200, 50)); // Größerer Button
        topButtonPanel.add(exportButton);
        topButtonPanel.add(importButton);
        this.add(topButtonPanel, BorderLayout.NORTH);
        bottomPanel.add(startMinimizingButton);
        this.add(bottomPanel, BorderLayout.SOUTH);

        addMouseListener(this);
        addMouseMotionListener(this);
        exportButton.addActionListener(e -> exportGraph());
        importButton.addActionListener(e -> importGraph());

        startMinimizingButton.addActionListener(e -> {
            if(!graph.isValid) {
                return;
            }
            graph.removeUnnecessaryStates();
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.setContentPane(new PanelMinimizing(graph));
            frame.revalidate();
            frame.repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (edgeStartNode != null) {
            g.setColor(Color.GRAY);
            g.drawLine(edgeStartNode.x, edgeStartNode.y, tempX, tempY);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
    //left mouse clicked = new node
            graph.getStates().add(new State(e.getX(), e.getY(), graph.currentNodeNumber++, 30));
            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
    //right mouse clicked = edit node
            boolean nodeClicked = false;
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    nodeClicked = true;
                    String[] options = {"Toggle Start", "Toggle End", "Delete Node", "Add Edge"};
                    int choice = JOptionPane.showOptionDialog(this, "Node Options",
                            "Choose Action", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);

                    switch (choice) {
                        case 0:  // Toggle Start Node
                            if (node.isStart) {
                                node.isStart = false;
                                graph.startState = null;
                            } else {
                                node.isStart = true;
                                if (graph.startState != null) graph.startState.isStart = false;
                                graph.startState = node;
                            }
                            break;
                        case 1: //Toggle End Node
                            if (node.isEnd) {
                                node.isEnd = false;
                                graph.endStates.remove(node);
                            } else {
                                node.isEnd = true;
                                graph.endStates.add(node);
                            }
                            break;
                        case 2:     //delete Node
                            for (Edge edge: new ArrayList<>(node.incomingEdges)) {
                                graph.removeEdge(edge);
                            }
                            for (Edge edge: new ArrayList<>(node.outgoingEdges)) {
                                graph.removeEdge(edge);
                            }
                            graph.removeNode(node);
                            repaint();
                            break;
                        case 3://add edge to self
                            String input = JOptionPane.showInputDialog(this, "Enter label for the edge (single characters only):");
                            if (input != null && !input.isEmpty()) {
                                Set<Character> uniqueChars = new LinkedHashSet<>();
                                for (char c : input.toCharArray()) {
                                    uniqueChars.add(c);
                                }
                                if (node.connected(node) != null) {
                                    graph.removeEdge(node.connected(node));
                                }
                                Edge edge = new Edge(node, node, uniqueChars, ArrowType.SELF);
                                graph.addEdge(edge);
                            }
                            repaint();
                            break;
                    }
                    repaint();
                    break;
                }
            }
            if (!nodeClicked) {
                for (Edge edge : graph.getEdges()) {
                    if (isClickOnEdge(e.getX(), e.getY(), edge)) {
                        String[] options = {"Delete Edge", "Edit Characters"};
                        int choice = JOptionPane.showOptionDialog(this, "Edge Options",
                                "Choose Action", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, options, options[0]);

                        switch (choice) {
                            case 0: // Delete Edge todo curved edge hitbox
                                if (edge.endState.connected(edge.startState) != null) {
                                    edge.endState.connected(edge.startState).arrowType = ArrowType.STRAIGHT; //not bidirectional anymore
                                }
                                graph.removeEdge(edge);
                                repaint();
                                break;
                            case 1: // Edit Label
                                String newLabel = JOptionPane.showInputDialog(this, "Enter new label for the edge (single characters only):");
                                if (newLabel != null && !newLabel.isEmpty()) {
                                    Set<Character> uniqueChars = new LinkedHashSet<>();
                                    for (char c : newLabel.toCharArray()) {
                                        uniqueChars.add(c);
                                    }
                                    edge.setCharactersAndLabel(uniqueChars);
                                }
                                repaint();
                                break;

                        }
                        break;
                    }
                }
            }
        }
        updateGraphState();
    }



    @Override
    public void mousePressed(MouseEvent e) {
    //mouse right pressed on node = start edge
        if (SwingUtilities.isRightMouseButton(e)) {
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    edgeStartNode = node;
                    tempX = e.getX();
                    tempY = e.getY();
                    break;
                }
            }
        } else {
    //mouse left pressed on node = move node
            for (State node : graph.getStates()) {
                if (node.contains(e.getX(), e.getY())) {
                    draggedNode = node;
                    break;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    //mouse drag = dynamic draw edge
        if (draggedNode != null) {
            draggedNode.x = e.getX();
            draggedNode.y = e.getY();
            repaint();
        } else if (edgeStartNode != null) {
            tempX = e.getX();
            tempY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    //mouse release while drawing edge + end on node = finalize edge
        if (SwingUtilities.isRightMouseButton(e)) {
            if (edgeStartNode != null) {
                for (State node : graph.getStates()) {
                    if (node.contains(e.getX(), e.getY()) && node != edgeStartNode) {
                        String input = JOptionPane.showInputDialog(this, "Enter label for the edge (single characters only):");
                        if (input != null && !input.isEmpty()) {
                            Set<Character> uniqueChars = new LinkedHashSet<>();
                            for (char c : input.toCharArray()) {
                                uniqueChars.add(c);
                            }
                            if (edgeStartNode.connected(node) != null) {
                                graph.removeEdge(edgeStartNode.connected(node));
                            }
                            Edge edge = new Edge(edgeStartNode, node, uniqueChars, ArrowType.STRAIGHT);
                            graph.addEdge(edge);
                            if (node.connected(edgeStartNode) != null) {
                                Edge invertedEdge = node.connected(edgeStartNode);
                                invertedEdge.arrowType = ArrowType.CURVE_LEFT;
                                edge.arrowType = ArrowType.CURVE_RIGHT;
                            }
                        }
                        break;
                    }
                }
                edgeStartNode = null;
                repaint();
            }
        } else {
            draggedNode = null;
        }
        updateGraphState();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateGraphState();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private boolean isClickOnEdge(int px, int py, Edge edge) {
        int x1 = edge.startState.x;
        int y1 = edge.startState.y;
        int x2 = edge.endState.x;
        int y2 = edge.endState.y;

        switch (edge.arrowType) {
            case STRAIGHT:
                return isPointNearLine(px, py, x1, y1, x2, y2, 5);

            case CURVE_LEFT:
            case CURVE_RIGHT:
                return isPointNearLine(px, py, x1, y1, x2, y2, 10); //todo

            case SELF:
                return isPointNearCircle(px, py, x1, y1, NODE_RADIUS / 2, 5);

            default:
                return false;
        }
    }


    private boolean isPointNearLine(int px, int py, int x1, int y1, int x2, int y2, double tolerance) {
        double distance = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) /
                Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        return distance <= tolerance;
    }


    private boolean isPointNearCurve(int px, int py, int x1, int y1, int x2, int y2, ArrowType type, double tolerance) {
        int controlX, controlY;

        // Kontrollpunkt für die Kurve bestimmen
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        int offset = (int) (Math.hypot(x2 - x1, y2 - y1) / 4);

        if (type == ArrowType.CURVE_LEFT) {
            controlX = midX - offset;
            controlY = midY - offset;
        } else { // CURVE_RIGHT
            controlX = midX + offset;
            controlY = midY + offset;
        }

        // Punkte auf der Bézierkurve approximieren
        for (double t = 0; t <= 1; t += 0.01) {
            int bx = (int) ((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * controlX + t * t * x2);
            int by = (int) ((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * controlY + t * t * y2);

            double distance = Math.hypot(px - bx, py - by);
            if (distance <= tolerance) {
                return true;
            }
        }
        return false;
    }


    private boolean isPointNearCircle(int px, int py, int centerX, int centerY, int radius, double tolerance) {
        double distance = Math.abs(Math.hypot(px - centerX, py - centerY) - radius);
        return distance <= tolerance;
    }

    private void exportGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            graph.exportGraph(fileToSave);
        }
    }

    private void importGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to import");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            graph.importGraph(fileToImport);
            repaint();
            updateGraphState();
        }
    }

}
