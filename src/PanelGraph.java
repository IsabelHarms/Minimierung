import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

class PanelGraph extends JPanel implements MouseListener, MouseMotionListener {

    private Graph graph;

    private Node selectedNode;
    private Node draggedNode;
    private Node startNode;
    private Node endNode;
    private Node edgeStartNode = null;
    private int tempX, tempY;

    JButton exportButton = new JButton("Export Graph");
    JButton importButton = new JButton("Import Graph");
    JButton startMinimizingButton = new JButton("Start Minimizing");

    private JTextArea graphStateTextArea;  // Text area to display the graph state
    private JScrollPane scrollPane;  // To make the text area scrollable
    public PanelGraph() {

        setLayout(new BorderLayout());

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.add(exportButton);
        topButtonPanel.add(importButton);
        this.add(topButtonPanel, BorderLayout.NORTH);

        this.add(startMinimizingButton, BorderLayout.SOUTH);

        addMouseListener(this);
        addMouseMotionListener(this);
        exportButton.addActionListener(e -> exportGraph());
        importButton.addActionListener(e -> importGraph());

        startMinimizingButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.setContentPane(new PanelMinimizing());
            frame.revalidate();
            frame.repaint();
        });
        this.graph = new Graph();
        graphStateTextArea = new JTextArea(20, 20);
        graphStateTextArea.setEditable(false);
        graphStateTextArea.setText(graph.getGraphState());  // Initialize with current state
        scrollPane = new JScrollPane(graphStateTextArea);

        // Add the text area to the panel (assuming you use a layout manager like BorderLayout)
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.EAST);  // Add to the right side of the panel

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (edgeStartNode != null) {
            g.setColor(Color.GRAY);
            g.drawLine(edgeStartNode.x, edgeStartNode.y, tempX, tempY);
        }
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

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
    //left mouse clicked = new node
            graph.nodes.add(new Node(e.getX(), e.getY(), graph.currentNodeNumber++));
            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
    //right mouse clicked = edit node
            boolean nodeClicked = false;
            for (Node node : graph.nodes) {
                if (node.contains(e.getX(), e.getY())) {
                    nodeClicked = true;
                    String[] options = {"Toggle Start", "Toggle End", "Delete Node"};
                    int choice = JOptionPane.showOptionDialog(this, "Node Options",
                            "Choose Action", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);

                    switch (choice) {
                        case 0:  // Toggle Start Node
                            if (node.isStart) {
                                node.isStart = false;
                                graph.startNode = null;
                            } else {
                                node.isStart = true;
                                graph.startNode.isStart = false;
                                graph.startNode = node;
                            }
                            break;
                        case 1: //Toggle End Node
                            node.isEnd = !node.isEnd;
                            break;
                        case 2:     //delete Node
                            graph.nodes.remove(node);
                            for (Edge edge: node.incomingEdges) {
                                graph.edges.remove(edge);
                            }
                            for (Edge edge: node.outgoingEdges) {
                                graph.edges.remove(edge);
                            }
                            repaint();
                            break;
                    }
                    repaint();
                    break;
                }
            }
            if (!nodeClicked) {
                for (Edge edge : graph.edges) {
                    if (isClickOnEdge(e.getX(), e.getY(), edge)) {
                        String[] options = {"Delete Edge", "Edit Characters"};
                        int choice = JOptionPane.showOptionDialog(this, "Edge Options",
                                "Choose Action", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                null, options, options[0]);

                        switch (choice) {
                            case 0: // Delete Edge
                                graph.edges.remove(edge);
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
            for (Node node : graph.nodes) {
                if (node.contains(e.getX(), e.getY())) {
                    edgeStartNode = node;
                    tempX = e.getX();
                    tempY = e.getY();
                    break;
                }
            }
        } else {
    //mouse left pressed on node = move node
            boolean nodeClicked = false;
            for (Node node : graph.nodes) {
                if (node.contains(e.getX(), e.getY())) {
                    draggedNode = node;
                    nodeClicked = true;
                    break;
                }
            }
            // If no node is clicked, check if an edge is clicked to toggle its curvature
            if (!nodeClicked) {
                for (Edge edge : graph.edges) {
                    // Check if the left-click is close to an edge (using a helper function)
                    if (isClickOnEdge(e.getX(), e.getY(), edge)) {
                        edge.curved = !edge.curved; // Toggle the curvature state of the edge
                        break;
                    }
                }
            }
        }
        //updateGraphState();
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
                for (Node node : graph.nodes) {
                    if (node.contains(e.getX(), e.getY()) && node != edgeStartNode) {
                        String input = JOptionPane.showInputDialog(this, "Enter label for the edge (single characters only):");
                        if (input != null && !input.isEmpty()) {
                            Set<Character> uniqueChars = new LinkedHashSet<>();
                            for (char c : input.toCharArray()) {
                                uniqueChars.add(c);
                            }
                            Edge edge = new Edge(edgeStartNode, node, uniqueChars);
                            graph.edges.add(edge);
                            edgeStartNode.outgoingEdges.add(edge);
                            node.outgoingEdges.add(edge);
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
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

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

    // Helper method to determine if a click is near an edge
    private boolean isClickOnEdge(int px, int py, Edge edge) {
        int x1 = edge.startNode.x;
        int y1 = edge.startNode.y;
        int x2 = edge.endNode.x;
        int y2 = edge.endNode.y;

        // Simple distance check to see if the click is near the edge (with a buffer distance)
        double distance = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) /
                Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));

        return distance <= 5; // Adjust buffer distance as necessary
    }



    private void exportGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write nodes
                for (Node node : graph.nodes) {
                    String type = "";
                    if (node == startNode) type += "START ";
                    if (node == endNode) type += "END ";
                    writer.write(node.number + "," + node.x + "," + node.y + "," + type.trim());
                    writer.newLine();
                }

                // Write edges
                for (Edge edge : graph.edges) {
                    writer.write("EDGE," + edge.startNode + "," + edge.endNode);
                    writer.newLine();
                }

                JOptionPane.showMessageDialog(this, "Graph exported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting graph: " + e.getMessage());
            }
        }
    }

    private void importGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to import");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(fileToImport))) {
                graph.nodes.clear();  // Clear existing nodes
                graph.edges.clear();  // Clear existing edges
                startNode = null;  // Reset start node
                endNode = null;  // Reset end node

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals("EDGE")) {
                        int fromIndex = Integer.parseInt(parts[1]);
                        int toIndex = Integer.parseInt(parts[2]);
                        //graph.edges.add(new Edge{fromIndex, toIndex});  // Store edge
                    } else {
                        int number = Integer.parseInt(parts[0]);
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        Node newNode = new Node(x, y, number);

                        // Determine if the node is start or end
                        if (parts.length > 3) {
                            for (String type : parts[3].split(" ")) {
                                if (type.equalsIgnoreCase("START")) {
                                    startNode = newNode;
                                }
                                if (type.equalsIgnoreCase("END")) {
                                    endNode = newNode;
                                }
                            }
                        }
                        graph.nodes.add(newNode);
                        graph.currentNodeNumber++;
                    }
                }
                repaint();
                JOptionPane.showMessageDialog(this, "Graph imported successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error importing graph: " + e.getMessage());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error in file format: " + e.getMessage());
            }
        }
    }

}
