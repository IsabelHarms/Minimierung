import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.io.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

class PanelGraph extends Panel implements MouseListener, MouseMotionListener {
    private Node selectedNode;
    private Node draggedNode;
    private Node startNode;
    private Node endNode;
    private Node edgeStartNode = null;
    private int tempX, tempY;

    JButton exportButton = new JButton("Export Graph");
    JButton importButton = new JButton("Import Graph");
    JButton startMinimizingButton = new JButton("Start Minimizing");
    public PanelGraph(Graph graph) {
        super(graph);
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
            if(!Objects.equals(graph.validate(), "valid")) {
                return;
            }
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            frame.setContentPane(new PanelMinimizing(graph));
            frame.revalidate();
            frame.repaint();
        });

        //add(scrollPane, BorderLayout.EAST);  // Add to the right side of the panel

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
            graph.getNodes().add(new Node(e.getX(), e.getY(), graph.currentNodeNumber++, 30));
            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
    //right mouse clicked = edit node
            boolean nodeClicked = false;
            for (Node node : graph.getNodes()) {
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
                                graph.startNode = null;
                            } else {
                                node.isStart = true;
                                if (graph.startNode != null) graph.startNode.isStart = false;
                                graph.startNode = node;
                            }
                            break;
                        case 1: //Toggle End Node
                            if (node.isEnd) {
                                node.isEnd = false;
                                graph.endNodes.remove(node);
                            } else {
                                node.isEnd = true;
                                graph.endNodes.add(node);
                            }
                            break;
                        case 2:     //delete Node
                            graph.removeNode(node);
                            for (Edge edge: node.incomingEdges) {
                                edge.startNode.outgoingEdges.remove(edge);
                                graph.edges.remove(edge);
                            }
                            for (Edge edge: node.outgoingEdges) {
                                edge.endNode.incomingEdges.remove(edge);
                                graph.edges.remove(edge);
                            }
                            repaint();
                            break;
                        case 3://add edge to self
                            Edge edge = new Edge(node, node, Collections.singleton('a'));
                            edge.arrowType = ArrowType.SELF;
                            graph.addEdge(edge);
                            node.incomingEdges.add(edge);
                            node.outgoingEdges.add(edge);
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
                            case 0: // Delete Edge todo curved edge hitbox
                                if (edge.endNode.connected(edge.startNode) != null) {
                                    edge.endNode.connected(edge.startNode).arrowType = ArrowType.STRAIGHT; //not bidirectional anymore
                                }
                                edge.startNode.outgoingEdges.remove(edge);
                                edge.endNode.incomingEdges.remove(edge);
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
            for (Node node : graph.getNodes()) {
                if (node.contains(e.getX(), e.getY())) {
                    edgeStartNode = node;
                    tempX = e.getX();
                    tempY = e.getY();
                    break;
                }
            }
        } else {
    //mouse left pressed on node = move node
            for (Node node : graph.getNodes()) {
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
                for (Node node : graph.getNodes()) {
                    if (node.contains(e.getX(), e.getY()) && node != edgeStartNode) {
                        String input = JOptionPane.showInputDialog(this, "Enter label for the edge (single characters only):");
                        if (input != null && !input.isEmpty()) {
                            Set<Character> uniqueChars = new LinkedHashSet<>();
                            for (char c : input.toCharArray()) {
                                uniqueChars.add(c);
                            }
                            if (edgeStartNode.connected(node) != null) {
                                Edge existingEdge = edgeStartNode.connected(node); //could use a remove method
                                graph.edges.remove(existingEdge);
                                edgeStartNode.outgoingEdges.remove(existingEdge);
                                node.incomingEdges.remove(existingEdge);
                            }
                            Edge edge = new Edge(edgeStartNode, node, uniqueChars);
                            graph.edges.add(edge);
                            edgeStartNode.outgoingEdges.add(edge);
                            node.incomingEdges.add(edge);
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
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private boolean isClickOnEdge(int px, int py, Edge edge) {
        int x1 = edge.startNode.x;
        int y1 = edge.startNode.y;
        int x2 = edge.endNode.x;
        int y2 = edge.endNode.y;

        double distance = Math.abs((y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1) /
                Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));

        return distance <= 5;
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
