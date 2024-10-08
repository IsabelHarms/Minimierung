import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class GraphPanel extends JPanel implements MouseListener, MouseMotionListener {
    private List<Node> nodes;
    private List<int[]> edges;
    private Node selectedNode;
    private Node draggedNode;
    private Node startNode;
    private Node endNode;
    private int nodeCount = 0;
    private Node edgeStartNode = null;
    private int tempX, tempY;

    JButton exportButton = new JButton("Export Graph");
    JButton importButton = new JButton("Import Graph");
    JButton startMinimizingButton = new JButton("Start Minimizing");

    public GraphPanel() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
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
            frame.setContentPane(new MinimizingPanel());
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
        g.setColor(Color.BLACK);
        for (int[] edge : edges) {
            Node node1 = nodes.get(edge[0]);
            Node node2 = nodes.get(edge[1]);
            g.drawLine(node1.x, node1.y, node2.x, node2.y);
        }
        for (Node node : nodes) {
            node.draw(g, node == startNode, node == endNode);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            String label = "Z" + nodeCount++;
            nodes.add(new Node(e.getX(), e.getY(), label));
            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            for (Node node : nodes) {
                if (node.contains(e.getX(), e.getY())) {
                    String[] options = {"Toggle Start", "Toggle End", "Delete Node"};
                    int choice = JOptionPane.showOptionDialog(this, "Node Options",
                            "Choose Action", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);

                    switch (choice) {
                        case 0:  // Toggle Start Node
                            if (startNode == node) {
                                startNode = null;
                            } else {
                                startNode = node;
                            }
                            break;
                        case 1:
                            if (endNode == node) {
                                endNode = null;
                            } else {
                                endNode = node;
                            }
                            break;
                        case 2:
                            nodes.remove(node);
                            nodeCount--;
                            for (int i = 0; i < nodes.size(); i++) {
                                nodes.get(i).label = "Z" + i;
                            }
                            repaint();
                            break;
                    }
                    repaint();
                    break;
                }
            }
        }
    }



    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            for (Node node : nodes) {
                if (node.contains(e.getX(), e.getY())) {
                    edgeStartNode = node;
                    tempX = e.getX();
                    tempY = e.getY();
                    break;
                }
            }
        } else {
            for (Node node : nodes) {
                if (node.contains(e.getX(), e.getY())) {
                    draggedNode = node;
                    break;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
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
        if (SwingUtilities.isRightMouseButton(e)) {
            if (edgeStartNode != null) {
                for (Node node : nodes) {
                    if (node.contains(e.getX(), e.getY()) && node != edgeStartNode) {
                        edges.add(new int[]{nodes.indexOf(edgeStartNode), nodes.indexOf(node)});
                        break;
                    }
                }
                edgeStartNode = null;
                repaint();
            }
        } else {
            draggedNode = null;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private void exportGraph() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Write nodes
                for (Node node : nodes) {
                    String type = "";
                    if (node == startNode) type += "START ";
                    if (node == endNode) type += "END ";
                    writer.write(node.label + "," + node.x + "," + node.y + "," + type.trim());
                    writer.newLine();
                }

                // Write edges
                for (int[] edge : edges) {
                    writer.write("EDGE," + edge[0] + "," + edge[1]);
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
                nodes.clear();  // Clear existing nodes
                edges.clear();  // Clear existing edges
                startNode = null;  // Reset start node
                endNode = null;  // Reset end node
                nodeCount = 0;  // Reset node count

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals("EDGE")) {
                        int fromIndex = Integer.parseInt(parts[1]);
                        int toIndex = Integer.parseInt(parts[2]);
                        edges.add(new int[]{fromIndex, toIndex});  // Store edge
                    } else {
                        String label = parts[0];
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        Node newNode = new Node(x, y, label);

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
                        nodes.add(newNode);
                        nodeCount++;
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
