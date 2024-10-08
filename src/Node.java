import java.awt.*;

class Node {
    int x, y;
    String label;

    public Node(int x, int y, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }


    public void draw(Graphics g, boolean isStart, boolean isEnd) {
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
        int labelWidth = fm.stringWidth(label);
        int labelHeight = fm.getAscent();
        g.drawString(label, x - labelWidth / 2, y + labelHeight / 4);
    }


    public boolean contains(int px, int py) {
        return Math.abs(x - px) <= 15 && Math.abs(y - py) <= 15;
    }
}
