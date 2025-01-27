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

    List<State> predecessors;
    List<State> successors;

    List<Edge> incomingEdges;
    List<Edge> outgoingEdges;

    public State(int x, int y, int number, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.number = number;
        this.label = "Z" + number;
        Edge epsilonEdge = new Edge(this, this, Collections.singleton('ε'), ArrowType.SELF);
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
        //incomingEdges.add(epsilonEdge);
        //outgoingEdges.add(epsilonEdge);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
