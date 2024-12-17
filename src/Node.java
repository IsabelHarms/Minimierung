import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class Node {
    int x, y;

    int radius;
    int number;

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
        Edge epsilonEdge = new Edge(this, this, Collections.singleton('ε'));
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
        //incomingEdges.add(epsilonEdge); todo hmmm maybe dont do this for now, as it breaks everything
        //outgoingEdges.add(epsilonEdge);
    }

    public String getLabel() {
        return "Z" + number;
    }

    public Node getNextState(char a) {
        for (Edge edge: outgoingEdges) {
            if (edge.characters.contains(a)) {
                return edge.endNode;
            }
        }
        return null;
    }

    public Edge connected(Node node) {
        for (Edge edge: outgoingEdges) {
            if (edge.endNode == node) {
                return edge;
            }
        }
        return null;
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
