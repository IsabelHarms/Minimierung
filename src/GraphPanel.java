import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

    public GraphPanel() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        addMouseListener(this);
        addMouseMotionListener(this);
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
}
