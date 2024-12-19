import javax.swing.*;
import java.awt.*;

public class Manager {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Graph Builder");
        Graph graph = new Graph();
        PanelGraph panelGraph = new PanelGraph(graph);
        panelGraph.setPreferredSize(new Dimension(800, 600));

        frame.add(panelGraph);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

//todo fix self edges
//todo make states movable even after minimizing?
// todo test more complex examples