import javax.swing.*;
import java.awt.*;

public class Manager {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Builder");
        PanelGraph panelGraph = new PanelGraph();
        panelGraph.setPreferredSize(new Dimension(800, 600));

        frame.add(panelGraph);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}