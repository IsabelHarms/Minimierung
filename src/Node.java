import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class Node {
    int x, y;
    int number;

    boolean isStart;

    boolean isEnd;

    List<Node> predecessors;
    List<Node> successors;

    List<Edge> incomingEdges;
    List<Edge> outgoingEdges;

    public Node(int x, int y, int number) {
        this.x = x;
        this.y = y;
        this.number = number;
        incomingEdges = new ArrayList<>();
        outgoingEdges = new ArrayList<>();
    }

    public String getLabel() {
        return "Z" + number;
    }


    public void draw(Graphics g) {
        if (isStart && isEnd) {
            g.setColor(Color.BLUE);
            g.fillOval(x - 15, y - 15, 30, 30);
        } else if (isStart) {
            g.setColor(Color.GREEN);
            g.fillOval(x - 15, y - 15, 30, 30);
        } else if (isEnd) {
            g.setColor(Color.RED);
            g.fillOval(x - 15, y - 15, 30, 30);
        } else {
            g.setColor(Color.WHITE);
            g.fillOval(x - 15, y - 15, 30, 30);
        }

        g.setColor(Color.BLACK);
        g.drawOval(x - 15, y - 15, 30, 30);

        FontMetrics fm = g.getFontMetrics();
        int labelWidth = fm.stringWidth(this.getLabel());
        int labelHeight = fm.getAscent();
        g.drawString(this.getLabel(), x - labelWidth / 2, y + labelHeight / 4);
    }


    public boolean contains(int px, int py) {
        return Math.abs(x - px) <= 15 && Math.abs(y - py) <= 15;
    }
}
