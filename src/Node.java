import java.awt.*;
import java.util.*;
import java.util.List;

class Node {
    int x, y;

    int radius;
    int number;

    String label;

    boolean isStart;

    boolean isEnd;

    List<Node> predecessors;
    List<Node> successors;

    List<Edge> incomingEdges;
    List<Edge> outgoingEdges;

    public Node(int x, int y, int number, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.number = number;
        this.label = "Z" + number;
        Edge epsilonEdge = new Edge(this, this, Collections.singleton('ε'), ArrowType.SELF);
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
        incomingEdges.add(epsilonEdge);
        outgoingEdges.add(epsilonEdge);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Node getNextState(char a) {
        for (Edge edge: outgoingEdges) {
            if (edge.characters.contains(a)) {
                return edge.endNode;
            }
        }
        return null;
    }

    public boolean hasPredecessor() {
        for(Edge edge: incomingEdges) {
            if (edge.startNode != this) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSuccessor() {
        for(Edge edge: outgoingEdges) {
            if (edge.endNode != this) {
                return true;
            }
        }
        return false;
    }

    public Edge connected(Node node) {
        for (Edge edge: outgoingEdges) {
            if (edge.endNode == node) {
                return edge;
            }
        }
        return null;
    }

    public void nextNodesRecursive(Set<Node> reachedNodes) {
        for (Edge outgoingEdge: outgoingEdges) {
            Node node = outgoingEdge.endNode;
            if (!reachedNodes.contains(node)) {
                reachedNodes.add(node);
                node.nextNodesRecursive(reachedNodes);
            }
        }
    }
    public void draw(Graphics g) {
        if (isStart && isEnd) {
            g.setColor(Color.BLUE);
            g.fillOval(x - radius, y - radius, radius*2, radius*2);
        } else if (isStart) {
            g.setColor(Color.GREEN);
            g.fillOval(x - radius, y - radius, radius*2, radius*2);
        } else if (isEnd) {
            g.setColor(Color.RED);
            g.fillOval(x - radius, y - radius, radius*2, radius*2);
        } else {
            g.setColor(Color.WHITE);
            g.fillOval(x - radius, y - radius, radius*2, radius*2);
        }

        g.setColor(Color.BLACK);
        g.drawOval(x - radius, y - radius, radius*2, radius*2);

        FontMetrics fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(this.getLabel());
        int labelHeight = fm.getAscent();
        g.drawString(this.getLabel(), x - labelWidth / 2, y + labelHeight / 4);
    }


    public boolean contains(int px, int py) {
        return Math.abs(x - px) <= radius && Math.abs(y - py) <= radius;
    }
}
