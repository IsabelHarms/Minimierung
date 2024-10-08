import javax.swing.*;
import java.awt.*;

public class Minimizer {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Builder");
        GraphPanel graphPanel = new GraphPanel();
        graphPanel.setPreferredSize(new Dimension(800, 600));

        frame.add(graphPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}