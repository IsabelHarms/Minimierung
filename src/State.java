import java.awt.*;
import java.util.*;
import java.util.List;

class State {
    int x, y;

    int radius;
    int number;

    String label;

    boolean isStart;

    boolean isEnd;

    int algorithmArrayIndex;

    int currentPartitionIndex;

    List<State> predecessors;
    List<State> successors;

    List<Edge> incomingEdges;
    List<Edge> outgoingEdges;

    Color color;

    public State(int x, int y, int number, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.number = number;
        this.label = "Z" + number;
        this.color = Color.white;
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getIndex() {
        return algorithmArrayIndex;
    }

    public void setIndex(int algorithmArrayIndex) {
        this.algorithmArrayIndex = algorithmArrayIndex;
    }

    public int getPartition() {
        return currentPartitionIndex;
    }

    public void setPartition(int currentPartitionIndex) {
        this.currentPartitionIndex = currentPartitionIndex;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public State getNextState(char a) {
        for (Edge edge: outgoingEdges) {
            if (edge.characters.contains(a)) {
                return edge.endState;
            }
        }
        return null;
    }

    public Set<State> getPreviousStates(char a) {
        Set<State> previousStates = new HashSet<>();
        for (Edge edge: incomingEdges) {
            if (edge.characters.contains(a)) {
                previousStates.add(edge.startState);
            }
        }
        return previousStates;
    }

    public boolean hasPredecessor() {
        for(Edge edge: incomingEdges) {
            if (edge.startState != this) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSuccessor() {
        for(Edge edge: outgoingEdges) {
            if (edge.endState != this) {
                return true;
            }
        }
        return false;
    }

    public Edge connected(State node) {
        for (Edge edge: outgoingEdges) {
            if (edge.endState == node) {
                return edge;
            }
        }
        return null;
    }

    public void nextNodesRecursive(Set<State> reachedNodes) {
        for (Edge outgoingEdge: outgoingEdges) {
            State node = outgoingEdge.endState;
            if (!reachedNodes.contains(node)) {
                reachedNodes.add(node);
                node.nextNodesRecursive(reachedNodes);
            }
        }
    }
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.fillOval(x - radius, y - radius, radius*2, radius*2);
        g.setColor(Color.BLACK);
        if (isStart) {
            drawStartArrow(g, x - radius - 15, y, x - radius, y);
        }
        if (isEnd) {
            g.drawOval(x - radius + 4, y - radius + 4, (radius * 2) - 8, (radius * 2) - 8);
        }
        g.drawOval(x - radius, y - radius, radius*2, radius*2);

        FontMetrics fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(this.getLabel());
        int labelHeight = fm.getAscent();
        g.drawString(this.getLabel(), x - labelWidth / 2, y + labelHeight / 4);
    }

    public boolean contains(int px, int py) {
        return Math.abs(x - px) <= radius && Math.abs(y - py) <= radius;
    }

    private void drawStartArrow(Graphics g, int x1, int y1, int x2, int y2) {
        int arrowSize = 8;
        double angle = Math.atan2(y2 - y1, x2 - x1);

        int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 6));
        int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 6));
        int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 6));
        int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 6));

        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y2, x3, y3);
        g.drawLine(x2, y2, x4, y4);
    }
}
